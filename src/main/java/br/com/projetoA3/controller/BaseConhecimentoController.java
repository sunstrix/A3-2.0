package br.com.projetoA3.controller;

import br.com.projetoA3.model.ArtigoBaseConhecimento;
import br.com.projetoA3.model.Usuario;
import br.com.projetoA3.service.BaseConhecimentoService;
import br.com.projetoA3.service.UsuarioService;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.security.Principal;
import java.util.Arrays;
import java.util.List;

/**
 * Controller responsavel por gerenciar as rotas e requisicoes HTTP 
 * do modulo de Base de Conhecimento (FAQ/Documentacao).
 * Integrado com seguranca por perfil, busca avancada e controle de visualizacoes.
 */
@Controller
@RequestMapping("/base-conhecimento")
public class BaseConhecimentoController {

    private static final Logger logger = LoggerFactory.getLogger(BaseConhecimentoController.class);

    @Autowired
    private BaseConhecimentoService baseConhecimentoService;

    @Autowired
    private UsuarioService usuarioService;

    // Categorias padrao da base de conhecimento
    private static final List<String> CATEGORIAS = Arrays.asList(
        "HARDWARE", "SOFTWARE", "REDE", "ACESSO", "GERAL"
    );

    /**
     * Exibe a lista de artigos ativos para os usuarios/clientes.
     * Suporta busca por termo textual e filtro por categoria.
     */
    @GetMapping
    public String listarArtigos(@RequestParam(required = false) String termo,
                                @RequestParam(required = false) String categoria,
                                Model model) {
        List<<ArtigoBaseConhecimento> artigos;
        
        if (termo != null && !termo.trim().isEmpty()) {
            artigos = baseConhecimentoService.buscarPorTexto(termo);
            model.addAttribute("termoBusca", termo);
            logger.info("Busca na base de conhecimento por termo: {}", termo);
        } else if (categoria != null && !categoria.trim().isEmpty()) {
            artigos = baseConhecimentoService.buscarPorCategoria(categoria);
            model.addAttribute("categoriaFiltro", categoria);
            logger.info("Filtro na base de conhecimento por categoria: {}", categoria);
        } else {
            artigos = baseConhecimentoService.listarArtigosAtivos();
        }
        
        model.addAttribute("artigos", artigos);
        model.addAttribute("topArtigos", baseConhecimentoService.listarTop5Populares());
        model.addAttribute("categorias", CATEGORIAS);
        model.addAttribute("tituloPagina", "kb.title");
        return "baseconhecimento/list";
    }

    /**
     * Exibe o conteudo completo de um artigo e incrementa o contador de visualizacoes.
     */
    @GetMapping("/detalhar/{id}")
    public String detalharArtigo(@PathVariable Long id, Model model) {
        ArtigoBaseConhecimento artigo = baseConhecimentoService.buscarPorIdComVisualizacao(id);
        if (artigo == null) {
            logger.warn("Artigo ID {} nao encontrado na base de conhecimento", id);
            return "redirect:/base-conhecimento";
        }
        model.addAttribute("artigo", artigo);
        model.addAttribute("tituloPagina", artigo.getTitulo());
        return "baseconhecimento/detalhar";
    }

    /**
     * Exibe o painel administrativo com todos os artigos (ativos e inativos).
     */
    @GetMapping("/admin")
    @PreAuthorize("hasAnyRole('ADMINISTRADOR', 'GERENTE', 'ATENDENTE')")
    public String painelAdmin(Model model, Principal principal) {
        List<<ArtigoBaseConhecimento> todosArtigos = baseConhecimentoService.listarTodos();
        model.addAttribute("artigos", todosArtigos);
        model.addAttribute("categorias", CATEGORIAS);
        model.addAttribute("usuarioLogado", getUsuarioLogado(principal));
        model.addAttribute("tituloPagina", "kb.title");
        return "baseconhecimento/admin";
    }

    /**
     * Exibe o formulario para criacao de um novo artigo.
     */
    @GetMapping("/admin/novo")
    @PreAuthorize("hasAnyRole('ADMINISTRADOR', 'GERENTE', 'ATENDENTE')")
    public String formularioNovoArtigo(Model model) {
        model.addAttribute("artigo", new ArtigoBaseConhecimento());
        model.addAttribute("categorias", CATEGORIAS);
        model.addAttribute("modoEdicao", false);
        return "baseconhecimento/formulario";
    }

    /**
     * Exibe o formulario para edicao de um artigo existente.
     */
    @GetMapping("/admin/editar/{id}")
    @PreAuthorize("hasAnyRole('ADMINISTRADOR', 'GERENTE', 'ATENDENTE')")
    public String formularioEditarArtigo(@PathVariable Long id, Model model) {
        ArtigoBaseConhecimento artigo = baseConhecimentoService.buscarPorId(id)
                .orElseThrow(() -> new IllegalArgumentException("kb.article.not.found"));
        model.addAttribute("artigo", artigo);
        model.addAttribute("categorias", CATEGORIAS);
        model.addAttribute("modoEdicao", true);
        return "baseconhecimento/formulario";
    }

    /**
     * Processa o formulario para criar ou atualizar um artigo.
     */
    @PostMapping("/admin/salvar")
    @PreAuthorize("hasAnyRole('ADMINISTRADOR', 'GERENTE', 'ATENDENTE')")
    public String salvarArtigo(@Valid @ModelAttribute("artigo") ArtigoBaseConhecimento artigo,
                               BindingResult result,
                               Principal principal,
                               RedirectAttributes attributes) {
        if (result.hasErrors()) {
            logger.warn("Erro de validacao ao salvar artigo: {}", artigo.getTitulo());
            return "baseconhecimento/formulario";
        }

        try {
            Usuario autor = getUsuarioLogado(principal);
            if (artigo.getId() == null) {
                baseConhecimentoService.criarArtigo(artigo, autor);
                attributes.addFlashAttribute("mensagemSucesso", "kb.article.create.success");
                logger.info("Artigo '{}' criado por {}", artigo.getTitulo(), autor.getNome());
            } else {
                baseConhecimentoService.atualizarArtigo(artigo.getId(), artigo);
                attributes.addFlashAttribute("mensagemSucesso", "kb.article.update.success");
                logger.info("Artigo ID {} atualizado por {}", artigo.getId(), autor.getNome());
            }
            return "redirect:/base-conhecimento/admin";
        } catch (Exception e) {
            logger.error("Erro ao salvar artigo '{}': {}", artigo.getTitulo(), e.getMessage(), e);
            attributes.addFlashAttribute("mensagemErro", "app.error.general");
            return "redirect:/base-conhecimento/admin/novo";
        }
    }

    /**
     * Remove um artigo da base de conhecimento.
     */
    @PostMapping("/admin/excluir/{id}")
    @PreAuthorize("hasAnyRole('ADMINISTRADOR', 'GERENTE')")
    public String excluirArtigo(@PathVariable Long id, RedirectAttributes attributes) {
        try {
            baseConhecimentoService.excluirArtigo(id);
            attributes.addFlashAttribute("mensagemSucesso", "kb.article.delete.success");
            logger.info("Artigo ID {} excluido da base de conhecimento", id);
        } catch (Exception e) {
            logger.error("Erro ao excluir artigo ID {}: {}", id, e.getMessage());
            attributes.addFlashAttribute("mensagemErro", "app.error.general");
        }
        return "redirect:/base-conhecimento/admin";
    }

    /**
     * Alterna o status (ativo/inativo) de um artigo.
     */
    @PostMapping("/admin/toggle/{id}")
    @PreAuthorize("hasAnyRole('ADMINISTRADOR', 'GERENTE', 'ATENDENTE')")
    public String toggleStatusArtigo(@PathVariable Long id, RedirectAttributes attributes) {
        try {
            baseConhecimentoService.toggleStatus(id);
            attributes.addFlashAttribute("mensagemSucesso", "kb.article.update.success");
            logger.info("Status do artigo ID {} alternado", id);
        } catch (Exception e) {
            logger.error("Erro ao alternar status do artigo ID {}: {}", id, e.getMessage());
            attributes.addFlashAttribute("mensagemErro", "app.error.general");
        }
        return "redirect:/base-conhecimento/admin";
    }

    // ==========================================
    // Metodo Auxiliar
    // ==========================================

    /**
     * Recupera a entidade Usuario completa a partir do Principal do Spring Security.
     */
    private Usuario getUsuarioLogado(Principal principal) {
        if (principal == null) {
            throw new RuntimeException("Usuario nao autenticado.");
        }
        return usuarioService.buscarPorEmail(principal.getName());
    }
}