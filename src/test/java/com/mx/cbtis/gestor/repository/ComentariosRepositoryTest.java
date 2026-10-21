package com.mx.cbtis.gestor.repository;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import java.util.List;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import com.mx.cbtis.gestor.modelo.Comentarios;

@SpringBootTest
@Transactional
public class ComentariosRepositoryTest {

    @Autowired
    private ComentariosRepository comentariosRepository;

    @Test
    @DisplayName("UT-BD-COM-01: Insertar nuevo comentario en una tarea")
    void testGuardarComentario() {
        Comentarios comentario = new Comentarios();
        comentario.setId_usuario("Carlos Mendoza");
        comentario.setId_tarea(199);
        comentario.setTexto_comentario("Se completó la verificación de las pruebas unitarias");
        comentario.setFecha_hora("2026-10-13 18:00:00");

        Comentarios guardado = comentariosRepository.save(comentario);

        assertNotNull(guardado.getId_comentario(), "El ID autogenerado del comentario no debe ser nulo");
        assertEquals("Se completó la verificación de las pruebas unitarias", guardado.getTexto_comentario());
    }

    @Test
    @DisplayName("UT-BD-COM-02: Consulta nativa findByTarea para listar comentarios de una tarea")
    void testBuscarComentariosPorTarea() {
        int idTareaPrueba = 7777;
        Comentarios comentario = new Comentarios();
        comentario.setId_usuario("Perla Joceline");
        comentario.setId_tarea(idTareaPrueba);
        comentario.setTexto_comentario("Comentario de prueba unitaria");
        comentario.setFecha_hora("2026-10-13 18:30:00");
        comentariosRepository.save(comentario);

        List<Comentarios> comentarios = comentariosRepository.findByTarea(idTareaPrueba);

        assertNotNull(comentarios, "La lista de comentarios no debe ser nula");
        assertFalse(comentarios.isEmpty(), "Debe contener el comentario insertado para dicha tarea");
        assertEquals(idTareaPrueba, comentarios.get(0).getId_tarea());
    }
}
