package br.com.projetoA3.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Entidade que representa um Ticket (Chamado) no modulo de Help Desk.
 * Preserva a integracao com o sistema de usuarios ja existente no projeto.
 */
@Entity
@Table(name = "tickets")
public class Ticket {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "{jakarta.validation.constraints.NotBlank.message}")
    @Column(nullable = false, length = 150)
    private String titulo;

    @NotBlank(message = "{jakarta.validation.constraints.NotBlank.message}")
    @Column(columnDefinition = "TEXT", nullable = false)
    private String descricao;

    @NotNull(message = "{jakarta.validation.constraints.NotNull.message}")
    @Column(nullable = false, length = 20)
    private String status; // ABERTO, EM_ANDAMENTO, RESOLVIDO, FECHADO

    @NotNull(message = "{jakarta.validation.constraints.NotNull.message}")
    @Column(nullable = false, length = 20)
    private String prioridade; // BAIXA, MEDIA, ALTA, URGENTE

    @Column(length = 50)
    private String categoria;

    @Column(name = "data_criacao", nullable = false, updatable = false)
    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime dataCriacao;

    @Column(name = "data_atualizacao")
    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime dataAtualizacao;

    @Column(name = "data_fechamento")
    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime dataFechamento;

    // Relacionamento com o usuario que abriu o ticket
    // NOTA: Assumindo que a classe de usuario do projeto original se chama 'Usuario'. 
    // Caso seja 'User', altere o tipo abaixo.
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_solicitante", nullable = false)
    private Usuario solicitante;

    // Relacionamento com o atendente responsavel (pode ser nulo se nao atribuido)
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_atendente")
    private Usuario atendente;

    // Historico de comentarios do ticket
    @OneToMany(mappedBy = "ticket", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    private List<ComentarioTicket> comentarios = new ArrayList<>();

    @PrePersist
    protected void onCreate() {
        dataCriacao = LocalDateTime.now();
        dataAtualizacao = LocalDateTime.now();
        if (status == null || status.trim().isEmpty()) {
            status = "ABERTO";
        }
    }

    @PreUpdate
    protected void onUpdate() {
        dataAtualizacao = LocalDateTime.now();
    }

    // ==========================================
    // Getters e Setters
    // ==========================================

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getTitulo() {
        return titulo;
    }

    public void setTitulo(String titulo) {
        this.titulo = titulo;
    }

    public String getDescricao() {
        return descricao;
    }

    public void setDescricao(String descricao) {
        this.descricao = descricao;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getPrioridade() {
        return prioridade;
    }

    public void setPrioridade(String prioridade) {
        this.prioridade = prioridade;
    }

    public String getCategoria() {
        return categoria;
    }

    public void setCategoria(String categoria) {
        this.categoria = categoria;
    }

    public LocalDateTime getDataCriacao() {
        return dataCriacao;
    }

    public void setDataCriacao(LocalDateTime dataCriacao) {
        this.dataCriacao = dataCriacao;
    }

    public LocalDateTime getDataAtualizacao() {
        return dataAtualizacao;
    }

    public void setDataAtualizacao(LocalDateTime dataAtualizacao) {
        this.dataAtualizacao = dataAtualizacao;
    }

    public LocalDateTime getDataFechamento() {
        return dataFechamento;
    }

    public void setDataFechamento(LocalDateTime dataFechamento) {
        this.dataFechamento = dataFechamento;
    }

    public Usuario getSolicitante() {
        return solicitante;
    }

    public void setSolicitante(Usuario solicitante) {
        this.solicitante = solicitante;
    }

    public Usuario getAtendente() {
        return atendente;
    }

    public void setAtendente(Usuario atendente) {
        this.atendente = atendente;
    }

    public List<ComentarioTicket> getComentarios() {
        return comentarios;
    }

    public void setComentarios(List<ComentarioTicket> comentarios) {
        this.comentarios = comentarios;
    }
    
    public void addComentario(ComentarioTicket comentario) {
        this.comentarios.add(comentario);
        comentario.setTicket(this);
    }
}