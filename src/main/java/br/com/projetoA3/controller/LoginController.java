package br.com.projetoA3.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

/**
 * ⚠️ CONTROLLER DESATIVADO - Conflito com Spring Security
 * 
 * O Spring Security já gerencia automaticamente:
 * - GET /login (exibe formulário)
 * - POST /login (processa autenticação)
 * 
 * Manter este controller causaria conflito de mapeamento.
 */
@Controller
public class LoginController {

    /**
     * ⚠️ Método desativado - Spring Security já fornece /login
     * 
     * Para customizar a página de login, edite apenas:
     * src/main/resources/templates/login.html
     */
    @GetMapping("/login")
    public String login() {
        // Spring Security já redireciona para login.html automaticamente
        return "login";
    }
}