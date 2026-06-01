package br.com.projetoA3.service;

import br.com.projetoA3.dto.TarefaDTO;
import br.com.projetoA3.enums.Prioridade;
import br.com.projetoA3.enums.StatusTarefa;
import br.com.projetoA3.exception.AcessoNegadoException;
import br.com.projetoA3.exception.RegraDeNegocioException;
import br.com.projetoA3.mapper.TarefaMapper;
import br.com.projetoA3.model.Equipe;
import br.com.projetoA3.model.Projeto;
import br.com.projetoA3.model.Tarefa;
import br.com.projetoA3.model.Usuario;
import br.com.projetoA3.repository.ProjetoRepository;
import br.com.projetoA3.repository.TarefaRepository;
import br.com.projetoA3.repository.UsuarioRepository;
import br.com.projetoA3.viewmodel.KanbanViewModel;
import jakarta.persistence.EntityNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * Testes unitários para TarefaService.
 * Restaurado com a lógica original de Kanban e Métricas.
 */
@ExtendWith(MockitoExtension.class)
class TarefaServiceTest {

    @Mock
    private TarefaRepository tarefaRepository;

    @Mock
    private ProjetoRepository projetoRepository;

    @Mock
    private UsuarioRepository usuarioRepository;

    @Mock
    private TarefaMapper tarefaMapper;

    @InjectMocks
    private TarefaService tarefaService;

    private Projeto projeto;
    private Tarefa tarefa;
    private Usuario usuario;
    private TarefaDTO tarefaDTO;

    @BeforeEach
    void setUp() {
        usuario = new Usuario();
        usuario.setId(1L);
        usuario.setNome("João Silva");
        usuario.setLogin("joao.silva");

        Equipe equipe = new Equipe();
        equipe.setLider(usuario);

        projeto = new Projeto();
        projeto.setId(1L);
        projeto.setNome("Projeto Teste");
        projeto.setDescricao("Descrição do projeto teste");
        projeto.setEquipe(equipe);

        tarefa = new Tarefa();
        tarefa.setId(100L);
        tarefa.setTitulo("Tarefa Teste");
        tarefa.setDescricao("Descrição da tarefa");
        tarefa.setStatus(StatusTarefa.A_FAZER);
        tarefa.setPrioridade(Prioridade.MEDIA);
        tarefa.setProjeto(projeto);
        tarefa.setResponsavel(usuario);
        tarefa.setDataVencimento(LocalDate.now().plusDays(7));

        // CORREÇÃO: Ajustado para o construtor de 11 parâmetros e tipos Enum
        tarefaDTO = new TarefaDTO(
            100L, "Tarefa Teste", "Descrição", 
            Prioridade.MEDIA, StatusTarefa.A_FAZER, 
            "João Silva", 1L, 
            LocalDate.now().plusDays(7), false,
            1L, "Projeto Teste"
        );
    }

    @Test
    @DisplayName("Deve lançar RegraDeNegocioException ao tentar mover tarefa cancelada")
    void deveLancarExcecaoAoMoverTarefaCancelada() {
        tarefa.setStatus(StatusTarefa.CANCELADA);
        when(tarefaRepository.findById(100L)).thenReturn(Optional.of(tarefa));

        RegraDeNegocioException exception = assertThrows(
            RegraDeNegocioException.class,
            () -> tarefaService.moverTarefa(100L, StatusTarefa.A_FAZER, "joao.silva")
        );

        assertEquals("Tarefas canceladas não podem ser reativadas diretamente.", exception.getMessage());
        verify(tarefaRepository, never()).save(any());
    }

    @Test
    @DisplayName("Deve mover tarefa com sucesso quando usuário tem permissão")
    void deveMoverTarefaComSucesso() {
        when(tarefaRepository.findById(100L)).thenReturn(Optional.of(tarefa));
        when(tarefaRepository.save(any(Tarefa.class))).thenReturn(tarefa);

        assertDoesNotThrow(() -> 
            tarefaService.moverTarefa(100L, StatusTarefa.EM_ANDAMENTO, "joao.silva")
        );

        verify(tarefaRepository).save(tarefa);
        assertEquals(StatusTarefa.EM_ANDAMENTO, tarefa.getStatus());
    }

    @Test
    @DisplayName("Deve lançar EntityNotFoundException quando tarefa não existe")
    void deveLancarExcecaoQuandoTarefaNaoExiste() {
        when(tarefaRepository.findById(999L)).thenReturn(Optional.empty());

        assertThrows(
            EntityNotFoundException.class,
            () -> tarefaService.moverTarefa(999L, StatusTarefa.CONCLUIDA, "joao.silva")
        );

        verify(tarefaRepository, never()).save(any());
    }

    @Test
    @DisplayName("Deve lançar RegraDeNegocioException quando novo status é nulo")
    void deveLancarExcecaoQuandoStatusNulo() {
        when(tarefaRepository.findById(100L)).thenReturn(Optional.of(tarefa));

        assertThrows(
            RegraDeNegocioException.class,
            () -> tarefaService.moverTarefa(100L, null, "joao.silva")
        );
    }

    @Test
    @DisplayName("Deve construir KanbanViewModel com métricas corretas")
    void deveConstruirKanbanViewModelComMetricasCorretas() {
        Tarefa t1 = criarTarefaComStatus(StatusTarefa.CONCLUIDA);
        Tarefa t2 = criarTarefaComStatus(StatusTarefa.CONCLUIDA);
        Tarefa t3 = criarTarefaComStatus(StatusTarefa.CONCLUIDA);
        Tarefa t4 = criarTarefaComStatus(StatusTarefa.EM_ANDAMENTO);
        Tarefa t5 = criarTarefaComStatus(StatusTarefa.A_FAZER);

        List<Tarefa> todasTarefas = List.of(t1, t2, t3, t4, t5);

        when(projetoRepository.findById(1L)).thenReturn(Optional.of(projeto));
        when(tarefaRepository.findByProjetoId(1L)).thenReturn(todasTarefas);
        
        when(tarefaMapper.toDTO(any(Tarefa.class))).thenAnswer(invocation -> {
            Tarefa t = invocation.getArgument(0);
            return new TarefaDTO(
                t.getId(), t.getTitulo(), t.getDescricao(),
                Prioridade.MEDIA, t.getStatus(),
                "Responsavel", 1L, LocalDate.now(), false, 1L, "Projeto"
            );
        });

        KanbanViewModel viewModel = tarefaService.buildKanbanViewModel(1L, "joao.silva");

        assertNotNull(viewModel);
        assertEquals(5, viewModel.getTotalTarefas());
        assertEquals(3, viewModel.getTarefasConcluidas().size());
        assertEquals(60.0, viewModel.getPercentualConcluido(), 0.01);
    }

    @Test
    @DisplayName("Deve identificar projeto como finalizado quando todas tarefas estão concluídas ou canceladas")
    void deveIdentificarProjetoFinalizado() {
        Tarefa tarefaConcluida = criarTarefaComStatus(StatusTarefa.CONCLUIDA);
        Tarefa tarefaCancelada = criarTarefaComStatus(StatusTarefa.CANCELADA);

        List<Tarefa> todasTarefas = List.of(tarefaConcluida, tarefaCancelada);

        when(projetoRepository.findById(1L)).thenReturn(Optional.of(projeto));
        when(tarefaRepository.findByProjetoId(1L)).thenReturn(todasTarefas);
        when(tarefaMapper.toDTO(any(Tarefa.class))).thenReturn(tarefaDTO);

        KanbanViewModel viewModel = tarefaService.buildKanbanViewModel(1L, "joao.silva");

        assertTrue(viewModel.isProjetoFinalizado());
    }

    @Test
    @DisplayName("Deve calcular corretamente tarefas atrasadas")
    void deveCalcularTarefasAtrasadas() {
        // CORREÇÃO: Assinatura de 11 parâmetros
        TarefaDTO tarefaAtrasada = new TarefaDTO(
            1L, "Atrasada", "desc", Prioridade.ALTA, StatusTarefa.A_FAZER,
            "João", 1L, LocalDate.now().minusDays(5), true, 1L, "Projeto"
        );
        TarefaDTO tarefaNoPrazo = new TarefaDTO(
            2L, "No Prazo", "desc", Prioridade.MEDIA, StatusTarefa.EM_ANDAMENTO,
            "João", 1L, LocalDate.now().plusDays(5), false, 1L, "Projeto"
        );

        KanbanViewModel viewModel = new KanbanViewModel(
            1L, "Projeto", "desc",
            List.of(tarefaAtrasada, tarefaNoPrazo),
            List.of(),
            List.of(),
            List.of(),
            true, true
        );

        assertEquals(1, viewModel.getTotalTarefasAtrasadas());
    }

    @Test
    @DisplayName("Deve salvar nova tarefa com sucesso e retornar DTO")
    void deveSalvarNovaTarefaComSucesso() {
        when(projetoRepository.findById(1L)).thenReturn(Optional.of(projeto));
        when(tarefaRepository.save(any(Tarefa.class))).thenReturn(tarefa);
        when(tarefaMapper.toDTO(tarefa)).thenReturn(tarefaDTO);

        TarefaDTO resultado = tarefaService.save(tarefa, "joao.silva");

        assertNotNull(resultado);
        assertEquals(100L, resultado.id());
        verify(tarefaRepository).save(tarefa);
    }

    @Test
    @DisplayName("Deve lançar EntityNotFoundException ao salvar tarefa com projeto inexistente")
    void deveLancarExcecaoAoSalvarTarefaComProjetoInexistente() {
        when(projetoRepository.findById(1L)).thenReturn(Optional.empty());

        assertThrows(
            EntityNotFoundException.class,
            () -> tarefaService.save(tarefa, "joao.silva")
        );
    }

    @Test
    @DisplayName("Deve atribuir status A_FAZER como padrão quando não especificado")
    void deveAtribuirStatusPadraoQuandoNaoEspecificado() {
        tarefa.setStatus(null);
        when(projetoRepository.findById(1L)).thenReturn(Optional.of(projeto));
        when(tarefaRepository.save(any(Tarefa.class))).thenReturn(tarefa);
        when(tarefaMapper.toDTO(tarefa)).thenReturn(tarefaDTO);

        tarefaService.save(tarefa, "joao.silva");

        assertEquals(StatusTarefa.A_FAZER, tarefa.getStatus());
    }

    private Tarefa criarTarefaComStatus(StatusTarefa status) {
        Tarefa t = new Tarefa();
        t.setId((long) (Math.random() * 10000));
        t.setTitulo("Tarefa " + status.name());
        t.setStatus(status);
        t.setProjeto(projeto);
        return t;
    }
}