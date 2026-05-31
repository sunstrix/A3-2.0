package br.com.projetoA3.repository;

import br.com.projetoA3.model.Tarefa;
import br.com.projetoA3.model.Usuario;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface TarefaRepository extends JpaRepository<Tarefa, Long> {

    // ✅ Métodos existentes (NÃO REMOVER)
    List<Tarefa> findByProjetoId(Long projetoId);
    List<Tarefa> findByResponsavel(Usuario responsavel);
    List<Tarefa> findByStatus(Tarefa.StatusTarefa status);

    // ✅ Novos métodos para o Kanban
    List<Tarefa> findByProjetoIdAndStatus(Long projetoId, Tarefa.StatusTarefa status);
    List<Tarefa> findByResponsavelIdAndStatus(Long responsavelId, Tarefa.StatusTarefa status);
    
    // Busca tarefas com prioridade ALTA e status não concluído (para destaque visual)
    @Query("SELECT t FROM Tarefa t WHERE t.prioridade = 'ALTA' AND t.status != 'CONCLUIDA'")
    List<Tarefa> findAltaPrioridadeNaoConcluidas();
    
    // Busca tarefas com data de vencimento passada e status não concluído
    @Query("SELECT t FROM Tarefa t WHERE t.dataVencimento < CURRENT_DATE AND t.status != 'CONCLUIDA'")
    List<Tarefa> findAtrasadas();
    
    // Contagem de tarefas por projeto e status (para contadores do Kanban)
    @Query("SELECT COUNT(t) FROM Tarefa t WHERE t.projeto.id = :projetoId AND t.status = :status")
    long countByProjetoIdAndStatus(@Param("projetoId") Long projetoId, @Param("status") Tarefa.StatusTarefa status);
}