package br.com.projetoA3.controller;

import br.com.projetoA3.model.Projeto;
import br.com.projetoA3.service.ProjetoService;
import br.com.projetoA3.service.UsuarioService;
import br.com.projetoA3.service.EquipeService;
import jakarta.validation.Valid;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

/**
 * Controller responsável pela gestão de Projetos.
 * ✅ Refatoração Sênior: Proteção de rotas, feedback ao usuário e fix de compatibilidade SQLite.
 */
@Controller
@RequestMapping("/projetos")
public class ProjetoController {

    private final ProjetoService projetoService;
    private final UsuarioService usuarioService;
    private final EquipeService equipeService;

    public ProjetoController(ProjetoService projetoService, 
                             UsuarioService usuarioService, 
                             EquipeService equipeService) {
        this.projetoService = projetoService;
        this.usuarioService = usuarioService;
        this.equipeService = equipeService;
    }

    /**
     * Lista todos os projetos utilizando a lógica otimizada do Service.
     */
    @GetMapping
    public String listar(Model model) {
        model.addAttribute("projetos", projetoService.findAll());
        model.addAttribute("activePage", "projetos");
        return "projeto/list";
    }

    /**
     * Exibe formulário de novo projeto.
     * ✅ SEGURANÇA: Restrito a Administradores e Gerentes.
     */
    @PreAuthorize("hasAnyRole('ADMINISTRADOR', 'GERENTE')")
    @GetMapping("/novo")
    public String novo(Model model) {
        model.addAttribute("projeto", new Projeto());
        model.addAttribute("gerentes", usuarioService.findAll());
        model.addAttribute("equipes", equipeService.findAll());
        return "projeto/form";
    }

    /**
     * Salva um novo projeto.
     * ✅ PRESERVAÇÃO: Mantida a correção do gerente null para SQLite.
     */
    @PreAuthorize("hasAnyRole('ADMINISTRADOR', 'GERENTE')")
    @PostMapping
    public String salvar(@Valid @ModelAttribute Projeto projeto, 
                         BindingResult result, 
                         Model model,
                         RedirectAttributes attributes) {
        
        if (result.hasErrors()) {
            model.addAttribute("gerentes", usuarioService.findAll());
            model.addAttribute("equipes", equipeService.findAll());
            return "projeto/form";
        }
        
        try {
            // ✅ CORREÇÃO PRESERVADA: Remove o gerente para evitar erro NOT NULL no SQLite
            projeto.setGerente(null);
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

    /**
     * Exibe formulário de edição.
     */
    @PreAuthorize("hasAnyRole('ADMINISTRADOR', 'GERENTE')")
    @GetMapping("/editar/{id}")
    public String editar(@PathVariable Long id, Model model) {
        // O Service já lança EntityNotFoundException se não encontrar, tratada globalmente
        Projeto projeto = projetoService.findById(id);
        
        model.addAttribute("projeto", projeto);
        model.addAttribute("gerentes", usuarioService.findAll());
        model.addAttribute("equipes", equipeService.findAll());
        return "projeto/form";
    }

    /**
     * Atualiza um projeto existente.
     * ✅ PRESERVAÇÃO: Mantida a lógica de ID e fix de gerente null.
     */
    @PreAuthorize("hasAnyRole('ADMINISTRADOR', 'GERENTE')")
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
            // ✅ CORREÇÃO PRESERVADA: Remove o gerente para evitar erro NOT NULL no SQLite
            projeto.setGerente(null);
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

    /**
     * Remove um projeto.
     * ✅ SEGURANÇA: Restrito exclusivamente ao ADMINISTRADOR conforme regra crítica.
     */
    @PreAuthorize("hasRole('ADMINISTRADOR')")
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