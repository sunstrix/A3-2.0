package br.com.projetoA3.repository;

import br.com.projetoA3.model.Usuario;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * Repository responsável pelas operações de banco de dados para a entidade Usuario.
 * Atualizado para suportar validação de unicidade de CPF.
 */
@Repository
public interface UsuarioRepository extends JpaRepository<Usuario, Long> {

    /**
     * Busca um usuário pelo login (username).
     * Usado pelo DataInitializer e pelo UserDetailsService do Spring Security.
     */
    Optional<Usuario> findByLogin(String login);

    /**
     * Busca um usuário pelo e-mail.
     */
    Optional<Usuario> findByEmail(String email);

    /**
     * Busca um usuário pelo CPF.
     */
    Optional<Usuario> findByCpf(String cpf);

    /**
     * Verifica se já existe um usuário com o login informado.
     */
    boolean existsByLogin(String login);

    /**
     * Verifica se já existe um usuário com o e-mail informado.
     */
    boolean existsByEmail(String email);

    /**
     * ✅ NOVO: Verifica se já existe um usuário com o CPF informado.
     * Essencial para evitar erros de ConstraintViolation do banco de dados (SQLite).
     */
    boolean existsByCpf(String cpf);
}