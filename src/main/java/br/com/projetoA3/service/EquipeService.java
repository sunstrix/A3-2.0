package br.com.projetoA3.service;

import br.com.projetoA3.model.Equipe;
import br.com.projetoA3.repository.EquipeRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

/**
 * Service responsável pela lógica de negócio da entidade {@link Equipe}.
 * 
 * <p>Esta classe implementa todas as operações CRUD (Create, Read, Update, Delete)
 * e métodos auxiliares para gerenciamento de equipes, incluindo validações de
 * unicidade de nome e consultas otimizadas com JOIN FETCH.</p>
 * 
 * <p><strong>Nota:</strong> Todos os métodos de escrita (save, delete) são
 * anotados com {@code @Transactional} para garantir a consistência dos dados.</p>
 */
@Service
@Transactional(readOnly = true)
public class EquipeService {

    @Autowired
    private EquipeRepository equipeRepository;

    /**
     * Busca todas as equipes cadastradas.
     * 
     * <p>Utiliza {@link EquipeRepository#findAllComMembros()} para carregar
     * eager as coleções de membros e evitar {@code LazyInitializationException}
     * nas views Thymeleaf.</p>
     * 
     * @return Lista de todas as equipes com membros e líder inicializados
     */
    public List<Equipe> findAll() {
        return equipeRepository.findAllComMembros();
    }

    /**
     * Busca uma equipe específica por ID.
     * 
     * <p>Utiliza {@link EquipeRepository#findByIdComMembros(Long)} para carregar
     * eager as coleções de membros e evitar {@code LazyInitializationException}
     * nas views Thymeleaf.</p>
     * 
     * @param id Identificador único da equipe
     * @return Optional contendo a equipe encontrada, ou vazio se não existir
     */
    public Optional<Equipe> findById(Long id) {
        return equipeRepository.findByIdComMembros(id);
    }

    /**
     * Salva ou atualiza uma equipe.
     * 
     * <p>Antes de salvar, valida se o nome da equipe já existe no banco de dados
     * (exceto para a própria equipe em caso de atualização).</p>
     * 
     * @param equipe Equipe a ser salva ou atualizada
     * @return Equipe salva com ID atribuído (se for nova)
     * @throws RuntimeException se o nome da equipe já existir
     */
    @Transactional
    public Equipe save(Equipe equipe) {
        validarNomeUnico(equipe);
        return equipeRepository.save(equipe);
    }

    /**
     * Exclui uma equipe por ID.
     * 
     * @param id Identificador da equipe a ser excluída
     * @throws RuntimeException se a equipe não for encontrada
     */
    @Transactional
    public void deleteById(Long id) {
        if (!equipeRepository.existsById(id)) {
            throw new RuntimeException("Equipe não encontrada com ID: " + id);
        }
        equipeRepository.deleteById(id);
    }

    /**
     * Busca todas as equipes ativas com seus membros.
     * 
     * <p>Método mantido para compatibilidade com código legado.
     * Atualmente retorna todas as equipes (não há campo 'ativo' no modelo).</p>
     * 
     * @return Lista de equipes com membros e líder inicializados
     */
    public List<Equipe> findAllAtivosComMembros() {
        return equipeRepository.findAllAtivosComMembros();
    }

    /**
     * Busca uma equipe por nome (case-insensitive).
     * 
     * @param nome Nome da equipe a ser buscado
     * @return Optional contendo a equipe encontrada, ou vazio se não existir
     */
    public Optional<Equipe> findByNome(String nome) {
        return equipeRepository.findByNomeIgnoreCase(nome);
    }

    /**
     * Verifica se existe uma equipe com o nome informado.
     * 
     * @param nome Nome a ser verificado
     * @return true se existir equipe com o nome, false caso contrário
     */
    public boolean existsByNome(String nome) {
        return equipeRepository.existsByNomeIgnoreCase(nome);
    }

    /**
     * Verifica se existe uma equipe com o nome informado, excluindo um ID específico.
     * 
     * <p>Usado em operações de UPDATE para validar unicidade sem considerar
     * a própria equipe sendo atualizada.</p>
     * 
     * @param nome Nome a ser verificado
     * @param id   ID a ser excluído da verificação
     * @return true se existir outra equipe com o mesmo nome
     */
    public boolean existsByNomeAndIdNot(String nome, Long id) {
        return equipeRepository.existsByNomeIgnoreCaseAndIdNot(nome, id);
    }

    /**
     * Conta o total de equipes cadastradas.
     * 
     * @return Total de equipes
     */
    public long count() {
        return equipeRepository.countEquipes();
    }

    // ==========================================
    // MÉTODOS PRIVADOS DE VALIDAÇÃO
    // ==========================================

    /**
     * Valida se o nome da equipe é único no banco de dados.
     * 
     * @param equipe Equipe a ser validada
     * @throws RuntimeException se o nome já existir
     */
    private void validarNomeUnico(Equipe equipe) {
        if (equipe.getNome() == null || equipe.getNome().trim().isEmpty()) {
            throw new RuntimeException("Nome da equipe é obrigatório");
        }

        boolean nomeExiste;
        if (equipe.getId() != null) {
            // Atualização: verifica se o nome existe para outra equipe
            nomeExiste = existsByNomeAndIdNot(equipe.getNome(), equipe.getId());
        } else {
            // Criação: verifica se o nome já existe
            nomeExiste = existsByNome(equipe.getNome());
        }

        if (nomeExiste) {
            throw new RuntimeException("Já existe uma equipe com o nome: " + equipe.getNome());
        }
    }
}