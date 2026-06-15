package br.com.projetoA3.service;

import br.com.projetoA3.exception.RegraDeNegocioException;
import br.com.projetoA3.model.Usuario;
import br.com.projetoA3.repository.UsuarioRepository;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

/**
 * Service responsavel pela logica de negocio dos Usuarios.
 * Atualizado com validacao de CPF, logging de diagnostico,
 * suporte ao modulo Help Desk e envio de e-mail de boas-vindas.
 */
@Service
@Transactional(readOnly = true)
public class UsuarioService {

    private final UsuarioRepository usuarioRepository;
    private final PasswordEncoder passwordEncoder;

    @Autowired
    private EmailService emailService;

    public UsuarioService(UsuarioRepository usuarioRepository,
                          PasswordEncoder passwordEncoder) {
        this.usuarioRepository = usuarioRepository;
        this.passwordEncoder = passwordEncoder;
    }

    // ==========================================
    // CONSULTAS (READ-ONLY)
    // ==========================================

    public List<<Usuario> findAll() {
        return usuarioRepository.findAll();
    }

    public Optional<<Usuario> findById(Long id) {
        return usuarioRepository.findById(id);
    }

    public Optional<<Usuario> findByLogin(String login) {
        return usuarioRepository.findByLogin(login);
    }

    public Optional<<Usuario> findByEmail(String email) {
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
     * Cria um novo usuario no sistema e envia e-mail de boas-vindas.
     */
    @Transactional
    public Usuario save(Usuario usuario) {
        System.out.println("Iniciando persistencia de novo usuario: " + usuario.getLogin());

        // 1. Valida unicidade do login
        if (usuarioRepository.existsByLogin(usuario.getLogin())) {
            throw new RegraDeNegocioException("O login '" + usuario.getLogin() + "' ja esta em uso.");
        }

        // 2. Valida unicidade do email
        if (usuario.getEmail() != null && !usuario.getEmail().isBlank()
                && usuarioRepository.existsByEmail(usuario.getEmail())) {
            throw new RegraDeNegocioException("O e-mail '" + usuario.getEmail() + "' ja esta cadastrado.");
        }

        // 3. Valida unicidade do CPF (Causa provavel do erro silencioso)
        if (usuario.getCpf() != null && !usuario.getCpf().isBlank()
                && usuarioRepository.existsByCpf(usuario.getCpf())) {
            throw new RegraDeNegocioException("O CPF '" + usuario.getCpf() + "' ja pertence a outro usuario.");
        }

        // 4. Tratamento de Senha
        if (usuario.getSenha() != null && !usuario.getSenha().isBlank()) {
            usuario.setSenha(passwordEncoder.encode(usuario.getSenha()));
        } else {
            throw new RegraDeNegocioException("A senha e obrigatoria para novos usuarios.");
        }

        // 5. Valores Padrao
        if (usuario.getAtivo() == null) {
            usuario.setAtivo(true);
        }

        Usuario salvo = usuarioRepository.save(usuario);
        System.out.println("Usuario salvo com sucesso no banco! ID: " + salvo.getId());

        // 6. Envia e-mail de boas-vindas de forma assincrona
        if (salvo.getEmail() != null && !salvo.getEmail().isBlank()) {
            emailService.enviarEmailBoasVindas(salvo);
        }

        return salvo;
    }

    /**
     * Atualiza um usuario existente.
     */
    @Transactional
    public Usuario update(Long id, Usuario dadosAtualizados) {
        Usuario existente = usuarioRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Usuario nao encontrado com ID: " + id));

        // Validacao de duplicidade para Login
        if (dadosAtualizados.getLogin() != null 
                && !dadosAtualizados.getLogin().equals(existente.getLogin())
                && usuarioRepository.existsByLogin(dadosAtualizados.getLogin())) {
            throw new RegraDeNegocioException("Novo login ja em uso.");
        }

        // Validacao de duplicidade para Email
        if (dadosAtualizados.getEmail() != null 
                && !dadosAtualizados.getEmail().equals(existente.getEmail())
                && usuarioRepository.existsByEmail(dadosAtualizados.getEmail())) {
            throw new RegraDeNegocioException("Novo e-mail ja cadastrado.");
        }

        // Validacao de duplicidade para CPF
        if (dadosAtualizados.getCpf() != null 
                && !dadosAtualizados.getCpf().equals(existente.getCpf())
                && usuarioRepository.existsByCpf(dadosAtualizados.getCpf())) {
            throw new RegraDeNegocioException("Novo CPF ja cadastrado.");
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
                .orElseThrow(() -> new EntityNotFoundException("Usuario nao encontrado com ID: " + id));
        usuario.setAtivo(usuario.getAtivo() == null || !usuario.getAtivo());
        return usuarioRepository.save(usuario);
    }

    @Transactional
    public void deleteById(Long id) {
        if (!usuarioRepository.existsById(id)) {
            throw new EntityNotFoundException("Usuario nao encontrado.");
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

    // ==========================================
    // SUPORTE AO MODULO HELP DESK
    // ==========================================

    /**
     * Metodo auxiliar para os Controllers do Help Desk recuperarem o usuario logado.
     * Tenta buscar pelo e-mail primeiro. Se nao encontrar, faz fallback para o login,
     * pois o Spring Security pode injetar qualquer um dos dois no objeto Principal.
     *
     * @param identificador O e-mail ou login do usuario logado.
     * @return A entidade Usuario completa.
     * @throws EntityNotFoundException se o usuario nao for encontrado.
     */
    public Usuario buscarPorEmail(String identificador) {
        return usuarioRepository.findByEmail(identificador)
                .or(() -> usuarioRepository.findByLogin(identificador))
                .orElseThrow(() -> new EntityNotFoundException("Usuario nao encontrado com o identificador: " + identificador));
    }
}