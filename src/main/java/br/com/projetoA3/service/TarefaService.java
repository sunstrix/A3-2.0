package br.com.projetoA3.service;

import br.com.projetoA3.model.Tarefa;
import br.com.projetoA3.repository.TarefaRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class TarefaService {

    private final TarefaRepository tarefaRepository;

    public TarefaService(TarefaRepository tarefaRepository) {
        this.tarefaRepository = tarefaRepository;
    }

    // ✅ Listar todas as tarefas
    public List<Tarefa> findAll() {
        return tarefaRepository.findAll();
    }

    // ✅ Buscar tarefa por ID
    public Optional<Tarefa> findById(Long id) {
        return tarefaRepository.findById(id);
    }

    // ✅ Buscar todas as tarefas de um projeto específico (essencial para o Kanban)
    public List<Tarefa> findByProjetoId(Long projetoId) {
        return tarefaRepository.findByProjetoId(projetoId);
    }

    // ✅ Buscar tarefas filtradas por status dentro de um projeto
    public List<Tarefa> findByProjetoIdAndStatus(Long projetoId, Tarefa.StatusTarefa status) {
        return tarefaRepository.findByProjetoIdAndStatus(projetoId, status);
    }

    // ✅ Salvar ou atualizar tarefa
    public Tarefa save(Tarefa tarefa) {
        return tarefaRepository.save(tarefa);
    }

    // ✅ Deletar tarefa por ID
    public void deleteById(Long id) {
        tarefaRepository.deleteById(id);
    }
}