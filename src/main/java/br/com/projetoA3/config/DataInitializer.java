package br.com.projetoA3.config;

import br.com.projetoA3.model.Usuario;
import br.com.projetoA3.repository.UsuarioRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class DataInitializer {

    @Bean
    CommandLineRunner initDatabase(UsuarioRepository usuarioRepository) {
        return args -> {
            // ✅ Só cria os usuários se a tabela estiver vazia
            if (usuarioRepository.count() == 0) {
                
                usuarioRepository.save(new Usuario(
                    "Administrador", "11144477735", "admin@empresa.com",
                    "TI", "admin", "admin", Usuario.Perfil.ADMINISTRADOR
                ));

                usuarioRepository.save(new Usuario(
                    "Gerente Projetos", "87654321596", "gerente@empresa.com",
                    "Gerente", "gerente", "gerente", Usuario.Perfil.GERENTE
                ));

                usuarioRepository.save(new Usuario(
                    "Colaborador", "39053344705", "colaborador@empresa.com",
                    "Analista", "colaborador", "123456", Usuario.Perfil.COLABORADOR
                ));

                System.out.println("✅ Usuários de teste criados com sucesso!");
            }
        };
    }
}