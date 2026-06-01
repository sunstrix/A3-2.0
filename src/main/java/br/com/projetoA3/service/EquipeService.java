package br.com.projetoA3.service;

import br.com.projetoA3.model.Equipe;
import br.com.projetoA3.model.Usuario;
import br.com.projetoA3.repository.EquipeRepository;
import br.com.projetoA3.repository.UsuarioRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
@Transactional
public class EquipeService {
    
    @Autowired
    private EquipeRepository equipeRepository;
    
    @Autowired
    private UsuarioRepository usuarioRepository;
    
    /**
     * Lista todas as equipes ativas com membros carregados via JOIN FETCH
     * para evitar LazyInitializationException nos templates.
     */
    @Transactional(readOnly = true)
    public List<Equipe> listarTodasComMembros() {
        return equipeRepository.findAllAtivosComMembros();
    }
    
    /**
     * Busca uma equipe por ID com membros carregados.
     */
    @Transactional(readOnly = true)
    public Optional<Equipe> buscarPorIdComMembros(Long id) {
        return equipeRepository.findByIdComMembros(id);
    }
    
    /**
     * Salva ou atualiza uma equipe.
     */
    public Equipe salvar(Equipe equipe) {
        // Garante que a equipe esteja ativa por padrão
        if (equipe.getAtivo() == null) {
            equipe.setAtivo(true);
        }
        return equipeRepository.save(equipe);
    }
    
    /**
     * Adiciona um membro a uma equipe existente.
     */
    public Equipe adicionarMembro(Long equipeId, Long usuarioId) {
        Equipe equipe = equipeRepository.findById(equipeId)
                .orElseThrow(() -> new RuntimeException("Equipe não encontrada"));
        
        Usuario usuario = usuarioRepository.findById(usuarioId)
                .orElseThrow(() -> new RuntimeException("Usuário não encontrado"));
        
        // Evita duplicação de membros
        if (!equipe.getMembros().contains(usuario)) {
            equipe.getMembros().add(usuario);
            equipeRepository.save(equipe);
        }
        
        return equipe;
    }
    
    /**
     * Remove um membro de uma equipe.
     */
    public Equipe removerMembro(Long equipeId, Long usuarioId) {
        Equipe equipe = equipeRepository.findById(equipeId)
                .orElseThrow(() -> new RuntimeException("Equipe não encontrada"));
        
        Usuario usuario = usuarioRepository.findById(usuarioId)
                .orElseThrow(() -> new RuntimeException("Usuário não encontrado"));
        
        equipe.getMembros().remove(usuario);
        return equipeRepository.save(equipe);
    }
    
    /**
     * Desativa uma equipe (soft delete).
     */
    public void desativar(Long id) {
        Equipe equipe = equipeRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Equipe não encontrada"));
        equipe.setAtivo(false);
        equipeRepository.save(equipe);
    }
    
    /**
     * Valida se o nome da equipe já existe (case-insensitive).
     */
    @Transactional(readOnly = true)
    public boolean nomeJaExiste(String nome, Long idExcluido) {
        Optional<Equipe> existente = equipeRepository.findByNomeIgnoreCase(nome);
        return existente.isPresent() && !existente.get().getId().equals(idExcluido);
    }
}