package br.com.projetoA3.controller;

import br.com.projetoA3.service.ProjetoService;
import br.com.projetoA3.service.TarefaService;
import br.com.projetoA3.service.UsuarioService;
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

    public MenuController(ProjetoService projetoService, TarefaService tarefaService, UsuarioService usuarioService) {
        this.projetoService = projetoService;
        this.tarefaService = tarefaService;
        this.usuarioService = usuarioService;
    }

    // ✅ Dashboard principal com resumos rápidos
    @GetMapping
    public String exibirDashboard(Model model) {
        model.addAttribute("totalProjetos", projetoService.findAll().size());
        model.addAttribute("totalTarefas", tarefaService.findAll().size());
        model.addAttribute("totalUsuarios", usuarioService.findAll().size());
        return "menu";
    }
}