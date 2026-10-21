package com.mx.cbtis.gestor.repository;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Optional;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import com.mx.cbtis.gestor.modelo.Usuario;

@SpringBootTest
@Transactional
public class UsuarioRepositoryTest {

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Test
    @DisplayName("UT-BD-USR-01: Insertar nuevo usuario con datos válidos")
    void testGuardarUsuario() {
        Usuario usuario = new Usuario();
        usuario.setNombre("Carlos Mendoza");
        usuario.setCorreo_electronico("carlos.mendoza@cbtis75.edu.mx");
        usuario.setContrasena("clave123");
        usuario.setRol("Desarrollador");
        usuario.setImagen_usuario("#FF5733");

        Usuario guardado = usuarioRepository.save(usuario);

        assertNotNull(guardado.getId_usuario(), "El ID autogenerado no debe ser nulo tras el INSERT");
        assertEquals("Carlos Mendoza", guardado.getNombre(), "El nombre persistido debe coincidir");
        assertEquals("carlos.mendoza@cbtis75.edu.mx", guardado.getCorreo_electronico(), "El correo persistido debe coincidir");
    }

    @Test
    @DisplayName("UT-BD-USR-02: Consultar usuario por ID existente")
    void testBuscarUsuarioPorId() {
        Usuario usuario = new Usuario();
        usuario.setNombre("Test Usuario Read");
        usuario.setCorreo_electronico("test.read@cbtis75.edu.mx");
        usuario.setContrasena("pass123");
        usuario.setRol("Tester");
        usuario.setImagen_usuario("#123456");
        Usuario guardado = usuarioRepository.save(usuario);

        Optional<Usuario> encontrado = usuarioRepository.findById(guardado.getId_usuario());

        assertTrue(encontrado.isPresent(), "El registro debe ser recuperado exitosamente por ID");
        assertEquals("Test Usuario Read", encontrado.get().getNombre());
    }

    @Test
    @DisplayName("UT-BD-USR-03: Consulta nativa de autenticación findByCorreoPass con credenciales válidas")
    void testFindByCorreoPass_Exitoso() {
        Usuario usuario = new Usuario();
        usuario.setNombre("Usuario Auth");
        usuario.setCorreo_electronico("auth.valido@cbtis75.edu.mx");
        usuario.setContrasena("secreta123");
        usuario.setRol("Admin");
        usuario.setImagen_usuario("#00FF00");
        usuarioRepository.save(usuario);

        Optional<Usuario> login = usuarioRepository.findByCorreoPass("auth.valido@cbtis75.edu.mx", "secreta123");

        assertTrue(login.isPresent(), "Debe encontrar el usuario cuando el correo y contraseña coinciden");
        assertEquals("Usuario Auth", login.get().getNombre());
    }

    @Test
    @DisplayName("UT-BD-USR-04: Consulta nativa findByCorreoPass con contraseña incorrecta")
    void testFindByCorreoPass_CredencialesIncorrectas() {
        Usuario usuario = new Usuario();
        usuario.setNombre("Usuario Auth Fail");
        usuario.setCorreo_electronico("auth.fail@cbtis75.edu.mx");
        usuario.setContrasena("correcta");
        usuario.setRol("Estudiante");
        usuario.setImagen_usuario("#FF0000");
        usuarioRepository.save(usuario);

        Optional<Usuario> login = usuarioRepository.findByCorreoPass("auth.fail@cbtis75.edu.mx", "erronea999");

        assertFalse(login.isPresent(), "No debe retornar usuario si la contraseña es incorrecta");
    }
}
