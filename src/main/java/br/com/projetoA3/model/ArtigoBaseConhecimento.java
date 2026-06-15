package br.com.projetoA3.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDateTime;

/**
 * Entidade que representa um artigo da Base de Conhecimento (FAQ/Documentacao).
 * Utilizado para armazenar solucoes conhecidas, tutoriais e manuais do sistema.
 */
@Entity
@Table(name = "artigos_base_conhecimento")
public class ArtigoBaseConhecimento {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "{jakarta.validation.constraints.NotBlank.message}")
    @Column(nullable = false, length = 200)
    private String titulo;

    @NotBlank(message = "{jakarta.validation.constraints.NotBlank.message}")
    @Column(columnDefinition = "TEXT", nullable = false)
    private String conteudo;

    @Column(length = 100)
    private String categoria;

    // Tags separadas por virgula para facilitar buscas simples no frontend
    @Column(length = 255)
    private String tags;

    @Column(name = "data_criacao", nullable = false, updatable = false)
    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime dataCriacao;

    @Column(name = "data_atualizacao")
    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime dataAtualizacao;

    // Controla se o artigo esta visivel para os usuarios/clientes
    @Column(nullable = false)
    private boolean ativo = true;

    // Contador de acessos para metricas de utilidade do artigo
    @Column(nullable = false)
    private int visualizacoes = 0;

    // Relacionamento com o Usuario que criou o artigo
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_autor", nullable = false)
    private Usuario autor;

    @PrePersist
    protected void onCreate() {
        dataCriacao = LocalDateTime.now();
        dataAtualizacao = LocalDateTime.now();
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

    public String getConteudo() {
        return conteudo;
    }

    public void setConteudo(String conteudo) {
        this.conteudo = conteudo;
    }

    public String getCategoria() {
        return categoria;
    }

    public void setCategoria(String categoria) {
        this.categoria = categoria;
    }

    public String getTags() {
        return tags;
    }

    public void setTags(String tags) {
        this.tags = tags;
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

    public boolean isAtivo() {
        return ativo;
    }

    public void setAtivo(boolean ativo) {
        this.ativo = ativo;
    }

    public int getVisualizacoes() {
        return visualizacoes;
    }

    public void setVisualizacoes(int visualizacoes) {
        this.visualizacoes = visualizacoes;
    }

    public void incrementarVisualizacoes() {
        this.visualizacoes++;
    }

    public Usuario getAutor() {
        return autor;
    }

    public void setAutor(Usuario autor) {
        this.autor = autor;
    }
}