package br.com.projetoA3.dto;

import br.com.projetoA3.enums.Prioridade;
import br.com.projetoA3.enums.StatusTarefa;
import br.com.projetoA3.model.Projeto;
import br.com.projetoA3.model.Tarefa;
import br.com.projetoA3.model.Usuario;

import java.time.LocalDate;

/**
 * DTO (Data Transfer Object) para Tarefa.
 * Utilizado para transferir dados entre camadas sem expor a entidade JPA diretamente.
 */
public class TarefaDTO {
    
    private Long id;
    private String titulo;
    private String descricao;
    private Projeto projeto;
    private Usuario responsavel;
    private String responsavelNome;
    private Prioridade prioridade;
    private StatusTarefa status;
    private LocalDate dataVencimento;
    private boolean atrasada;
    
    // Construtores
    public TarefaDTO() {
    }
    
    public TarefaDTO(Tarefa tarefa) {
        this.id = tarefa.getId();
        this.titulo = tarefa.getTitulo();
        this.descricao = tarefa.getDescricao();
        this.projeto = tarefa.getProjeto();
        this.responsavel = tarefa.getResponsavel();
        this.responsavelNome = (tarefa.getResponsavel() != null) 
                ? tarefa.getResponsavel().getNome() 
                : null;
        this.prioridade = tarefa.getPrioridade();
        this.status = tarefa.getStatus();
        this.dataVencimento = tarefa.getDataVencimento();
        this.atrasada = calcularAtraso(tarefa.getDataVencimento(), tarefa.getStatus());
    }
    
    // Métodos auxiliares
    private boolean calcularAtraso(LocalDate dataVencimento, StatusTarefa status) {
        if (dataVencimento == null || status == StatusTarefa.CONCLUIDA || status == StatusTarefa.CANCELADA) {
            return false;
        }
        return dataVencimento.isBefore(LocalDate.now());
    }
    
    // Getters e Setters
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
    
    public Projeto getProjeto() {
        return projeto;
    }
    
    public void setProjeto(Projeto projeto) {
        this.projeto = projeto;
    }
    
    public Usuario getResponsavel() {
        return responsavel;
    }
    
    public void setResponsavel(Usuario responsavel) {
        this.responsavel = responsavel;
        this.responsavelNome = (responsavel != null) ? responsavel.getNome() : null;
    }
    
    public String getResponsavelNome() {
        return responsavelNome;
    }
    
    public void setResponsavelNome(String responsavelNome) {
        this.responsavelNome = responsavelNome;
    }
    
    public Prioridade getPrioridade() {
        return prioridade;
    }
    
    public void setPrioridade(Prioridade prioridade) {
        this.prioridade = prioridade;
    }
    
    public StatusTarefa getStatus() {
        return status;
    }
    
    public void setStatus(StatusTarefa status) {
        this.status = status;
    }
    
    public LocalDate getDataVencimento() {
        return dataVencimento;
    }
    
    public void setDataVencimento(LocalDate dataVencimento) {
        this.dataVencimento = dataVencimento;
        this.atrasada = calcularAtraso(dataVencimento, this.status);
    }
    
    public boolean isAtrasada() {
        return atrasada;
    }
    
    public void setAtrasada(boolean atrasada) {
        this.atrasada = atrasada;
    }
    
    @Override
    public String toString() {
        return "TarefaDTO{" +
                "id=" + id +
                ", titulo='" + titulo + '\'' +
                ", prioridade=" + prioridade +
                ", status=" + status +
                ", responsavelNome='" + responsavelNome + '\'' +
                '}';
    }
}