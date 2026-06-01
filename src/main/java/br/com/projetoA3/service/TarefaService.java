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
 * Refatorado para compatibilidade com Java 21 Records e lógica robusta de Kanban.
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
    // CONSULTAS (READ-ONLY)
    // ==========================================

    public List<TarefaDTO> findAll() {
        return tarefaMapper.toDTOList(tarefaRepository.findAll());
    }

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

    public long countByProjetoIdAndStatus(Long projetoId, StatusTarefa status) {
        return tarefaRepository.countByProjetoIdAndStatus(projetoId, status);
    }

    // ==========================================
    // VIEWMODEL - Lógica de apresentação
    // ==========================================

    public KanbanViewModel buildKanbanViewModel(Long projetoId, String username) {
        Projeto projeto = projetoRepository.findById(projetoId)
                .orElseThrow(() -> new EntityNotFoundException("Projeto não encontrado: " + projetoId));

        validarAcessoAoProjeto(projeto, username);

        List<Tarefa> todasTarefas = tarefaRepository.findByProjetoId(projetoId);

        // Uso de stream para agrupar tarefas no ViewModel usando os novos Records
        List<TarefaDTO> aFazer = filtrarPorStatus(todasTarefas, StatusTarefa.A_FAZER);
        List<TarefaDTO> emAndamento = filtrarPorStatus(todasTarefas, StatusTarefa.EM_ANDAMENTO);
        List<TarefaDTO> concluidas = filtrarPorStatus(todasTarefas, StatusTarefa.CONCLUIDA);
        List<TarefaDTO> canceladas = filtrarPorStatus(todasTarefas, StatusTarefa.CANCELADA);

        boolean podeAdicionar = podeCriarTarefa(projeto, username);
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
    // ESCRITA (TRANSACTIONAL)
    // ==========================================

    @Transactional
    public TarefaDTO save(Tarefa tarefa, String username) {
        if (tarefa.getProjeto() == null || tarefa.getProjeto().getId() == null) {
            throw new RegraDeNegocioException("Uma tarefa deve estar associada a um projeto.");
        }

        Projeto projeto = projetoRepository.findById(tarefa.getProjeto().getId())
                .orElseThrow(() -> new EntityNotFoundException("Projeto associado não encontrado."));

        if (!podeCriarTarefa(projeto, username)) {
            throw new AcessoNegadoException("Você não tem permissão para criar tarefas neste projeto.");
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

        if (novoStatus == null) {
            throw new RegraDeNegocioException("O novo status não pode ser nulo.");
        }

        if (!podeMoverTarefa(tarefa, username)) {
            throw new AcessoNegadoException("Você não tem permissão para mover esta tarefa.");
        }

        if (tarefa.getStatus() == StatusTarefa.CANCELADA) {
            throw new RegraDeNegocioException("Tarefas canceladas não podem ser reativadas diretamente.");
        }

        tarefa.setStatus(novoStatus);
        tarefaRepository.save(tarefa);
    }

    @Transactional
    public TarefaDTO update(Long id, Tarefa dadosAtualizados) {
        Tarefa existente = tarefaRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Tarefa não encontrada: " + id));

        existente.setTitulo(dadosAtualizados.getTitulo());
        existente.setDescricao(dadosAtualizados.getDescricao());
        existente.setStatus(dadosAtualizados.getStatus());
        existente.setPrioridade(dadosAtualizados.getPrioridade());
        existente.setDataVencimento(dadosAtualizados.getDataVencimento());
        existente.setResponsavel(dadosAtualizados.getResponsavel());

        Tarefa atualizada = tarefaRepository.save(existente);
        return tarefaMapper.toDTO(atualizada);
    }

    @Transactional
    public void deleteById(Long id) {
        if (!tarefaRepository.existsById(id)) {
            throw new EntityNotFoundException("Tarefa não encontrada: " + id);
        }
        tarefaRepository.deleteById(id);
    }

    // ==========================================
    // MÉTODOS AUXILIARES
    // ==========================================

    private List<TarefaDTO> filtrarPorStatus(List<Tarefa> tarefas, StatusTarefa status) {
        return tarefas.stream()
                .filter(t -> t.getStatus() == status)
                .map(tarefaMapper::toDTO)
                .toList();
    }

    private void validarAcessoAoProjeto(Projeto projeto, String username) {
        if (projeto == null) {
            throw new AcessoNegadoException("Projeto não acessível.");
        }
        // Futura lógica de membros de equipe aqui
    }

    private boolean podeCriarTarefa(Projeto projeto, String username) {
        return username != null && !username.isBlank();
    }

    private boolean podeEditarProjeto(Projeto projeto, String username) {
        if (username == null) return false;
        if (projeto.getEquipe() != null && projeto.getEquipe().getLider() != null) {
            return projeto.getEquipe().getLider().getLogin().equals(username);
        }
        return false;
    }

    private boolean podeMoverTarefa(Tarefa tarefa, String username) {
        if (username == null) return false;
        if (tarefa.getResponsavel() != null && tarefa.getResponsavel().getLogin().equals(username)) {
            return true;
        }
        return podeEditarProjeto(tarefa.getProjeto(), username);
    }
}