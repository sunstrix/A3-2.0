package br.com.projetoA3.service;

import br.com.projetoA3.exception.RegraDeNegocioException;
import br.com.projetoA3.model.Usuario;
import br.com.projetoA3.repository.UsuarioRepository;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

/**
 * Service responsável pela lógica de negócio dos Usuários.
 * Atualizado com validação de CPF e logging de diagnóstico.
 */
@Service
@Transactional(readOnly = true)
public class UsuarioService {

    private final UsuarioRepository usuarioRepository;
    private final PasswordEncoder passwordEncoder;

    public UsuarioService(UsuarioRepository usuarioRepository,
                          PasswordEncoder passwordEncoder) {
        this.usuarioRepository = usuarioRepository;
        this.passwordEncoder = passwordEncoder;
    }

    // ==========================================
    // CONSULTAS (READ-ONLY)
    // ==========================================

    public List<Usuario> findAll() {
        return usuarioRepository.findAll();
    }

    public Optional<Usuario> findById(Long id) {
        return usuarioRepository.findById(id);
    }

    public Optional<Usuario> findByLogin(String login) {
        return usuarioRepository.findByLogin(login);
    }

    public Optional<Usuario> findByEmail(String email) {
        return usuarioRepository.findByEmail(email);
    }

    public long countAtivos() {
        return usuarioRepository.findAll().stream()
                .filter(u -> u.getAtivo() != null && u.getAtivo())
                .count();
    }

    // ==========================================
    // ESCRITA (TRANSACTIONAL)
    // ==========================================

    /**
     * Cria um novo usuário no sistema.
     */
    @Transactional
    public Usuario save(Usuario usuario) {
        System.out.println("🚀 Iniciando persistência de novo usuário: " + usuario.getLogin());

        // 1. Valida unicidade do login
        if (usuarioRepository.existsByLogin(usuario.getLogin())) {
            throw new RegraDeNegocioException("O login '" + usuario.getLogin() + "' já está em uso.");
        }

        // 2. Valida unicidade do email
        if (usuario.getEmail() != null && !usuario.getEmail().isBlank()
                && usuarioRepository.existsByEmail(usuario.getEmail())) {
            throw new RegraDeNegocioException("O e-mail '" + usuario.getEmail() + "' já está cadastrado.");
        }

        // 3. Valida unicidade do CPF (Causa provável do erro silencioso)
        if (usuario.getCpf() != null && !usuario.getCpf().isBlank()
                && usuarioRepository.existsByCpf(usuario.getCpf())) {
            throw new RegraDeNegocioException("O CPF '" + usuario.getCpf() + "' já pertence a outro usuário.");
        }

        // 4. Tratamento de Senha
        if (usuario.getSenha() != null && !usuario.getSenha().isBlank()) {
            usuario.setSenha(passwordEncoder.encode(usuario.getSenha()));
        } else {
            throw new RegraDeNegocioException("A senha é obrigatória para novos usuários.");
        }

        // 5. Valores Padrão
        if (usuario.getAtivo() == null) {
            usuario.setAtivo(true);
        }

        Usuario salvo = usuarioRepository.save(usuario);
        System.out.println("✅ Usuário salvo com sucesso no banco! ID: " + salvo.getId());
        return salvo;
    }

    /**
     * Atualiza um usuário existente.
     */
    @Transactional
    public Usuario update(Long id, Usuario dadosAtualizados) {
        Usuario existente = usuarioRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Usuário não encontrado com ID: " + id));

        // Validação de duplicidade para Login
        if (dadosAtualizados.getLogin() != null 
                && !dadosAtualizados.getLogin().equals(existente.getLogin())
                && usuarioRepository.existsByLogin(dadosAtualizados.getLogin())) {
            throw new RegraDeNegocioException("Novo login já em uso.");
        }

        // Validação de duplicidade para Email
        if (dadosAtualizados.getEmail() != null 
                && !dadosAtualizados.getEmail().equals(existente.getEmail())
                && usuarioRepository.existsByEmail(dadosAtualizados.getEmail())) {
            throw new RegraDeNegocioException("Novo e-mail já cadastrado.");
        }

        // Validação de duplicidade para CPF
        if (dadosAtualizados.getCpf() != null 
                && !dadosAtualizados.getCpf().equals(existente.getCpf())
                && usuarioRepository.existsByCpf(dadosAtualizados.getCpf())) {
            throw new RegraDeNegocioException("Novo CPF já cadastrado.");
        }

        // Atualiza campos
        existente.setNome(dadosAtualizados.getNome());
        existente.setLogin(dadosAtualizados.getLogin());
        existente.setEmail(dadosAtualizados.getEmail());
        existente.setCpf(dadosAtualizados.getCpf());
        existente.setCargo(dadosAtualizados.getCargo());
        existente.setPerfil(dadosAtualizados.getPerfil());

        if (dadosAtualizados.getSenha() != null && !dadosAtualizados.getSenha().isBlank()) {
            existente.setSenha(passwordEncoder.encode(dadosAtualizados.getSenha()));
        }

        return usuarioRepository.save(existente);
    }

    @Transactional
    public Usuario toggleAtivo(Long id) {
        Usuario usuario = usuarioRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Usuário não encontrado com ID: " + id));
        usuario.setAtivo(usuario.getAtivo() == null || !usuario.getAtivo());
        return usuarioRepository.save(usuario);
    }

    @Transactional
    public void deleteById(Long id) {
        if (!usuarioRepository.existsById(id)) {
            throw new EntityNotFoundException("Usuário não encontrado.");
        }
        usuarioRepository.deleteById(id);
    }

    @Transactional
    public void ativar(Long id) {
        Usuario usuario = usuarioRepository.findById(id).orElseThrow();
        usuario.setAtivo(true);
        usuarioRepository.save(usuario);
    }

    @Transactional
    public void desativar(Long id) {
        Usuario usuario = usuarioRepository.findById(id).orElseThrow();
        usuario.setAtivo(false);
        usuarioRepository.save(usuario);
    }
}