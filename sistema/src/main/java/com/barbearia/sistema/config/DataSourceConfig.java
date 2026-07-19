package com.barbearia.sistema.config;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import javax.sql.DataSource;

/** Configura explicitamente o pool de conexões usado para acessar o banco. */
@Configuration // Faz o Spring procurar métodos @Bean nesta classe.
public class DataSourceConfig {

    // O objeto retornado fica disponível para JPA e demais componentes da aplicação.
    @Bean
    DataSource dataSource(
            // @Value lê as propriedades; ":" define string vazia como valor padrão.
            @Value("${spring.datasource.url}") String url,
            @Value("${spring.datasource.username:}") String username,
            @Value("${spring.datasource.password:}") String password) {
        // HikariCP mantém conexões reutilizáveis, evitando abrir uma conexão a cada consulta.
        HikariConfig config = new HikariConfig();
        config.setJdbcUrl(url);
        if (!username.isBlank()) {
            config.setUsername(username);
        }
        if (!password.isBlank()) {
            config.setPassword(password);
        }
        // Limita o consumo de conexões e mantém ao menos uma pronta para uso.
        config.setMaximumPoolSize(5);
        config.setMinimumIdle(1);
        return new HikariDataSource(config);
    }
}
