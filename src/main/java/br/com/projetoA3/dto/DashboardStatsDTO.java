package br.com.projetoA3.dto;

import java.util.List;
import java.util.Map;

/**
 * DTO Imutável para transporte de métricas analíticas do Dashboard.
 * Utiliza Java Records para performance e segurança.
 */
public record DashboardStatsDTO(
    // Métricas Globais (Visíveis para ADMIN e GERENTE)
    long totalProjetos,
    long totalTarefas,
    long totalUsuarios,
    long tarefasAtrasadas,
    double taxaConclusaoPercentual,
    
    // Dados para Gráficos (Chave do Status -> Quantidade)
    Map<String, Long> distribuicaoTarefas,
    Map<String, Long> projetosPorStatus,
    
    // Listas para Widgets de Prazos
    List<TarefaDTO> prazosProximos,
    
    // Métricas Específicas do Usuário (Visíveis para COLABORADOR)
    long minhasTarefasPendentes,
    List<TarefaDTO> meusPrazosProximos
) {
    /**
     * Construtor compacto para garantir que listas e mapas nunca sejam nulos,
     * evitando NullPointerException no Thymeleaf ou JavaScript.
     */
    public DashboardStatsDTO {
        distribuicaoTarefas = distribuicaoTarefas != null ? distribuicaoTarefas : Map.of();
        projetosPorStatus = projetosPorStatus != null ? projetosPorStatus : Map.of();
        prazosProximos = prazosProximos != null ? List.copyOf(prazosProximos) : List.of();
        meusPrazosProximos = meusPrazosProximos != null ? List.copyOf(meusPrazosProximos) : List.of();
    }
}