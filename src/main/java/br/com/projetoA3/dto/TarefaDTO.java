package br.com.projetoA3.dto;

import br.com.projetoA3.enums.Prioridade;
import br.com.projetoA3.enums.StatusTarefa;
import br.com.projetoA3.model.Tarefa;

import java.time.LocalDate;

/**
 * Data Transfer Object (DTO) para Tarefa.
 * 
 * Este Record imutável desacopla a entidade JPA Tarefa das views Thymeleaf,
 * evitando problemas de serialização com associações lazy e permitindo
 * evolução independente da API.
 */
public record TarefaDTO(
    Long id,
    String titulo,
    String descricao,
    Prioridade prioridade,
    StatusTarefa status,
    String responsavelNome,
    Long responsavelId,
    LocalDate dataVencimento,
    boolean atrasada,
    Long projetoId,
    String projetoNome
) {

    /**
     * Construtor de mapeamento a partir da entidade JPA Tarefa.
     */
    public TarefaDTO(Tarefa tarefa) {
        this(
            tarefa != null ? tarefa.getId() : null,
            tarefa != null ? tarefa.getTitulo() : null,
            tarefa != null ? tarefa.getDescricao() : null,
            tarefa != null ? tarefa.getPrioridade() : null,
            tarefa != null ? tarefa.getStatus() : null,
            extrairNomeResponsavel(tarefa),
            extrairIdResponsavel(tarefa),
            tarefa != null ? tarefa.getDataVencimento() : null,
            calcularAtraso(tarefa),
            extrairIdProjeto(tarefa),
            extrairNomeProjeto(tarefa)
        );
    }

    // ==========================================
    // MÉTODOS UTILITÁRIOS (extração null-safe)
    // ==========================================

    private static String extrairNomeResponsavel(Tarefa tarefa) {
        if (tarefa == null || tarefa.getResponsavel() == null) {
            return null;
        }
        return tarefa.getResponsavel().getNome();
    }

    private static Long extrairIdResponsavel(Tarefa tarefa) {
        if (tarefa == null || tarefa.getResponsavel() == null) {
            return null;
        }
        return tarefa.getResponsavel().getId();
    }

    private static Long extrairIdProjeto(Tarefa tarefa) {
        if (tarefa == null || tarefa.getProjeto() == null) {
            return null;
        }
        return tarefa.getProjeto().getId();
    }

    private static String extrairNomeProjeto(Tarefa tarefa) {
        if (tarefa == null || tarefa.getProjeto() == null) {
            return null;
        }
        return tarefa.getProjeto().getNome();
    }

    private static boolean calcularAtraso(Tarefa tarefa) {
        if (tarefa == null || tarefa.getDataVencimento() == null) {
            return false;
        }
        StatusTarefa statusAtual = tarefa.getStatus();
        if (statusAtual == StatusTarefa.CONCLUIDA || statusAtual == StatusTarefa.CANCELADA) {
            return false;
        }
        return tarefa.getDataVencimento().isBefore(LocalDate.now());
    }

    // ==========================================
    // MÉTODOS DE CONVENIÊNCIA PARA THYMELEAF
    // ==========================================

    public boolean isConcluida() {
        return status == StatusTarefa.CONCLUIDA;
    }

    public boolean isCancelada() {
        return status == StatusTarefa.CANCELADA;
    }

    public boolean isEmAndamento() {
        return status == StatusTarefa.EM_ANDAMENTO;
    }

    public boolean isAFazer() {
        return status == StatusTarefa.A_FAZER;
    }

    public String getStatusDescricao() {
        if (status == null) return "-";
        return switch (status) {
            case A_FAZER -> "A Fazer";
            case EM_ANDAMENTO -> "Em Andamento";
            case CONCLUIDA -> "Concluída";
            case CANCELADA -> "Cancelada";
        };
    }

    public String getPrioridadeDescricao() {
        if (prioridade == null) return "-";
        return switch (prioridade) {
            case BAIXA -> "Baixa";
            case MEDIA -> "Média";
            case ALTA -> "Alta";
            case CRITICA -> "Crítica";
        };
    }

    public String getPrioridadeCssClass() {
        if (prioridade == null) return "bg-secondary";
        return switch (prioridade) {
            case BAIXA -> "bg-success";
            case MEDIA -> "bg-warning text-dark";
            case ALTA, CRITICA -> "bg-danger";
        };
    }

    public String getStatusCssClass() {
        if (status == null) return "bg-secondary";
        return switch (status) {
            case A_FAZER -> "bg-secondary";
            case EM_ANDAMENTO -> "bg-primary";
            case CONCLUIDA -> "bg-success";
            case CANCELADA -> "bg-danger";
        };
    }

    public String getProjetoNomeOuTraco() {
        return projetoNome != null ? projetoNome : "-";
    }

    public String getResponsavelNomeOuTraco() {
        return responsavelNome != null ? responsavelNome : "Não atribuído";
    }
}