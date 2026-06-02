package br.com.projetoA3.repository;

import br.com.projetoA3.enums.StatusTarefa;
import br.com.projetoA3.model.Tarefa;
import br.com.projetoA3.model.Usuario;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Collection;
import java.util.List;

public interface TarefaRepository extends JpaRepository<Tarefa, Long> {

    // ==========================================
    // MÉTODOS EXISTENTES (PRESERVADOS)
    // ==========================================
    
    List<Tarefa> findByProjetoId(Long projetoId);
    List<Tarefa> findByResponsavel(Usuario responsavel);
    List<Tarefa> findByStatus(StatusTarefa status);
    List<Tarefa> findByProjetoIdAndStatus(Long projetoId, StatusTarefa status);
    List<Tarefa> findByResponsavelId(Long responsavelId);

    @Query("SELECT t FROM Tarefa t WHERE (t.prioridade = 'ALTA' OR t.prioridade = 'CRITICA') AND t.status != 'CONCLUIDA' AND t.status != 'CANCELADA'")
    List<Tarefa> findAltaPrioridadeNaoConcluidas();

    @Query("SELECT t FROM Tarefa t WHERE t.dataVencimento < CURRENT_DATE AND t.status != 'CONCLUIDA' AND t.status != 'CANCELADA'")
    List<Tarefa> findAtrasadas();

    @Query("SELECT COUNT(t) FROM Tarefa t WHERE t.projeto.id = :projetoId AND t.status = :status")
    long countByProjetoIdAndStatus(@Param("projetoId") Long projetoId, @Param("status") StatusTarefa status);

    List<Tarefa> findByProjetoIdOrderByIdAsc(Long projetoId);

    // ==========================================
    // ✅ NOVOS MÉTODOS ANALÍTICOS (DASHBOARD)
    // ==========================================

    /**
     * Conta o total de tarefas ignorando as canceladas.
     */
    long countByStatusNot(StatusTarefa status);

    /**
     * Conta tarefas atrasadas de forma otimizada.
     */
    @Query("SELECT COUNT(t) FROM Tarefa t WHERE t.dataVencimento < CURRENT_DATE AND t.status NOT IN ('CONCLUIDA', 'CANCELADA')")
    long countAtrasadasOtimizado();

    /**
     * Agrupa tarefas por status para o gráfico de Rosca.
     * Retorna uma lista de arrays: [Status, Quantidade]
     */
    @Query("SELECT t.status, COUNT(t) FROM Tarefa t GROUP BY t.status")
    List<Object[]> countTasksByStatusGrouped();

    /**
     * Busca os 5 prazos mais próximos que não estão concluídos.
     */
    List<Tarefa> findTop5ByStatusNotInOrderByDataVencimentoAsc(Collection<StatusTarefa> statuses);

    /**
     * MÉTRICAS DO COLABORADOR: Conta tarefas pendentes de um usuário específico.
     */
    long countByResponsavelIdAndStatusIn(Long responsavelId, Collection<StatusTarefa> statuses);

    /**
     * MÉTRICAS DO COLABORADOR: Top 5 prazos próximos de um usuário específico.
     */
    List<Tarefa> findTop5ByResponsavelIdAndStatusInOrderByDataVencimentoAsc(Long responsavelId, Collection<StatusTarefa> statuses);
}