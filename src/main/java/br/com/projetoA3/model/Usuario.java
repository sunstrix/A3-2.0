package br.com.projetoA3.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

import java.time.LocalDateTime;

@Entity
@Table(name = "usuarios")
public class Usuario {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "Nome e obrigatorio")
    @Column(nullable = false)
    private String nome;

    @NotBlank(message = "CPF e obrigatorio")
    @Column(unique = true, nullable = false)
    @Pattern(regexp = "\\d{11}", message = "CPF deve conter exatamente 11 digitos numericos")
    private String cpf;

    @Email(message = "Email invalido")
    @NotBlank(message = "Email e obrigatorio")
    @Column(unique = true, nullable = false)
    private String email;

    @Column(length = 20)
    private String telefone;

    @NotBlank(message = "Cargo e obrigatorio")
    @Column(nullable = false)
    private String cargo;

    @NotBlank(message = "Login e obrigatorio")
    @Column(unique = true, nullable = false)
    private String login;

    @Column(nullable = false)
    private String senha;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Perfil perfil;

    @Column(nullable = false)
    private Boolean ativo = true;

    @Column(name = "avatar")
    private String avatar; // Nome do arquivo de imagem do avatar

    @Column(name = "notificacoes_email")
    private Boolean notificacoesEmail = true; // Opt-in para receber e-mails de notificacao

    @Column(name = "data_cadastro", nullable = false, updatable = false)
    private LocalDateTime dataCadastro;

    @Column(name = "ultimo_acesso")
    private LocalDateTime ultimoAcesso;

    // Enum de Perfis de Acesso
    public enum Perfil {
        ADMINISTRADOR("Administrador"),
        GERENTE("Gerente"),
        COLABORADOR("Colaborador"),
        ATENDENTE("Atendente");

        private final String descricao;

        Perfil(String descricao) {
            this.descricao = descricao;
        }

        public String getDescricao() {
            return descricao;
        }
    }

    @PrePersist
    protected void onCreate() {
        dataCadastro = LocalDateTime.now();
        if (ativo == null) {
            ativo = true;
        }
        if (notificacoesEmail == null) {
            notificacoesEmail = true;
        }
    }

    // Construtores
    public Usuario() {
    }

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

    public String getTelefone() { return telefone; }
    public void setTelefone(String telefone) { this.telefone = telefone; }

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

    public String getAvatar() { return avatar; }
    public void setAvatar(String avatar) { this.avatar = avatar; }

    public Boolean getNotificacoesEmail() { return notificacoesEmail; }
    public void setNotificacoesEmail(Boolean notificacoesEmail) { this.notificacoesEmail = notificacoesEmail; }

    public LocalDateTime getDataCadastro() { return dataCadastro; }
    public void setDataCadastro(LocalDateTime dataCadastro) { this.dataCadastro = dataCadastro; }

    public LocalDateTime getUltimoAcesso() { return ultimoAcesso; }
    public void setUltimoAcesso(LocalDateTime ultimoAcesso) { this.ultimoAcesso = ultimoAcesso; }

    /**
     * Verifica se o usuario possui perfil de administrador.
     */
    public boolean isAdministrador() {
        return Perfil.ADMINISTRADOR.equals(this.perfil);
    }

    /**
     * Verifica se o usuario possui perfil de gerente.
     */
    public boolean isGerente() {
        return Perfil.GERENTE.equals(this.perfil);
    }

    /**
     * Verifica se o usuario possui perfil de atendente.
     */
    public boolean isAtendente() {
        return Perfil.ATENDENTE.equals(this.perfil);
    }

    /**
     * Retorna o nome completo formatado para exibicao.
     */
    public String getNomeExibicao() {
        return nome != null ? nome : login;
    }

    /**
     * Retorna as iniciais do nome para avatares genericos.
     */
    public String getIniciais() {
        if (nome == null || nome.isBlank()) return "?";
        String[] partes = nome.trim().split("\\s+");
        if (partes.length == 1) {
            return partes[0].substring(0, 1).toUpperCase();
        }
        return (partes[0].substring(0, 1) + partes[partes.length - 1].substring(0, 1)).toUpperCase();
    }
}