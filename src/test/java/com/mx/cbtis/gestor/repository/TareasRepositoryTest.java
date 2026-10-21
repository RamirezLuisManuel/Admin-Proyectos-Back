package com.mx.cbtis.gestor.repository;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import com.mx.cbtis.gestor.modelo.Tareas;

@SpringBootTest
@Transactional
public class TareasRepositoryTest {

    @Autowired
    private TareasRepository tareasRepository;

    @Test
    @DisplayName("UT-BD-TAR-01: Insertar nueva tarea asociada a un proyecto")
    void testGuardarTarea() {
        Tareas tarea = new Tareas();
        tarea.setNombre("Configuración de JaCoCo");
        tarea.setDescripcion("Implementar plugin de cobertura de código");
        tarea.setFecha_vencimiento("2026-10-20");
        tarea.setLista("Backlog");
        tarea.setEstado("Pendiente");
        tarea.setId_proyecto(197);

        Tareas guardada = tareasRepository.save(tarea);

        assertNotNull(guardada.getId_tarea(), "El ID de la tarea creada no debe ser nulo");
        assertEquals("Configuración de JaCoCo", guardada.getNombre());
        assertEquals(197, guardada.getId_proyecto());
    }

    @Test
    @DisplayName("UT-BD-TAR-02: Consultar tarea por ID existente")
    void testBuscarTareaPorId() {
        Tareas tarea = new Tareas();
        tarea.setNombre("Prueba Unitaria de Tarea");
        tarea.setDescripcion("Verificar persistencia individual");
        tarea.setFecha_vencimiento("2026-10-25");
        tarea.setLista("Doing");
        tarea.setEstado("En proceso");
        tarea.setId_proyecto(197);
        Tareas guardada = tareasRepository.save(tarea);

        Optional<Tareas> encontrada = tareasRepository.findById(guardada.getId_tarea());

        assertTrue(encontrada.isPresent(), "La tarea debe encontrarse por su ID");
        assertEquals("Prueba Unitaria de Tarea", encontrada.get().getNombre());
    }

    @Test
    @DisplayName("UT-BD-TAR-03: Consulta nativa findByIdProyecto para filtrar tareas por proyecto")
    void testBuscarTareasPorIdProyecto() {
        int idProyectoPrueba = 8888;
        Tareas t1 = new Tareas();
        t1.setNombre("Tarea de Proyecto A");
        t1.setDescripcion("Subtarea 1");
        t1.setFecha_vencimiento("2026-10-30");
        t1.setLista("To Do");
        t1.setEstado("Pendiente");
        t1.setId_proyecto(idProyectoPrueba);
        tareasRepository.save(t1);

        List<Tareas> tareasProyecto = tareasRepository.findByIdProyecto(idProyectoPrueba);

        assertNotNull(tareasProyecto, "La lista devuelta por id_proyecto no debe ser nula");
        assertFalse(tareasProyecto.isEmpty(), "Debe recuperar al menos una tarea asociada al ID del proyecto");
        assertEquals(idProyectoPrueba, tareasProyecto.get(0).getId_proyecto());
    }
}
