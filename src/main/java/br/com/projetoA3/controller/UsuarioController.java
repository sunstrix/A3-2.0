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
 * 
 * ✅ BUG 1 FIX: Garante mapeamento GET /usuarios para listar todos os usuários.
 * Acesso restrito a ADMINISTRADOR via SecurityConfig.
 * 
 * Princípios aplicados:
 * - Injeção por construtor (sem @Autowired em campo)
 * - Zero lógica de negócio - delegação ao UsuarioService
 * - Uso de DTOs quando apropriado para listagens
 * - Validação com @Valid + BindingResult
 * - Feedback ao usuário via RedirectAttributes
 */
@Controller
@RequestMapping("/usuarios")
public class UsuarioController {

    private final UsuarioService usuarioService;

    /**
     * Injeção de dependências via construtor (padrão Spring moderno).
     */
    public UsuarioController(UsuarioService usuarioService) {
        this.usuarioService = usuarioService;
    }

    // ==========================================
    // LISTAGEM (GET /usuarios)
    // ==========================================

    /**
     * Lista todos os usuários do sistema.
     * Rota acessível apenas por ADMINISTRADOR (configurado no SecurityConfig).
     * 
     * @param model Model do Spring MVC
     * @return Nome do template usuario/list.html
     */
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

    /**
     * Exibe formulário para criar um novo usuário.
     */
    @GetMapping("/novo")
    public String novo(Model model) {
        model.addAttribute("usuario", new Usuario());
        model.addAttribute("perfis", Usuario.Perfil.values());
        model.addAttribute("activePage", "usuarios");
        return "usuario/form";
    }

    /**
     * Salva um novo usuário.
     */
    @PostMapping
    public String salvar(@Valid @ModelAttribute Usuario usuario,
                         BindingResult result,
                         Model model,
                         RedirectAttributes attributes) {
        if (result.hasErrors()) {
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
            model.addAttribute("erro", "Erro ao salvar: " + e.getMessage());
            model.addAttribute("perfis", Usuario.Perfil.values());
            model.addAttribute("activePage", "usuarios");
            return "usuario/form";
        }
    }

    // ==========================================
    // EDIÇÃO
    // ==========================================

    /**
     * Exibe formulário para editar um usuário existente.
     */
    @GetMapping("/editar/{id}")
    public String editar(@PathVariable Long id, Model model) {
        Usuario usuario = usuarioService.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Usuário não encontrado"));
        
        model.addAttribute("usuario", usuario);
        model.addAttribute("perfis", Usuario.Perfil.values());
        model.addAttribute("activePage", "usuarios");
        return "usuario/form";
    }

    /**
     * Atualiza um usuário existente.
     */
    @PostMapping("/atualizar/{id}")
    public String atualizar(@PathVariable Long id,
                            @Valid @ModelAttribute Usuario usuario,
                            BindingResult result,
                            Model model,
                            RedirectAttributes attributes) {
        if (result.hasErrors()) {
            model.addAttribute("perfis", Usuario.Perfil.values());
            model.addAttribute("activePage", "usuarios");
            return "usuario/form";
        }

        try {
            usuarioService.update(id, usuario);
            attributes.addFlashAttribute("sucesso", "Usuário atualizado com sucesso!");
            return "redirect:/usuarios";
        } catch (Exception e) {
            attributes.addFlashAttribute("erro", "Erro ao atualizar: " + e.getMessage());
            return "redirect:/usuarios";
        }
    }

    // ==========================================
    // EXCLUSÃO
    // ==========================================

    /**
     * Remove um usuário do sistema.
     */
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

    /**
     * Alterna o status ativo/inativo de um usuário.
     */
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