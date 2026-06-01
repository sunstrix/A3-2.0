package br.com.projetoA3.dto;

import java.time.LocalDate;

/**
 * DTO imutável para Tarefa usando Java Record.
 * Compatível com TarefaMapper, KanbanViewModel e templates Thymeleaf.
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
) {}
