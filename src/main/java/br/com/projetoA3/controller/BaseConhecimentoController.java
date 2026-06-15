package br.com.projetoA3.controller;

import br.com.projetoA3.model.ArtigoBaseConhecimento;
import br.com.projetoA3.model.Usuario;
import br.com.projetoA3.service.BaseConhecimentoService;
import br.com.projetoA3.service.UsuarioService; // ADAPTE: Use o nome do Service de Usuarios do seu projeto original
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.security.Principal;
import java.util.List;

/**
 * Controller responsavel por gerenciar as rotas e requisicoes HTTP 
 * do modulo de Base de Conhecimento (FAQ/Documentacao).
 */
@Controller
@RequestMapping("/base-conhecimento")
public class BaseConhecimentoController {

    private static final Logger logger = LoggerFactory.getLogger(BaseConhecimentoController.class);

    @Autowired
    private BaseConhecimentoService baseConhecimentoService;

    @Autowired
    private UsuarioService usuarioService; // ADAPTE: Injete o Service de Usuarios do seu projeto

    /**
     * Exibe a lista de artigos ativos para os usuarios/clientes.
     * Suporta busca por termo textual e filtro por categoria.
     */
    @GetMapping
    public String listarArtigos(@RequestParam(required = false) String termo,
                                @RequestParam(required = false) String categoria,
                                Model model) {
        List<ArtigoBaseConhecimento> artigos;
        
        if (termo != null && !termo.trim().isEmpty()) {
            artigos = baseConhecimentoService.buscarPorTexto(termo);
            model.addAttribute("termoBusca", termo);
        } else if (categoria != null && !categoria.trim().isEmpty()) {
            artigos = baseConhecimentoService.buscarPorCategoria(categoria);
            model.addAttribute("categoriaFiltro", categoria);
        } else {
            artigos = baseConhecimentoService.listarArtigosAtivos();
        }
        
        model.addAttribute("artigos", artigos);
        model.addAttribute("topArtigos", baseConhecimentoService.listarTop5Populares());
        return "kb/listar"; // Crie a view kb/listar.html no futuro
    }

    /**
     * Exibe o conteudo completo de um artigo e incrementa o contador de visualizacoes.
     */
    @GetMapping("/detalhar/{id}")
    public String detalharArtigo(@PathVariable Long id, Model model) {
        ArtigoBaseConhecimento artigo = baseConhecimentoService.buscarPorIdComVisualizacao(id);
        model.addAttribute("artigo", artigo);
        return "kb/detalhar"; // Crie a view kb/detalhar.html no futuro
    }

    /**
     * Exibe o painel administrativo com todos os artigos (ativos e inativos).
     * ADAPTE: Adicione sua logica de verificacao de permissoes (Admin/Gerente) aqui.
     */
    @GetMapping("/admin")
    public String painelAdmin(Model model, Principal principal) {
        List<ArtigoBaseConhecimento> todosArtigos = baseConhecimentoService.listarTodos();
        model.addAttribute("artigos", todosArtigos);
        model.addAttribute("usuarioLogado", getUsuarioLogado(principal));
        return "kb/admin"; // Crie a view kb/admin.html no futuro
    }

    /**
     * Exibe o formulario para criacao de um novo artigo.
     */
    @GetMapping("/admin/novo")
    public String formularioNovoArtigo(Model model) {
        model.addAttribute("artigo", new ArtigoBaseConhecimento());
        return "kb/formulario"; // Crie a view kb/formulario.html no futuro
    }

    /**
     * Processa o formulario para criar ou atualizar um artigo.
     */
    @PostMapping("/admin/salvar")
    public String salvarArtigo(@Valid @ModelAttribute("artigo") ArtigoBaseConhecimento artigo,
                               BindingResult result,
                               Principal principal,
                               RedirectAttributes attributes) {
        if (result.hasErrors()) {
            return "kb/formulario";
        }

        try {
            Usuario autor = getUsuarioLogado(principal);
            if (artigo.getId() == null) {
                baseConhecimentoService.criarArtigo(artigo, autor);
                attributes.addFlashAttribute("mensagemSucesso", "Artigo criado com sucesso!");
            } else {
                baseConhecimentoService.atualizarArtigo(artigo.getId(), artigo);
                attributes.addFlashAttribute("mensagemSucesso", "Artigo atualizado com sucesso!");
            }
            return "redirect:/base-conhecimento/admin";
        } catch (Exception e) {
            logger.error("Erro ao salvar artigo: {}", e.getMessage());
            attributes.addFlashAttribute("mensagemErro", "Erro ao salvar o artigo.");
            return "redirect:/base-conhecimento/admin/novo";
        }
    }

    /**
     * Remove um artigo da base de conhecimento.
     */
    @PostMapping("/admin/excluir/{id}")
    public String excluirArtigo(@PathVariable Long id, RedirectAttributes attributes) {
        try {
            baseConhecimentoService.excluirArtigo(id);
            attributes.addFlashAttribute("mensagemSucesso", "Artigo excluído com sucesso!");
        } catch (Exception e) {
            attributes.addFlashAttribute("mensagemErro", "Erro ao excluir o artigo.");
        }
        return "redirect:/base-conhecimento/admin";
    }

    // ==========================================
    // Metodo Auxiliar
    // ==========================================

    /**
     * Recupera a entidade Usuario completa a partir do Principal do Spring Security.
     * ADAPTE ESTE METODO conforme a implementacao do seu UsuarioService original.
     */
    private Usuario getUsuarioLogado(Principal principal) {
        if (principal == null) {
            throw new RuntimeException("Usuário não autenticado.");
        }
        return usuarioService.buscarPorEmail(principal.getName()); 
    }
}