package br.com.projetoA3.repository;

import br.com.projetoA3.model.Usuario;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface UsuarioRepository extends JpaRepository<Usuario, Long> {
    
    // ✅ Busca usuário pelo campo "login" (usado na autenticação)
    Optional<Usuario> findByLogin(String login);
}