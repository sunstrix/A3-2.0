package br.com.projetoA3.service;

import br.com.projetoA3.model.Tarefa;
import br.com.projetoA3.model.Tarefa.StatusTarefa;
import br.com.projetoA3.repository.TarefaRepository;
import br.com.projetoA3.repository.UsuarioRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
public class TarefaService {

    private final TarefaRepository tarefaRepository;

    public TarefaService(TarefaRepository tarefaRepository) {
        this.tarefaRepository = tarefaRepository;
    }

    public List<Tarefa> findAll() {
        return tarefaRepository.findAll();
    }

    public Optional<Tarefa> findById(Long id) {
        return tarefaRepository.findById(id);
    }

    public Tarefa save(Tarefa tarefa) {
        return tarefaRepository.save(tarefa);
    }

    public void deleteById(Long id) {
        tarefaRepository.deleteById(id);
    }

    // ✅ Métodos específicos para o Kanban
    public List<Tarefa> findByProjetoId(Long projetoId) {
        return tarefaRepository.findByProjetoId(projetoId);
    }

    public List<Tarefa> findByProjetoIdAndStatus(Long projetoId, StatusTarefa status) {
        return tarefaRepository.findByProjetoIdAndStatus(projetoId, status);
    }

    public List<Tarefa> findByResponsavelId(Long usuarioId) {
        return tarefaRepository.findByResponsavelId(usuarioId);
    }
    
    // Método auxiliar para contar tarefas no Kanban
    public long countByProjetoIdAndStatus(Long projetoId, StatusTarefa status) {
        return tarefaRepository.countByProjetoIdAndStatus(projetoId, status);
    }
}