package br.com.projetoA3.repository;

import br.com.projetoA3.model.Tarefa;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface TarefaRepository extends JpaRepository<Tarefa, Long> {

    // ✅ Buscar todas as tarefas de um projeto específico (usado no Kanban)
    List<Tarefa> findByProjetoId(Long projetoId);

    // ✅ Buscar tarefas filtradas por status dentro de um projeto
    List<Tarefa> findByProjetoIdAndStatus(Long projetoId, Tarefa.StatusTarefa status);
}