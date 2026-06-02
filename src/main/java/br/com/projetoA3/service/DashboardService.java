package br.com.projetoA3.service;

import br.com.projetoA3.dto.DashboardStatsDTO;
import br.com.projetoA3.dto.TarefaDTO;
import br.com.projetoA3.enums.StatusTarefa;
import br.com.projetoA3.mapper.TarefaMapper;
import br.com.projetoA3.model.Usuario;
import br.com.projetoA3.repository.ProjetoRepository;
import br.com.projetoA3.repository.TarefaRepository;
import br.com.projetoA3.repository.UsuarioRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

@Service
public class DashboardService {

    private final TarefaRepository tarefaRepository;
    private final ProjetoRepository projetoRepository;
    private final UsuarioRepository usuarioRepository;
    private final UsuarioService usuarioService;
    private final TarefaMapper tarefaMapper;

    public DashboardService(TarefaRepository tarefaRepository,
                            ProjetoRepository projetoRepository,
                            UsuarioRepository usuarioRepository,
                            UsuarioService usuarioService,
                            TarefaMapper tarefaMapper) {
        this.tarefaRepository = tarefaRepository;
        this.projetoRepository = projetoRepository;
        this.usuarioRepository = usuarioRepository;
        this.usuarioService = usuarioService;
        this.tarefaMapper = tarefaMapper;
    }

    @Transactional(readOnly = true)
    public DashboardStatsDTO getDashboardStats(String username) {
        // CORREÇÃO: Tratando o Optional retornado pelo findByLogin
        Usuario usuarioLogado = usuarioService.findByLogin(username)
                .orElseThrow(() -> new RuntimeException("Usuário não encontrado: " + username));
                
        boolean isGestor = usuarioLogado.getPerfil().name().equals("ADMINISTRADOR") || 
                           usuarioLogado.getPerfil().name().equals("GERENTE");

        // 1. Métricas Globais (Otimizadas)
        long totalProjetos = projetoRepository.count();
        long totalTarefas = tarefaRepository.count();
        long totalUsuarios = usuarioRepository.count();
        long tarefasAtrasadas = tarefaRepository.countAtrasadasOtimizado();

        // 2. Gráfico de Rosca: Distribuição de Tarefas
        Map<String, Long> distribuicaoTarefas = convertToMap(tarefaRepository.countTasksByStatusGrouped());

        // 3. Gráfico de Barras: Projetos por Status
        Map<String, Long> projetosPorStatus = convertToMap(projetoRepository.countProjectsByStatusGrouped());

        // 4. Cálculo de Taxa de Conclusão (Seguro contra divisão por zero)
        long concluidas = distribuicaoTarefas.getOrDefault("CONCLUIDA", 0L);
        double taxaConclusao = (totalTarefas > 0) ? (concluidas * 100.0) / totalTarefas : 0.0;

        // 5. Lista de Prazos Próximos (Top 5 Global)
        List<TarefaDTO> prazosProximos = tarefaRepository.findTop5ByStatusNotInOrderByDataVencimentoAsc(
                List.of(StatusTarefa.CONCLUIDA, StatusTarefa.CANCELADA))
                .stream().map(tarefaMapper::toDTO).toList();

        // 6. Métricas Específicas do Colaborador
        long minhasPendentes = 0;
        List<TarefaDTO> meusPrazos = List.of();

        if (!isGestor) {
            minhasPendentes = tarefaRepository.countByResponsavelIdAndStatusIn(
                    usuarioLogado.getId(), List.of(StatusTarefa.A_FAZER, StatusTarefa.EM_ANDAMENTO));
            
            meusPrazos = tarefaRepository.findTop5ByResponsavelIdAndStatusInOrderByDataVencimentoAsc(
                    usuarioLogado.getId(), List.of(StatusTarefa.A_FAZER, StatusTarefa.EM_ANDAMENTO))
                    .stream().map(tarefaMapper::toDTO).toList();
        }

        return new DashboardStatsDTO(
                totalProjetos, totalTarefas, totalUsuarios, tarefasAtrasadas, taxaConclusao,
                distribuicaoTarefas, projetosPorStatus, prazosProximos, minhasPendentes, meusPrazos
        );
    }

    private Map<String, Long> convertToMap(List<Object[]> results) {
        return results.stream()
                .collect(Collectors.toMap(
                        row -> row[0].toString(),
                        row -> (Long) row[1]
                ));
    }
}