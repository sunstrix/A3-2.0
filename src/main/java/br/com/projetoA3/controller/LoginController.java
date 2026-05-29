package br.com.projetoA3.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class LoginController {

    // ✅ Rota raiz redireciona automaticamente para o login
    @GetMapping("/")
    public String index() {
        return "redirect:/login";
    }

    // ✅ Exibe a página de login personalizada
    @GetMapping("/login")
    public String loginPage() {
        return "login"; // Renderiza templates/login.html
    }

    // ✅ Exibe o menu/dashboard após autenticação bem-sucedida
    @GetMapping("/menu")
    public String menuPage() {
        return "menu"; // Renderiza templates/menu.html
    }
}