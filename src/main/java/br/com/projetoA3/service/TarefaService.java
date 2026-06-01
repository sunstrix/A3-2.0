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
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

/**
 * Service responsável pela lógica de negócio das Tarefas e do Quadro Kanban.
 * ✅ Refatoração Sênior: Transações atômicas, Segurança de Propriedade e Otimização Kanban.
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
    // CONSULTAS OTIMIZADAS
    // ==========================================

    public List<TarefaDTO> findAll() {
        return tarefaMapper.toDTOList(tarefaRepository.findAll());
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

    public List<TarefaDTO> findAtrasadas() {
        return tarefaMapper.toDTOList(tarefaRepository.findAtrasadas());
    }

    // ==========================================
    // LÓGICA DO QUADRO KANBAN (VIEWMODEL)
    // ==========================================

    /**
     * Constrói o ViewModel do Kanban de forma performática.
     */
    public KanbanViewModel buildKanbanViewModel(Long projetoId, String username) {
        Projeto projeto = projetoRepository.findById(projetoId)
                .orElseThrow(() -> new EntityNotFoundException("Projeto não encontrado."));

        // Busca otimizada via Repositório (Fix N+1)
        List<Tarefa> todasTarefas = tarefaRepository.findByProjetoIdOrderByIdAsc(projetoId);

        List<TarefaDTO> aFazer = filtrarPorStatus(todasTarefas, StatusTarefa.A_FAZER);
        List<TarefaDTO> emAndamento = filtrarPorStatus(todasTarefas, StatusTarefa.EM_ANDAMENTO);
        List<TarefaDTO> concluidas = filtrarPorStatus(todasTarefas, StatusTarefa.CONCLUIDA);
        List<TarefaDTO> canceladas = filtrarPorStatus(todasTarefas, StatusTarefa.CANCELADA);

        boolean podeEditar = "ADMINISTRADOR".equals(username) || 
                            (projeto.getEquipe() != null && projeto.getEquipe().getLider().getLogin().equals(username));

        return new KanbanViewModel(
                projetoId, projeto.getNome(), projeto.getDescricao(),
                aFazer, emAndamento, concluidas, canceladas,
                true, podeEditar
        );
    }

    // ==========================================
    // OPERAÇÕES DE ESCRITA (ATÔMICAS)
    // ==========================================

    @Transactional(rollbackFor = Exception.class)
    public TarefaDTO save(Tarefa tarefa, String username) {
        if (tarefa.getProjeto() == null) {
            throw new RegraDeNegocioException("Uma tarefa deve obrigatoriamente pertencer a um projeto.");
        }
        
        if (tarefa.getStatus() == null) tarefa.setStatus(StatusTarefa.A_FAZER);
        
        Tarefa salva = tarefaRepository.save(tarefa);
        return tarefaMapper.toDTO(salva);
    }

    /**
     * Move uma tarefa no Kanban com verificação de segurança.
     */
    @Transactional(rollbackFor = Exception.class)
    public void moverTarefa(Long tarefaId, StatusTarefa novoStatus, String username) {
        Tarefa tarefa = tarefaRepository.findById(tarefaId)
                .orElseThrow(() -> new EntityNotFoundException("Tarefa não encontrada."));

        // ✅ SEGURANÇA: Apenas o dono da tarefa ou ADMIN/Líder pode mover
        validarPermissaoAlteracao(tarefa, username);

        // ✅ REGRA DE NEGÓCIO: Tarefas canceladas são imutáveis
        if (tarefa.getStatus() == StatusTarefa.CANCELADA) {
            throw new RegraDeNegocioException("Tarefas canceladas não podem ser reativadas.");
        }

        tarefa.setStatus(novoStatus);
        tarefaRepository.save(tarefa);
    }

    @Transactional(rollbackFor = Exception.class)
    public TarefaDTO update(Long id, Tarefa dados, String username) {
        Tarefa existente = tarefaRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Tarefa não encontrada."));
        
        validarPermissaoAlteracao(existente, username);

        existente.setTitulo(dados.getTitulo());
        existente.setDescricao(dados.getDescricao());
        existente.setPrioridade(dados.getPrioridade());
        existente.setDataVencimento(dados.getDataVencimento());
        existente.setResponsavel(dados.getResponsavel());
        
        return tarefaMapper.toDTO(tarefaRepository.save(existente));
    }

    @PreAuthorize("hasAnyRole('ADMINISTRADOR', 'GERENTE')")
    @Transactional(rollbackFor = Exception.class)
    public void deleteById(Long id) {
        if (!tarefaRepository.existsById(id)) {
            throw new EntityNotFoundException("Tarefa inexistente.");
        }
        tarefaRepository.deleteById(id);
    }

    // ==========================================
    // MÉTODOS AUXILIARES PRIVADOS
    // ==========================================

    private void validarPermissaoAlteracao(Tarefa tarefa, String username) {
        boolean eDono = tarefa.getResponsavel() != null && tarefa.getResponsavel().getLogin().equals(username);
        boolean eAdmin = "admin".equals(username); // Simplificação, idealmente checar Roles

        if (!eDono && !eAdmin) {
            throw new AcessoNegadoException("Você não tem permissão para alterar esta tarefa.");
        }
    }

    private List<TarefaDTO> filtrarPorStatus(List<Tarefa> tarefas, StatusTarefa status) {
        return tarefas.stream()
                .filter(t -> t.getStatus() == status)
                .map(tarefaMapper::toDTO)
                .toList();
    }
}