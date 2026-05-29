package br.com.projetoA3.controller;

import br.com.projetoA3.model.Equipe;
import br.com.projetoA3.model.EquipeMembro;
import br.com.projetoA3.service.EquipeService;
import br.com.projetoA3.service.EquipeMembroService;
import br.com.projetoA3.service.UsuarioService;
import jakarta.validation.Valid;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

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
        model.addAttribute("usuarios", usuarioService.findAll());
        return "equipe/form";
    }

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

    @GetMapping("/editar/{id}")
    public String editar(@PathVariable Long id, Model model) {
        Equipe equipe = equipeService.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Equipe não encontrada"));
        model.addAttribute("equipe", equipe);
        model.addAttribute("usuarios", usuarioService.findAll());
        return "equipe/form";
    }

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

    // ✅ NOVO: Visualizar detalhes da equipe com membros
    @GetMapping("/{id}")
    public String detalhes(@PathVariable Long id, Model model) {
        Equipe equipe = equipeService.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Equipe não encontrada"));
        model.addAttribute("equipe", equipe);
        model.addAttribute("membros", membroService.findByEquipeId(id));
        model.addAttribute("usuariosDisponiveis", usuarioService.findAll());
        return "equipe/detalhes";
    }

    // ✅ NOVO: Adicionar membro à equipe
    @PostMapping("/{id}/adicionar-membro")
    public String adicionarMembro(@PathVariable Long id,
                                  @RequestParam Long usuarioId,
                                  @RequestParam String funcao,
                                  RedirectAttributes attributes) {
        try {
            Equipe equipe = equipeService.findById(id)
                    .orElseThrow(() -> new IllegalArgumentException("Equipe não encontrada"));
            
            EquipeMembro membro = new EquipeMembro();
            membro.setEquipe(equipe);
            membro.setUsuario(usuarioService.findById(usuarioId)
                    .orElseThrow(() -> new IllegalArgumentException("Usuário não encontrado")));
            membro.setFuncao(funcao);
            
            membroService.save(membro);
            attributes.addFlashAttribute("sucesso", "Membro adicionado à equipe!");
        } catch (Exception e) {
            attributes.addFlashAttribute("erro", "Erro ao adicionar membro: " + e.getMessage());
        }
        return "redirect:/equipes/" + id;
    }

    // ✅ NOVO: Remover membro da equipe
    @GetMapping("/{equipeId}/remover-membro/{membroId}")
    public String removerMembro(@PathVariable Long equipeId,
                                @PathVariable Long membroId,
                                RedirectAttributes attributes) {
        try {
            membroService.deleteById(membroId);
            attributes.addFlashAttribute("sucesso", "Membro removido da equipe!");
        } catch (Exception e) {
            attributes.addFlashAttribute("erro", "Erro ao remover membro: " + e.getMessage());
        }
        return "redirect:/equipes/" + equipeId;
    }
}