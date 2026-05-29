package br.com.projetoA3.controller;

import br.com.projetoA3.model.Equipe;
import br.com.projetoA3.service.EquipeService;
import br.com.projetoA3.service.UsuarioService;
import jakarta.validation.Valid;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;

@Controller
@RequestMapping("/equipes")
public class EquipeController {

    private final EquipeService equipeService;
    private final UsuarioService usuarioService;

    public EquipeController(EquipeService equipeService, UsuarioService usuarioService) {
        this.equipeService = equipeService;
        this.usuarioService = usuarioService;
    }

    // ✅ Listar todas as equipes
    @GetMapping
    public String listar(Model model) {
        model.addAttribute("equipes", equipeService.findAll());
        return "equipe/list";
    }

    // ✅ Mostrar formulário para nova equipe
    @GetMapping("/novo")
    public String novo(Model model) {
        model.addAttribute("equipe", new Equipe());
        model.addAttribute("usuarios", usuarioService.findAll());
        return "equipe/form";
    }

    // ✅ Salvar nova equipe
    @PostMapping
    public String salvar(@Valid @ModelAttribute Equipe equipe, 
                         BindingResult result, 
                         Model model,
                         RedirectAttributes attributes) {
        if (result.hasErrors()) {
            model.addAttribute("usuarios", usuarioService.findAll());
            return "equipe/form";
        }
        try {
            equipeService.save(equipe);
            attributes.addFlashAttribute("sucesso", "Equipe criada com sucesso!");
            return "redirect:/equipes";
        } catch (Exception e) {
            model.addAttribute("erro", "Erro ao salvar: " + e.getMessage());
            model.addAttribute("usuarios", usuarioService.findAll());
            return "equipe/form";
        }
    }

    // ✅ Mostrar formulário de edição
    @GetMapping("/editar/{id}")
    public String editar(@PathVariable Long id, Model model) {
        Equipe equipe = equipeService.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Equipe não encontrada"));
        model.addAttribute("equipe", equipe);
        model.addAttribute("usuarios", usuarioService.findAll());
        return "equipe/form";
    }

    // ✅ Atualizar equipe existente
    @PostMapping("/atualizar/{id}")
    public String atualizar(@PathVariable Long id, 
                            @Valid @ModelAttribute Equipe equipe, 
                            BindingResult result, 
                            Model model,
                            RedirectAttributes attributes) {
        if (result.hasErrors()) {
            model.addAttribute("usuarios", usuarioService.findAll());
            return "equipe/form";
        }
        try {
            equipe.setId(id);
            equipeService.save(equipe);
            attributes.addFlashAttribute("sucesso", "Equipe atualizada com sucesso!");
            return "redirect:/equipes";
        } catch (Exception e) {
            model.addAttribute("erro", "Erro ao atualizar: " + e.getMessage());
            model.addAttribute("usuarios", usuarioService.findAll());
            return "equipe/form";
        }
    }

    // ✅ Deletar equipe
    @GetMapping("/deletar/{id}")
    public String deletar(@PathVariable Long id, RedirectAttributes attributes) {
        try {
            equipeService.deleteById(id);
            attributes.addFlashAttribute("sucesso", "Equipe removida com sucesso!");
        } catch (Exception e) {
            attributes.addFlashAttribute("erro", "Erro ao remover: " + e.getMessage());
        }
        return "redirect:/equipes";
    }
}