package br.com.projetoA3.config;

import br.com.projetoA3.model.Usuario;
import br.com.projetoA3.repository.UsuarioRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Inicializador de dados do sistema.
 * 
 * Cria usuários padrão na primeira execução com senhas em TEXTO PURO.
 * 
 * ⚠️ ATENÇÃO: Isso só funciona porque o SecurityConfig foi configurado
 * para usar NoOpPasswordEncoder. Em um ambiente de produção real, você
 * DEVERIA injetar o PasswordEncoder e usar encoder.encode(senha).
 * 
 * Para recriar os usuários do zero, delete o arquivo do banco SQLite
 * (pasta ./data/) antes de iniciar a aplicação.
 */
@Configuration
public class DataInitializer {

    /**
     * Cria usuários de teste na inicialização do sistema.
     * 
     * Usuários criados:
     * - admin / admin123 (Perfil: ADMINISTRADOR)
     * - gerente / gerente123 (Perfil: GERENTE)
     * - colaborador / colab123 (Perfil: COLABORADOR)
     */
    @Bean
    public CommandLineRunner initUsuarios(UsuarioRepository usuarioRepository) {
        return args -> {
            criarUsuarioSeNaoExistir(
                usuarioRepository, 
                "Administrador",
                "admin",
                "admin@projetoA3.com",
                "00000000000",  // CPF sem formatação (11 dígitos)
                "Administrador do Sistema",
                Usuario.Perfil.ADMINISTRADOR,
                "admin123"       // Senha em texto puro
            );

            criarUsuarioSeNaoExistir(
                usuarioRepository, 
                "Gerente Silva",
                "gerente",
                "gerente@projetoA3.com",
                "11111111111",
                "Gerente de Projetos",
                Usuario.Perfil.GERENTE,
                "gerente123"
            );

            criarUsuarioSeNaoExistir(
                usuarioRepository, 
                "João Colaborador",
                "colaborador",
                "colaborador@projetoA3.com",
                "22222222222",
                "Desenvolvedor",
                Usuario.Perfil.COLABORADOR,
                "colab123"
            );

            System.out.println("========================================");
            System.out.println("✅ Usuários de teste criados/verificados!");
            System.out.println("========================================");
            System.out.println("👤 ADMIN:        login=admin        | senha=admin123");
            System.out.println("👔 GERENTE:      login=gerente      | senha=gerente123");
            System.out.println("👷 COLABORADOR:  login=colaborador  | senha=colab123");
            System.out.println("========================================");
        };
    }

    /**
     * Cria um usuário apenas se ele ainda não existir no banco.
     * A senha é salva em texto puro (compatível com NoOpPasswordEncoder).
     */
    private void criarUsuarioSeNaoExistir(UsuarioRepository repository,
                                           String nome,
                                           String login,
                                           String email,
                                           String cpf,
                                           String cargo,
                                           Usuario.Perfil perfil,
                                           String senhaPlana) {
        // Verifica se já existe um usuário com esse login
        if (repository.findByLogin(login).isEmpty()) {
            Usuario usuario = new Usuario();
            usuario.setNome(nome);
            usuario.setLogin(login);
            usuario.setEmail(email);
            usuario.setCpf(cpf);
            usuario.setCargo(cargo);
            usuario.setPerfil(perfil);
            usuario.setAtivo(true);
            
            // ✅ Salva a senha em texto puro (sem hash)
            usuario.setSenha(senhaPlana);
            
            repository.save(usuario);
            System.out.println("✅ Usuário criado: " + login + " (" + perfil + ")");
        } else {
            System.out.println("ℹ️  Usuário já existe: " + login);
        }
    }
}