package br.com.projetoA3.service;

import br.com.projetoA3.dto.TarefaDTO;
import br.com.projetoA3.enums.StatusTarefa;
import br.com.projetoA3.exception.AcessoNegadoException;
import br.com.projetoA3.exception.RegraDeNegocioException;
import br.com.projetoA3.mapper.TarefaMapper;
import br.com.projetoA3.model.Projeto;
import br.com.projetoA3.model.Tarefa;
import br.com.projetoA3.model.Usuario;
import br.com.projetoA3.repository.ProjetoRepository;
import br.com.projetoA3.repository.TarefaRepository;
import br.com.projetoA3.repository.UsuarioRepository;
import br.com.projetoA3.viewmodel.KanbanViewModel;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * Service responsável pela lógica de negócio das Tarefas.
 * 
 * Princípios aplicados:
 * - Injeção por construtor (sem @Autowired em campo)
 * - @Transactional(readOnly = true) na classe (otimiza transações de leitura)
 * - Override com @Transactional apenas em métodos de escrita
 * - Uso de DTOs e Mappers para desacoplar entidades da camada de apresentação
 * - Validação de negócio com exceções customizadas
 * - Construção do ViewModel no Service (não no Controller)
 */
@Service
@Transactional(readOnly = true)
public class TarefaService {

    private final TarefaRepository tarefaRepository;
    private final ProjetoRepository projetoRepository;
    private final UsuarioRepository usuarioRepository;
    private final TarefaMapper tarefaMapper;

    /**
     * Injeção de dependências via construtor (padrão Spring moderno).
     * Elimina a necessidade de @Autowired e permite testes unitários
     * com mocks sem contexto Spring.
     */
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
    // CONSULTAS (READ-ONLY) - herdado readOnly = true da classe
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
    // VIEWMODEL - Lógica de apresentação concentrada no Java
    // ==========================================

    /**
     * Constrói o ViewModel completo para a tela Kanban.
     * Agrupa as tarefas por status e calcula métricas de apresentação.
     * 
     * @param projetoId ID do projeto
     * @param username  Login do usuário logado (para verificar permissões)
     * @return KanbanViewModel pronto para ser enviado à view
     * @throws EntityNotFoundException se o projeto não existir
     * @throws AcessoNegadoException se o usuário não tiver acesso ao projeto
     */
    public KanbanViewModel buildKanbanViewModel(Long projetoId, String username) {
        Projeto projeto = projetoRepository.findById(projetoId)
                .orElseThrow(() -> new EntityNotFoundException("Projeto não encontrado: " + projetoId));

        // Validação de acesso (pode ser expandida com regras de equipe no futuro)
        validarAcessoAoProjeto(projeto, username);

        // Busca todas as tarefas do projeto (uma única query)
        List<Tarefa> todasTarefas = tarefaRepository.findByProjetoId(projetoId);

        // Agrupa por status usando streams (mais eficiente que múltiplas queries)
        List<TarefaDTO> aFazer = filtrarPorStatus(todasTarefas, StatusTarefa.A_FAZER);
        List<TarefaDTO> emAndamento = filtrarPorStatus(todasTarefas, StatusTarefa.EM_ANDAMENTO);
        List<TarefaDTO> concluidas = filtrarPorStatus(todasTarefas, StatusTarefa.CONCLUIDA);
        List<TarefaDTO> canceladas = filtrarPorStatus(todasTarefas, StatusTarefa.CANCELADA);

        // Verifica permissões do usuário no contexto do projeto
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
    // ESCRITA (TRANSACTIONAL) - override do readOnly
    // ==========================================

    /**
     * Salva uma nova tarefa.
     * 
     * @param tarefa    Entidade a ser persistida
     * @param username  Usuário que está criando (para auditoria)
     * @return TarefaDTO da tarefa recém-criada
     */
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

        // Garante status padrão se não foi definido
        if (tarefa.getStatus() == null) {
            tarefa.setStatus(StatusTarefa.A_FAZER);
        }

        Tarefa salva = tarefaRepository.save(tarefa);
        return tarefaMapper.toDTO(salva);
    }

    /**
     * Move uma tarefa para outro status (operação principal do Kanban - Drag & Drop).
     * 
     * @param tarefaId    ID da tarefa a ser movida
     * @param novoStatus  Novo status desejado
     * @param username    Usuário que está movendo (para validação de permissão)
     * @throws EntityNotFoundException se a tarefa não existir
     * @throws AcessoNegadoException se o usuário não puder mover
     * @throws RegraDeNegocioException se a transição for inválida
     */
    @Transactional
    public void moverTarefa(Long tarefaId, StatusTarefa novoStatus, String username) {
        Tarefa tarefa = tarefaRepository.findById(tarefaId)
                .orElseThrow(() -> new EntityNotFoundException("Tarefa não encontrada: " + tarefaId));

        if (novoStatus == null) {
            throw new RegraDeNegocioException("O novo status não pode ser nulo.");
        }

        // Valida permissão do usuário
        if (!podeMoverTarefa(tarefa, username)) {
            throw new AcessoNegadoException("Você não tem permissão para mover esta tarefa.");
        }

        // Valida regra de negócio: tarefas canceladas não podem ser reativadas
        if (tarefa.getStatus() == StatusTarefa.CANCELADA) {
            throw new RegraDeNegocioException("Tarefas canceladas não podem ser reativadas diretamente.");
        }

        // Aplica a mudança
        tarefa.setStatus(novoStatus);
        tarefaRepository.save(tarefa);
    }

    /**
     * Atualiza uma tarefa existente.
     */
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

    /**
     * Remove uma tarefa.
     */
    @Transactional
    public void deleteById(Long id) {
        if (!tarefaRepository.existsById(id)) {
            throw new EntityNotFoundException("Tarefa não encontrada: " + id);
        }
        tarefaRepository.deleteById(id);
    }

    // ==========================================
    // MÉTODOS AUXILIARES PRIVADOS
    // ==========================================

    private List<TarefaDTO> filtrarPorStatus(List<Tarefa> tarefas, StatusTarefa status) {
        return tarefas.stream()
                .filter(t -> t.getStatus() == status)
                .map(tarefaMapper::toDTO)
                .toList();
    }

    private void validarAcessoAoProjeto(Projeto projeto, String username) {
        // Lógica de validação de acesso pode ser expandida no futuro
        // (ex: verificar se usuário pertence à equipe do projeto)
        if (projeto == null) {
            throw new AcessoNegadoException("Projeto não acessível.");
        }
    }

    private boolean podeCriarTarefa(Projeto projeto, String username) {
        // Por enquanto, qualquer usuário autenticado pode criar tarefas
        // No futuro, restringir a membros da equipe ou gerente do projeto
        return username != null && !username.isBlank();
    }

    private boolean podeEditarProjeto(Projeto projeto, String username) {
        // Regra básica: apenas o gerente do projeto pode editá-lo
        if (username == null) return false;
        if (projeto.getEquipe() != null && projeto.getEquipe().getLider() != null) {
            return projeto.getEquipe().getLider().getLogin().equals(username);
        }
        return false;
    }

    private boolean podeMoverTarefa(Tarefa tarefa, String username) {
        if (username == null) return false;
        // Responsável pela tarefa pode movê-la
        if (tarefa.getResponsavel() != null && tarefa.getResponsavel().getLogin().equals(username)) {
            return true;
        }
        // Gerente do projeto também pode
        return podeEditarProjeto(tarefa.getProjeto(), username);
    }
}