package br.com.projetoA3.controller;

import br.com.projetoA3.model.Tarefa;
import br.com.projetoA3.model.Tarefa.StatusTarefa;
import br.com.projetoA3.model.Projeto;
import br.com.projetoA3.service.TarefaService;
import br.com.projetoA3.service.ProjetoService;
import br.com.projetoA3.service.UsuarioService;
import jakarta.validation.Valid;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;

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

    // ✅ Listagem padrão de tarefas
    @GetMapping
    public String listar(Model model) {
        model.addAttribute("tarefas", tarefaService.findAll());
        return "tarefa/list";
    }

    // ✅ NOVO: Visualização do Kanban por Projeto
    @GetMapping("/kanban/{projetoId}")
    public String kanban(@PathVariable Long projetoId, Model model) {
        Projeto projeto = projetoService.findById(projetoId)
                .orElseThrow(() -> new IllegalArgumentException("Projeto não encontrado"));

        // Busca tarefas separadas por status para as colunas do Kanban
        List<Tarefa> aFazer = tarefaService.findByProjetoIdAndStatus(projetoId, StatusTarefa.A_FAZER);
        List<Tarefa> emProgresso = tarefaService.findByProjetoIdAndStatus(projetoId, StatusTarefa.EM_PROGRESSO);
        List<Tarefa> concluidas = tarefaService.findByProjetoIdAndStatus(projetoId, StatusTarefa.CONCLUIDA);

        model.addAttribute("projeto", projeto);
        model.addAttribute("tarefasAFazer", aFazer);
        model.addAttribute("tarefasEmProgresso", emProgresso);
        model.addAttribute("tarefasConcluidas", concluidas);
        model.addAttribute("totalTarefas", aFazer.size() + emProgresso.size() + concluidas.size());

        return "tarefa/kanban";
    }

    // ✅ Formulário de criação
    @GetMapping("/novo")
    public String novo(@RequestParam Long projetoId, Model model) {
        model.addAttribute("tarefa", new Tarefa());
        model.addAttribute("projetoId", projetoId); // Para associar ao projeto
        model.addAttribute("usuarios", usuarioService.findAll());
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
            return "tarefa/form";
        }
        try {
            tarefaService.save(tarefa);
            attributes.addFlashAttribute("sucesso", "Tarefa criada com sucesso!");
            return "redirect:/tarefas/kanban/" + tarefa.getProjeto().getId();
        } catch (Exception e) {
            model.addAttribute("erro", "Erro ao salvar: " + e.getMessage());
            model.addAttribute("usuarios", usuarioService.findAll());
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
            return "tarefa/form";
        }
    }

    // ✅ Deletar tarefa
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
            attributes.addFlashAttribute("erro", "Erro ao remover: " + e.getMessage());
        }
        return "redirect:/tarefas";
    }
    
    // ✅ NOVO: Endpoint para mover tarefa via AJAX (Drag and Drop)
    @PostMapping("/mover")
    @ResponseBody
    public String moverTarefa(@RequestParam Long tarefaId, @RequestParam String novoStatus) {
        try {
            Tarefa tarefa = tarefaService.findById(tarefaId)
                    .orElseThrow(() -> new IllegalArgumentException("Tarefa não encontrada"));
            tarefa.setStatus(StatusTarefa.valueOf(novoStatus));
            tarefaService.save(tarefa);
            return "Sucesso";
        } catch (Exception e) {
            return "Erro: " + e.getMessage();
        }
    }
}