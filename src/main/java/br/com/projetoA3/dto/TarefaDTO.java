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
 * 
 * Records geram automaticamente accessors no estilo {@code campo()} em vez
 * de {@code getCampo()}, o que é esperado pelo KanbanViewModel e pelos
 * templates Thymeleaf deste projeto.
 * 
 * @param id                 ID da tarefa
 * @param titulo             Título da tarefa
 * @param descricao          Descrição detalhada
 * @param prioridade         Prioridade da tarefa (BAIXA, MEDIA, ALTA, CRITICA)
 * @param status             Status atual (A_FAZER, EM_ANDAMENTO, CONCLUIDA, CANCELADA)
 * @param responsavelNome    Nome do responsável (null se não atribuído)
 * @param responsavelId      ID do responsável (null se não atribuído)
 * @param dataVencimento     Data de vencimento (null se não definida)
 * @param atrasada           Flag indicando se a tarefa está atrasada
 * @param projetoId          ID do projeto ao qual a tarefa pertence
 * @param projetoNome        Nome do projeto (para exibição em listas)
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
     * 
     * Converte uma entidade {@link Tarefa} em um DTO imutável, extraindo
     * apenas os dados necessários para apresentação e calculando campos
     * derivados como {@code atrasada}.
     * 
     * @param tarefa Entidade JPA a ser convertida (pode ser null)
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

    /**
     * Calcula se a tarefa está atrasada com base na data de vencimento e status.
     * 
     * Uma tarefa é considerada atrasada se:
     * - Tem data de vencimento definida
     * - A data de vencimento é anterior à data atual
     * - NÃO está concluída (CONCLUIDA)
     * - NÃO está cancelada (CANCELADA)
     * 
     * @param tarefa Entidade a ser avaliada
     * @return true se a tarefa está atrasada, false caso contrário
     */
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
    // MÉTODOS DE CONVENIÊNCIA
    // ==========================================

    /**
     * Verifica se a tarefa está concluída.
     */
    public boolean isConcluida() {
        return status == StatusTarefa.CONCLUIDA;
    }

    /**
     * Verifica se a tarefa está cancelada.
     */
    public boolean isCancelada() {
        return status == StatusTarefa.CANCELADA;
    }

    /**
     * Verifica se a tarefa está em andamento.
     */
    public boolean isEmAndamento() {
        return status == StatusTarefa.EM_ANDAMENTO;
    }

    /**
     * Verifica se a tarefa está pendente (A Fazer).
     */
    public boolean isAFazer() {
        return status == StatusTarefa.A_FAZER;
    }

    /**
     * Retorna a descrição do status em português.
     */
    public String getStatusDescricao() {
        if (status == null) {
            return "-";
        }
        return switch (status) {
            case A_FAZER -> "A Fazer";
            case EM_ANDAMENTO -> "Em Andamento";
            case CONCLUIDA -> "Concluída";
            case CANCELADA -> "Cancelada";
        };
    }

    /**
     * Retorna a descrição da prioridade em português.
     */
    public String getPrioridadeDescricao() {
        if (prioridade == null) {
            return "-";
        }
        return switch (prioridade) {
            case BAIXA -> "Baixa";
            case MEDIA -> "Média";
            case ALTA -> "Alta";
            case CRITICA -> "Crítica";
        };
    }

    /**
     * Retorna a classe CSS Bootstrap apropriada para o badge de prioridade.
     */
    public String getPrioridadeCssClass() {
        if (prioridade == null) {
            return "bg-secondary";
        }
        return switch (prioridade) {
            case BAIXA -> "bg-success";
            case MEDIA -> "bg-warning text-dark";
            case ALTA, CRITICA -> "bg-danger";
        };
    }

    /**
     * Retorna a classe CSS Bootstrap apropriada para o badge de status.
     */
    public String getStatusCssClass() {
        if (status == null) {
            return "bg-secondary";
        }
        return switch (status) {
            case A_FAZER -> "bg-secondary";
            case EM_ANDAMENTO -> "bg-primary";
            case CONCLUIDA -> "bg-success";
            case CANCELADA -> "bg-danger";
        };
    }

    /**
     * Retorna o nome do projeto ou um placeholder se não houver.
     * Útil para templates Thymeleaf que precisam de fallback.
     */
    public String getProjetoNomeOuTraco() {
        return projetoNome != null ? projetoNome : "-";
    }

    /**
     * Retorna o nome do responsável ou um placeholder se não houver.
     */
    public String getResponsavelNomeOuTraco() {
        return responsavelNome != null ? responsavelNome : "Não atribuído";
    }
}