package br.com.projetoA3.controller;

import br.com.projetoA3.model.Projeto;
import br.com.projetoA3.service.ProjetoService;
import br.com.projetoA3.service.UsuarioService;
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

    public ProjetoController(ProjetoService projetoService, UsuarioService usuarioService) {
        this.projetoService = projetoService;
        this.usuarioService = usuarioService;
    }

    // ✅ Listar todos os projetos
    @GetMapping
    public String listar(Model model) {
        model.addAttribute("projetos", projetoService.findAll());
        return "projeto/list";
    }

    // ✅ Mostrar formulário para novo projeto
    @GetMapping("/novo")
    public String novo(Model model) {
        model.addAttribute("projeto", new Projeto());
        model.addAttribute("gerentes", usuarioService.findAll());
        return "projeto/form";
    }

    // ✅ Salvar novo projeto
    @PostMapping
    public String salvar(@Valid @ModelAttribute Projeto projeto,
                         BindingResult result,
                         Model model,
                         RedirectAttributes attributes) {
        if (result.hasErrors()) {
            model.addAttribute("gerentes", usuarioService.findAll());
            return "projeto/form";
        }
        try {
            projetoService.save(projeto);
            attributes.addFlashAttribute("sucesso", "Projeto criado com sucesso!");
            return "redirect:/projetos";
        } catch (Exception e) {
            model.addAttribute("erro", "Erro ao salvar: " + e.getMessage());
            model.addAttribute("gerentes", usuarioService.findAll());
            return "projeto/form";
        }
    }

    // ✅ Mostrar formulário de edição
    @GetMapping("/editar/{id}")
    public String editar(@PathVariable Long id, Model model) {
        Projeto projeto = projetoService.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Projeto não encontrado"));
        model.addAttribute("projeto", projeto);
        model.addAttribute("gerentes", usuarioService.findAll());
        return "projeto/form";
    }

    // ✅ Atualizar projeto existente
    @PostMapping("/atualizar/{id}")
    public String atualizar(@PathVariable Long id,
                            @Valid @ModelAttribute Projeto projeto,
                            BindingResult result,
                            Model model,
                            RedirectAttributes attributes) {
        if (result.hasErrors()) {
            model.addAttribute("gerentes", usuarioService.findAll());
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
            return "projeto/form";
        }
    }

    // ✅ Deletar projeto
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