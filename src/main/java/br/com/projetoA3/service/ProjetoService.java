package br.com.projetoA3.service;

import br.com.projetoA3.model.Projeto;
import br.com.projetoA3.repository.ProjetoRepository;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

/**
 * Service responsável pela lógica de negócio dos Projetos.
 * Atualizado para garantir transacionalidade em consultas de relatórios.
 */
@Service
@Transactional(readOnly = true)
public class ProjetoService {

    private final ProjetoRepository projetoRepository;

    public ProjetoService(ProjetoRepository projetoRepository) {
        this.projetoRepository = projetoRepository;
    }

    // ==========================================
    // CONSULTAS (Otimizadas para UI e Relatórios)
    // ==========================================

    /**
     * Busca todos os projetos. 
     * @Transactional garante que relacionamentos Lazy (como Equipe) sejam carregados nos relatórios.
     */
    public List<Projeto> findAll() {
        return projetoRepository.findAll();
    }

    public Optional<Projeto> findById(Long id) {
        return projetoRepository.findById(id);
    }

    public List<Projeto> findByStatus(Projeto.StatusProjeto status) {
        return projetoRepository.findByStatus(status);
    }

    public List<Projeto> buscarPorNome(String nome) {
        return projetoRepository.findByNomeContainingIgnoreCase(nome);
    }

    // ==========================================
    // OPERAÇÕES DE ESCRITA
    // ==========================================

    @Transactional
    public Projeto save(Projeto projeto) {
        // Lógica de inicialização de status se necessário
        if (projeto.getStatus() == null) {
            projeto.setStatus(Projeto.StatusProjeto.PLANEJAMENTO);
        }
        return projetoRepository.save(projeto);
    }

    @Transactional
    public Projeto update(Long id, Projeto dadosAtualizados) {
        Projeto existente = projetoRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Projeto não encontrado com ID: " + id));

        // Preserva a lógica original de atualização de campos
        existente.setNome(dadosAtualizados.getNome());
        existente.setDescricao(dadosAtualizados.getDescricao());
        existente.setDataInicio(dadosAtualizados.getDataInicio());
        existente.setDataTerminoPrevista(dadosAtualizados.getDataTerminoPrevista());
        existente.setStatus(dadosAtualizados.getStatus());
        existente.setEquipe(dadosAtualizados.getEquipe());

        return projetoRepository.save(existente);
    }

    @Transactional
    public void deleteById(Long id) {
        if (!projetoRepository.existsById(id)) {
            throw new EntityNotFoundException("Não é possível excluir: Projeto não encontrado.");
        }
        projetoRepository.deleteById(id);
    }
}