package br.com.projetoA3.controller;

import br.com.projetoA3.model.EquipeMembro;
import br.com.projetoA3.service.EquipeMembroService;
import br.com.projetoA3.service.EquipeService;
import br.com.projetoA3.service.UsuarioService;
import jakarta.validation.Valid;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/membros")
public class EquipeMembroController {

    private final EquipeMembroService membroService;
    private final EquipeService equipeService;
    private final UsuarioService usuarioService;

    public EquipeMembroController(EquipeMembroService membroService, EquipeService equipeService, UsuarioService usuarioService) {
        this.membroService = membroService;
        this.equipeService = equipeService;
        this.usuarioService = usuarioService;
    }

    @GetMapping
    public String listar(Model model) {
        model.addAttribute("membros", membroService.findAll());
        return "equipeMembro/list";
    }

    @GetMapping("/novo")
    public String novo(@RequestParam(required = false) Long equipeId, Model model) {
        EquipeMembro membro = new EquipeMembro();
        if (equipeId != null) {
            membro.setEquipe(equipeService.findById(equipeId).orElse(null));
        }
        model.addAttribute("membro", membro);
        model.addAttribute("equipes", equipeService.findAll());
        model.addAttribute("usuarios", usuarioService.findAll());
        return "equipeMembro/form";
    }

    @PostMapping
    public String salvar(@Valid @ModelAttribute EquipeMembro membro, 
                         BindingResult result, 
                         Model model,
                         RedirectAttributes attributes) {
        if (result.hasErrors()) {
            model.addAttribute("equipes", equipeService.findAll());
            model.addAttribute("usuarios", usuarioService.findAll());
            return "equipeMembro/form";
        }
        try {
            membroService.save(membro);
            attributes.addFlashAttribute("sucesso", "Membro adicionado à equipe!");
            return "redirect:/equipes/" + membro.getEquipe().getId();
        } catch (Exception e) {
            model.addAttribute("erro", "Erro ao salvar: " + e.getMessage());
            model.addAttribute("equipes", equipeService.findAll());
            model.addAttribute("usuarios", usuarioService.findAll());
            return "equipeMembro/form";
        }
    }

    @GetMapping("/deletar/{id}")
    public String deletar(@PathVariable Long id, RedirectAttributes attributes) {
        try {
            EquipeMembro membro = membroService.findById(id).orElse(null);
            Long equipeId = membro != null ? membro.getEquipe().getId() : null;
            membroService.deleteById(id);
            attributes.addFlashAttribute("sucesso", "Membro removido da equipe!");
            if (equipeId != null) {
                return "redirect:/equipes/" + equipeId;
            }
        } catch (Exception e) {
            attributes.addFlashAttribute("erro", "Erro ao remover: " + e.getMessage());
        }
        return "redirect:/membros";
    }
}