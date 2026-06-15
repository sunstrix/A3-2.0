package br.com.projetoA3.controller;

import br.com.projetoA3.dto.UsuarioDTO;
import br.com.projetoA3.model.Usuario;
import br.com.projetoA3.service.UsuarioService;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;

/**
 * Controller responsavel pelo gerenciamento de Usuarios do sistema.
 * Atualizado com logging profissional, seguranca por perfil e
 * integracao com envio de e-mail de boas-vindas via UsuarioService.
 */
@Controller
@RequestMapping("/usuarios")
public class UsuarioController {

    private static final Logger logger = LoggerFactory.getLogger(UsuarioController.class);

    private final UsuarioService usuarioService;

    public UsuarioController(UsuarioService usuarioService) {
        this.usuarioService = usuarioService;
    }

    // ==========================================
    // LISTAGEM (GET /usuarios)
    // ==========================================

    @GetMapping
    @PreAuthorize("hasAnyRole('ADMINISTRADOR', 'GERENTE')")
    public String listar(Model model) {
        List<<Usuario> usuarios = usuarioService.findAll();
        
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
    @PreAuthorize("hasAnyRole('ADMINISTRADOR', 'GERENTE')")
    public String novo(Model model) {
        model.addAttribute("usuario", new Usuario());
        model.addAttribute("perfis", Usuario.Perfil.values());
        model.addAttribute("activePage", "usuarios");
        return "usuario/form";
    }

    /**
     * Salva um novo usuario.
     * O envio de e-mail de boas-vindas ocorre automaticamente no UsuarioService.save().
     */
    @PostMapping
    @PreAuthorize("hasAnyRole('ADMINISTRADOR', 'GERENTE')")
    public String salvar(@Valid @ModelAttribute Usuario usuario,
                         BindingResult result,
                         Model model,
                         RedirectAttributes attributes) {
        
        if (result.hasErrors()) {
            logger.warn("Erro de validacao ao salvar usuario: {}", usuario.getLogin());
            result.getFieldErrors().forEach(err -> 
                logger.warn("Campo: {} | Erro: {}", err.getField(), err.getDefaultMessage())
            );
            model.addAttribute("perfis", Usuario.Perfil.values());
            model.addAttribute("activePage", "usuarios");
            return "usuario/form";
        }

        try {
            usuarioService.save(usuario);
            attributes.addFlashAttribute("mensagemSucesso", 
                "Usuario '" + usuario.getNome() + "' criado com sucesso! Um e-mail de boas-vindas foi enviado.");
            return "redirect:/usuarios";
        } catch (Exception e) {
            logger.error("Erro no Service ao salvar usuario {}: {}", usuario.getLogin(), e.getMessage(), e);
            model.addAttribute("mensagemErro", "Erro ao salvar: " + e.getMessage());
            model.addAttribute("perfis", Usuario.Perfil.values());
            model.addAttribute("activePage", "usuarios");
            return "usuario/form";
        }
    }

    // ==========================================
    // EDICAO
    // ==========================================

    @GetMapping("/editar/{id}")
    @PreAuthorize("hasAnyRole('ADMINISTRADOR', 'GERENTE')")
    public String editar(@PathVariable Long id, Model model) {
        Usuario usuario = usuarioService.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Usuario nao encontrado"));
        
        model.addAttribute("usuario", usuario);
        model.addAttribute("perfis", Usuario.Perfil.values());
        model.addAttribute("activePage", "usuarios");
        return "usuario/form";
    }

    @PostMapping("/atualizar/{id}")
    @PreAuthorize("hasAnyRole('ADMINISTRADOR', 'GERENTE')")
    public String atualizar(@PathVariable Long id,
                            @Valid @ModelAttribute Usuario usuario,
                            BindingResult result,
                            Model model,
                            RedirectAttributes attributes) {
        if (result.hasErrors()) {
            logger.warn("Erro de validacao ao atualizar usuario ID {}", id);
            model.addAttribute("perfis", Usuario.Perfil.values());
            model.addAttribute("activePage", "usuarios");
            return "usuario/form";
        }

        try {
            usuarioService.update(id, usuario);
            attributes.addFlashAttribute("mensagemSucesso", "Usuario atualizado com sucesso!");
            return "redirect:/usuarios";
        } catch (Exception e) {
            logger.error("Erro ao atualizar usuario ID {}: {}", id, e.getMessage(), e);
            attributes.addFlashAttribute("mensagemErro", "Erro ao atualizar: " + e.getMessage());
            return "redirect:/usuarios";
        }
    }

    // ==========================================
    // EXCLUSAO
    // ==========================================

    @GetMapping("/deletar/{id}")
    @PreAuthorize("hasRole('ADMINISTRADOR')")
    public String deletar(@PathVariable Long id, RedirectAttributes attributes) {
        try {
            usuarioService.deleteById(id);
            attributes.addFlashAttribute("mensagemSucesso", "Usuario removido com sucesso!");
        } catch (Exception e) {
            logger.error("Erro ao remover usuario ID {}: {}", id, e.getMessage());
            attributes.addFlashAttribute("mensagemErro", "Erro ao remover: " + e.getMessage());
        }
        return "redirect:/usuarios";
    }

    // ==========================================
    // ATIVACAO / DESATIVACAO
    // ==========================================

    @GetMapping("/toggle-ativo/{id}")
    @PreAuthorize("hasAnyRole('ADMINISTRADOR', 'GERENTE')")
    public String toggleAtivo(@PathVariable Long id, RedirectAttributes attributes) {
        try {
            usuarioService.toggleAtivo(id);
            attributes.addFlashAttribute("mensagemSucesso", "Status do usuario atualizado!");
        } catch (Exception e) {
            logger.error("Erro ao alterar status do usuario ID {}: {}", id, e.getMessage());
            attributes.addFlashAttribute("mensagemErro", "Erro ao alterar status: " + e.getMessage());
        }
        return "redirect:/usuarios";
    }
}