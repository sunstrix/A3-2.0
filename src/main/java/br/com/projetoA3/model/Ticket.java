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
 * Expandida com campos de SLA, origem, resolucao e anexos.
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
    private String status; // ABERTO, EM_ANDAMENTO, AGUARDANDO_USUARIO, RESOLVIDO, FECHADO, REABERTO

    @NotNull(message = "{jakarta.validation.constraints.NotNull.message}")
    @Column(nullable = false, length = 20)
    private String prioridade; // BAIXA, MEDIA, ALTA, CRITICA

    @Column(length = 50)
    private String categoria; // HARDWARE, SOFTWARE, REDE, ACESSO, GERAL

    @Column(length = 30)
    private String origem; // WEB, EMAIL, TELEFONE, PRESENCIAL

    @Column(name = "data_criacao", nullable = false, updatable = false)
    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime dataCriacao;

    @Column(name = "data_atualizacao")
    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime dataAtualizacao;

    @Column(name = "data_fechamento")
    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime dataFechamento;

    @Column(name = "data_resolucao")
    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime dataResolucao;

    @Column(name = "sla_vencimento")
    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime slaVencimento; // Prazo maximo de resolucao baseado na prioridade

    @Column(name = "sla_cumprido")
    private Boolean slaCumprido; // true = resolvido dentro do prazo

    @Column(name = "resolucao", columnDefinition = "TEXT")
    private String resolucao; // Descricao da solucao aplicada (preenchido no fechamento)

    @Column(name = "avaliacao_solicitante")
    private Integer avaliacaoSolicitante; // Nota de 1 a 5 dada pelo solicitante

    @Column(name = "feedback_solicitante", columnDefinition = "TEXT")
    private String feedbackSolicitante; // Comentario do solicitante sobre o atendimento

    // Relacionamento com o usuario que abriu o ticket
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_solicitante", nullable = false)
    private Usuario solicitante;

    // Relacionamento com o atendente responsavel (pode ser nulo se nao atribuido)
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_atendente")
    private Usuario atendente;

    // Historico de comentarios do ticket
    @OneToMany(mappedBy = "ticket", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    private List<<ComentarioTicket> comentarios = new ArrayList<>();

    // Anexos do ticket (nomes dos arquivos armazenados no disco)
    @ElementCollection
    @CollectionTable(name = "ticket_anexos", joinColumns = @JoinColumn(name = "ticket_id"))
    @Column(name = "nome_arquivo")
    private List<String> anexos = new ArrayList<>();

    @PrePersist
    protected void onCreate() {
        dataCriacao = LocalDateTime.now();
        dataAtualizacao = LocalDateTime.now();
        if (status == null || status.trim().isEmpty()) {
            status = "ABERTO";
        }
        // Calcula o SLA com base na prioridade
        calcularSla();
    }

    @PreUpdate
    protected void onUpdate() {
        dataAtualizacao = LocalDateTime.now();
        // Verifica se o SLA foi cumprido ao resolver/fechar
        if (("RESOLVIDO".equals(status) || "FECHADO".equals(status)) && slaCumprido == null) {
            dataResolucao = LocalDateTime.now();
            slaCumprido = (slaVencimento == null) || !dataResolucao.isAfter(slaVencimento);
        }
    }

    /**
     * Calcula o prazo de SLA com base na prioridade do ticket.
     * BAIXA: 72h | MEDIA: 24h | ALTA: 8h | CRITICA: 2h
     */
    private void calcularSla() {
        if (prioridade == null) {
            slaVencimento = dataCriacao.plusHours(72);
            return;
        }
        switch (prioridade.toUpperCase()) {
            case "BAIXA":
                slaVencimento = dataCriacao.plusHours(72);
                break;
            case "MEDIA":
                slaVencimento = dataCriacao.plusHours(24);
                break;
            case "ALTA":
                slaVencimento = dataCriacao.plusHours(8);
                break;
            case "CRITICA":
                slaVencimento = dataCriacao.plusHours(2);
                break;
            default:
                slaVencimento = dataCriacao.plusHours(72);
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
        // Recalcula o SLA quando a prioridade muda
        if (dataCriacao != null) {
            calcularSla();
        }
    }

    public String getCategoria() {
        return categoria;
    }

    public void setCategoria(String categoria) {
        this.categoria = categoria;
    }

    public String getOrigem() {
        return origem;
    }

    public void setOrigem(String origem) {
        this.origem = origem;
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

    public LocalDateTime getDataResolucao() {
        return dataResolucao;
    }

    public void setDataResolucao(LocalDateTime dataResolucao) {
        this.dataResolucao = dataResolucao;
    }

    public LocalDateTime getSlaVencimento() {
        return slaVencimento;
    }

    public void setSlaVencimento(LocalDateTime slaVencimento) {
        this.slaVencimento = slaVencimento;
    }

    public Boolean getSlaCumprido() {
        return slaCumprido;
    }

    public void setSlaCumprido(Boolean slaCumprido) {
        this.slaCumprido = slaCumprido;
    }

    public String getResolucao() {
        return resolucao;
    }

    public void setResolucao(String resolucao) {
        this.resolucao = resolucao;
    }

    public Integer getAvaliacaoSolicitante() {
        return avaliacaoSolicitante;
    }

    public void setAvaliacaoSolicitante(Integer avaliacaoSolicitante) {
        this.avaliacaoSolicitante = avaliacaoSolicitante;
    }

    public String getFeedbackSolicitante() {
        return feedbackSolicitante;
    }

    public void setFeedbackSolicitante(String feedbackSolicitante) {
        this.feedbackSolicitante = feedbackSolicitante;
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

    public List<<ComentarioTicket> getComentarios() {
        return comentarios;
    }

    public void setComentarios(List<<ComentarioTicket> comentarios) {
        this.comentarios = comentarios;
    }

    public void addComentario(ComentarioTicket comentario) {
        this.comentarios.add(comentario);
        comentario.setTicket(this);
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
     * Verifica se o ticket esta atrasado em relacao ao SLA.
     */
    public boolean isSlaAtrasado() {
        return slaVencimento != null && LocalDateTime.now().isAfter(slaVencimento) && !"FECHADO".equals(status) && !"RESOLVIDO".equals(status);
    }

    /**
     * Retorna o tempo decorrido desde a abertura do ticket em formato legivel.
     */
    public String getTempoDecorrido() {
        if (dataCriacao == null) return "-";
        LocalDateTime referencia = dataFechamento != null ? dataFechamento : LocalDateTime.now();
        long horas = java.time.Duration.between(dataCriacao, referencia).toHours();
        if (horas < 24) {
            return horas + "h";
        }
        long dias = horas / 24;
        long restoHoras = horas % 24;
        return dias + "d " + restoHoras + "h";
    }
}