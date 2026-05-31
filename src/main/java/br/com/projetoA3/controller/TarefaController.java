package br.com.projetoA3.controller;

import br.com.projetoA3.dto.TarefaDTO;
import br.com.projetoA3.enums.StatusTarefa;
import br.com.projetoA3.model.Tarefa;
import br.com.projetoA3.service.ProjetoService;
import br.com.projetoA3.service.TarefaService;
import br.com.projetoA3.service.UsuarioService;
import br.com.projetoA3.viewmodel.KanbanViewModel;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
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
 * 
 * Princípios aplicados:
 * - Injeção por construtor (sem @Autowired em campo)
 * - Zero lógica de negócio - apenas delegação ao Service
 * - Uso de @AuthenticationPrincipal para obter o usuário logado
 * - Uso de ResponseEntity para endpoints AJAX (Drag & Drop do Kanban)
 * - Validação com @Valid + BindingResult
 * - Feedback ao usuário via RedirectAttributes
 */
@Controller
@RequestMapping("/tarefas")
public class TarefaController {

    private final TarefaService tarefaService;
    private final ProjetoService projetoService;
    private final UsuarioService usuarioService;

    /**
     * Injeção de dependências via construtor (padrão Spring moderno).
     */
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

    /**
     * Lista todas as tarefas do sistema (visão geral).
     */
    @GetMapping
    public String listar(Model model) {
        List<TarefaDTO> tarefas = tarefaService.findAll();
        model.addAttribute("tarefas", tarefas);
        return "tarefa/list";
    }

    /**
     * Exibe o quadro Kanban de um projeto específico.
     * O KanbanViewModel já vem pronto do Service com todas as métricas calculadas.
     */
    @GetMapping("/kanban/{projetoId}")
    public String kanban(@PathVariable Long projetoId,
                         Model model,
                         @AuthenticationPrincipal UserDetails userDetails) {
        String username = userDetails != null ? userDetails.getUsername() : null;
        
        KanbanViewModel kanban = tarefaService.buildKanbanViewModel(projetoId, username);
        
        model.addAttribute("kanban", kanban);
        model.addAttribute("projetoId", projetoId);
        model.addAttribute("projetoNome", kanban.getNomeProjeto());
        model.addAttribute("totalTarefas", kanban.getTotalTarefas());
        
        // Mantém compatibilidade com template antigo (tarefas separadas por status)
        model.addAttribute("tarefasAFazer", kanban.getTarefasAFazer());
        model.addAttribute("tarefasEmProgresso", kanban.getTarefasEmAndamento());
        model.addAttribute("tarefasConcluidas", kanban.getTarefasConcluidas());
        model.addAttribute("tarefasCanceladas", kanban.getTarefasCanceladas());
        
        return "tarefa/kanban";
    }

    // ==========================================
    // CRUD - FORMULÁRIOS
    // ==========================================

    /**
     * Exibe formulário para criar uma nova tarefa.
     */
    @GetMapping("/novo")
    public String novo(@RequestParam Long projetoId, Model model) {
        model.addAttribute("tarefa", new Tarefa());
        model.addAttribute("projetoId", projetoId);
        model.addAttribute("usuarios", usuarioService.findAll());
        model.addAttribute("statusOptions", StatusTarefa.values());
        return "tarefa/form";
    }

    /**
     * Salva uma nova tarefa.
     */
    @PostMapping
    public String salvar(@Valid @ModelAttribute Tarefa tarefa,
                         BindingResult result,
                         Model model,
                         RedirectAttributes attributes,
                         @AuthenticationPrincipal UserDetails userDetails) {
        if (result.hasErrors()) {
            model.addAttribute("usuarios", usuarioService.findAll());
            model.addAttribute("statusOptions", StatusTarefa.values());
            return "tarefa/form";
        }
        
        try {
            String username = userDetails != null ? userDetails.getUsername() : null;
            TarefaDTO criada = tarefaService.save(tarefa, username);
            attributes.addFlashAttribute("sucesso", "Tarefa '" + criada.titulo() + "' criada com sucesso!");
            return "redirect:/tarefas/kanban/" + tarefa.getProjeto().getId();
        } catch (Exception e) {
            model.addAttribute("erro", e.getMessage());
            model.addAttribute("usuarios", usuarioService.findAll());
            model.addAttribute("statusOptions", StatusTarefa.values());
            return "tarefa/form";
        }
    }

    /**
     * Exibe formulário para editar uma tarefa existente.
     */
    @GetMapping("/editar/{id}")
    public String editar(@PathVariable Long id, Model model) {
        Tarefa tarefa = tarefaService.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Tarefa não encontrada"));
        
        model.addAttribute("tarefa", tarefa);
        model.addAttribute("projetoId", tarefa.getProjeto().getId());
        model.addAttribute("usuarios", usuarioService.findAll());
        model.addAttribute("statusOptions", StatusTarefa.values());
        return "tarefa/form";
    }

    /**
     * Atualiza uma tarefa existente.
     */
    @PostMapping("/atualizar/{id}")
    public String atualizar(@PathVariable Long id,
                            @Valid @ModelAttribute Tarefa tarefa,
                            BindingResult result,
                            Model model,
                            RedirectAttributes attributes) {
        if (result.hasErrors()) {
            model.addAttribute("usuarios", usuarioService.findAll());
            model.addAttribute("statusOptions", StatusTarefa.values());
            return "tarefa/form";
        }
        
        try {
            tarefaService.update(id, tarefa);
            attributes.addFlashAttribute("sucesso", "Tarefa atualizada com sucesso!");
            return "redirect:/tarefas/kanban/" + tarefa.getProjeto().getId();
        } catch (Exception e) {
            attributes.addFlashAttribute("erro", e.getMessage());
            return "redirect:/tarefas/kanban/" + tarefa.getProjeto().getId();
        }
    }

    /**
     * Remove uma tarefa.
     */
    @GetMapping("/deletar/{id}")
    public String deletar(@PathVariable Long id, RedirectAttributes attributes) {
        try {
            Tarefa tarefa = tarefaService.findById(id).orElse(null);
            Long projetoId = tarefa != null ? tarefa.getProjeto().getId() : null;
            tarefaService.deleteById(id);
            attributes.addFlashAttribute("sucesso", "Tarefa removida com sucesso!");
            if (projetoId != null) {
                return "redirect:/tarefas/kanban/" + projetoId;
            }
        } catch (Exception e) {
            attributes.addFlashAttribute("erro", e.getMessage());
        }
        return "redirect:/tarefas";
    }

    // ==========================================
    // ENDPOINT AJAX - DRAG & DROP DO KANBAN
    // ==========================================

    /**
     * Move uma tarefa para outro status (chamado via JavaScript/AJAX).
     * 
     * Espera receber os parâmetros:
     * - tarefaId: ID da tarefa
     * - novoStatus: nome do novo status (A_FAZER, EM_ANDAMENTO, CONCLUIDA, CANCELADA)
     * 
     * Retorna JSON com sucesso ou erro para o frontend processar.
     */
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
            return ResponseEntity.badRequest().body(Map.of(
                "sucesso", false,
                "mensagem", e.getMessage()
            ));
        }
    }
}