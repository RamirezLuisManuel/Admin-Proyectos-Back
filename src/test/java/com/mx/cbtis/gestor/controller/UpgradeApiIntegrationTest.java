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

import com.mx.cbtis.gestor.modelo.Proyectos;
import com.mx.cbtis.gestor.modelo.Tareas;
import com.mx.cbtis.gestor.modelo.Usuario;
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
    }
}
