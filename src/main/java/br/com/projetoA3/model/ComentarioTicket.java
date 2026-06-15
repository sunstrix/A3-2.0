package br.com.projetoA3.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDateTime;

/**
 * Entidade que representa um comentario (interacao/resposta) dentro de um Ticket.
 * Permite o acompanhamento detalhado do historico de resolucao do chamado.
 */
@Entity
@Table(name = "comentarios_ticket")
public class ComentarioTicket {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "{jakarta.validation.constraints.NotBlank.message}")
    @Column(columnDefinition = "TEXT", nullable = false)
    private String texto;

    @Column(name = "data_criacao", nullable = false, updatable = false)
    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime dataCriacao;

    // Indica se o comentario e uma nota interna (visivel apenas para atendentes/gerentes)
    // ou uma resposta publica (visivel para o solicitante).
    @Column(name = "nota_interna", nullable = false)
    private boolean notaInterna = false;

    // Relacionamento com o Ticket pai
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_ticket", nullable = false)
    private Ticket ticket;

    // Relacionamento com o Usuario que registrou o comentario
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_autor", nullable = false)
    private Usuario autor;

    @PrePersist
    protected void onCreate() {
        dataCriacao = LocalDateTime.now();
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

    public String getTexto() {
        return texto;
    }

    public void setTexto(String texto) {
        this.texto = texto;
    }

    public LocalDateTime getDataCriacao() {
        return dataCriacao;
    }

    public void setDataCriacao(LocalDateTime dataCriacao) {
        this.dataCriacao = dataCriacao;
    }

    public boolean isNotaInterna() {
        return notaInterna;
    }

    public void setNotaInterna(boolean notaInterna) {
        this.notaInterna = notaInterna;
    }

    public Ticket getTicket() {
        return ticket;
    }

    public void setTicket(Ticket ticket) {
        this.ticket = ticket;
    }

    public Usuario getAutor() {
        return autor;
    }

    public void setAutor(Usuario autor) {
        this.autor = autor;
    }
}