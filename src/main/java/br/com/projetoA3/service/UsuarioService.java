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
 * 
 * Princípios aplicados:
 * - Injeção por construtor (sem @Autowired em campo)
 * - @Transactional(readOnly = true) na classe (otimiza transações de leitura)
 * - Override com @Transactional apenas em métodos de escrita
 * - Validação de negócio com exceções customizadas
 * - Hash de senha com PasswordEncoder (BCrypt ou NoOp conforme SecurityConfig)
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

    /**
     * Lista todos os usuários do sistema.
     */
    public List<Usuario> findAll() {
        return usuarioRepository.findAll();
    }

    /**
     * Busca um usuário por ID.
     */
    public Optional<Usuario> findById(Long id) {
        return usuarioRepository.findById(id);
    }

    /**
     * Busca um usuário por login (username).
     */
    public Optional<Usuario> findByLogin(String login) {
        return usuarioRepository.findByLogin(login);
    }

    /**
     * Busca um usuário por e-mail.
     */
    public Optional<Usuario> findByEmail(String email) {
        return usuarioRepository.findByEmail(email);
    }

    /**
     * Conta o total de usuários ativos no sistema.
     */
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
     * 
     * @param usuario Dados do usuário a ser criado
     * @return Usuário persistido
     * @throws RegraDeNegocioException se login ou email já estiverem em uso
     */
    @Transactional
    public Usuario save(Usuario usuario) {
        // Valida unicidade do login
        if (usuarioRepository.existsByLogin(usuario.getLogin())) {
            throw new RegraDeNegocioException(
                "Já existe um usuário com o login: " + usuario.getLogin()
            );
        }

        // Valida unicidade do email (se informado)
        if (usuario.getEmail() != null && !usuario.getEmail().isBlank()
                && usuarioRepository.existsByEmail(usuario.getEmail())) {
            throw new RegraDeNegocioException(
                "Já existe um usuário com o e-mail: " + usuario.getEmail()
            );
        }

        // Hash da senha (usa o encoder configurado no SecurityConfig)
        if (usuario.getSenha() != null && !usuario.getSenha().isBlank()) {
            usuario.setSenha(passwordEncoder.encode(usuario.getSenha()));
        }

        // Define como ativo por padrão
        if (usuario.getAtivo() == null) {
            usuario.setAtivo(true);
        }

        return usuarioRepository.save(usuario);
    }

    /**
     * ✅ NOVO: Atualiza um usuário existente.
     * 
     * @param id ID do usuário a ser atualizado
     * @param dadosAtualizados Novos dados do usuário
     * @return Usuário atualizado
     * @throws EntityNotFoundException se o usuário não existir
     * @throws RegraDeNegocioException se login ou email já estiverem em uso por outro usuário
     */
    @Transactional
    public Usuario update(Long id, Usuario dadosAtualizados) {
        Usuario existente = usuarioRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException(
                    "Usuário não encontrado com ID: " + id
                ));

        // Valida unicidade do login (se foi alterado)
        if (dadosAtualizados.getLogin() != null 
                && !dadosAtualizados.getLogin().equals(existente.getLogin())
                && usuarioRepository.existsByLogin(dadosAtualizados.getLogin())) {
            throw new RegraDeNegocioException(
                "Já existe um usuário com o login: " + dadosAtualizados.getLogin()
            );
        }

        // Valida unicidade do email (se foi alterado)
        if (dadosAtualizados.getEmail() != null 
                && !dadosAtualizados.getEmail().equals(existente.getEmail())
                && usuarioRepository.existsByEmail(dadosAtualizados.getEmail())) {
            throw new RegraDeNegocioException(
                "Já existe um usuário com o e-mail: " + dadosAtualizados.getEmail()
            );
        }

        // Atualiza os campos editáveis
        existente.setNome(dadosAtualizados.getNome());
        existente.setLogin(dadosAtualizados.getLogin());
        existente.setEmail(dadosAtualizados.getEmail());
        existente.setCpf(dadosAtualizados.getCpf());
        existente.setCargo(dadosAtualizados.getCargo());
        existente.setPerfil(dadosAtualizados.getPerfil());

        // Só atualiza a senha se foi informada uma nova (não vazia)
        if (dadosAtualizados.getSenha() != null 
                && !dadosAtualizados.getSenha().isBlank()) {
            existente.setSenha(passwordEncoder.encode(dadosAtualizados.getSenha()));
        }

        return usuarioRepository.save(existente);
    }

    /**
     * ✅ NOVO: Alterna o status ativo/inativo de um usuário.
     * Útil para desativar contas sem removê-las do sistema.
     * 
     * @param id ID do usuário
     * @return Usuário com status alterado
     * @throws EntityNotFoundException se o usuário não existir
     */
    @Transactional
    public Usuario toggleAtivo(Long id) {
        Usuario usuario = usuarioRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException(
                    "Usuário não encontrado com ID: " + id
                ));

        // Inverte o status atual
        boolean statusAtual = usuario.getAtivo() != null ? usuario.getAtivo() : true;
        usuario.setAtivo(!statusAtual);

        return usuarioRepository.save(usuario);
    }

    /**
     * Remove um usuário do sistema.
     * 
     * @param id ID do usuário a ser removido
     * @throws EntityNotFoundException se o usuário não existir
     */
    @Transactional
    public void deleteById(Long id) {
        if (!usuarioRepository.existsById(id)) {
            throw new EntityNotFoundException(
                "Usuário não encontrado com ID: " + id
            );
        }
        usuarioRepository.deleteById(id);
    }

    /**
     * Ativa um usuário específico.
     */
    @Transactional
    public void ativar(Long id) {
        Usuario usuario = usuarioRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException(
                    "Usuário não encontrado com ID: " + id
                ));
        usuario.setAtivo(true);
        usuarioRepository.save(usuario);
    }

    /**
     * Desativa um usuário específico (soft delete).
     */
    @Transactional
    public void desativar(Long id) {
        Usuario usuario = usuarioRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException(
                    "Usuário não encontrado com ID: " + id
                ));
        usuario.setAtivo(false);
        usuarioRepository.save(usuario);
    }
}