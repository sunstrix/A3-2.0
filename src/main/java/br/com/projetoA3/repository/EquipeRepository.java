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
 */
@Repository
public interface EquipeRepository extends JpaRepository<Equipe, Long> {

    /**
     * Busca todas as equipes com suas coleções de membros e líder carregadas eagerly.
     * Resolve o erro LazyInitializationException nas views Thymeleaf.
     */
    @Query("SELECT DISTINCT e FROM Equipe e " +
           "LEFT JOIN FETCH e.membros " +
           "LEFT JOIN FETCH e.lider " +
           "ORDER BY e.nome ASC")
    List<Equipe> findAllComMembros();

    /**
     * Busca todas as equipes ativas (mantido para compatibilidade com código legado).
     */
    @Query("SELECT DISTINCT e FROM Equipe e " +
           "LEFT JOIN FETCH e.membros " +
           "LEFT JOIN FETCH e.lider " +
           "ORDER BY e.nome ASC")
    List<Equipe> findAllAtivosComMembros();

    /**
     * Busca uma equipe específica por ID com suas coleções carregadas eagerly.
     */
    @Query("SELECT DISTINCT e FROM Equipe e " +
           "LEFT JOIN FETCH e.membros " +
           "LEFT JOIN FETCH e.lider " +
           "WHERE e.id = :id")
    Optional<Equipe> findByIdComMembros(@Param("id") Long id);

    /**
     * Busca equipe por nome exato.
     */
    Optional<Equipe> findByNome(String nome);

    /**
     * Busca equipe por nome ignorando maiúsculas/minúsculas.
     */
    Optional<Equipe> findByNomeIgnoreCase(String nome);

    /**
     * Verifica se existe uma equipe com o nome informado (case-insensitive).
     */
    boolean existsByNomeIgnoreCase(String nome);

    /**
     * Verifica se existe uma equipe com o nome informado, excluindo um ID específico.
     */
    @Query("SELECT CASE WHEN COUNT(e) > 0 THEN true ELSE false END " +
           "FROM Equipe e " +
           "WHERE LOWER(e.nome) = LOWER(:nome) AND e.id <> :id")
    boolean existsByNomeIgnoreCaseAndIdNot(@Param("nome") String nome, @Param("id") Long id);

    /**
     * Conta o total de equipes cadastradas.
     * 
     * ✅ CORREÇÃO APLICADA: Adicionada anotação @Query para definir manualmente a contagem.
     * Isso impede que o Spring Data JPA tente derivar o nome do método incorretamente.
     * 
     * @return Total de equipes
     */
    @Query("SELECT count(e) FROM Equipe e")
    long countEquipes();
}