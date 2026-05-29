package br.com.projetoA3.controller;

import br.com.projetoA3.model.Equipe;
import br.com.projetoA3.model.Usuario;
import br.com.projetoA3.service.EquipeService;
import br.com.projetoA3.service.UsuarioService;
import br.com.projetoA3.service.EquipeMembroService;
import jakarta.validation.Valid;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;
import java.util.stream.Collectors;

@Controller
@RequestMapping("/equipes")
public class EquipeController {

    private final EquipeService equipeService;
    private final UsuarioService usuarioService;
    private final EquipeMembroService membroService;

    public EquipeController(EquipeService equipeService, UsuarioService usuarioService, EquipeMembroService membroService) {
        this.equipeService = equipeService;
        this.usuarioService = usuarioService;
        this.membroService = membroService;
    }

    @GetMapping
    public String listar(Model model) {
        model.addAttribute("equipes", equipeService.findAll());
        return "equipe/list";
    }

    @GetMapping("/novo")
    public String novo(Model model) {
        model.addAttribute("equipe", new Equipe());
        // ✅ FILTRO: Apenas Admin e Gerente podem ser líderes
        List<Usuario> lideres = usuarioService.findAll().stream()
            .filter(u -> u.getPerfil().name().equals("ADMINISTRADOR") || u.getPerfil().name().equals("GERENTE"))
            .collect(Collectors.toList());
        model.addAttribute("lideres", lideres);
        return "equipe/form";
    }

    @PostMapping
    public String salvar(@Valid @ModelAttribute Equipe equipe, 
                         BindingResult result, 
                         Model model,
                         RedirectAttributes attributes) {
        if (result.hasErrors()) {
            List<Usuario> lideres = usuarioService.findAll().stream()
                .filter(u -> u.getPerfil().name().equals("ADMINISTRADOR") || u.getPerfil().name().equals("GERENTE"))
                .collect(Collectors.toList());
            model.addAttribute("lideres", lideres);
            return "equipe/form";
        }
        try {
            equipeService.save(equipe);
            attributes.addFlashAttribute("sucesso", "Equipe criada com sucesso!");
            return "redirect:/equipes";
        } catch (Exception e) {
            model.addAttribute("erro", "Erro ao salvar: " + e.getMessage());
            List<Usuario> lideres = usuarioService.findAll().stream()
                .filter(u -> u.getPerfil().name().equals("ADMINISTRADOR") || u.getPerfil().name().equals("GERENTE"))
                .collect(Collectors.toList());
            model.addAttribute("lideres", lideres);
            return "equipe/form";
        }
    }

    @GetMapping("/editar/{id}")
    public String editar(@PathVariable Long id, Model model) {
        Equipe equipe = equipeService.findById(id).orElseThrow();
        model.addAttribute("equipe", equipe);
        List<Usuario> lideres = usuarioService.findAll().stream()
            .filter(u -> u.getPerfil().name().equals("ADMINISTRADOR") || u.getPerfil().name().equals("GERENTE"))
            .collect(Collectors.toList());
        model.addAttribute("lideres", lideres);
        return "equipe/form";
    }

    @PostMapping("/atualizar/{id}")
    public String atualizar(@PathVariable Long id, 
                            @Valid @ModelAttribute Equipe equipe, 
                            BindingResult result, 
                            Model model,
                            RedirectAttributes attributes) {
        if (result.hasErrors()) {
            List<Usuario> lideres = usuarioService.findAll().stream()
                .filter(u -> u.getPerfil().name().equals("ADMINISTRADOR") || u.getPerfil().name().equals("GERENTE"))
                .collect(Collectors.toList());
            model.addAttribute("lideres", lideres);
            return "equipe/form";
        }
        try {
            equipe.setId(id);
            equipeService.save(equipe);
            attributes.addFlashAttribute("sucesso", "Equipe atualizada com sucesso!");
            return "redirect:/equipes";
        } catch (Exception e) {
            model.addAttribute("erro", "Erro ao atualizar: " + e.getMessage());
            List<Usuario> lideres = usuarioService.findAll().stream()
                .filter(u -> u.getPerfil().name().equals("ADMINISTRADOR") || u.getPerfil().name().equals("GERENTE"))
                .collect(Collectors.toList());
            model.addAttribute("lideres", lideres);
            return "equipe/form";
        }
    }

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

    // ✅ Tela de Detalhes para Gerenciar Membros
    @GetMapping("/{id}")
    public String detalhes(@PathVariable Long id, Model model) {
        model.addAttribute("equipe", equipeService.findById(id).orElseThrow());
        model.addAttribute("membros", membroService.findByEquipeId(id));
        model.addAttribute("usuariosDisponiveis", usuarioService.findAll()); // Todos os funcionários
        return "equipe/detalhes";
    }
}