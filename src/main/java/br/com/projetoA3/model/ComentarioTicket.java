package br.com.projetoA3.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Entidade que representa um comentario (interacao/resposta) dentro de um Ticket.
 * Permite o acompanhamento detalhado do historico de resolucao do chamado.
 * Suporta anexos, notas internas e rastreamento de notificacoes por e-mail.
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

    // Indica se o autor do comentario foi notificado por e-mail sobre esta interacao.
    // Util para rastrear o envio de notificacoes no historico.
    @Column(name = "email_notificado")
    private Boolean emailNotificado = false;

    // Relacionamento com o Ticket pai
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_ticket", nullable = false)
    private Ticket ticket;

    // Relacionamento com o Usuario que registrou o comentario
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_autor", nullable = false)
    private Usuario autor;

    // Anexos do comentario (nomes dos arquivos armazenados no disco)
    @ElementCollection
    @CollectionTable(name = "comentario_anexos", joinColumns = @JoinColumn(name = "comentario_id"))
    @Column(name = "nome_arquivo")
    private List<String> anexos = new ArrayList<>();

    @PrePersist
    protected void onCreate() {
        dataCriacao = LocalDateTime.now();
        if (emailNotificado == null) {
            emailNotificado = false;
        }
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

    public Boolean getEmailNotificado() {
        return emailNotificado;
    }

    public void setEmailNotificado(Boolean emailNotificado) {
        this.emailNotificado = emailNotificado;
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

    public List<String> getAnexos() {
        return anexos;
    }

    public void setAnexos(List<String> anexos) {
        this.anexos = anexos;
    }

    public void addAnexo(String nomeArquivo) {
        this.anexos.add(nomeArquivo);
    }

    public void removeAnexo(String nomeArquivo) {
        this.anexos.remove(nomeArquivo);
    }

    /**
     * Retorna o texto resumido para exibicao em listas (maximo 100 caracteres).
     */
    public String getTextoResumido() {
        if (texto == null) return "";
        return texto.length() > 100 ? texto.substring(0, 100) + "..." : texto;
    }

    /**
     * Retorna a data de criacao formatada para exibicao em templates.
     */
    public String getDataFormatada() {
        if (dataCriacao == null) return "-";
        return java.time.format.DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm").format(dataCriacao);
    }

    /**
     * Verifica se o comentario possui anexos.
     */
    public boolean possuiAnexos() {
        return anexos != null && !anexos.isEmpty();
    }
}