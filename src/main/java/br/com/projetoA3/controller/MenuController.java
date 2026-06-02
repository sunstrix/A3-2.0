package br.com.projetoA3.controller;

import br.com.projetoA3.dto.DashboardStatsDTO;
import br.com.projetoA3.service.DashboardService;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

/**
 * Controller responsável pela página inicial (Dashboard).
 * Refatorado para utilizar DashboardService visando performance e separação de conceitos.
 */
@Controller
@RequestMapping("/menu")
public class MenuController {

    private final DashboardService dashboardService;

    public MenuController(DashboardService dashboardService) {
        this.dashboardService = dashboardService;
    }

    /**
     * Exibe o Dashboard principal com métricas analíticas baseadas no perfil do usuário.
     * @param model Objeto para passar dados para a View
     * @param userDetails Detalhes do usuário autenticado injetados pelo Spring Security
     */
    @GetMapping
    public String exibirDashboard(Model model, @AuthenticationPrincipal UserDetails userDetails) {
        if (userDetails == null) {
            return "redirect:/login";
        }

        // Obtém as estatísticas processadas de forma otimizada
        DashboardStatsDTO stats = dashboardService.getDashboardStats(userDetails.getUsername());

        // Adiciona o DTO ao modelo para renderização no Thymeleaf
        model.addAttribute("stats", stats);
        
        // Identificador da página ativa para a Navbar
        model.addAttribute("activePage", "dashboard");

        return "menu";
    }
}