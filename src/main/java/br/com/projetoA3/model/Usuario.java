package br.com.projetoA3.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

@Entity
@Table(name = "usuarios")
public class Usuario {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "Nome é obrigatório")
    @Column(nullable = false)
    private String nome;

    @NotBlank(message = "CPF é obrigatório")
    @Column(unique = true, nullable = false)
    @Pattern(regexp = "\\d{11}", message = "CPF deve conter 11 dígitos")
    private String cpf;

    @Email(message = "Email inválido")
    @NotBlank(message = "Email é obrigatório")
    @Column(unique = true, nullable = false)
    private String email;

    @NotBlank(message = "Cargo é obrigatório")
    @Column(nullable = false)
    private String cargo;

    @NotBlank(message = "Login é obrigatório")
    @Column(unique = true, nullable = false)
    private String login;

    @NotBlank(message = "Senha é obrigatória")
    @Column(nullable = false)
    private String senha;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Perfil perfil;

    @Column(nullable = false)
    private Boolean ativo = true;

    // Enum Perfis
    public enum Perfil {
        ADMINISTRADOR("Administrador"),
        GERENTE("Gerente"),
        COLABORADOR("Colaborador");

        private final String descricao;

        Perfil(String descricao) {
            this.descricao = descricao;
        }

        public String getDescricao() {
            return descricao;
        }
    }

    // Construtores
    public Usuario() {}

    public Usuario(String nome, String cpf, String email, String cargo, String login, String senha, Perfil perfil) {
        this.nome = nome;
        this.cpf = cpf;
        this.email = email;
        this.cargo = cargo;
        this.login = login;
        this.senha = senha;
        this.perfil = perfil;
    }

    // Getters e Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getNome() { return nome; }
    public void setNome(String nome) { this.nome = nome; }
    public String getCpf() { return cpf; }
    public void setCpf(String cpf) { this.cpf = cpf; }
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
    public String getCargo() { return cargo; }
    public void setCargo(String cargo) { this.cargo = cargo; }
    public String getLogin() { return login; }
    public void setLogin(String login) { this.login = login; }
    public String getSenha() { return senha; }
    public void setSenha(String senha) { this.senha = senha; }
    public Perfil getPerfil() { return perfil; }
    public void setPerfil(Perfil perfil) { this.perfil = perfil; }
    public Boolean getAtivo() { return ativo; }
    public void setAtivo(Boolean ativo) { this.ativo = ativo; }
}