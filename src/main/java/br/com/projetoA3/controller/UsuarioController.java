package br.com.projetoA3.controller;

import br.com.projetoA3.dto.UsuarioDTO;
import br.com.projetoA3.model.Usuario;
import br.com.projetoA3.service.UsuarioService;
import jakarta.validation.Valid;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;

/**
 * Controller responsável pelo gerenciamento de Usuários do sistema.
 * Atualizado com logging detalhado para diagnóstico de falha na criação.
 */
@Controller
@RequestMapping("/usuarios")
public class UsuarioController {

    private final UsuarioService usuarioService;

    public UsuarioController(UsuarioService usuarioService) {
        this.usuarioService = usuarioService;
    }

    // ==========================================
    // LISTAGEM (GET /usuarios)
    // ==========================================

    @GetMapping
    public String listar(Model model) {
        List<Usuario> usuarios = usuarioService.findAll();
        
        model.addAttribute("usuarios", usuarios);
        model.addAttribute("activePage", "usuarios");
        model.addAttribute("totalUsuarios", usuarios.size());
        model.addAttribute("totalAtivos", usuarios.stream()
                .filter(u -> u.getAtivo() != null && u.getAtivo())
                .count());
        
        return "usuario/list";
    }

    // ==========================================
    // CADASTRO (GET e POST)
    // ==========================================

    @GetMapping("/novo")
    public String novo(Model model) {
        model.addAttribute("usuario", new Usuario());
        model.addAttribute("perfis", Usuario.Perfil.values());
        model.addAttribute("activePage", "usuarios");
        return "usuario/form";
    }

    /**
     * Salva um novo usuário.
     * Log detalhado adicionado para diagnosticar por que o insert não ocorre.
     */
    @PostMapping
    public String salvar(@Valid @ModelAttribute Usuario usuario,
                         BindingResult result,
                         Model model,
                         RedirectAttributes attributes) {
        
        if (result.hasErrors()) {
            System.err.println("⚠️ Erro de validação ao salvar usuário:");
            result.getFieldErrors().forEach(err -> 
                System.err.println("Campo: " + err.getField() + " | Erro: " + err.getDefaultMessage())
            );
            model.addAttribute("perfis", Usuario.Perfil.values());
            model.addAttribute("activePage", "usuarios");
            return "usuario/form";
        }

        try {
            usuarioService.save(usuario);
            attributes.addFlashAttribute("sucesso", 
                "Usuário '" + usuario.getNome() + "' criado com sucesso!");
            return "redirect:/usuarios";
        } catch (Exception e) {
            System.err.println("❌ Erro no Service ao salvar usuário: " + e.getMessage());
            e.printStackTrace(); // Log da stack trace para identificar a causa raiz (ex: erro de SQL)
            model.addAttribute("erro", "Erro ao salvar: " + e.getMessage());
            model.addAttribute("perfis", Usuario.Perfil.values());
            model.addAttribute("activePage", "usuarios");
            return "usuario/form";
        }
    }

    // ==========================================
    // EDIÇÃO
    // ==========================================

    @GetMapping("/editar/{id}")
    public String editar(@PathVariable Long id, Model model) {
        Usuario usuario = usuarioService.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Usuário não encontrado"));
        
        model.addAttribute("usuario", usuario);
        model.addAttribute("perfis", Usuario.Perfil.values());
        model.addAttribute("activePage", "usuarios");
        return "usuario/form";
    }

    @PostMapping("/atualizar/{id}")
    public String atualizar(@PathVariable Long id,
                            @Valid @ModelAttribute Usuario usuario,
                            BindingResult result,
                            Model model,
                            RedirectAttributes attributes) {
        if (result.hasErrors()) {
            System.err.println("⚠️ Erro de validação ao atualizar usuário ID " + id);
            model.addAttribute("perfis", Usuario.Perfil.values());
            model.addAttribute("activePage", "usuarios");
            return "usuario/form";
        }

        try {
            usuarioService.update(id, usuario);
            attributes.addFlashAttribute("sucesso", "Usuário atualizado com sucesso!");
            return "redirect:/usuarios";
        } catch (Exception e) {
            System.err.println("❌ Erro ao atualizar: " + e.getMessage());
            attributes.addFlashAttribute("erro", "Erro ao atualizar: " + e.getMessage());
            return "redirect:/usuarios";
        }
    }

    // ==========================================
    // EXCLUSÃO
    // ==========================================

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

    // ==========================================
    // ATIVAÇÃO / DESATIVAÇÃO
    // ==========================================

    @GetMapping("/toggle-ativo/{id}")
    public String toggleAtivo(@PathVariable Long id, RedirectAttributes attributes) {
        try {
            usuarioService.toggleAtivo(id);
            attributes.addFlashAttribute("sucesso", "Status do usuário atualizado!");
        } catch (Exception e) {
            attributes.addFlashAttribute("erro", "Erro ao alterar status: " + e.getMessage());
        }
        return "redirect:/usuarios";
    }
}