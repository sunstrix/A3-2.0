package br.com.projetoA3.repository;

import br.com.projetoA3.enums.StatusTarefa;
import br.com.projetoA3.model.Tarefa;
import br.com.projetoA3.model.Usuario;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface TarefaRepository extends JpaRepository<Tarefa, Long> {

    // ==========================================
    // MÉTODOS EXISTENTES (MANTER)
    // ==========================================
    
    /**
     * Busca todas as tarefas de um projeto específico
     */
    List<Tarefa> findByProjetoId(Long projetoId);

    /**
     * Busca todas as tarefas atribuídas a um responsável específico
     */
    List<Tarefa> findByResponsavel(Usuario responsavel);

    /**
     * Busca todas as tarefas com um status específico
     */
    List<Tarefa> findByStatus(StatusTarefa status);

    // ==========================================
    // ✅ NOVOS MÉTODOS PARA O KANBAN E REFACTORAÇÃO
    // ==========================================

    /**
     * Busca tarefas de um projeto com um status específico
     * Útil para popular colunas do Kanban
     */
    List<Tarefa> findByProjetoIdAndStatus(Long projetoId, StatusTarefa status);

    /**
     * ✅ NOVO: Busca tarefas atribuídas a um responsável específico (por ID)
     * Usado pelo TarefaService para listar "Minhas Tarefas"
     */
    List<Tarefa> findByResponsavelId(Long responsavelId);

    /**
     * Busca tarefas com prioridade ALTA ou CRÍTICA que não estão concluídas
     * Útil para dashboard de tarefas urgentes
     */
    @Query("SELECT t FROM Tarefa t WHERE (t.prioridade = 'ALTA' OR t.prioridade = 'CRITICA') AND t.status != 'CONCLUIDA' AND t.status != 'CANCELADA'")
    List<Tarefa> findAltaPrioridadeNaoConcluidas();

    /**
     * Busca tarefas atrasadas (data de vencimento passada e não concluídas/canceladas)
     */
    @Query("SELECT t FROM Tarefa t WHERE t.dataVencimento < CURRENT_DATE AND t.status != 'CONCLUIDA' AND t.status != 'CANCELADA'")
    List<Tarefa> findAtrasadas();

    /**
     * Conta tarefas de um projeto com um status específico
     * Útil para contadores no Kanban
     */
    @Query("SELECT COUNT(t) FROM Tarefa t WHERE t.projeto.id = :projetoId AND t.status = :status")
    long countByProjetoIdAndStatus(@Param("projetoId") Long projetoId, @Param("status") StatusTarefa status);

    /**
     * Busca todas as tarefas de um projeto ordenadas por ordem de criação
     * Útil para garantir consistência na exibição do Kanban
     */
    List<Tarefa> findByProjetoIdOrderByIdAsc(Long projetoId);
}