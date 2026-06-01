package br.com.projetoA3;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.boot.CommandLineRunner;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Classe principal da aplicação A3-2.0.
 * 
 * O pacote base para escaneamento é o próprio pacote da aplicação.
 * A exclusão do DataSourceAutoConfiguration é opcional, pois temos
 * DatabaseConfig definido manualmente. Mantemos para evitar conflitos.
 */
@SpringBootApplication(exclude = DataSourceAutoConfiguration.class)
public class A320Application {

    private static final Logger logger = LoggerFactory.getLogger(A320Application.class);

    public static void main(String[] args) {
        SpringApplication.run(A320Application.class, args);
        logger.info("Aplicação A3-2.0 iniciada com sucesso!");
    }

    /**
     * CommandLineRunner opcional para executar tarefas após a inicialização.
     * Útil para carregar dados iniciais ou verificar conectividade.
     */
    @Bean
    public CommandLineRunner demo() {
        return args -> {
            logger.info("Aplicação pronta para receber requisições. Banco SQLite configurado.");
            // Aqui você pode adicionar código de inicialização, como criar um usuário admin padrão se não existir
        };
    }
}