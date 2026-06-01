package br.com.projetoA3.controller;

import br.com.projetoA3.service.ProjetoService;
import br.com.projetoA3.service.TarefaService;
import br.com.projetoA3.service.UsuarioService;
import br.com.projetoA3.service.EquipeService;
import br.com.projetoA3.enums.StatusProjeto;
import br.com.projetoA3.enums.StatusTarefa;
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

    // ✅ Dashboard principal com resumos rápidos e contagens reais
    @GetMapping
    public String exibirDashboard(Model model) {
        // Estatísticas para os Cards do Dashboard
        long totalEquipes = equipeService.findAll().size();
        
        long projetosEmAndamento = projetoService.findAll().stream()
                .filter(p -> p.getStatus() == StatusProjeto.EM_ANDAMENTO)
                .count();
                
        long tarefasPendentes = tarefaService.findAll().stream()
                .filter(t -> t.getStatus() == StatusTarefa.A_FAZER || t.getStatus() == StatusTarefa.EM_ANDAMENTO)
                .count();

        // Atributos originais preservados
        model.addAttribute("totalProjetos", projetoService.findAll().size());
        model.addAttribute("totalTarefas", tarefaService.findAll().size());
        model.addAttribute("totalUsuarios", usuarioService.findAll().size());

        // Novos atributos para os cards de estatísticas do menu.html
        model.addAttribute("totalEquipes", totalEquipes);
        model.addAttribute("projetosEmAndamento", projetosEmAndamento);
        model.addAttribute("tarefasPendentes", tarefasPendentes);

        return "menu";
    }
}