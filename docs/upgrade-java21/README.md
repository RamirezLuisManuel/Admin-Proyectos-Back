# Actualización del backend a Java 21 y Spring Boot 4.1.1

Fecha de verificación: 21 de octubre de 2026.

## Evidencias de los POM

- `pom-antes.xml`: POM del último commit previo a esta actualización (Spring Boot 2.7.17 y Java 11).
- `pom-previo-springboot4.xml`: cambios manuales de Java 21 y plugins que ya estaban en el workspace antes de migrar Spring Boot.
- `pom-despues.xml`: copia del POM final, equivalente a `../../pom.xml`.

## Diagnóstico del error de arranque

Se reprodujo `spring-boot:run` con Spring Boot 2.7.17 y Java 21. La aplicación compiló, pero falló al inicializar la conexión JDBC de Hikari y posteriormente `entityManagerFactory`. La terminal de diagnóstico no tenía `DB_USERNAME` ni `DB_PASSWORD`. El mensaje final `Application finished with exit code: 1` no acredita por sí solo una incompatibilidad de Spring Boot: deben revisarse las excepciones anteriores del log y configurar las credenciales en cada terminal que inicia la API.

La actualización se realizó para adoptar la versión estable publicada solicitada. No sustituye la configuración de la base de datos.

## Cambios aplicados

| Elemento | Antes | Después / motivo |
|---|---|---|
| Spring Boot | 2.7.17 | 4.1.1, versión estable verificada en Spring y Maven Central |
| Java objetivo | 11 | 21, con `maven.compiler.release=21` |
| Maven Wrapper | 3.9.6 en el commit original | 3.9.16, cambio manual conservado |
| Starter web | `spring-boot-starter-web` | `spring-boot-starter-webmvc` |
| Persistencia | `javax.persistence` | `jakarta.persistence`, en las cinco entidades |
| Generación de identificadores | `GenerationType.AUTO` | `GenerationType.IDENTITY`; las cinco claves existentes de MariaDB son `AUTO_INCREMENT` |
| Driver JDBC | Propiedad limitada a DBCP2 | `spring.datasource.driver-class-name`, aplicable al datasource utilizado |
| Pruebas | Starter genérico y MariaDB local | Starters de pruebas Web MVC/JPA y H2 exclusivamente en el classpath de pruebas |
| Plugins | JaCoCo 0.8.11; otros heredados | Compiler 3.13.0, Surefire 3.5.2, JaCoCo 0.8.12 y SonarScanner 5.5.0.6356 |

Se conservaron las rutas y nombres JSON con guiones bajos. Los datos y el esquema de producción no se migraron ni borraron. H2 genera y descarta únicamente tablas en memoria durante las pruebas. No es una sustitución de las pruebas específicas sobre MariaDB.

## Verificación realizada

- `mvnw.cmd clean verify` completó la primera construcción; tras añadir regresiones HTTP se ejecutó `mvnw.cmd verify`.
- Resultado final: **17 pruebas, 0 fallos, 0 errores, 0 omitidas**.
- Se generaron JAR ejecutable, reportes Surefire, `target/site/jacoco/index.html` y `target/site/jacoco/jacoco.xml`.
- Se comprobó bytecode major version 65 (Java 21).
- El JAR arrancó sobre MariaDB `dbgestor_p2`, con `spring.jpa.hibernate.ddl-auto=none` durante la comprobación para evitar cambios al esquema.
- `/buscarProyectos` respondió y sus identificadores coincidieron con la consulta realizada a través del frontend actualizado.
- Las tres regresiones nuevas validan CRUD de proyecto, consultas nativas de tareas/comentarios y autenticación por consulta parametrizada mediante MockMvc + JPA + H2.

El análisis SonarQube queda preparado en el POM; no se ejecutó contra un servidor ni se atribuye un Quality Gate aprobado.

## Reproducción en PowerShell

Desde la raíz de este repositorio:

```powershell
java -version
.\mvnw.cmd -version
.\mvnw.cmd clean verify
```

Las pruebas no requieren variables de MariaDB. Para iniciar la aplicación sí debes proporcionar credenciales válidas:

```powershell
$env:DB_URL = 'jdbc:mariadb://localhost:3306/dbgestor'
$env:DB_USERNAME = 'TU_USUARIO_BD'
$env:DB_PASSWORD = 'TU_PASSWORD_BD'
.\mvnw.cmd spring-boot:run
```

La API usa el puerto 8081. Las variables anteriores se deben configurar en la misma terminal del arranque. Jenkins puede ejecutar `clean verify` sin credenciales de MariaDB; los despliegues y pruebas sobre MariaDB sí necesitan credenciales.

## Fuentes oficiales

- [Requisitos de Spring Boot 4.1.1](https://docs.spring.io/spring-boot/system-requirements.html)
- [Migración a Spring Boot 4](https://github.com/spring-projects/spring-boot/wiki/Spring-Boot-4.0-Migration-Guide)
- [Maven Central: Spring Boot](https://repo.maven.apache.org/maven2/org/springframework/boot/spring-boot-starter-parent/4.1.1/)
