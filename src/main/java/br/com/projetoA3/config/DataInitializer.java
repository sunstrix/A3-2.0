package br.com.projetoA3.config;

import br.com.projetoA3.model.Usuario;
import br.com.projetoA3.repository.UsuarioRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Inicializador de dados do sistema.
 * 
 * Cria usuarios padrao na primeira execucao com senhas em TEXTO PURO.
 * 
 * ATENCAO: Isso so funciona porque o SecurityConfig foi configurado
 * para usar NoOpPasswordEncoder. Em um ambiente de producao real, voce
 * DEVERIA injetar o PasswordEncoder e usar encoder.encode(senha).
 * 
 * Para recriar os usuarios do zero, delete o arquivo do banco SQLite
 * (pasta ./data/) antes de iniciar a aplicacao.
 */
@Configuration
public class DataInitializer {

    /**
     * Cria usuarios de teste na inicializacao do sistema.
     * 
     * Usuarios criados:
     * - admin / admin123 (Perfil: ADMINISTRADOR)
     * - gerente / gerente123 (Perfil: GERENTE)
     * - colaborador / colab123 (Perfil: COLABORADOR)
     * - atendente / atendente123 (Perfil: ATENDENTE)
     */
    @Bean
    public CommandLineRunner initUsuarios(UsuarioRepository usuarioRepository) {
        return args -> {
            criarUsuarioSeNaoExistir(
                usuarioRepository, 
                "Administrador",
                "admin",
                "admin@projetoA3.com",
                "00000000000",  // CPF sem formatacao (11 digitos)
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
                "Joao Colaborador",
                "colaborador",
                "colaborador@projetoA3.com",
                "22222222222",
                "Desenvolvedor",
                Usuario.Perfil.COLABORADOR,
                "colab123"
            );

            criarUsuarioSeNaoExistir(
                usuarioRepository, 
                "Maria Atendente",
                "atendente",
                "atendente@projetoA3.com",
                "33333333333",
                "Analista de Suporte",
                Usuario.Perfil.ATENDENTE,
                "atendente123"
            );

            System.out.println("========================================");
            System.out.println("Usuarios de teste criados/verificados!");
            System.out.println("========================================");
            System.out.println("ADMIN:        login=admin        | senha=admin123");
            System.out.println("GERENTE:      login=gerente      | senha=gerente123");
            System.out.println("COLABORADOR:  login=colaborador  | senha=colab123");
            System.out.println("ATENDENTE:    login=atendente    | senha=atendente123");
            System.out.println("========================================");
        };
    }

    /**
     * Cria um usuario apenas se ele ainda nao existir no banco.
     * A senha e salva em texto puro (compativel com NoOpPasswordEncoder).
     */
    private void criarUsuarioSeNaoExistir(UsuarioRepository repository,
                                           String nome,
                                           String login,
                                           String email,
                                           String cpf,
                                           String cargo,
                                           Usuario.Perfil perfil,
                                           String senhaPlana) {
        // Verifica se ja existe um usuario com esse login
        if (repository.findByLogin(login).isEmpty()) {
            Usuario usuario = new Usuario();
            usuario.setNome(nome);
            usuario.setLogin(login);
            usuario.setEmail(email);
            usuario.setCpf(cpf);
            usuario.setCargo(cargo);
            usuario.setPerfil(perfil);
            usuario.setAtivo(true);
            usuario.setNotificacoesEmail(true);
            
            // Salva a senha em texto puro (sem hash)
            usuario.setSenha(senhaPlana);
            
            repository.save(usuario);
            System.out.println("[OK] Usuario criado: " + login + " (" + perfil + ")");
        } else {
            System.out.println("[INFO] Usuario ja existe: " + login);
        }
    }
}