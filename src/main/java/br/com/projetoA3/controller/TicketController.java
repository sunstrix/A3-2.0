package br.com.projetoA3.controller;

import br.com.projetoA3.model.ComentarioTicket;
import br.com.projetoA3.model.Ticket;
import br.com.projetoA3.model.Usuario;
import br.com.projetoA3.service.TicketService;
import br.com.projetoA3.service.UsuarioService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.security.Principal;
import java.util.List;
import java.util.UUID;

/**
 * Controller responsavel por gerenciar as rotas e requisicoes HTTP 
 * do modulo de Help Desk (Tickets/Chamados).
 * Integra notificacoes por e-mail, upload de anexos e controle de permissoes.
 */
@Controller
@RequestMapping("/tickets")
public class TicketController {

    private static final Logger logger = LoggerFactory.getLogger(TicketController.class);

    @Autowired
    private TicketService ticketService;

    @Autowired
    private UsuarioService usuarioService;

    @Value("${a3.upload.dir:./uploads/tickets/}")
    private String uploadDir;

    /**
     * Exibe o painel com todos os tickets (Visao do Atendente/Admin).
     */
    @GetMapping
    @PreAuthorize("hasAnyRole('ADMINISTRADOR', 'GERENTE', 'ATENDENTE')")
    public String listarTodos(Model model, Principal principal) {
        List<Ticket> tickets = ticketService.listarTodos();
        model.addAttribute("tickets", tickets);
        model.addAttribute("usuarioLogado", getUsuarioLogado(principal));
        model.addAttribute("tituloPagina", "ticket.all");
        return "ticket/list";
    }

    /**
     * Exibe apenas os tickets do usuario logado (Visao do Solicitante/Cliente).
     */
    @GetMapping("/meus")
    public String listarMeusTickets(Model model, Principal principal) {
        Usuario usuario = getUsuarioLogado(principal);
        List<Ticket> meusTickets = ticketService.listarPorSolicitante(usuario);
        model.addAttribute("tickets", meusTickets);
        model.addAttribute("usuarioLogado", usuario);
        model.addAttribute("tituloPagina", "ticket.list");
        return "ticket/list";
    }

    /**
     * Exibe os tickets atribuidos ao atendente logado.
     */
    @GetMapping("/atribuidos")
    @PreAuthorize("hasAnyRole('ADMINISTRADOR', 'GERENTE', 'ATENDENTE')")
    public String listarAtribuidos(Model model, Principal principal) {
        Usuario usuario = getUsuarioLogado(principal);
        List<Ticket> tickets = ticketService.listarPorAtendente(usuario);
        model.addAttribute("tickets", tickets);
        model.addAttribute("usuarioLogado", usuario);
        model.addAttribute("tituloPagina", "dashboard.my.tickets");
        return "ticket/list";
    }

    /**
     * Exibe o formulario para abertura de um novo ticket.
     */
    @GetMapping("/abrir")
    public String formularioAbrirTicket(Model model) {
        model.addAttribute("ticket", new Ticket());
        return "ticket/abrir";
    }

    /**
     * Processa o formulario e cria o ticket no banco de dados.
     * Suporta upload de anexos.
     */
    @PostMapping("/abrir")
    public String salvarTicket(@Valid @ModelAttribute("ticket") Ticket ticket, 
                               BindingResult result, 
                               @RequestParam(value = "arquivos", required = false) List<<MultipartFile> arquivos,
                               Principal principal, 
                               RedirectAttributes attributes) {
        if (result.hasErrors()) {
            return "ticket/abrir";
        }

        try {
            Usuario solicitante = getUsuarioLogado(principal);
            Ticket novoTicket = ticketService.criarTicket(ticket, solicitante);

            // Processa anexos se houver
            if (arquivos != null && !arquivos.isEmpty()) {
                processarAnexos(novoTicket, arquivos);
            }

            attributes.addFlashAttribute("mensagemSucesso", "ticket.create.success");
            return "redirect:/tickets/meus";
        } catch (Exception e) {
            logger.error("Erro ao criar ticket: {}", e.getMessage());
            attributes.addFlashAttribute("mensagemErro", "app.error.general");
            return "redirect:/tickets/abrir";
        }
    }

    /**
     * Exibe os detalhes, historico e formulario de resposta de um ticket especifico.
     */
    @GetMapping("/detalhar/{id}")
    public String detalharTicket(@PathVariable Long id, Model model, Principal principal) {
        Usuario usuario = getUsuarioLogado(principal);
        Ticket ticket = ticketService.buscarPorId(id)
                .orElseThrow(() -> new RuntimeException("ticket.not.found"));

        boolean isAtendenteOuAdmin = usuario.isAdministrador() || usuario.isGerente() || usuario.isAtendente();
        boolean isSolicitante = ticket.getSolicitante().getId().equals(usuario.getId());
        boolean isAtendenteResponsavel = ticket.getAtendente() != null && ticket.getAtendente().getId().equals(usuario.getId());

        // Bloqueia acesso de terceiros
        if (!isSolicitante && !isAtendenteOuAdmin && !isAtendenteResponsavel) {
            return "redirect:/tickets/meus";
        }

        // Se for atendente, ve notas internas. Se for cliente, ve apenas respostas publicas.
        var historico = ticketService.buscarHistoricoTicket(ticket, isAtendenteOuAdmin || isAtendenteResponsavel);

        model.addAttribute("ticket", ticket);
        model.addAttribute("historico", historico);
        model.addAttribute("comentario", new ComentarioTicket());
        model.addAttribute("isAtendenteOuAdmin", isAtendenteOuAdmin || isAtendenteResponsavel);
        model.addAttribute("isSolicitante", isSolicitante);
        model.addAttribute("usuarioLogado", usuario);
        
        return "ticket/detalhar";
    }

    /**
     * Adiciona um novo comentario (interacao) ao ticket.
     * Suporta upload de anexos no comentario.
     */
    @PostMapping("/{id}/comentar")
    public String adicionarComentario(@PathVariable Long id, 
                                      @RequestParam String texto, 
                                      @RequestParam(required = false, defaultValue = "false") boolean notaInterna,
                                      @RequestParam(value = "arquivos", required = false) List<<MultipartFile> arquivos,
                                      Principal principal, 
                                      RedirectAttributes attributes) {
        try {
            Usuario autor = getUsuarioLogado(principal);
            ComentarioTicket comentario = ticketService.adicionarComentario(id, texto, notaInterna, autor);

            // Processa anexos do comentario se houver
            if (arquivos != null && !arquivos.isEmpty()) {
                processarAnexosComentario(comentario, arquivos);
            }

            attributes.addFlashAttribute("mensagemSucesso", "ticket.comment.success");
        } catch (Exception e) {
            logger.error("Erro ao adicionar comentario no ticket #{}: {}", id, e.getMessage());
            attributes.addFlashAttribute("mensagemErro", "app.error.general");
        }
        return "redirect:/tickets/detalhar/" + id;
    }

    /**
     * Altera o status do ticket (ex: Resolver, Fechar, Reabrir).
     */
    @PostMapping("/{id}/status")
    public String alterarStatus(@PathVariable Long id, 
                                @RequestParam String novoStatus, 
                                Principal principal, 
                                RedirectAttributes attributes) {
        try {
            Usuario usuario = getUsuarioLogado(principal);
            ticketService.atualizarStatus(id, novoStatus, usuario);
            attributes.addFlashAttribute("mensagemSucesso", "ticket.update.success");
        } catch (Exception e) {
            logger.error("Erro ao alterar status do ticket #{}: {}", id, e.getMessage());
            attributes.addFlashAttribute("mensagemErro", "ticket.status.invalid");
        }
        return "redirect:/tickets/detalhar/" + id;
    }

    /**
     * Reabre um ticket encerrado ou resolvido.
     */
    @PostMapping("/{id}/reabrir")
    public String reabrirTicket(@PathVariable Long id,
                                Principal principal,
                                RedirectAttributes attributes) {
        try {
            Usuario usuario = getUsuarioLogado(principal);
            ticketService.reabrirTicket(id, usuario);
            attributes.addFlashAttribute("mensagemSucesso", "ticket.reopen.success");
        } catch (Exception e) {
            logger.error("Erro ao reabrir ticket #{}: {}", id, e.getMessage());
            attributes.addFlashAttribute("mensagemErro", "app.error.general");
        }
        return "redirect:/tickets/detalhar/" + id;
    }

    /**
     * Atribui um atendente ao ticket.
     */
    @PostMapping("/{id}/atribuir")
    @PreAuthorize("hasAnyRole('ADMINISTRADOR', 'GERENTE', 'ATENDENTE')")
    public String atribuirAtendente(@PathVariable Long id, 
                                    @RequestParam Long idAtendente, 
                                    Principal principal,
                                    RedirectAttributes attributes) {
        try {
            Usuario atendente = usuarioService.findById(idAtendente)
                    .orElseThrow(() -> new RuntimeException("user.not.found"));
            
            ticketService.atribuirAtendente(id, atendente);
            attributes.addFlashAttribute("mensagemSucesso", "ticket.assign.success");
        } catch (Exception e) {
            logger.error("Erro ao atribuir atendente ao ticket #{}: {}", id, e.getMessage());
            attributes.addFlashAttribute("mensagemErro", "app.error.general");
        }
        return "redirect:/tickets/detalhar/" + id;
    }

    /**
     * Auto-atribuicao de ticket pelo atendente logado.
     */
    @PostMapping("/{id}/atribuir-me")
    @PreAuthorize("hasAnyRole('ADMINISTRADOR', 'GERENTE', 'ATENDENTE')")
    public String atribuirAMim(@PathVariable Long id,
                               Principal principal,
                               RedirectAttributes attributes) {
        try {
            Usuario atendente = getUsuarioLogado(principal);
            ticketService.atribuirAtendente(id, atendente);
            attributes.addFlashAttribute("mensagemSucesso", "ticket.assign.success");
        } catch (Exception e) {
            logger.error("Erro ao auto-atribuir ticket #{}: {}", id, e.getMessage());
            attributes.addFlashAttribute("mensagemErro", "app.error.general");
        }
        return "redirect:/tickets/detalhar/" + id;
    }

    // ==========================================
    // Metodos de Upload de Anexos
    // ==========================================

    /**
     * Processa o upload de anexos para um ticket.
     */
    private void processarAnexos(Ticket ticket, List<<MultipartFile> arquivos) throws IOException {
        Path diretorio = Paths.get(uploadDir, ticket.getId().toString());
        if (!Files.exists(diretorio)) {
            Files.createDirectories(diretorio);
        }

        for (MultipartFile arquivo : arquivos) {
            if (!arquivo.isEmpty()) {
                String nomeOriginal = arquivo.getOriginalFilename();
                String extensao = nomeOriginal != null ? nomeOriginal.substring(nomeOriginal.lastIndexOf(".")) : "";
                String nomeUnico = UUID.randomUUID() + extensao;
                Path destino = diretorio.resolve(nomeUnico);
                Files.copy(arquivo.getInputStream(), destino, StandardCopyOption.REPLACE_EXISTING);
                ticket.addAnexo(nomeUnico);
            }
        }
    }

    /**
     * Processa o upload de anexos para um comentario.
     */
    private void processarAnexosComentario(ComentarioTicket comentario, List<<MultipartFile> arquivos) throws IOException {
        Path diretorio = Paths.get(uploadDir, "comentarios", comentario.getId().toString());
        if (!Files.exists(diretorio)) {
            Files.createDirectories(diretorio);
        }

        for (MultipartFile arquivo : arquivos) {
            if (!arquivo.isEmpty()) {
                String nomeOriginal = arquivo.getOriginalFilename();
                String extensao = nomeOriginal != null ? nomeOriginal.substring(nomeOriginal.lastIndexOf(".")) : "";
                String nomeUnico = UUID.randomUUID() + extensao;
                Path destino = diretorio.resolve(nomeUnico);
                Files.copy(arquivo.getInputStream(), destino, StandardCopyOption.REPLACE_EXISTING);
                comentario.addAnexo(nomeUnico);
            }
        }
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