package br.com.projetoA3.dto;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;

/**
 * Data Transfer Object (DTO) para Projeto.
 * 
 * Este Record imutável encapsula os dados do Projeto para exibição nas
 * views Thymeleaf e APIs, evitando o vazamento de informações sensíveis
 * da entidade JPA (como senhas de usuários associados) e problemas de
 * LazyInitializationException.
 * 
 * @param id ID do projeto
 * @param nome Nome do projeto
 * @param descricao Descrição detalhada
 * @param status Status atual (PLANEJAMENTO, EM_ANDAMENTO, CONCLUIDO, CANCELADO)
 * @param dataInicio Data de início do projeto
 * @param dataTerminoPrevista Data prevista para término
 * @param equipeNome Nome da equipe responsável (null se não atribuída)
 * @param equipeId ID da equipe responsável (null se não atribuída)
 * @param gerenteNome Nome do gerente do projeto (null se não atribuído)
 */
public record ProjetoDTO(
    Long id,
    String nome,
    String descricao,
    String status,
    LocalDate dataInicio,
    LocalDate dataTerminoPrevista,
    String equipeNome,
    Long equipeId,
    String gerenteNome
) {
    
    /**
     * Verifica se o projeto está concluído
     */
    public boolean isConcluido() {
        return "CONCLUIDO".equals(status);
    }
    
    /**
     * Verifica se o projeto está cancelado
     */
    public boolean isCancelado() {
        return "CANCELADO".equals(status);
    }
    
    /**
     * Verifica se o projeto está atrasado (data prevista já passou e não está concluído/cancelado)
     */
    public boolean isAtrasado() {
        if (dataTerminoPrevista == null || isConcluido() || isCancelado()) {
            return false;
        }
        return dataTerminoPrevista.isBefore(LocalDate.now());
    }
    
    /**
     * Calcula quantos dias faltam para o término (ou quantos dias está atrasado)
     * Retorna null se não houver data prevista.
     */
    public Long getDiasRestantes() {
        if (dataTerminoPrevista == null) return null;
        return ChronoUnit.DAYS.between(LocalDate.now(), dataTerminoPrevista);
    }
    
    /**
     * Retorna a descrição amigável do status
     */
    public String getStatusDescricao() {
        return switch (status) {
            case "PLANEJAMENTO" -> "Planejamento";
            case "EM_ANDAMENTO" -> "Em Andamento";
            case "CONCLUIDO" -> "Concluído";
            case "CANCELADO" -> "Cancelado";
            default -> status;
        };
    }
    
    /**
     * Retorna a cor do badge Bootstrap baseada no status
     */
    public String getStatusCorBadge() {
        return switch (status) {
            case "PLANEJAMENTO" -> "bg-secondary";
            case "EM_ANDAMENTO" -> "bg-primary";
            case "CONCLUIDO" -> "bg-success";
            case "CANCELADO" -> "bg-danger";
            default -> "bg-light text-dark";
        };
    }
}