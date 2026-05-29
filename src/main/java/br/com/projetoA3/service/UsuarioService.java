package br.com.projetoA3.service;

import br.com.projetoA3.model.Usuario;
import br.com.projetoA3.repository.UsuarioRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class UsuarioService {

    private final UsuarioRepository usuarioRepository;

    // Injeção de dependência via construtor
    public UsuarioService(UsuarioRepository usuarioRepository) {
        this.usuarioRepository = usuarioRepository;
    }

    // ✅ Buscar todos os usuários
    public List<Usuario> findAll() {
        return usuarioRepository.findAll();
    }

    // ✅ Buscar usuário por ID
    public Optional<Usuario> findById(Long id) {
        return usuarioRepository.findById(id);
    }

    // ✅ Buscar usuário pelo campo "login" (usado na autenticação)
    public Optional<Usuario> findByLogin(String login) {
        return usuarioRepository.findByLogin(login);
    }

    // ✅ Salvar ou atualizar usuário
    public Usuario save(Usuario usuario) {
        return usuarioRepository.save(usuario);
    }

    // ✅ Deletar usuário por ID
    public void deleteById(Long id) {
        usuarioRepository.deleteById(id);
    }
}