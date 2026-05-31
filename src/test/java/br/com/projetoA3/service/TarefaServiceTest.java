package br.com.projetoA3.service;

import br.com.projetoA3.dto.TarefaDTO;
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
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

/**
 * Testes unitários para TarefaService.
 * 
 * Utiliza JUnit 5 e Mockito para validar a lógica de negócio sem
 * dependências do Spring Context, garantindo execução rápida e isolada.
 * 
 * Cenários testados:
 * - Regras de negócio (tarefas canceladas não podem ser reativadas)
 * - Validação de permissões (usuário não autorizado não pode mover tarefas)
 * - Cálculo de métricas do KanbanViewModel (percentual concluído, atrasadas)
 * - Fluxo normal de criação de tarefas
 * - Tratamento de entidades não encontradas
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
        // Arrange - Configuração comum para os testes
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
        tarefa.setProjeto(projeto);
        tarefa.setResponsavel(usuario);
        tarefa.setDataVencimento(LocalDate.now().plusDays(7));

        tarefaDTO = new TarefaDTO(
            100L, "Tarefa Teste", "Descrição", 
            "A_FAZER", "MEDIA", 
            "João Silva", 1L, 
            LocalDate.now().plusDays(7), false
        );
    }

    // ==========================================
    // TESTES DE REGRAS DE NEGÓCIO
    // ==========================================

    @Test
    @DisplayName("Deve lançar RegraDeNegocioException ao tentar mover tarefa cancelada")
    void deveLancarExcecaoAoMoverTarefaCancelada() {
        // Arrange
        tarefa.setStatus(StatusTarefa.CANCELADA);
        when(tarefaRepository.findById(100L)).thenReturn(Optional.of(tarefa));

        // Act & Assert
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
        // Arrange
        when(tarefaRepository.findById(100L)).thenReturn(Optional.of(tarefa));
        when(tarefaRepository.save(any(Tarefa.class))).thenReturn(tarefa);

        // Act
        assertDoesNotThrow(() -> 
            tarefaService.moverTarefa(100L, StatusTarefa.EM_ANDAMENTO, "joao.silva")
        );

        // Assert
        verify(tarefaRepository).save(tarefa);
        assertEquals(StatusTarefa.EM_ANDAMENTO, tarefa.getStatus());
    }

    @Test
    @DisplayName("Deve lançar EntityNotFoundException quando tarefa não existe")
    void deveLancarExcecaoQuandoTarefaNaoExiste() {
        // Arrange
        when(tarefaRepository.findById(999L)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(
            EntityNotFoundException.class,
            () -> tarefaService.moverTarefa(999L, StatusTarefa.CONCLUIDA, "joao.silva")
        );

        verify(tarefaRepository, never()).save(any());
    }

    @Test
    @DisplayName("Deve lançar RegraDeNegocioException quando novo status é nulo")
    void deveLancarExcecaoQuandoStatusNulo() {
        // Arrange
        when(tarefaRepository.findById(100L)).thenReturn(Optional.of(tarefa));

        // Act & Assert
        assertThrows(
            RegraDeNegocioException.class,
            () -> tarefaService.moverTarefa(100L, null, "joao.silva")
        );
    }

    // ==========================================
    // TESTES DE VIEWMODEL E MÉTRICAS
    // ==========================================

    @Test
    @DisplayName("Deve construir KanbanViewModel com métricas corretas")
    void deveConstruirKanbanViewModelComMetricasCorretas() {
        // Arrange
        Tarefa tarefaConcluida1 = criarTarefaComStatus(StatusTarefa.CONCLUIDA);
        Tarefa tarefaConcluida2 = criarTarefaComStatus(StatusTarefa.CONCLUIDA);
        Tarefa tarefaConcluida3 = criarTarefaComStatus(StatusTarefa.CONCLUIDA);
        Tarefa tarefaEmAndamento = criarTarefaComStatus(StatusTarefa.EM_ANDAMENTO);
        Tarefa tarefaAFazer = criarTarefaComStatus(StatusTarefa.A_FAZER);

        List<Tarefa> todasTarefas = List.of(
            tarefaConcluida1, tarefaConcluida2, tarefaConcluida3,
            tarefaEmAndamento, tarefaAFazer
        );

        when(projetoRepository.findById(1L)).thenReturn(Optional.of(projeto));
        when(tarefaRepository.findByProjetoId(1L)).thenReturn(todasTarefas);
        
        // Mock do mapper para converter entidades em DTOs
        when(tarefaMapper.toDTO(any(Tarefa.class))).thenAnswer(invocation -> {
            Tarefa t = invocation.getArgument(0);
            return new TarefaDTO(
                t.getId(), t.getTitulo(), t.getDescricao(),
                t.getStatus().name(), "MEDIA",
                null, null, null, false
            );
        });

        // Act
        KanbanViewModel viewModel = tarefaService.buildKanbanViewModel(1L, "joao.silva");

        // Assert
        assertNotNull(viewModel);
        assertEquals(5, viewModel.getTotalTarefas());
        assertEquals(3, viewModel.getTarefasConcluidas().size());
        assertEquals(1, viewModel.getTarefasEmAndamento().size());
        assertEquals(1, viewModel.getTarefasAFazer().size());
        assertEquals(60.0, viewModel.getPercentualConcluido(), 0.01);
        assertFalse(viewModel.isEmpty());
        assertFalse(viewModel.isProjetoFinalizado());
    }

    @Test
    @DisplayName("Deve identificar projeto como finalizado quando todas tarefas estão concluídas ou canceladas")
    void deveIdentificarProjetoFinalizado() {
        // Arrange
        Tarefa tarefaConcluida = criarTarefaComStatus(StatusTarefa.CONCLUIDA);
        Tarefa tarefaCancelada = criarTarefaComStatus(StatusTarefa.CANCELADA);

        List<Tarefa> todasTarefas = List.of(tarefaConcluida, tarefaCancelada);

        when(projetoRepository.findById(1L)).thenReturn(Optional.of(projeto));
        when(tarefaRepository.findByProjetoId(1L)).thenReturn(todasTarefas);
        when(tarefaMapper.toDTO(any(Tarefa.class))).thenReturn(tarefaDTO);

        // Act
        KanbanViewModel viewModel = tarefaService.buildKanbanViewModel(1L, "joao.silva");

        // Assert
        assertTrue(viewModel.isProjetoFinalizado());
        assertEquals(100.0, viewModel.getPercentualConcluido(), 0.01);
    }

    @Test
    @DisplayName("Deve calcular corretamente tarefas atrasadas")
    void deveCalcularTarefasAtrasadas() {
        // Arrange
        TarefaDTO tarefaAtrasada = new TarefaDTO(
            1L, "Atrasada", "desc", "A_FAZER", "ALTA",
            "João", 1L, LocalDate.now().minusDays(5), true
        );
        TarefaDTO tarefaNoPrazo = new TarefaDTO(
            2L, "No Prazo", "desc", "EM_ANDAMENTO", "MEDIA",
            "João", 1L, LocalDate.now().plusDays(5), false
        );

        KanbanViewModel viewModel = new KanbanViewModel(
            1L, "Projeto", "desc",
            List.of(tarefaAtrasada, tarefaNoPrazo),
            List.of(),
            List.of(),
            List.of(),
            true, true
        );

        // Act & Assert
        assertEquals(1, viewModel.getTotalTarefasAtrasadas());
    }

    // ==========================================
    // TESTES DE CRUD
    // ==========================================

    @Test
    @DisplayName("Deve salvar nova tarefa com sucesso e retornar DTO")
    void deveSalvarNovaTarefaComSucesso() {
        // Arrange
        when(projetoRepository.findById(1L)).thenReturn(Optional.of(projeto));
        when(tarefaRepository.save(any(Tarefa.class))).thenReturn(tarefa);
        when(tarefaMapper.toDTO(tarefa)).thenReturn(tarefaDTO);

        // Act
        TarefaDTO resultado = tarefaService.save(tarefa, "joao.silva");

        // Assert
        assertNotNull(resultado);
        assertEquals(100L, resultado.id());
        assertEquals("Tarefa Teste", resultado.titulo());
        verify(tarefaRepository).save(tarefa);
    }

    @Test
    @DisplayName("Deve lançar EntityNotFoundException ao salvar tarefa com projeto inexistente")
    void deveLancarExcecaoAoSalvarTarefaComProjetoInexistente() {
        // Arrange
        when(projetoRepository.findById(1L)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(
            EntityNotFoundException.class,
            () -> tarefaService.save(tarefa, "joao.silva")
        );

        verify(tarefaRepository, never()).save(any());
    }

    @Test
    @DisplayName("Deve atribuir status A_FAZER como padrão quando não especificado")
    void deveAtribuirStatusPadraoQuandoNaoEspecificado() {
        // Arrange
        tarefa.setStatus(null);
        when(projetoRepository.findById(1L)).thenReturn(Optional.of(projeto));
        when(tarefaRepository.save(any(Tarefa.class))).thenReturn(tarefa);
        when(tarefaMapper.toDTO(tarefa)).thenReturn(tarefaDTO);

        // Act
        tarefaService.save(tarefa, "joao.silva");

        // Assert
        assertEquals(StatusTarefa.A_FAZER, tarefa.getStatus());
    }

    // ==========================================
    // MÉTODOS AUXILIARES
    // ==========================================

    private Tarefa criarTarefaComStatus(StatusTarefa status) {
        Tarefa t = new Tarefa();
        t.setId((long) (Math.random() * 10000));
        t.setTitulo("Tarefa " + status.name());
        t.setStatus(status);
        t.setProjeto(projeto);
        return t;
    }
}