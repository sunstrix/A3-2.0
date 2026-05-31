package br.com.projetoA3.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDate;

@Entity
@Table(name = "tarefas")
public class Tarefa {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "Título é obrigatório")
    private String titulo;

    @Column(length = 1000)
    private String descricao;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private StatusTarefa status = StatusTarefa.A_FAZER;

    // ✅ NOVO: Prioridade da tarefa
    @Enumerated(EnumType.STRING)
    private PrioridadeTarefa prioridade = PrioridadeTarefa.MEDIA;

    // ✅ NOVO: Data de vencimento para destaque visual
    private LocalDate dataVencimento;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "projeto_id", nullable = false)
    @NotNull(message = "Projeto é obrigatório")
    private Projeto projeto;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "responsavel_id")
    private Usuario responsavel;

    // Construtores
    public Tarefa() {}

    public Tarefa(String titulo, String descricao, StatusTarefa status) {
        this.titulo = titulo;
        this.descricao = descricao;
        this.status = status;
    }

    // Getters e Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getTitulo() { return titulo; }
    public void setTitulo(String titulo) { this.titulo = titulo; }
    public String getDescricao() { return descricao; }
    public void setDescricao(String descricao) { this.descricao = descricao; }
    public StatusTarefa getStatus() { return status; }
    public void setStatus(StatusTarefa status) { this.status = status; }
    public PrioridadeTarefa getPrioridade() { return prioridade; }
    public void setPrioridade(PrioridadeTarefa prioridade) { this.prioridade = prioridade; }
    public LocalDate getDataVencimento() { return dataVencimento; }
    public void setDataVencimento(LocalDate dataVencimento) { this.dataVencimento = dataVencimento; }
    public Projeto getProjeto() { return projeto; }
    public void setProjeto(Projeto projeto) { this.projeto = projeto; }
    public Usuario getResponsavel() { return responsavel; }
    public void setResponsavel(Usuario responsavel) { this.responsavel = responsavel; }

    // Enums
    public enum StatusTarefa {
        A_FAZER("A Fazer"),
        EM_PROGRESSO("Em Progresso"),
        CONCLUIDA("Concluída");

        private final String descricao;
        StatusTarefa(String descricao) { this.descricao = descricao; }
        public String getDescricao() { return descricao; }
    }

    // ✅ NOVO: Enum para prioridade
    public enum PrioridadeTarefa {
        BAIXA("Baixa"),
        MEDIA("Média"),
        ALTA("Alta");

        private final String descricao;
        PrioridadeTarefa(String descricao) { this.descricao = descricao; }
        public String getDescricao() { return descricao; }
    }
}