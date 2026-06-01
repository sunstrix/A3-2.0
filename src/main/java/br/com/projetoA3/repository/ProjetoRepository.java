package br.com.projetoA3.repository;

import br.com.projetoA3.model.Projeto;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Repositório para a entidade Projeto.
 * ✅ Refatoração Sênior: Otimização de queries para evitar N+1.
 */
@Repository
public interface ProjetoRepository extends JpaRepository<Projeto, Long> {

    /**
     * Busca todos os projetos carregando as tarefas e a equipe em uma única consulta.
     * ✅ Otimização: @EntityGraph evita múltiplas idas ao banco de dados.
     */
    @Override
    @EntityGraph(attributePaths = {"tarefas", "equipe"})
    List<Projeto> findAll();

    /**
     * Busca um projeto por ID carregando ansiosamente suas tarefas.
     */
    @EntityGraph(attributePaths = {"tarefas", "equipe"})
    Optional<Projeto> findById(Long id);

    /**
     * Exemplo de busca customizada com JOIN FETCH via JPQL.
     * Utilizado para relatórios ou listagens específicas.
     */
    @Query("SELECT DISTINCT p FROM Projeto p LEFT JOIN FETCH p.tarefas t LEFT JOIN FETCH p.equipe e")
    List<Projeto> findAllOptimized();

    /**
     * Verifica se existe projeto com o nome informado.
     */
    boolean existsByNome(String nome);
}