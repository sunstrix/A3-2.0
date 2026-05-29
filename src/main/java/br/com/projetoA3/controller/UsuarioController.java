package br.com.projetoA3.controller;

import br.com.projetoA3.model.Usuario;
import br.com.projetoA3.service.UsuarioService;
import jakarta.validation.Valid;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;

@Controller
@RequestMapping("/usuarios")
public class UsuarioController {

    private final UsuarioService usuarioService;

    public UsuarioController(UsuarioService usuarioService) {
        this.usuarioService = usuarioService;
    }

    // ✅ Listar todos os usuários
    @GetMapping
    public String listar(Model model) {
        List<Usuario> usuarios = usuarioService.findAll();
        model.addAttribute("usuarios", usuarios);
        return "usuario/list";
    }

    // ✅ Mostrar formulário de novo usuário
    @GetMapping("/novo")
    public String novo(Model model) {
        model.addAttribute("usuario", new Usuario());
        model.addAttribute("perfis", Usuario.Perfil.values());
        return "usuario/form";
    }

    // ✅ Salvar novo usuário
    @PostMapping
    public String salvar(@Valid @ModelAttribute Usuario usuario, 
                         BindingResult result, 
                         Model model,
                         RedirectAttributes attributes) {
        if (result.hasErrors()) {
            model.addAttribute("perfis", Usuario.Perfil.values());
            return "usuario/form";
        }

        try {
            usuarioService.save(usuario);
            attributes.addFlashAttribute("sucesso", "Usuário criado com sucesso!");
            return "redirect:/usuarios";
        } catch (Exception e) {
            model.addAttribute("erro", "Erro ao salvar: " + e.getMessage());
            model.addAttribute("perfis", Usuario.Perfil.values());
            return "usuario/form";
        }
    }

    // ✅ Mostrar formulário de edição
    @GetMapping("/editar/{id}")
    public String editar(@PathVariable Long id, Model model) {
        Usuario usuario = usuarioService.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Usuário não encontrado"));
        model.addAttribute("usuario", usuario);
        model.addAttribute("perfis", Usuario.Perfil.values());
        return "usuario/form";
    }

    // ✅ Atualizar usuário existente
    @PostMapping("/atualizar/{id}")
    public String atualizar(@PathVariable Long id, 
                            @Valid @ModelAttribute Usuario usuario, 
                            BindingResult result, 
                            Model model,
                            RedirectAttributes attributes) {
        if (result.hasErrors()) {
            model.addAttribute("perfis", Usuario.Perfil.values());
            return "usuario/form";
        }

        try {
            usuario.setId(id); // Garante que o ID seja mantido
            usuarioService.save(usuario);
            attributes.addFlashAttribute("sucesso", "Usuário atualizado com sucesso!");
            return "redirect:/usuarios";
        } catch (Exception e) {
            model.addAttribute("erro", "Erro ao atualizar: " + e.getMessage());
            model.addAttribute("perfis", Usuario.Perfil.values());
            return "usuario/form";
        }
    }

    // ✅ Deletar usuário
    @GetMapping("/deletar/{id}")
    public String deletar(@PathVariable Long id, RedirectAttributes attributes) {
        try {
            usuarioService.deleteById(id);
            attributes.addFlashAttribute("sucesso", "Usuário removido com sucesso!");
        } catch (Exception e) {
            attributes.addFlashAttribute("erro", "Erro ao remover: " + e.getMessage());
        }
        return "redirect:/usuarios";
    }
}