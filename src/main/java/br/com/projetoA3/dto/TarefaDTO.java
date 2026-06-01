package br.com.projetoA3.dto;

import br.com.projetoA3.model.enums.Prioridade;
import br.com.projetoA3.model.enums.StatusTarefa;

import java.time.LocalDateTime;

public class TarefaDTO {
    
    private Long id;
    private String titulo;
    private String descricao;
    private Prioridade prioridade;
    private StatusTarefa status;
    private LocalDateTime dataCriacao;
    private LocalDateTime dataConclusao;
    private Long responsavelId;
    private String responsavelNome;
    private Long projetoId;
    private String projetoNome;
    
    public TarefaDTO() {
    }
    
    public TarefaDTO(Long id, String titulo, String descricao, Prioridade prioridade, 
                    StatusTarefa status, LocalDateTime dataCriacao, LocalDateTime dataConclusao,
                    Long responsavelId, String responsavelNome, Long projetoId, String projetoNome) {
        this.id = id;
        this.titulo = titulo;
        this.descricao = descricao;
        this.prioridade = prioridade;
        this.status = status;
        this.dataCriacao = dataCriacao;
        this.dataConclusao = dataConclusao;
        this.responsavelId = responsavelId;
        this.responsavelNome = responsavelNome;
        this.projetoId = projetoId;
        this.projetoNome = projetoNome;
    }
    
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    
    public String getTitulo() { return titulo; }
    public void setTitulo(String titulo) { this.titulo = titulo; }
    
    public String getDescricao() { return descricao; }
    public void setDescricao(String descricao) { this.descricao = descricao; }
    
    public Prioridade getPrioridade() { return prioridade; }
    public void setPrioridade(Prioridade prioridade) { this.prioridade = prioridade; }
    
    public StatusTarefa getStatus() { return status; }
    public void setStatus(StatusTarefa status) { this.status = status; }
    
    public LocalDateTime getDataCriacao() { return dataCriacao; }
    public void setDataCriacao(LocalDateTime dataCriacao) { this.dataCriacao = dataCriacao; }
    
    public LocalDateTime getDataConclusao() { return dataConclusao; }
    public void setDataConclusao(LocalDateTime dataConclusao) { this.dataConclusao = dataConclusao; }
    
    public Long getResponsavelId() { return responsavelId; }
    public void setResponsavelId(Long responsavelId) { this.responsavelId = responsavelId; }
    
    public String getResponsavelNome() { return responsavelNome; }
    public void setResponsavelNome(String responsavelNome) { this.responsavelNome = responsavelNome; }
    
    public Long getProjetoId() { return projetoId; }
    public void setProjetoId(Long projetoId) { this.projetoId = projetoId; }
    
    public String getProjetoNome() { return projetoNome; }
    public void setProjetoNome(String projetoNome) { this.projetoNome = projetoNome; }
    
    @Override
    public String toString() {
        return "TarefaDTO{" +
                "id=" + id +
                ", titulo='" + titulo + '\'' +
                ", prioridade=" + prioridade +
                ", status=" + status +
                ", projetoNome='" + projetoNome + '\'' +
                '}';
    }
}
