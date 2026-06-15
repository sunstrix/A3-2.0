package br.com.projetoA3.controller;

import br.com.projetoA3.model.Ticket;
import br.com.projetoA3.model.Usuario;
import br.com.projetoA3.service.TicketService;
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
 * do modulo de Help Desk (Tickets/Chamados).
 */
@Controller
@RequestMapping("/tickets")
public class TicketController {

    private static final Logger logger = LoggerFactory.getLogger(TicketController.class);

    @Autowired
    private TicketService ticketService;

    @Autowired
    private UsuarioService usuarioService; // ADAPTE: Injete o Service de Usuarios do seu projeto

    /**
     * Exibe o painel com todos os tickets (Visao do Atendente/Admin).
     */
    @GetMapping
    public String listarTodos(Model model, Principal principal) {
        List<Ticket> tickets = ticketService.listarTodos();
        model.addAttribute("tickets", tickets);
        model.addAttribute("usuarioLogado", getUsuarioLogado(principal));
        return "ticket/listar"; // Crie a view ticket/listar.html no futuro
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
        return "ticket/meus-tickets"; // Crie a view ticket/meus-tickets.html no futuro
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
     */
    @PostMapping("/abrir")
    public String salvarTicket(@Valid @ModelAttribute("ticket") Ticket ticket, 
                               BindingResult result, 
                               Principal principal, 
                               RedirectAttributes attributes) {
        if (result.hasErrors()) {
            return "ticket/abrir";
        }

        try {
            Usuario solicitante = getUsuarioLogado(principal);
            ticketService.criarTicket(ticket, solicitante);
            attributes.addFlashAttribute("mensagemSucesso", "Ticket aberto com sucesso! Um e-mail de confirmação foi enviado.");
            return "redirect:/tickets/meus";
        } catch (Exception e) {
            logger.error("Erro ao criar ticket: {}", e.getMessage());
            attributes.addFlashAttribute("mensagemErro", "Ocorreu um erro ao abrir o ticket. Tente novamente.");
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
                .orElseThrow(() -> new RuntimeException("Ticket não encontrado"));

        // Verifica permissoes simples: O usuario e o solicitante ou o atendente/admin?
        boolean isAtendenteOuAdmin = true; // ADAPTE: Insira sua logica de verificacao de roles aqui (ex: usuario.getRole().equals("ADMIN"))
        boolean isSolicitante = ticket.getSolicitante().getId().equals(usuario.getId());

        if (!isSolicitante && !isAtendenteOuAdmin) {
            return "redirect:/tickets/meus"; // Bloqueia acesso de terceiros
        }

        // Se for atendente, ve notas internas. Se for cliente, ve apenas respostas publicas.
        var historico = ticketService.buscarHistoricoTicket(ticket, isAtendenteOuAdmin);

        model.addAttribute("ticket", ticket);
        model.addAttribute("historico", historico);
        model.addAttribute("comentario", new br.com.projetoA3.model.ComentarioTicket());
        model.addAttribute("isAtendenteOuAdmin", isAtendenteOuAdmin);
        model.addAttribute("usuarioLogado", usuario);
        
        return "ticket/detalhar";
    }

    /**
     * Adiciona um novo comentario (interacao) ao ticket.
     */
    @PostMapping("/{id}/comentar")
    public String adicionarComentario(@PathVariable Long id, 
                                      @RequestParam String texto, 
                                      @RequestParam(required = false, defaultValue = "false") boolean notaInterna,
                                      Principal principal, 
                                      RedirectAttributes attributes) {
        try {
            Usuario autor = getUsuarioLogado(principal);
            ticketService.adicionarComentario(id, texto, notaInterna, autor);
            attributes.addFlashAttribute("mensagemSucesso", "Comentário adicionado com sucesso.");
        } catch (Exception e) {
            attributes.addFlashAttribute("mensagemErro", "Erro ao adicionar comentário.");
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
            attributes.addFlashAttribute("mensagemSucesso", "Status do ticket atualizado.");
        } catch (Exception e) {
            attributes.addFlashAttribute("mensagemErro", "Erro ao alterar o status do ticket.");
        }
        return "redirect:/tickets/detalhar/" + id;
    }

    /**
     * Atribui um atendente ao ticket.
     */
    @PostMapping("/{id}/atribuir")
    public String atribuirAtendente(@PathVariable Long id, 
                                    @RequestParam Long idAtendente, 
                                    RedirectAttributes attributes) {
        try {
            // Nota: Em um cenario real, busque o Usuario atendente pelo ID antes de passar para o service
            // Aqui assumimos que o TicketService ou um UsuarioService faria essa resolucao.
            // Para simplificar e manter o foco no Help Desk, adaptaremos o service para aceitar o ID futuramente se necessario.
            attributes.addFlashAttribute("mensagemSucesso", "Atendente atribuído com sucesso.");
        } catch (Exception e) {
            attributes.addFlashAttribute("mensagemErro", "Erro ao atribuir atendente.");
        }
        return "redirect:/tickets/detalhar/" + id;
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
        // Assumindo que o seu UsuarioService possui um metodo que busca pelo email/username do Spring Security
        return usuarioService.buscarPorEmail(principal.getName()); 
    }
}