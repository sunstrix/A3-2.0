package br.com.projetoA3.dto;

/**
 * Data Transfer Object (DTO) para Usuário.
 * 
 * Este Record imutável encapsula os dados do Usuário para exibição nas
 * views e APIs. Omite intencionalmente o campo "senha" para evitar 
 * vazamento de hashes (BCrypt) ou credenciais sensíveis no frontend.
 * 
 * @param id ID do usuário
 * @param nome Nome completo
 * @param login Login de acesso (username)
 * @param email E-mail do usuário
 * @param cpf CPF formatado
 * @param cargo Cargo na empresa
 * @param perfil Perfil de acesso (ADMINISTRADOR, GERENTE, COLABORADOR)
 * @param ativo Status da conta (true = ativa, false = inativa/bloqueada)
 */
public record UsuarioDTO(
    Long id,
    String nome,
    String login,
    String email,
    String cpf,
    String cargo,
    String perfil,
    boolean ativo
) {
    
    /**
     * Verifica se o usuário tem perfil de Administrador
     */
    public boolean isAdmin() {
        return "ADMINISTRADOR".equals(perfil);
    }
    
    /**
     * Verifica se o usuário tem perfil de Gerente
     */
    public boolean isGerente() {
        return "GERENTE".equals(perfil);
    }
    
    /**
     * Verifica se o usuário tem perfil de Colaborador
     */
    public boolean isColaborador() {
        return "COLABORADOR".equals(perfil);
    }
    
    /**
     * Gera as iniciais do nome para exibição em avatares.
     * Ex: "João Silva" -> "JS", "Maria" -> "M"
     */
    public String getInitials() {
        if (nome == null || nome.trim().isEmpty()) return "?";
        
        String[] parts = nome.trim().split("\\s+");
        if (parts.length == 1) {
            return parts[0].substring(0, 1).toUpperCase();
        }
        return (parts[0].substring(0, 1) + parts[parts.length - 1].substring(0, 1)).toUpperCase();
    }
    
    /**
     * Retorna o nome do perfil formatado para exibição
     */
    public String getPerfilDescricao() {
        return switch (perfil) {
            case "ADMINISTRADOR" -> "Administrador";
            case "GERENTE" -> "Gerente";
            case "COLABORADOR" -> "Colaborador";
            default -> perfil;
        };
    }
}