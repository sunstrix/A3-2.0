package br.com.projetoA3.service;

import br.com.projetoA3.model.Usuario;
import br.com.projetoA3.repository.UsuarioRepository;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.Collections;

/**
 * Implementação customizada do UserDetailsService do Spring Security.
 * 
 * Esta classe é a "ponte" entre o Spring Security e o nosso banco de dados.
 * Quando um usuário tenta fazer login, o Spring Security chama o método
 * loadUserByUsername() para buscar os dados do usuário no repositório.
 * 
 * O username usado aqui é o campo "login" da entidade Usuario.
 */
@Service
public class CustomUserDetailsService implements UserDetailsService {

    private final UsuarioRepository usuarioRepository;

    public CustomUserDetailsService(UsuarioRepository usuarioRepository) {
        this.usuarioRepository = usuarioRepository;
    }

    @Override
    public UserDetails loadUserByUsername(String login) throws UsernameNotFoundException {
        // Busca o usuário pelo campo "login" no banco
        Usuario usuario = usuarioRepository.findByLogin(login)
                .orElseThrow(() -> new UsernameNotFoundException(
                    "Usuário não encontrado com o login: " + login
                ));

        // Verifica se o usuário está ativo
        if (!usuario.isAtivo()) {
            throw new UsernameNotFoundException("Usuário está desativado: " + login);
        }

        // Converte o perfil do nosso sistema para GrantedAuthority do Spring Security
        // O Spring Security espera que roles comecem com "ROLE_"
        String role = "ROLE_" + usuario.getPerfil().name();
        SimpleGrantedAuthority authority = new SimpleGrantedAuthority(role);

        // Retorna um UserDetails do Spring Security com os dados do nosso Usuario
        return new User(
                usuario.getLogin(),           // username (usado para login)
                usuario.getSenha(),           // senha (já com hash BCrypt)
                usuario.isAtivo(),            // enabled
                true,                         // accountNonExpired
                true,                         // credentialsNonExpired
                true,                         // accountNonLocked
                Collections.singletonList(authority)  // authorities (roles)
        );
    }
}