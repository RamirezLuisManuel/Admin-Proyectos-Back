package com.mx.cbtis.gestor.controller;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.stream.StreamSupport;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import com.mx.cbtis.gestor.modelo.Asignaciones;
import com.mx.cbtis.gestor.modelo.Comentarios;
import com.mx.cbtis.gestor.modelo.Proyectos;
import com.mx.cbtis.gestor.modelo.Tareas;
import com.mx.cbtis.gestor.modelo.Usuario;
import com.mx.cbtis.gestor.repository.AsignacionesRepository;
import com.mx.cbtis.gestor.repository.ComentariosRepository;
import com.mx.cbtis.gestor.repository.ProtectosRepository;
import com.mx.cbtis.gestor.repository.TareasRepository;
import com.mx.cbtis.gestor.repository.UsuarioRepository;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class UpgradeApiIntegrationTest {

    @Autowired private MockMvc mvc;
    @Autowired private ProtectosRepository proyectos;
    @Autowired private TareasRepository tareas;
    @Autowired private UsuarioRepository usuarios;
    @Autowired private ComentariosRepository comentarios;
    @Autowired private AsignacionesRepository asignaciones;

    @Test
    void proyectoCrudConservaContratoJsonYClaveIdentity() throws Exception {
        mvc.perform(post("/addProyecto").contentType(MediaType.APPLICATION_JSON).content("""
                {"nombre":"Upgrade Java 21","descripcion":"Regresión Spring Boot 4",
                 "fecha_inicio":"2026-10-21","fecha_finalizacion":"2026-10-22",
                 "estado_proyecto":"En proceso","fondo":"/Images/blanco.png"}
                """))
                .andExpect(status().isOk()).andExpect(content().string("1"));

        Proyectos proyecto = StreamSupport.stream(proyectos.findAll().spliterator(), false)
                .filter(p -> "Upgrade Java 21".equals(p.getNombre())).findFirst().orElseThrow();
        int id = proyecto.getId_proyecto();
        assertTrue(id > 0);

        mvc.perform(get("/buscarProyecto").param("id_proyecto", String.valueOf(id)))
                .andExpect(status().isOk()).andExpect(jsonPath("$.id_proyecto").value(id))
                .andExpect(jsonPath("$.fecha_inicio").value("2026-10-21"));

        mvc.perform(post("/updateProyecto").contentType(MediaType.APPLICATION_JSON)
                .content("""
                        {"id_proyecto":%d,"nombre":"Proyecto actualizado","estado_proyecto":"Finalizado"}
                        """.formatted(id)))
                .andExpect(status().isOk()).andExpect(content().string("1"));
        mvc.perform(get("/buscarProyecto").param("id_proyecto", String.valueOf(id)))
                .andExpect(jsonPath("$.nombre").value("Proyecto actualizado"));

        mvc.perform(delete("/deleteProyecto").contentType(MediaType.APPLICATION_JSON)
                .content("{\"id_proyecto\":" + id + "}"))
                .andExpect(status().isOk()).andExpect(content().string("1"));
        assertFalse(proyectos.existsById(id));
    }

    @Test
    void tareasYComentariosMantienenConsultasNativasYCamposConGuionBajo() throws Exception {
        Tareas tarea = new Tareas();
        tarea.setNombre("Tarea de regresión");
        tarea.setId_proyecto(987654);
        tarea.setLista("pendientes");
        tarea = tareas.save(tarea);
        int id = tarea.getId_tarea();

        mvc.perform(post("/getTarea").contentType(MediaType.APPLICATION_JSON)
                .content("{\"id_proyecto\":987654}"))
                .andExpect(status().isOk()).andExpect(jsonPath("$[0].id_tarea").value(id))
                .andExpect(jsonPath("$[0].id_proyecto").value(987654));

        mvc.perform(post("/addComentario").contentType(MediaType.APPLICATION_JSON)
                .content("""
                        {"id_tarea":%d,"id_usuario":"Prueba de migración",
                         "texto_comentario":"Comentario con acentos: integración",
                         "fecha_hora":"2026-10-21T08:00:00"}
                        """.formatted(id)))
                .andExpect(status().isOk()).andExpect(content().string("1"));

        mvc.perform(post("/getComentario").contentType(MediaType.APPLICATION_JSON)
                .content("{\"id_tarea\":" + id + "}"))
                .andExpect(status().isOk()).andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].id_tarea").value(id))
                .andExpect(jsonPath("$[0].texto_comentario").value("Comentario con acentos: integración"));
    }

    @Test
    void loginMantieneBusquedaNativaConParametros() throws Exception {
        Usuario usuario = new Usuario();
        usuario.setNombre("Usuario de migración");
        usuario.setCorreo_electronico("upgrade@example.test");
        usuario.setContrasena("solo-pruebas");
        usuarios.save(usuario);

        mvc.perform(post("/getUsuarioLogin").contentType(MediaType.APPLICATION_JSON)
                .content("""
                        {"correo_electronico":"upgrade@example.test","contrasena":"solo-pruebas"}
                        """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.nombre").value("Usuario de migración"))
                .andExpect(jsonPath("$.correo_electronico").value("upgrade@example.test"));

        // Login fallido
        mvc.perform(post("/getUsuarioLogin").contentType(MediaType.APPLICATION_JSON)
                .content("""
                        {"correo_electronico":"upgrade@example.test","contrasena":"erronea"}
                        """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.nombre").value("000"));

        // CRUD completo de Usuario
        mvc.perform(post("/addUsuario").contentType(MediaType.APPLICATION_JSON)
                .content("""
                        {"nombre":"Nuevo Usuario Test","correo_electronico":"nuevo@test.com",
                         "contrasena":"123456","rol":"Admin","imagen_usuario":"#000000"}
                        """))
                .andExpect(status().isOk()).andExpect(content().string("1"));

        Usuario u = StreamSupport.stream(usuarios.findAll().spliterator(), false)
                .filter(usr -> "nuevo@test.com".equals(usr.getCorreo_electronico())).findFirst().orElseThrow();
        int idU = u.getId_usuario();

        mvc.perform(get("/buscarUsuario").param("id", String.valueOf(idU)))
                .andExpect(status().isOk()).andExpect(jsonPath("$.nombre").value("Nuevo Usuario Test"));

        mvc.perform(post("/updateUsuario").contentType(MediaType.APPLICATION_JSON)
                .content("""
                        {"id_usuario":%d,"nombre":"Usuario Modificado","correo_electronico":"nuevo@test.com"}
                        """.formatted(idU)))
                .andExpect(status().isOk()).andExpect(content().string("1"));

        mvc.perform(post("/buscarUsuarios").contentType(MediaType.APPLICATION_JSON).content("{}"))
                .andExpect(status().isOk()).andExpect(jsonPath("$").isArray());

        mvc.perform(delete("/deleteUsuario").contentType(MediaType.APPLICATION_JSON)
                .content("{\"id_usuario\":" + idU + "}"))
                .andExpect(status().isOk()).andExpect(content().string("1"));
    }

    @Test
    void proyectosListaYBuscarTodos() throws Exception {
        mvc.perform(post("/buscarProyectos").contentType(MediaType.APPLICATION_JSON).content("{}"))
                .andExpect(status().isOk()).andExpect(jsonPath("$").isArray());
    }

    @Test
    void tareasCrudCompletoYListados() throws Exception {
        mvc.perform(post("/addTareas").contentType(MediaType.APPLICATION_JSON)
                .content("""
                        {"nombre":"Tarea de Prueba Completa","descripcion":"Verificar todo el controlador",
                         "fecha_vencimiento":"2026-10-31","lista":"To Do","estado":"Pendiente","id_proyecto":100}
                        """))
                .andExpect(status().isOk()).andExpect(content().string("1"));

        Tareas t = StreamSupport.stream(tareas.findAll().spliterator(), false)
                .filter(tar -> "Tarea de Prueba Completa".equals(tar.getNombre())).findFirst().orElseThrow();
        int idT = t.getId_tarea();

        mvc.perform(get("/buscarTarea").param("id_tarea", String.valueOf(idT)))
                .andExpect(status().isOk()).andExpect(jsonPath("$.nombre").value("Tarea de Prueba Completa"));

        mvc.perform(post("/updateTareas").contentType(MediaType.APPLICATION_JSON)
                .content("""
                        {"id_tarea":%d,"nombre":"Tarea Actualizada","estado":"En proceso"}
                        """.formatted(idT)))
                .andExpect(status().isOk()).andExpect(content().string("1"));

        mvc.perform(post("/buscarTareas").contentType(MediaType.APPLICATION_JSON).content("{}"))
                .andExpect(status().isOk()).andExpect(jsonPath("$").isArray());

        mvc.perform(delete("/deleteTareas").contentType(MediaType.APPLICATION_JSON)
                .content("{\"id_tarea\":" + idT + "}"))
                .andExpect(status().isOk()).andExpect(content().string("1"));
    }

    @Test
    void comentariosCrudCompletoYListados() throws Exception {
        mvc.perform(post("/addComentario").contentType(MediaType.APPLICATION_JSON)
                .content("""
                        {"id_usuario":"Tester","id_tarea":101,"texto_comentario":"Comentario general",
                         "fecha_hora":"2026-10-27T10:00:00"}
                        """))
                .andExpect(status().isOk()).andExpect(content().string("1"));

        Comentarios c = StreamSupport.stream(comentarios.findAll().spliterator(), false)
                .filter(com -> "Comentario general".equals(com.getTexto_comentario())).findFirst().orElseThrow();
        int idC = c.getId_comentario();

        mvc.perform(get("/buscarComentario").param("id", String.valueOf(idC)))
                .andExpect(status().isOk());

        mvc.perform(post("/updateComentario").contentType(MediaType.APPLICATION_JSON)
                .content("""
                        {"id_comentario":%d,"id_tarea":101,"id_usuario":"Tester","texto_comentario":"Comentario modificado"}
                        """.formatted(idC)))
                .andExpect(status().isOk()).andExpect(content().string("1"));

        mvc.perform(post("/buscarComentarios").contentType(MediaType.APPLICATION_JSON).content("{}"))
                .andExpect(status().isOk()).andExpect(jsonPath("$").isArray());

        mvc.perform(delete("/deleteComentario").contentType(MediaType.APPLICATION_JSON)
                .content("""
                        {"id_comentario":%d,"id_tarea":101,"id_usuario":"Tester","texto_comentario":"Comentario modificado"}
                        """.formatted(idC)))
                .andExpect(status().isOk()).andExpect(content().string("1"));
    }

    @Test
    void asignacionesCrudCompletoYListados() throws Exception {
        mvc.perform(post("/addAsignacion").contentType(MediaType.APPLICATION_JSON)
                .content("""
                        {"id_usuario":1,"id_tarea":10,"fecha_asignacion":"2026-10-27"}
                        """))
                .andExpect(status().isOk()).andExpect(content().string("1"));

        Asignaciones a = StreamSupport.stream(asignaciones.findAll().spliterator(), false)
                .filter(asg -> Integer.valueOf(1).equals(asg.getId_usuario())).findFirst().orElseThrow();
        int idA = a.getId_asignacion();

        mvc.perform(get("/buscarAsignacion").param("id", String.valueOf(idA)))
                .andExpect(status().isOk());

        mvc.perform(post("/updateAsignacion").contentType(MediaType.APPLICATION_JSON)
                .content("""
                        {"id_asignacion":%d,"id_usuario":2,"id_tarea":10,"fecha_asignacion":"2026-10-28"}
                        """.formatted(idA)))
                .andExpect(status().isOk()).andExpect(content().string("1"));

        mvc.perform(post("/buscarAsignaciones").contentType(MediaType.APPLICATION_JSON).content("{}"))
                .andExpect(status().isOk()).andExpect(jsonPath("$").isArray());

        mvc.perform(delete("/deleteAsignacion").contentType(MediaType.APPLICATION_JSON)
                .content("""
                        {"id_asignacion":%d,"id_usuario":2,"id_tarea":10,"fecha_asignacion":"2026-10-28"}
                        """.formatted(idA)))
                .andExpect(status().isOk()).andExpect(content().string("1"));
    }

    @Test
    void ramasNegativasYEntidadesInexistentes() throws Exception {
        mvc.perform(get("/buscarUsuario").param("id", "999999"))
                .andExpect(status().isOk());

        mvc.perform(get("/buscarProyecto").param("id_proyecto", "999999"))
                .andExpect(status().isOk());

        mvc.perform(get("/buscarTarea").param("id_tarea", "999999"))
                .andExpect(status().isOk());

        mvc.perform(get("/buscarComentario").param("id", "999999"))
                .andExpect(status().isOk());

        mvc.perform(get("/buscarAsignacion").param("id", "999999"))
                .andExpect(status().isOk());

        mvc.perform(post("/getUsuarioLogin").contentType(MediaType.APPLICATION_JSON)
                .content("""
                        {"correo_electronico":"inexistente@cbtis.edu.mx","contrasena":"erronea"}
                        """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.nombre").value("000"));
    }
}
