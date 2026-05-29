package br.com.projetoA3.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;

@Entity
@Table(name = "tarefas")
public class Tarefa {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "Título da tarefa é obrigatório")
    @Column(nullable = false)
    private String titulo;

    @Column(length = 1000)
    private String descricao;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private StatusTarefa status = StatusTarefa.A_FAZER;

    // ✅ Relacionamento N:1 com Projeto
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "projeto_id", nullable = false)
    private Projeto projeto;

    // ✅ Relacionamento N:1 com Usuario (responsável)
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "responsavel_id")
    private Usuario responsavel;

    // Enum de Status da Tarefa (Kanban)
    public enum StatusTarefa {
        A_FAZER("A Fazer"),
        EM_PROGRESSO("Em Progresso"),
        CONCLUIDA("Concluída");

        private final String descricao;

        StatusTarefa(String descricao) {
            this.descricao = descricao;
        }

        public String getDescricao() {
            return descricao;
        }
    }

    // Construtores
    public Tarefa() {}

    public Tarefa(String titulo, String descricao, Projeto projeto, Usuario responsavel) {
        this.titulo = titulo;
        this.descricao = descricao;
        this.projeto = projeto;
        this.responsavel = responsavel;
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
    public Projeto getProjeto() { return projeto; }
    public void setProjeto(Projeto projeto) { this.projeto = projeto; }
    public Usuario getResponsavel() { return responsavel; }
    public void setResponsavel(Usuario responsavel) { this.responsavel = responsavel; }
}