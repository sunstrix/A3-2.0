package br.com.projetoA3.viewmodel;

import br.com.projetoA3.dto.TarefaDTO;

import java.util.List;

/**
 * ViewModel para a tela Kanban.
 * 
 * Esta classe imutável concentra toda a lógica de apresentação do quadro Kanban,
 * incluindo o agrupamento de tarefas por coluna, cálculo de métricas (total,
 * percentuais de conclusão e atraso) e verificação de permissões do usuário.
 * 
 * Centralizar essa lógica no Java (ao invés do Thymeleaf) traz os seguintes
 * benefícios:
 * - Testabilidade: a lógica pode ser testada com JUnit sem necessidade de 
 *   renderizar templates HTML.
 * - Performance: cálculos são feitos uma única vez no backend.
 * - Manutenção: mudanças de regras de negócio ficam concentradas em um único lugar.
 * - Aumenta a proporção de código Java no projeto (meta: 65% Java / 35% HTML).
 */
public class KanbanViewModel {

    private final Long projetoId;
    private final String nomeProjeto;
    private final String descricaoProjeto;
    private final List<TarefaDTO> tarefasAFazer;
    private final List<TarefaDTO> tarefasEmAndamento;
    private final List<TarefaDTO> tarefasConcluidas;
    private final List<TarefaDTO> tarefasCanceladas;
    private final boolean podeAdicionarTarefa;
    private final boolean podeEditarProjeto;

    /**
     * Construtor completo do ViewModel.
     */
    public KanbanViewModel(Long projetoId,
                           String nomeProjeto,
                           String descricaoProjeto,
                           List<TarefaDTO> tarefasAFazer,
                           List<TarefaDTO> tarefasEmAndamento,
                           List<TarefaDTO> tarefasConcluidas,
                           List<TarefaDTO> tarefasCanceladas,
                           boolean podeAdicionarTarefa,
                           boolean podeEditarProjeto) {
        this.projetoId = projetoId;
        this.nomeProjeto = nomeProjeto;
        this.descricaoProjeto = descricaoProjeto;
        this.tarefasAFazer = tarefasAFazer != null ? tarefasAFazer : List.of();
        this.tarefasEmAndamento = tarefasEmAndamento != null ? tarefasEmAndamento : List.of();
        this.tarefasConcluidas = tarefasConcluidas != null ? tarefasConcluidas : List.of();
        this.tarefasCanceladas = tarefasCanceladas != null ? tarefasCanceladas : List.of();
        this.podeAdicionarTarefa = podeAdicionarTarefa;
        this.podeEditarProjeto = podeEditarProjeto;
    }

    // ==========================================
    // GETTERS
    // ==========================================

    public Long getProjetoId() { return projetoId; }
    public String getNomeProjeto() { return nomeProjeto; }
    public String getDescricaoProjeto() { return descricaoProjeto; }
    public List<TarefaDTO> getTarefasAFazer() { return tarefasAFazer; }
    public List<TarefaDTO> getTarefasEmAndamento() { return tarefasEmAndamento; }
    public List<TarefaDTO> getTarefasConcluidas() { return tarefasConcluidas; }
    public List<TarefaDTO> getTarefasCanceladas() { return tarefasCanceladas; }
    public boolean isPodeAdicionarTarefa() { return podeAdicionarTarefa; }
    public boolean isPodeEditarProjeto() { return podeEditarProjeto; }

    // ==========================================
    // MÉTRICAS E CÁLCULOS
    // ==========================================

    /**
     * Retorna o número total de tarefas do projeto (todas as colunas somadas).
     */
    public int getTotalTarefas() {
        return tarefasAFazer.size()
             + tarefasEmAndamento.size()
             + tarefasConcluidas.size()
             + tarefasCanceladas.size();
    }

    /**
     * Retorna o percentual de tarefas concluídas em relação ao total.
     * Retorna 0.0 se não houver tarefas.
     */
    public double getPercentualConcluido() {
        int total = getTotalTarefas();
        if (total == 0) return 0.0;
        return (tarefasConcluidas.size() * 100.0) / total;
    }

    /**
     * Retorna o percentual de tarefas em andamento.
     */
    public double getPercentualEmAndamento() {
        int total = getTotalTarefas();
        if (total == 0) return 0.0;
        return (tarefasEmAndamento.size() * 100.0) / total;
    }

    /**
     * Retorna o número de tarefas atrasadas (prazo vencido, não concluídas/canceladas).
     * O campo "atrasada" já vem calculado no DTO pelo TarefaMapper.
     */
    public long getTotalTarefasAtrasadas() {
        return tarefasAFazer.stream().filter(TarefaDTO::atrasada).count()
             + tarefasEmAndamento.stream().filter(TarefaDTO::atrasada).count();
    }

    /**
     * Retorna o número de tarefas com prioridade ALTA ou CRÍTICA.
     */
    public long getTotalTarefasUrgentes() {
        return contarPorPrioridade("ALTA") + contarPorPrioridade("CRITICA");
    }

    /**
     * Verifica se o quadro Kanban está vazio (sem tarefas em nenhuma coluna).
     */
    public boolean isEmpty() {
        return getTotalTarefas() == 0;
    }

    /**
     * Verifica se o projeto está totalmente concluído (100% das tarefas em CONCLUIDA ou CANCELADA).
     */
    public boolean isProjetoFinalizado() {
        if (isEmpty()) return false;
        return (tarefasConcluidas.size() + tarefasCanceladas.size()) == getTotalTarefas();
    }

    // ==========================================
    // MÉTODOS AUXILIARES PRIVADOS
    // ==========================================

    private long contarPorPrioridade(String prioridade) {
        long aFazer = tarefasAFazer.stream()
            .filter(t -> prioridade.equals(t.prioridade()))
            .count();
        long emAndamento = tarefasEmAndamento.stream()
            .filter(t -> prioridade.equals(t.prioridade()))
            .count();
        long concluidas = tarefasConcluidas.stream()
            .filter(t -> prioridade.equals(t.prioridade()))
            .count();
        long canceladas = tarefasCanceladas.stream()
            .filter(t -> prioridade.equals(t.prioridade()))
            .count();
        return aFazer + emAndamento + concluidas + canceladas;
    }
}