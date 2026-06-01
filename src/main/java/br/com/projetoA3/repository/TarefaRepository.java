package br.com.projetoA3.repository;

import br.com.projetoA3.enums.StatusTarefa;
import br.com.projetoA3.model.Tarefa;
import br.com.projetoA3.model.Usuario;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Repositório para a entidade Tarefa.
 * ✅ Refatoração Sênior: Fusão de funcionalidades Kanban com otimização EntityGraph (N+1 Fix).
 */
@Repository
public interface TarefaRepository extends JpaRepository<Tarefa, Long> {

    // ==========================================
    // MÉTODOS ORIGINAIS (PRESERVADOS E OTIMIZADOS)
    // ==========================================
    
    @EntityGraph(attributePaths = {"projeto", "responsavel"})
    List<Tarefa> findByProjetoId(Long projetoId);

    @EntityGraph(attributePaths = {"projeto", "responsavel"})
    List<Tarefa> findByResponsavel(Usuario responsavel);

    @EntityGraph(attributePaths = {"projeto", "responsavel"})
    List<Tarefa> findByStatus(StatusTarefa status);

    // ==========================================
    // ✅ MÉTODOS PARA O KANBAN E DASHBOARD
    // ==========================================

    /**
     * Busca tarefas de um projeto com um status específico.
     * Otimizado para popular colunas do Kanban sem múltiplas consultas.
     */
    @EntityGraph(attributePaths = {"projeto", "responsavel"})
    List<Tarefa> findByProjetoIdAndStatus(Long projetoId, StatusTarefa status);

    /**
     * Busca tarefas atribuídas a um responsável específico (por ID).
     */
    @EntityGraph(attributePaths = {"projeto", "responsavel"})
    List<Tarefa> findByResponsavelId(Long responsavelId);

    /**
     * Busca tarefas com prioridade ALTA ou CRÍTICA que não estão concluídas.
     */
    @EntityGraph(attributePaths = {"projeto", "responsavel"})
    @Query("SELECT t FROM Tarefa t WHERE (t.prioridade = 'ALTA' OR t.prioridade = 'CRITICA') " +
           "AND t.status != 'CONCLUIDA' AND t.status != 'CANCELADA'")
    List<Tarefa> findAltaPrioridadeNaoConcluidas();

    /**
     * Busca tarefas atrasadas (data de vencimento passada e não concluídas/canceladas).
     */
    @EntityGraph(attributePaths = {"projeto", "responsavel"})
    @Query("SELECT t FROM Tarefa t WHERE t.dataVencimento < CURRENT_DATE " +
           "AND t.status != 'CONCLUIDA' AND t.status != 'CANCELADA'")
    List<Tarefa> findAtrasadas();

    /**
     * Conta tarefas de um projeto com um status específico.
     */
    @Query("SELECT COUNT(t) FROM Tarefa t WHERE t.projeto.id = :projetoId AND t.status = :status")
    long countByProjetoIdAndStatus(@Param("projetoId") Long projetoId, @Param("status") StatusTarefa status);

    /**
     * Busca todas as tarefas de um projeto ordenadas por ordem de criação.
     * Otimizado para exibição consistente.
     */
    @EntityGraph(attributePaths = {"projeto", "responsavel"})
    List<Tarefa> findByProjetoIdOrderByIdAsc(Long projetoId);
}