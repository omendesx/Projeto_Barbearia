package com.barbearia.sistema;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/** Ponto de entrada da aplicação. */
@SpringBootApplication // Combina configuração, auto-configuração e busca de componentes.
public class SistemaApplication {

	public static void main(String[] args) {
		// Cria o contexto Spring e inicia o servidor web embutido.
		SpringApplication.run(SistemaApplication.class, args);
	}

}
