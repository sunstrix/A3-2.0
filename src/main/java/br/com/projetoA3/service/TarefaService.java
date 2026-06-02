package br.com.projetoA3.service;

import br.com.projetoA3.dto.TarefaDTO;
import br.com.projetoA3.enums.StatusTarefa;
import br.com.projetoA3.exception.AcessoNegadoException;
import br.com.projetoA3.exception.RegraDeNegocioException;
import br.com.projetoA3.mapper.TarefaMapper;
import br.com.projetoA3.model.Projeto;
import br.com.projetoA3.model.Tarefa;
import br.com.projetoA3.repository.ProjetoRepository;
import br.com.projetoA3.repository.TarefaRepository;
import br.com.projetoA3.repository.UsuarioRepository;
import br.com.projetoA3.viewmodel.KanbanViewModel;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

/**
 * Service responsável pela lógica de negócio das Tarefas.
 * Atualizado para suportar extração de Entidades para relatórios sem quebrar o Kanban.
 */
@Service
@Transactional(readOnly = true)
public class TarefaService {

    private final TarefaRepository tarefaRepository;
    private final ProjetoRepository projetoRepository;
    private final UsuarioRepository usuarioRepository;
    private final TarefaMapper tarefaMapper;

    public TarefaService(TarefaRepository tarefaRepository,
                         ProjetoRepository projetoRepository,
                         UsuarioRepository usuarioRepository,
                         TarefaMapper tarefaMapper) {
        this.tarefaRepository = tarefaRepository;
        this.projetoRepository = projetoRepository;
        this.usuarioRepository = usuarioRepository;
        this.tarefaMapper = tarefaMapper;
    }

    // ==========================================
    // CONSULTAS
    // ==========================================

    /**
     * Retorna DTOs para listagem comum na UI.
     */
    public List<TarefaDTO> findAll() {
        return tarefaMapper.toDTOList(tarefaRepository.findAll());
    }

    /**
     * ✅ MÉTODO CRÍTICO PARA RELATÓRIOS:
     * Retorna Entidades brutas para processamento do Apache POI / OpenPDF.
     */
    public List<Tarefa> findAllEntities() {
        return tarefaRepository.findAll();
    }

    public Optional<Tarefa> findById(Long id) {
        return tarefaRepository.findById(id);
    }

    public Optional<TarefaDTO> findDTOById(Long id) {
        return tarefaRepository.findById(id).map(tarefaMapper::toDTO);
    }

    public List<TarefaDTO> findByProjetoId(Long projetoId) {
        return tarefaMapper.toDTOList(tarefaRepository.findByProjetoId(projetoId));
    }

    public List<TarefaDTO> findByResponsavelId(Long usuarioId) {
        return tarefaMapper.toDTOList(tarefaRepository.findByResponsavelId(usuarioId));
    }

    public List<TarefaDTO> findAtrasadas() {
        return tarefaMapper.toDTOList(tarefaRepository.findAtrasadas());
    }

    // ==========================================
    // VIEWMODEL (KANBAN)
    // ==========================================

    public KanbanViewModel buildKanbanViewModel(Long projetoId, String username) {
        Projeto projeto = projetoRepository.findById(projetoId)
                .orElseThrow(() -> new EntityNotFoundException("Projeto não encontrado: " + projetoId));

        List<Tarefa> todasTarefas = tarefaRepository.findByProjetoId(projetoId);

        List<TarefaDTO> aFazer = filtrarPorStatus(todasTarefas, StatusTarefa.A_FAZER);
        List<TarefaDTO> emAndamento = filtrarPorStatus(todasTarefas, StatusTarefa.EM_ANDAMENTO);
        List<TarefaDTO> concluidas = filtrarPorStatus(todasTarefas, StatusTarefa.CONCLUIDA);
        List<TarefaDTO> canceladas = filtrarPorStatus(todasTarefas, StatusTarefa.CANCELADA);

        boolean podeAdicionar = username != null;
        boolean podeEditar = podeEditarProjeto(projeto, username);

        return new KanbanViewModel(
                projetoId,
                projeto.getNome(),
                projeto.getDescricao(),
                aFazer,
                emAndamento,
                concluidas,
                canceladas,
                podeAdicionar,
                podeEditar
        );
    }

    // ==========================================
    // ESCRITA (PERSISTÊNCIA)
    // ==========================================

    @Transactional
    public TarefaDTO save(Tarefa tarefa, String username) {
        if (tarefa.getProjeto() == null || (tarefa.getProjeto().getId() == null)) {
            throw new RegraDeNegocioException("Uma tarefa deve estar associada a um projeto.");
        }

        if (tarefa.getStatus() == null) {
            tarefa.setStatus(StatusTarefa.A_FAZER);
        }

        Tarefa salva = tarefaRepository.save(tarefa);
        return tarefaMapper.toDTO(salva);
    }

    @Transactional
    public void moverTarefa(Long tarefaId, StatusTarefa novoStatus, String username) {
        Tarefa tarefa = tarefaRepository.findById(tarefaId)
                .orElseThrow(() -> new EntityNotFoundException("Tarefa não encontrada: " + tarefaId));

        if (!podeMoverTarefa(tarefa, username)) {
            throw new AcessoNegadoException("Você não tem permissão para mover esta tarefa.");
        }

        if (tarefa.getStatus() == StatusTarefa.CANCELADA && novoStatus != StatusTarefa.CANCELADA) {
            throw new RegraDeNegocioException("Tarefas canceladas não podem ser reativadas diretamente.");
        }

        tarefa.setStatus(novoStatus);
        tarefaRepository.save(tarefa);
    }

    @Transactional
    public TarefaDTO update(Long id, Tarefa dados) {
        Tarefa existente = tarefaRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Tarefa não encontrada"));
        
        existente.setTitulo(dados.getTitulo());
        existente.setDescricao(dados.getDescricao());
        existente.setStatus(dados.getStatus());
        existente.setPrioridade(dados.getPrioridade());
        existente.setDataVencimento(dados.getDataVencimento());
        existente.setResponsavel(dados.getResponsavel());
        
        return tarefaMapper.toDTO(tarefaRepository.save(existente));
    }

    @Transactional
    public void deleteById(Long id) {
        tarefaRepository.deleteById(id);
    }

    // ==========================================
    // LÓGICA DE PERMISSÃO (PRESERVADA)
    // ==========================================

    private boolean podeMoverTarefa(Tarefa tarefa, String username) {
        if (username == null) return false;
        if ("admin".equals(username)) return true;

        if (tarefa.getResponsavel() != null && tarefa.getResponsavel().getLogin().equals(username)) {
            return true;
        }

        return podeEditarProjeto(tarefa.getProjeto(), username);
    }

    private boolean podeEditarProjeto(Projeto projeto, String username) {
        if (username == null) return false;
        if ("admin".equals(username)) return true;
        
        if (projeto.getEquipe() != null && projeto.getEquipe().getLider() != null) {
            return projeto.getEquipe().getLider().getLogin().equals(username);
        }
        
        return false;
    }

    private List<TarefaDTO> filtrarPorStatus(List<Tarefa> tarefas, StatusTarefa status) {
        return tarefas.stream()
                .filter(t -> t.getStatus() == status)
                .map(tarefaMapper::toDTO)
                .toList();
    }
}