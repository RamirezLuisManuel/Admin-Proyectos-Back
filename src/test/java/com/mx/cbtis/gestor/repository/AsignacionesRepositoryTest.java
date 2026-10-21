package com.mx.cbtis.gestor.repository;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Optional;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import com.mx.cbtis.gestor.modelo.Asignaciones;

@SpringBootTest
@Transactional
public class AsignacionesRepositoryTest {

    @Autowired
    private AsignacionesRepository asignacionesRepository;

    @Test
    @DisplayName("UT-BD-ASG-01: Insertar y consultar asignación de tarea a un usuario")
    void testGuardarYConsultarAsignacion() {
        Asignaciones asignacion = new Asignaciones();
        asignacion.setId_usuario(131);
        asignacion.setId_tarea(199);
        asignacion.setFecha_asignacion("2026-10-13");

        Asignaciones guardada = asignacionesRepository.save(asignacion);

        assertNotNull(guardada.getId_asignacion(), "El ID autogenerado de asignación no debe ser nulo");
        assertEquals(131, guardada.getId_usuario());
        assertEquals(199, guardada.getId_tarea());

        Optional<Asignaciones> encontrada = asignacionesRepository.findById(guardada.getId_asignacion());
        assertTrue(encontrada.isPresent(), "La asignación debe existir en la base de datos");
    }
}
