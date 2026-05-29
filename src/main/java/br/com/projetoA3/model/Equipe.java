package br.com.projetoA3.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "equipes")
public class Equipe {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "Nome da equipe é obrigatório")
    @Column(nullable = false)
    private String nome;

    @Column(length = 1000)
    private String descricao;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "lider_id", nullable = false)
    private Usuario lider;

    @OneToMany(mappedBy = "equipe", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<EquipeMembro> membros = new ArrayList<>();

    // Construtores
    public Equipe() {}

    public Equipe(String nome, String descricao, Usuario lider) {
        this.nome = nome;
        this.descricao = descricao;
        this.lider = lider;
    }

    // Getters e Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getNome() { return nome; }
    public void setNome(String nome) { this.nome = nome; }
    public String getDescricao() { return descricao; }
    public void setDescricao(String descricao) { this.descricao = descricao; }
    public Usuario getLider() { return lider; }
    public void setLider(Usuario lider) { this.lider = lider; }
    public List<EquipeMembro> getMembros() { return membros; }
    public void setMembros(List<EquipeMembro> membros) { this.membros = membros; }
}