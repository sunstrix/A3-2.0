package br.com.projetoA3.enums;

/**
 * Enum para status de tarefas.
 * Substitui strings mágicas por valores tipados, garantindo consistência
 * no banco de dados, serviços e templates Thymeleaf.
 */
public enum StatusTarefa {
    A_FAZER("A Fazer"),
    EM_ANDAMENTO("Em Andamento"),
    CONCLUIDA("Concluída"),
    CANCELADA("Cancelada");

    private final String descricao;

    StatusTarefa(String descricao) {
        this.descricao = descricao;
    }

    public String getDescricao() {
        return descricao;
    }
}