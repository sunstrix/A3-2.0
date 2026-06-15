package br.com.projetoA3.service;

import br.com.projetoA3.model.ComentarioTicket;
import br.com.projetoA3.model.Ticket;
import br.com.projetoA3.model.Usuario;
import br.com.projetoA3.repository.ComentarioTicketRepository;
import br.com.projetoA3.repository.TicketRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * Servico responsavel pela regra de negocio e orquestracao do modulo de Help Desk (Tickets).
 * Gerencia o ciclo de vida dos chamados e dispara as notificacoes por e-mail automaticamente
 * para todas as movimentacoes: criacao, atualizacao de status, atribuicao, comentarios,
 * encerramento e reabertura.
 */
@Service
public class TicketService {

    private static final Logger logger = LoggerFactory.getLogger(TicketService.class);

    @Autowired
    private TicketRepository ticketRepository;

    @Autowired
    private ComentarioTicketRepository comentarioRepository;

    @Autowired
    private EmailService emailService;

    /**
     * Cria um novo ticket no sistema e notifica o solicitante sobre a abertura.
     */
    @Transactional
    public Ticket criarTicket(Ticket ticket, Usuario solicitante) {
        ticket.setSolicitante(solicitante);
        ticket.setStatus("ABERTO");

        Ticket novoTicket = ticketRepository.save(ticket);
        logger.info("Ticket #{} criado pelo usuario {}", novoTicket.getId(), solicitante.getNome());

        // Notifica o solicitante sobre a abertura do ticket
        emailService.enviarNotificacaoNovoTicket(novoTicket, solicitante);

        return novoTicket;
    }

    /**
     * Atualiza o status de um ticket (ex: EM_ANDAMENTO, AGUARDANDO_USUARIO, RESOLVIDO, FECHADO).
     * Registra a data de fechamento quando aplicavel e notifica as partes envolvidas.
     */
    @Transactional
    public Ticket atualizarStatus(Long id, String novoStatus, Usuario usuario) {
        Ticket ticket = ticketRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Ticket nao encontrado"));

        String statusAnterior = ticket.getStatus();
        ticket.setStatus(novoStatus);

        if ("FECHADO".equals(novoStatus) || "RESOLVIDO".equals(novoStatus)) {
            ticket.setDataFechamento(LocalDateTime.now());
        }

        Ticket ticketAtualizado = ticketRepository.save(ticket);
        logger.info("Ticket #{} teve status alterado de {} para {} por {}",
                ticketAtualizado.getId(), statusAnterior, novoStatus, usuario.getNome());

        // Notifica o solicitante sobre a mudanca de status
        emailService.enviarNotificacaoAtualizacaoTicket(ticketAtualizado, statusAnterior, ticketAtualizado.getSolicitante());

        // Se tiver um atendente atribuido e for diferente do solicitante, notifica ele tambem
        if (ticketAtualizado.getAtendente() != null &&
            !ticketAtualizado.getAtendente().getId().equals(ticketAtualizado.getSolicitante().getId())) {
            emailService.enviarNotificacaoAtualizacaoTicket(ticketAtualizado, statusAnterior, ticketAtualizado.getAtendente());
        }

        // Se o status for de encerramento, envia notificacao especifica de encerramento
        if ("FECHADO".equals(novoStatus) || "RESOLVIDO".equals(novoStatus)) {
            emailService.enviarNotificacaoEncerramentoTicket(ticketAtualizado, ticketAtualizado.getSolicitante());
            if (ticketAtualizado.getAtendente() != null &&
                !ticketAtualizado.getAtendente().getId().equals(ticketAtualizado.getSolicitante().getId())) {
                emailService.enviarNotificacaoEncerramentoTicket(ticketAtualizado, ticketAtualizado.getAtendente());
            }
        }

        return ticketAtualizado;
    }

    /**
     * Atribui um atendente responsavel a um ticket.
     * Se o ticket estiver ABERTO, muda automaticamente para EM_ANDAMENTO.
     */
    @Transactional
    public Ticket atribuirAtendente(Long id, Usuario atendente) {
        Ticket ticket = ticketRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Ticket nao encontrado"));

        ticket.setAtendente(atendente);
        if ("ABERTO".equals(ticket.getStatus())) {
            ticket.setStatus("EM_ANDAMENTO");
        }

        Ticket ticketAtualizado = ticketRepository.save(ticket);
        logger.info("Ticket #{} atribuido ao atendente {}", ticketAtualizado.getId(), atendente.getNome());

        // Notifica o atendente sobre a nova atribuicao
        emailService.enviarNotificacaoAtribuicaoTicket(ticketAtualizado, atendente);

        // Notifica o solicitante sobre quem esta cuidando do chamado
        if (!atendente.getId().equals(ticketAtualizado.getSolicitante().getId())) {
            emailService.enviarNotificacaoTicket(ticketAtualizado, "Ticket atribuido a " + atendente.getNome(), ticketAtualizado.getSolicitante());
        }

        return ticketAtualizado;
    }

    /**
     * Reabre um ticket encerrado ou resolvido.
     */
    @Transactional
    public Ticket reabrirTicket(Long id, Usuario usuario) {
        Ticket ticket = ticketRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Ticket nao encontrado"));

        ticket.setStatus("REABERTO");
        ticket.setDataFechamento(null);

        Ticket ticketReaberto = ticketRepository.save(ticket);
        logger.info("Ticket #{} reaberto por {}", ticketReaberto.getId(), usuario.getNome());

        // Notifica o solicitante e o atendente sobre a reabertura
        emailService.enviarNotificacaoReaberturaTicket(ticketReaberto, ticketReaberto.getSolicitante());
        if (ticketReaberto.getAtendente() != null &&
            !ticketReaberto.getAtendente().getId().equals(ticketReaberto.getSolicitante().getId())) {
            emailService.enviarNotificacaoReaberturaTicket(ticketReaberto, ticketReaberto.getAtendente());
        }

        return ticketReaberto;
    }

    /**
     * Adiciona um novo comentario (interacao) ao ticket.
     * Suporta notas internas (restritas a equipe) e respostas publicas.
     * Notifica as partes envolvidas apenas para comentarios publicos.
     */
    @Transactional
    public ComentarioTicket adicionarComentario(Long ticketId, String texto, boolean notaInterna, Usuario autor) {
        Ticket ticket = ticketRepository.findById(ticketId)
                .orElseThrow(() -> new RuntimeException("Ticket nao encontrado"));

        ComentarioTicket comentario = new ComentarioTicket();
        comentario.setTexto(texto);
        comentario.setNotaInterna(notaInterna);
        comentario.setAutor(autor);
        comentario.setTicket(ticket);

        ComentarioTicket novoComentario = comentarioRepository.save(comentario);
        logger.info("Comentario #{} adicionado ao ticket #{} por {}", novoComentario.getId(), ticketId, autor.getNome());

        // Logica de notificacao de comentarios (apenas para interacoes publicas)
        if (!notaInterna) {
            // Se o autor for o solicitante, notifica o atendente
            if (ticket.getAtendente() != null && !autor.getId().equals(ticket.getAtendente().getId())) {
                emailService.enviarNotificacaoComentarioTicket(ticket, novoComentario, ticket.getAtendente());
            }
            // Se o autor for o atendente (ou gerente), notifica o solicitante
            else if (!autor.getId().equals(ticket.getSolicitante().getId())) {
                emailService.enviarNotificacaoComentarioTicket(ticket, novoComentario, ticket.getSolicitante());
            }
        }

        return novoComentario;
    }

    // ==========================================
    // Metodos de Leitura e Listagem
    // ==========================================

    public List<Ticket> listarTodos() {
        return ticketRepository.findAllByOrderByDataCriacaoDesc();
    }

    public List<Ticket> listarPorStatus(String status) {
        return ticketRepository.findByStatus(status);
    }

    public List<Ticket> listarPorSolicitante(Usuario solicitante) {
        return ticketRepository.findBySolicitante(solicitante);
    }

    public List<Ticket> listarPorAtendente(Usuario atendente) {
        return ticketRepository.findByAtendente(atendente);
    }

    public Optional<Ticket> buscarPorId(Long id) {
        return ticketRepository.findById(id);
    }

    /**
     * Recupera o historico de comentarios de um ticket.
     * Filtra notas internas caso o usuario logado seja apenas o solicitante (cliente).
     */
    public List<<ComentarioTicket> buscarHistoricoTicket(Ticket ticket, boolean incluirNotasInternas) {
        if (incluirNotasInternas) {
            return comentarioRepository.findByTicketOrderByDataCriacaoAsc(ticket);
        } else {
            return comentarioRepository.findByTicketAndNotaInternaFalseOrderByDataCriacaoAsc(ticket);
        }
    }
}