package br.com.projetoA3.controller;

import br.com.projetoA3.model.Projeto;
import br.com.projetoA3.service.ProjetoService;
import br.com.projetoA3.service.UsuarioService;
import br.com.projetoA3.service.EquipeService;
import jakarta.validation.Valid;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/projetos")
public class ProjetoController {

    private final ProjetoService projetoService;
    private final UsuarioService usuarioService;
    private final EquipeService equipeService; // ✅ Novo: injetado via construtor

    public ProjetoController(ProjetoService projetoService, UsuarioService usuarioService, EquipeService equipeService) {
        this.projetoService = projetoService;
        this.usuarioService = usuarioService;
        this.equipeService = equipeService;
    }

    @GetMapping
    public String listar(Model model) {
        model.addAttribute("projetos", projetoService.findAll());
        return "projeto/list";
    }

    @GetMapping("/novo")
    public String novo(Model model) {
        model.addAttribute("projeto", new Projeto());
        model.addAttribute("gerentes", usuarioService.findAll()); // Mantido para compatibilidade
        model.addAttribute("equipes", equipeService.findAll());   // ✅ Novo: lista de equipes para o form
        return "projeto/form";
    }

    @PostMapping
    public String salvar(@Valid @ModelAttribute Projeto projeto, 
                         BindingResult result, 
                         Model model,
                         RedirectAttributes attributes) {
        if (result.hasErrors()) {
            model.addAttribute("gerentes", usuarioService.findAll());
            model.addAttribute("equipes", equipeService.findAll()); // ✅ Mantido para re-renderizar form com erro
            return "projeto/form";
        }
        try {
            projetoService.save(projeto);
            attributes.addFlashAttribute("sucesso", "Projeto criado com sucesso!");
            return "redirect:/projetos";
        } catch (Exception e) {
            model.addAttribute("erro", "Erro ao salvar: " + e.getMessage());
            model.addAttribute("gerentes", usuarioService.findAll());
            model.addAttribute("equipes", equipeService.findAll());
            return "projeto/form";
        }
    }

    @GetMapping("/editar/{id}")
    public String editar(@PathVariable Long id, Model model) {
        Projeto projeto = projetoService.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Projeto não encontrado"));
        model.addAttribute("projeto", projeto);
        model.addAttribute("gerentes", usuarioService.findAll());
        model.addAttribute("equipes", equipeService.findAll()); // ✅ Novo: lista de equipes para edição
        return "projeto/form";
    }

    @PostMapping("/atualizar/{id}")
    public String atualizar(@PathVariable Long id, 
                            @Valid @ModelAttribute Projeto projeto, 
                            BindingResult result, 
                            Model model,
                            RedirectAttributes attributes) {
        if (result.hasErrors()) {
            model.addAttribute("gerentes", usuarioService.findAll());
            model.addAttribute("equipes", equipeService.findAll());
            return "projeto/form";
        }
        try {
            projeto.setId(id);
            projetoService.save(projeto);
            attributes.addFlashAttribute("sucesso", "Projeto atualizado com sucesso!");
            return "redirect:/projetos";
        } catch (Exception e) {
            model.addAttribute("erro", "Erro ao atualizar: " + e.getMessage());
            model.addAttribute("gerentes", usuarioService.findAll());
            model.addAttribute("equipes", equipeService.findAll());
            return "projeto/form";
        }
    }

    @GetMapping("/deletar/{id}")
    public String deletar(@PathVariable Long id, RedirectAttributes attributes) {
        try {
            projetoService.deleteById(id);
            attributes.addFlashAttribute("sucesso", "Projeto removido com sucesso!");
        } catch (Exception e) {
            attributes.addFlashAttribute("erro", "Erro ao remover: " + e.getMessage());
        }
        return "redirect:/projetos";
    }
}