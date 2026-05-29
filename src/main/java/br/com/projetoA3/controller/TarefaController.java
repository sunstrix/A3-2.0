package br.com.projetoA3.controller;

import br.com.projetoA3.model.Tarefa;
import br.com.projetoA3.service.TarefaService;
import br.com.projetoA3.service.ProjetoService;
import br.com.projetoA3.service.UsuarioService;
import jakarta.validation.Valid;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/tarefas")
public class TarefaController {

    private final TarefaService tarefaService;
    private final ProjetoService projetoService;
    private final UsuarioService usuarioService;

    public TarefaController(TarefaService tarefaService, ProjetoService projetoService, UsuarioService usuarioService) {
        this.tarefaService = tarefaService;
        this.projetoService = projetoService;
        this.usuarioService = usuarioService;
    }

    // ✅ Listar todas as tarefas
    @GetMapping
    public String listar(Model model) {
        model.addAttribute("tarefas", tarefaService.findAll());
        return "tarefa/list";
    }

    // ✅ Exibir quadro Kanban de um projeto específico
    @GetMapping("/kanban/{projetoId}")
    public String kanban(@PathVariable Long projetoId, Model model) {
        model.addAttribute("projeto", projetoService.findById(projetoId).orElse(null));
        model.addAttribute("tarefas", tarefaService.findByProjetoId(projetoId));
        model.addAttribute("statusTarefas", Tarefa.StatusTarefa.values());
        return "tarefa/kanban";
    }

    // ✅ Formulário para nova tarefa
    @GetMapping("/novo")
    public String novo(@RequestParam Long projetoId, Model model) {
        Tarefa tarefa = new Tarefa();
        tarefa.setProjeto(projetoService.findById(projetoId).orElse(null));
        model.addAttribute("tarefa", tarefa);
        model.addAttribute("usuarios", usuarioService.findAll());
        model.addAttribute("status", Tarefa.StatusTarefa.values());
        return "tarefa/form";
    }

    // ✅ Salvar nova tarefa
    @PostMapping
    public String salvar(@Valid @ModelAttribute Tarefa tarefa, 
                         BindingResult result, 
                         Model model,
                         RedirectAttributes attributes) {
        if (result.hasErrors()) {
            model.addAttribute("usuarios", usuarioService.findAll());
            model.addAttribute("status", Tarefa.StatusTarefa.values());
            return "tarefa/form";
        }
        try {
            tarefaService.save(tarefa);
            attributes.addFlashAttribute("sucesso", "Tarefa criada com sucesso!");
            return "redirect:/tarefas/kanban/" + tarefa.getProjeto().getId();
        } catch (Exception e) {
            model.addAttribute("erro", "Erro ao salvar: " + e.getMessage());
            model.addAttribute("usuarios", usuarioService.findAll());
            model.addAttribute("status", Tarefa.StatusTarefa.values());
            return "tarefa/form";
        }
    }

    // ✅ Formulário de edição
    @GetMapping("/editar/{id}")
    public String editar(@PathVariable Long id, Model model) {
        Tarefa tarefa = tarefaService.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Tarefa não encontrada"));
        model.addAttribute("tarefa", tarefa);
        model.addAttribute("usuarios", usuarioService.findAll());
        model.addAttribute("status", Tarefa.StatusTarefa.values());
        return "tarefa/form";
    }

    // ✅ Atualizar tarefa
    @PostMapping("/atualizar/{id}")
    public String atualizar(@PathVariable Long id, 
                            @Valid @ModelAttribute Tarefa tarefa, 
                            BindingResult result, 
                            Model model,
                            RedirectAttributes attributes) {
        if (result.hasErrors()) {
            model.addAttribute("usuarios", usuarioService.findAll());
            model.addAttribute("status", Tarefa.StatusTarefa.values());
            return "tarefa/form";
        }
        try {
            tarefa.setId(id);
            tarefaService.save(tarefa);
            attributes.addFlashAttribute("sucesso", "Tarefa atualizada com sucesso!");
            return "redirect:/tarefas/kanban/" + tarefa.getProjeto().getId();
        } catch (Exception e) {
            model.addAttribute("erro", "Erro ao atualizar: " + e.getMessage());
            model.addAttribute("usuarios", usuarioService.findAll());
            model.addAttribute("status", Tarefa.StatusTarefa.values());
            return "tarefa/form";
        }
    }

    // ✅ Deletar tarefa
    @GetMapping("/deletar/{id}")
    public String deletar(@PathVariable Long id, RedirectAttributes attributes) {
        try {
            Tarefa tarefa = tarefaService.findById(id).orElse(null);
            if (tarefa != null) {
                Long projetoId = tarefa.getProjeto().getId();
                tarefaService.deleteById(id);
                attributes.addFlashAttribute("sucesso", "Tarefa removida com sucesso!");
                return "redirect:/tarefas/kanban/" + projetoId;
            }
        } catch (Exception e) {
            attributes.addFlashAttribute("erro", "Erro ao remover: " + e.getMessage());
        }
        return "redirect:/projetos";
    }
}