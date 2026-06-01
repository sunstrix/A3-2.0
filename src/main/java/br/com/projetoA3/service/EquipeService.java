package br.com.projetoA3.service;

import br.com.projetoA3.exception.RegraDeNegocioException;
import br.com.projetoA3.model.Equipe;
import br.com.projetoA3.repository.EquipeRepository;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

/**
 * Service responsável pela lógica de negócio da entidade {@link Equipe}.
 * 
 * Refatorado para injeção via construtor e padronização de exceções de negócio.
 */
@Service
@Transactional(readOnly = true)
public class EquipeService {

    private final EquipeRepository equipeRepository;

    /**
     * Injeção via construtor: Padrão recomendado para facilitar testes e garantir imutabilidade.
     */
    public EquipeService(EquipeRepository equipeRepository) {
        this.equipeRepository = equipeRepository;
    }

    // ==========================================
    // CONSULTAS (READ-ONLY)
    // ==========================================

    /**
     * Busca todas as equipes com coleções carregadas para evitar LazyInitializationException.
     */
    public List<Equipe> findAll() {
        return equipeRepository.findAllComMembros();
    }

    /**
     * Busca uma equipe específica por ID com carregamento Eager dos membros.
     */
    public Optional<Equipe> findById(Long id) {
        return equipeRepository.findByIdComMembros(id);
    }

    public List<Equipe> findAllAtivosComMembros() {
        return equipeRepository.findAllAtivosComMembros();
    }

    public Optional<Equipe> findByNome(String nome) {
        return equipeRepository.findByNomeIgnoreCase(nome);
    }

    public boolean existsByNome(String nome) {
        return equipeRepository.existsByNomeIgnoreCase(nome);
    }

    public boolean existsByNomeAndIdNot(String nome, Long id) {
        return equipeRepository.existsByNomeIgnoreCaseAndIdNot(nome, id);
    }

    /**
     * Retorna o total de equipes usando a query manual corrigida.
     */
    public long count() {
        return equipeRepository.countEquipes();
    }

    // ==========================================
    // OPERAÇÕES DE ESCRITA (TRANSACTIONAL)
    // ==========================================

    /**
     * Salva ou atualiza uma equipe com validação de nome único.
     */
    @Transactional
    public Equipe save(Equipe equipe) {
        validarNomeUnico(equipe);
        return equipeRepository.save(equipe);
    }

    /**
     * Exclui uma equipe verificando sua existência prévia.
     */
    @Transactional
    public void deleteById(Long id) {
        if (!equipeRepository.existsById(id)) {
            throw new EntityNotFoundException("Equipe não encontrada com ID: " + id);
        }
        equipeRepository.deleteById(id);
    }

    // ==========================================
    // MÉTODOS PRIVADOS DE VALIDAÇÃO
    // ==========================================

    /**
     * Valida se o nome da equipe é único, lançando exceção de negócio em caso de duplicidade.
     */
    private void validarNomeUnico(Equipe equipe) {
        if (equipe.getNome() == null || equipe.getNome().trim().isEmpty()) {
            throw new RegraDeNegocioException("O nome da equipe é obrigatório.");
        }

        boolean nomeExiste;
        if (equipe.getId() != null) {
            nomeExiste = existsByNomeAndIdNot(equipe.getNome(), equipe.getId());
        } else {
            nomeExiste = existsByNome(equipe.getNome());
        }

        if (nomeExiste) {
            throw new RegraDeNegocioException("Já existe uma equipe cadastrada com o nome: " + equipe.getNome());
        }
    }
}