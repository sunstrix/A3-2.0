package br.com.projetoA3.config;

import br.com.projetoA3.model.Usuario;
import br.com.projetoA3.repository.UsuarioRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;

/**
 * Inicializador de dados do sistema.
 * 
 * Cria usuários padrão na primeira execução com senhas hasheadas usando BCrypt.
 * Isso garante compatibilidade com o Spring Security 6 que exige senhas codificadas.
 * 
 * ⚠️ IMPORTANTE: Se os usuários já existirem no banco, este código não os sobrescreve.
 * Para recriar os usuários do zero, delete o arquivo do banco SQLite (./data/a3_projeto.db)
 * antes de iniciar a aplicação.
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
    public CommandLineRunner initUsuarios(UsuarioRepository usuarioRepository,
                                            PasswordEncoder passwordEncoder) {
        return args -> {
            criarUsuarioSeNaoExistir(
                usuarioRepository, 
                passwordEncoder,
                "Administrador",
                "admin",
                "admin@projetoA3.com",
                "00000000000",  // ✅ CPF sem formatação (11 dígitos)
                "Administrador do Sistema",
                Usuario.Perfil.ADMINISTRADOR,
                "admin123"
            );

            criarUsuarioSeNaoExistir(
                usuarioRepository, 
                passwordEncoder,
                "Gerente Silva",
                "gerente",
                "gerente@projetoA3.com",
                "11111111111",  // ✅ CPF sem formatação (11 dígitos)
                "Gerente de Projetos",
                Usuario.Perfil.GERENTE,
                "gerente123"
            );

            criarUsuarioSeNaoExistir(
                usuarioRepository, 
                passwordEncoder,
                "João Colaborador",
                "colaborador",
                "colaborador@projetoA3.com",
                "22222222222",  // ✅ CPF sem formatação (11 dígitos)
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
     * A senha é hasheada com BCrypt antes de ser salva.
     */
    private void criarUsuarioSeNaoExistir(UsuarioRepository repository,
                                           PasswordEncoder encoder,
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
            
            // ✅ CRÍTICO: Hash da senha com BCrypt
            usuario.setSenha(encoder.encode(senhaPlana));
            
            repository.save(usuario);
            System.out.println("✅ Usuário criado: " + login + " (" + perfil + ")");
        } else {
            System.out.println("ℹ️  Usuário já existe: " + login);
        }
    }
}