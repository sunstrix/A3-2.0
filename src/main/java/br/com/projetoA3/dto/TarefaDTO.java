package br.com.projetoA3.dto;

import java.time.LocalDate;

/**
 * Data Transfer Object (DTO) para Tarefa.
 * 
 * Este Record imutável desacopla a entidade JPA Tarefa das views Thymeleaf,
 * evitando problemas de serialização com associações lazy e permitindo
 * evolução independente da API.
 * 
 * @param id ID da tarefa
 * @param titulo Título da tarefa
 * @param descricao Descrição detalhada
 * @param status Status atual (A_FAZER, EM_ANDAMENTO, CONCLUIDA, CANCELADA)
 * @param prioridade Prioridade da tarefa (BAIXA, MEDIA, ALTA, CRITICA)
 * @param responsavelNome Nome do responsável (null se não atribuído)
 * @param responsavelId ID do responsável (null se não atribuído)
 * @param dataVencimento Data de vencimento (null se não definida)
 * @param atrasada Flag indicando se a tarefa está atrasada
 */
public record TarefaDTO(
    Long id,
    String titulo,
    String descricao,
    String status,
    String prioridade,
    String responsavelNome,
    Long responsavelId,
    LocalDate dataVencimento,
    boolean atrasada
) {
    /**
     * Verifica se a tarefa está concluída
     */
    public boolean isConcluida() {
        return "CONCLUIDA".equals(status);
    }
    
    /**
     * Verifica se a tarefa está cancelada
     */
    public boolean isCancelada() {
        return "CANCELADA".equals(status);
    }
    
    /**
     * Retorna a descrição do status em português
     */
    public String getStatusDescricao() {
        return switch (status) {
            case "A_FAZER" -> "A Fazer";
            case "EM_ANDAMENTO" -> "Em Andamento";
            case "CONCLUIDA" -> "Concluída";
            case "CANCELADA" -> "Cancelada";
            default -> status;
        };
    }
    
    /**
     * Retorna a descrição da prioridade em português
     */
    public String getPrioridadeDescricao() {
        return switch (prioridade) {
            case "BAIXA" -> "Baixa";
            case "MEDIA" -> "Média";
            case "ALTA" -> "Alta";
            case "CRITICA" -> "Crítica";
            default -> prioridade;
        };
    }
}