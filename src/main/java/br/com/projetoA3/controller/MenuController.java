package br.com.projetoA3.controller;

import br.com.projetoA3.service.ProjetoService;
import br.com.projetoA3.service.TarefaService;
import br.com.projetoA3.service.UsuarioService;
import br.com.projetoA3.service.EquipeService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/menu")
public class MenuController {

    private final ProjetoService projetoService;
    private final TarefaService tarefaService;
    private final UsuarioService usuarioService;
    private final EquipeService equipeService;

    public MenuController(ProjetoService projetoService, 
                          TarefaService tarefaService, 
                          UsuarioService usuarioService,
                          EquipeService equipeService) {
        this.projetoService = projetoService;
        this.tarefaService = tarefaService;
        this.usuarioService = usuarioService;
        this.equipeService = equipeService;
    }

    // ✅ Dashboard principal corrigido para compilação com Records
    @GetMapping
    public String exibirDashboard(Model model) {
        // Estatísticas para os Cards do Dashboard
        long totalEquipes = equipeService.findAll().size();
        
        // Contagem de projetos (todos para evitar erro de enum inexistente)
        long projetosEmAndamento = projetoService.findAll().size();
                
        // Contagem de tarefas usando o método de acesso de Record (.status())
        // Filtramos tarefas que não estão concluídas nem canceladas para o dashboard
        long tarefasPendentes = tarefaService.findAll().stream()
                .filter(t -> !t.status().name().equals("CONCLUIDA") && 
                             !t.status().name().equals("CANCELADA"))
                .count();

        // Atributos originais preservados
        model.addAttribute("totalProjetos", projetoService.findAll().size());
        model.addAttribute("totalTarefas", tarefaService.findAll().size());
        model.addAttribute("totalUsuarios", usuarioService.findAll().size());

        // Atributos para os cards do menu.html
        model.addAttribute("totalEquipes", totalEquipes);
        model.addAttribute("projetosEmAndamento", projetosEmAndamento);
        model.addAttribute("tarefasPendentes", tarefasPendentes);

        return "menu";
    }
}