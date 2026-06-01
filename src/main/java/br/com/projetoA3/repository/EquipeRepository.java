package br.com.projetoA3.repository;

import br.com.projetoA3.model.Equipe;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Repository responsável pelas operações de persistência da entidade {@link Equipe}.
 * 
 * <p>Utiliza {@code JOIN FETCH} nas queries customizadas para resolver o problema de
 * {@code LazyInitializationException} que ocorria quando o Thymeleaf tentava acessar
 * a coleção {@code membros} após o fechamento da sessão do Hibernate.</p>
 * 
 * <p><strong>Importante:</strong> O uso de {@code DISTINCT} com {@code JOIN FETCH}
 * em coleções é obrigatório para evitar resultados duplicados quando uma equipe
 * possui múltiplos membros.</p>
 * 
 * @see Equipe
 */
@Repository
public interface EquipeRepository extends JpaRepository<Equipe, Long> {

    /**
     * Busca todas as equipes com suas coleções de membros e líder carregadas eagerly.
     * 
     * <p>Resolve o erro {@code LazyInitializationException} ao acessar
     * {@code equipe.membros} nas views Thymeleaf.</p>
     * 
     * <p>Uso típico:</p>
     * <pre>
     * List&lt;Equipe&gt; equipes = equipeRepository.findAllComMembros();
     * // Agora é seguro acessar equipe.getMembros().size() na view
     * </pre>
     * 
     * @return Lista de equipes com membros e líder inicializados
     */
    @Query("SELECT DISTINCT e FROM Equipe e " +
           "LEFT JOIN FETCH e.membros " +
           "LEFT JOIN FETCH e.lider " +
           "ORDER BY e.nome ASC")
    List<Equipe> findAllComMembros();

    /**
     * Busca todas as equipes ativas (mantido para compatibilidade com código legado).
     * Alias para {@link #findAllComMembros()} já que o modelo não possui campo 'ativo'.
     * 
     * @return Lista de equipes com membros e líder inicializados
     */
    @Query("SELECT DISTINCT e FROM Equipe e " +
           "LEFT JOIN FETCH e.membros " +
           "LEFT JOIN FETCH e.lider " +
           "ORDER BY e.nome ASC")
    List<Equipe> findAllAtivosComMembros();

    /**
     * Busca uma equipe específica por ID com suas coleções carregadas eagerly.
     * 
     * <p>Útil para telas de detalhe/edição onde é necessário exibir os membros
     * sem provocar {@code LazyInitializationException}.</p>
     * 
     * @param id Identificador único da equipe
     * @return Optional contendo a equipe com membros e líder, ou vazio se não encontrada
     */
    @Query("SELECT DISTINCT e FROM Equipe e " +
           "LEFT JOIN FETCH e.membros " +
           "LEFT JOIN FETCH e.lider " +
           "WHERE e.id = :id")
    Optional<Equipe> findByIdComMembros(@Param("id") Long id);

    /**
     * Busca equipe por nome exato.
     * 
     * @param nome Nome da equipe
     * @return Optional contendo a equipe, ou vazio se não encontrada
     */
    Optional<Equipe> findByNome(String nome);

    /**
     * Busca equipe por nome ignorando maiúsculas/minúsculas.
     * 
     * <p>Útil para validação de unicidade no cadastro de equipes.</p>
     * 
     * @param nome Nome da equipe (case-insensitive)
     * @return Optional contendo a equipe, ou vazio se não encontrada
     */
    Optional<Equipe> findByNomeIgnoreCase(String nome);

    /**
     * Verifica se existe uma equipe com o nome informado (case-insensitive).
     * 
     * <p>Método otimizado que não carrega a entidade completa, ideal para
     * validações de unicidade.</p>
     * 
     * @param nome Nome a ser verificado
     * @return true se existir equipe com o nome, false caso contrário
     */
    boolean existsByNomeIgnoreCase(String nome);

    /**
     * Verifica se existe uma equipe com o nome informado, excluindo um ID específico.
     * 
     * <p>Usado em operações de UPDATE para validar unicidade sem considerar
     * a própria entidade sendo atualizada.</p>
     * 
     * @param nome Nome a ser verificado
     * @param id   ID a ser excluído da verificação
     * @return true se existir outra equipe com o mesmo nome
     */
    @Query("SELECT CASE WHEN COUNT(e) > 0 THEN true ELSE false END " +
           "FROM Equipe e " +
           "WHERE LOWER(e.nome) = LOWER(:nome) AND e.id <> :id")
    boolean existsByNomeIgnoreCaseAndIdNot(@Param("nome") String nome, @Param("id") Long id);

    /**
     * Conta o total de equipes cadastradas.
     * 
     * <p>Alias semântico para {@link #count()} do JpaRepository.</p>
     * 
     * @return Total de equipes
     */
    long countEquipes();
}