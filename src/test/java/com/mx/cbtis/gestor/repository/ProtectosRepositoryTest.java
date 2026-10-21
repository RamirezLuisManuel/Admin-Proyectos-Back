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

import com.mx.cbtis.gestor.modelo.Proyectos;

@SpringBootTest
@Transactional
public class ProtectosRepositoryTest {

    @Autowired
    private ProtectosRepository protectosRepository;

    @Test
    @DisplayName("UT-BD-PRY-01: Insertar nuevo proyecto con datos válidos")
    void testGuardarProyecto() {
        Proyectos proyecto = new Proyectos();
        proyecto.setNombre("Gestor de Calidad de Software");
        proyecto.setDescripcion("Sistema para gestión de fases de testing y métricas");
        proyecto.setFecha_inicio("2026-10-01");
        proyecto.setFecha_finalizacion("2026-12-15");
        proyecto.setEstado_proyecto("En progreso");
        proyecto.setFondo("#3357FF");

        Proyectos guardado = protectosRepository.save(proyecto);

        assertNotNull(guardado.getId_proyecto(), "El ID autogenerado del proyecto no debe ser nulo");
        assertEquals("Gestor de Calidad de Software", guardado.getNombre());
        assertEquals("En progreso", guardado.getEstado_proyecto());
    }

    @Test
    @DisplayName("UT-BD-PRY-02: Consultar proyecto por ID existente")
    void testBuscarProyectoPorId() {
        Proyectos proyecto = new Proyectos();
        proyecto.setNombre("Proyecto de Prueba Consulta");
        proyecto.setDescripcion("Validación de lectura en BD");
        proyecto.setFecha_inicio("2026-10-10");
        proyecto.setFecha_finalizacion("2026-10-30");
        proyecto.setEstado_proyecto("Activo");
        proyecto.setFondo("#00AA55");
        Proyectos guardado = protectosRepository.save(proyecto);

        Optional<Proyectos> encontrado = protectosRepository.findById(guardado.getId_proyecto());

        assertTrue(encontrado.isPresent(), "El proyecto debe ser recuperado por su ID");
        assertEquals("Proyecto de Prueba Consulta", encontrado.get().getNombre());
    }

    @Test
    @DisplayName("UT-BD-PRY-03: Listar todos los proyectos registrados (findAll)")
    void testListarProyectos() {
        Proyectos proyecto = new Proyectos();
        proyecto.setNombre("Proyecto Auxiliar");
        proyecto.setDescripcion("Proyecto para validar conteo/listado");
        proyecto.setFecha_inicio("2026-10-01");
        proyecto.setFecha_finalizacion("2026-11-01");
        proyecto.setEstado_proyecto("Planificado");
        proyecto.setFondo("#555555");
        protectosRepository.save(proyecto);

        Iterable<Proyectos> lista = protectosRepository.findAll();

        assertNotNull(lista, "La lista de proyectos devuelta no debe ser nula");
        assertTrue(lista.iterator().hasNext(), "La BD debe contener al menos un proyecto");
    }
}
