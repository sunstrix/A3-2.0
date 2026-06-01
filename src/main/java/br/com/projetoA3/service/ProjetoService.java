package br.com.projetoA3.service;

import br.com.projetoA3.model.Projeto;
import br.com.projetoA3.repository.ProjetoRepository;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

/**
 * Service para gestão de Projetos.
 * ✅ Refatoração Sênior: Integridade transacional e segurança de acesso.
 */
@Service
public class ProjetoService {

    private final ProjetoRepository projetoRepository;

    public ProjetoService(ProjetoRepository projetoRepository) {
        this.projetoRepository = projetoRepository;
    }

    /**
     * Lista todos os projetos utilizando a query otimizada com EntityGraph.
     */
    @Transactional(readOnly = true)
    public List<Projeto> findAll() {
        return projetoRepository.findAll();
    }

    /**
     * Busca um projeto por ID com tratamento de exceção.
     */
    @Transactional(readOnly = true)
    public Projeto findById(Long id) {
        return projetoRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Projeto não encontrado com ID: " + id));
    }

    /**
     * Salva um novo projeto.
     * ✅ Garantia de rollback em caso de erro.
     */
    @Transactional(rollbackFor = Exception.class)
    public Projeto save(Projeto projeto) {
        if (projeto.getNome() == null || projeto.getNome().trim().isEmpty()) {
            throw new IllegalArgumentException("O nome do projeto é obrigatório.");
        }
        return projetoRepository.save(projeto);
    }

    /**
     * Atualiza um projeto existente.
     * ✅ Verificação de existência obrigatória.
     */
    @Transactional(rollbackFor = Exception.class)
    public Projeto update(Long id, Projeto projetoAtualizado) {
        Projeto projetoExistente = findById(id);
        
        projetoExistente.setNome(projetoAtualizado.getNome());
        projetoExistente.setDescricao(projetoAtualizado.getDescricao());
        projetoExistente.setEquipe(projetoAtualizado.getEquipe());
        
        // Mantemos a integridade de datas e outras propriedades originais
        return projetoRepository.save(projetoExistente);
    }

    /**
     * Exclui um projeto.
     * ✅ SEGURANÇA: Apenas administradores podem deletar projetos.
     */
    @PreAuthorize("hasRole('ADMINISTRADOR')")
    @Transactional(rollbackFor = Exception.class)
    public void deleteById(Long id) {
        if (!projetoRepository.existsById(id)) {
            throw new EntityNotFoundException("Impossível excluir: Projeto inexistente.");
        }
        projetoRepository.deleteById(id);
    }

    /**
     * Verifica se existe projeto com o nome informado.
     */
    @Transactional(readOnly = true)
    public boolean existsByNome(String nome) {
        return projetoRepository.existsByNome(nome);
    }
}