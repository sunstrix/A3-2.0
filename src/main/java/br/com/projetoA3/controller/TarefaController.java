package br.com.projetoA3.controller;

import br.com.projetoA3.dto.TarefaDTO;
import br.com.projetoA3.enums.Prioridade;
import br.com.projetoA3.enums.StatusTarefa;
import br.com.projetoA3.model.Tarefa;
import br.com.projetoA3.service.ProjetoService;
import br.com.projetoA3.service.TarefaService;
import br.com.projetoA3.service.UsuarioService;
import br.com.projetoA3.viewmodel.KanbanViewModel;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;
import java.util.Map;

/**
 * Controller responsável por orquestrar requisições HTTP relacionadas a Tarefas.
 * Restaurado com lógica completa e reforçado com segurança e performance.
 */
@Controller
@RequestMapping("/tarefas")
public class TarefaController {

    private final TarefaService tarefaService;
    private final ProjetoService projetoService;
    private final UsuarioService usuarioService;

    public TarefaController(TarefaService tarefaService,
                            ProjetoService projetoService,
                            UsuarioService usuarioService) {
        this.tarefaService = tarefaService;
        this.projetoService = projetoService;
        this.usuarioService = usuarioService;
    }

    // ==========================================
    // LISTAGENS E VISUALIZAÇÕES
    // ==========================================

    @GetMapping
    public String listar(Model model) {
        List<TarefaDTO> tarefas = tarefaService.findAll();
        model.addAttribute("tarefas", tarefas);
        model.addAttribute("activePage", "tarefas");
        return "tarefa/list";
    }

    @GetMapping("/kanban/{projetoId}")
    public String kanban(@PathVariable Long projetoId,
                         Model model,
                         @AuthenticationPrincipal UserDetails userDetails) {
        String username = userDetails != null ? userDetails.getUsername() : null;
        
        // Service utiliza consultas otimizadas (JOIN FETCH / EntityGraph) para evitar N+1
        KanbanViewModel kanban = tarefaService.buildKanbanViewModel(projetoId, username);
        
        model.addAttribute("kanban", kanban);
        model.addAttribute("projetoId", projetoId);
        model.addAttribute("projetoNome", kanban.getNomeProjeto());
        model.addAttribute("totalTarefas", kanban.getTotalTarefas());
        model.addAttribute("activePage", "tarefas");
        
        // Sincronização com o template kanban.html
        model.addAttribute("tarefasAFazer", kanban.getTarefasAFazer());
        model.addAttribute("tarefasEmAndamento", kanban.getTarefasEmAndamento());
        model.addAttribute("tarefasConcluidas", kanban.getTarefasConcluidas());
        model.addAttribute("tarefasCanceladas", kanban.getTarefasCanceladas());
        
        return "tarefa/kanban";
    }

    // ==========================================
    // CRUD - FORMULÁRIOS
    // ==========================================

    @PreAuthorize("isAuthenticated()")
    @GetMapping("/novo")
    public String novo(@RequestParam Long projetoId, Model model) {
        if (projetoService.findById(projetoId) == null) {
            model.addAttribute("erro", "Projeto não encontrado: " + projetoId);
            return "redirect:/projetos";
        }
        
        Tarefa tarefa = new Tarefa();
        tarefa.setStatus(StatusTarefa.A_FAZER);
        tarefa.setPrioridade(Prioridade.MEDIA);
        
        model.addAttribute("tarefa", tarefa);
        model.addAttribute("projetoId", projetoId);
        model.addAttribute("usuarios", usuarioService.findAll());
        model.addAttribute("statusOptions", StatusTarefa.values());
        model.addAttribute("activePage", "tarefas");
        
        return "tarefa/form";
    }

    @PreAuthorize("isAuthenticated()")
    @PostMapping
    public String salvar(@Valid @ModelAttribute Tarefa tarefa,
                         BindingResult result,
                         Model model,
                         RedirectAttributes attributes,
                         @AuthenticationPrincipal UserDetails userDetails) {
        Long projetoId = tarefa.getProjeto() != null ? tarefa.getProjeto().getId() : null;
        
        if (result.hasErrors()) {
            model.addAttribute("usuarios", usuarioService.findAll());
            model.addAttribute("statusOptions", StatusTarefa.values());
            model.addAttribute("projetoId", projetoId);
            model.addAttribute("activePage", "tarefas");
            return "tarefa/form";
        }
        
        try {
            String username = userDetails != null ? userDetails.getUsername() : null;
            TarefaDTO criada = tarefaService.save(tarefa, username);
            attributes.addFlashAttribute("sucesso", 
                "Tarefa '" + criada.titulo() + "' criada com sucesso!");
            return "redirect:/tarefas/kanban/" + projetoId;
        } catch (Exception e) {
            model.addAttribute("erro", "Erro ao salvar: " + e.getMessage());
            model.addAttribute("usuarios", usuarioService.findAll());
            model.addAttribute("statusOptions", StatusTarefa.values());
            model.addAttribute("projetoId", projetoId);
            model.addAttribute("activePage", "tarefas");
            return "tarefa/form";
        }
    }

    @PreAuthorize("hasAnyRole('ADMINISTRADOR', 'GERENTE')")
    @GetMapping("/editar/{id}")
    public String editar(@PathVariable Long id, Model model, RedirectAttributes attributes) {
        try {
            Tarefa tarefa = tarefaService.findById(id)
                    .orElseThrow(() -> new IllegalArgumentException("Tarefa não encontrada"));
            
            Long projetoId = tarefa.getProjeto() != null ? tarefa.getProjeto().getId() : null;
            
            model.addAttribute("tarefa", tarefa);
            model.addAttribute("projetoId", projetoId);
            model.addAttribute("usuarios", usuarioService.findAll());
            model.addAttribute("statusOptions", StatusTarefa.values());
            model.addAttribute("activePage", "tarefas");
            return "tarefa/form";
        } catch (Exception e) {
            attributes.addFlashAttribute("erro", "Erro: " + e.getMessage());
            return "redirect:/tarefas";
        }
    }

    @PreAuthorize("hasAnyRole('ADMINISTRADOR', 'GERENTE')")
    @PostMapping("/atualizar/{id}")
    public String atualizar(@PathVariable Long id,
                            @Valid @ModelAttribute Tarefa tarefa,
                            BindingResult result,
                            Model model,
                            RedirectAttributes attributes,
                            @AuthenticationPrincipal UserDetails userDetails) {
        Long projetoId = tarefa.getProjeto() != null ? tarefa.getProjeto().getId() : null;
        
        if (result.hasErrors()) {
            model.addAttribute("usuarios", usuarioService.findAll());
            model.addAttribute("statusOptions", StatusTarefa.values());
            model.addAttribute("projetoId", projetoId);
            model.addAttribute("activePage", "tarefas");
            return "tarefa/form";
        }
        
        try {
            tarefaService.update(id, tarefa, userDetails.getUsername());
            attributes.addFlashAttribute("sucesso", "Tarefa atualizada com sucesso!");
            return "redirect:/tarefas/kanban/" + projetoId;
        } catch (Exception e) {
            attributes.addFlashAttribute("erro", "Erro ao atualizar: " + e.getMessage());
            return "redirect:/tarefas/kanban/" + projetoId;
        }
    }

    @PreAuthorize("hasRole('ADMINISTRADOR')")
    @GetMapping("/deletar/{id}")
    public String deletar(@PathVariable Long id, RedirectAttributes attributes) {
        try {
            Tarefa tarefa = tarefaService.findById(id).orElse(null);
            Long projetoId = tarefa != null && tarefa.getProjeto() != null 
                           ? tarefa.getProjeto().getId() : null;
            
            tarefaService.deleteById(id);
            attributes.addFlashAttribute("sucesso", "Tarefa removida com sucesso!");
            
            if (projetoId != null) {
                return "redirect:/tarefas/kanban/" + projetoId;
            }
        } catch (Exception e) {
            attributes.addFlashAttribute("erro", "Erro ao remover: " + e.getMessage());
        }
        return "redirect:/tarefas";
    }

    // ==========================================
    // ENDPOINT AJAX - DRAG & DROP DO KANBAN
    // ==========================================

    @PostMapping("/mover")
    @ResponseBody
    public ResponseEntity<?> moverTarefa(@RequestParam Long tarefaId,
                                         @RequestParam String novoStatus,
                                         @AuthenticationPrincipal UserDetails userDetails) {
        try {
            String username = userDetails != null ? userDetails.getUsername() : null;
            StatusTarefa status = StatusTarefa.valueOf(novoStatus);
            
            tarefaService.moverTarefa(tarefaId, status, username);
            
            return ResponseEntity.ok(Map.of(
                "sucesso", true,
                "mensagem", "Tarefa movida com sucesso"
            ));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of(
                "sucesso", false,
                "mensagem", "Status inválido: " + novoStatus
            ));
        } catch (Exception e) {
            return ResponseEntity.status(403).body(Map.of(
                "sucesso", false,
                "mensagem", e.getMessage()
            ));
        }
    }
}