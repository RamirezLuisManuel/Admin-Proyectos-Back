package com.mx.cbtis.gestor;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
class Ejercicio5MyApiGestorApplicationTests {

	@Test
	void contextLoads() {
	}

	@Test
	void mainRuns() {
		Ejercicio5MyApiGestorApplication.main(new String[] {
				"--server.port=0",
				"--spring.datasource.url=jdbc:h2:mem:gestor-main;DB_CLOSE_DELAY=-1;MODE=MySQL"
		});
	}
}
