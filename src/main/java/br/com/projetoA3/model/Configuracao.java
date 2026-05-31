package br.com.projetoA3.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;

@Entity
@Table(name = "configuracoes")
public class Configuracao {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "Host do servidor é obrigatório")
    @Column(nullable = false, unique = true)
    private String chave; // Ex: "EMAIL_HOST", "EMAIL_PORT", etc.

    @NotBlank(message = "Valor é obrigatório")
    @Column(nullable = false, columnDefinition = "TEXT")
    private String valor;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String descricao;

    // Construtores
    public Configuracao() {}

    public Configuracao(String chave, String valor, String descricao) {
        this.chave = chave;
        this.valor = valor;
        this.descricao = descricao;
    }

    // Getters e Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getChave() { return chave; }
    public void setChave(String chave) { this.chave = chave; }
    public String getValor() { return valor; }
    public void setValor(String valor) { this.valor = valor; }
    public String getDescricao() { return descricao; }
    public void setDescricao(String descricao) { this.descricao = descricao; }
}