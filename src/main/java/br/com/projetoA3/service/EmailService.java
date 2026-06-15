package br.com.projetoA3.service;

import br.com.projetoA3.model.ComentarioTicket;
import br.com.projetoA3.model.Ticket;
import br.com.projetoA3.model.Usuario;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.env.Environment;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

import java.io.UnsupportedEncodingException;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;

/**
 * Servico responsavel pelo processamento e envio de e-mails do sistema.
 * Utiliza o Thymeleaf para renderizar templates HTML dinamicos e o JavaMailSender
 * para a transmissao via SMTP.
 *
 * Todas as operacoes de envio sao assincronas para nao bloquear a thread principal.
 */
@Service
public class EmailService {

    private static final Logger logger = LoggerFactory.getLogger(EmailService.class);

    @Autowired
    private JavaMailSender mailSender;

    @Autowired
    private TemplateEngine templateEngine;

    @Autowired
    private Environment environment;

    @Value("${spring.mail.username}")
    private String remetente;

    @Value("${spring.mail.properties.mail.smtp.from:A3-2.0 Sistema de Help Desk}")
    private String nomeRemetente;

    private static final Locale LOCALE_PT_BR = new Locale("pt", "BR");

    /**
     * Verifica se o ambiente atual e de teste ou desenvolvimento sem SMTP real.
     * Em ambientes de teste, os e-mails sao apenas logados e nao enviados.
     */
    private boolean isModoSimulado() {
        List<String> perfisAtivos = Arrays.asList(environment.getActiveProfiles());
        return perfisAtivos.contains("test") || perfisAtivos.contains("dev");
    }

    /**
     * Envia o e-mail de boas-vindas para um novo usuario cadastrado no sistema.
     *
     * @param usuario O usuario recem-cadastrado.
     */
    @Async
    public void enviarEmailBoasVindas(Usuario usuario) {
        try {
            Context context = new Context(LOCALE_PT_BR);
            context.setVariable("nome", usuario.getNome());
            context.setVariable("email", usuario.getEmail());
            context.setVariable("sistema", nomeRemetente);

            String html = templateEngine.process("emails/boas-vindas", context);

            enviarEmailHtml(
                usuario.getEmail(),
                "Bem-vindo ao A3-2.0 Help Desk!",
                html
            );
            logger.info("E-mail de boas-vindas enviado com sucesso para: {}", usuario.getEmail());

        } catch (Exception e) {
            logger.error("Erro ao processar ou enviar e-mail de boas-vindas para {}: {}", usuario.getEmail(), e.getMessage());
        }
    }

    /**
     * Notifica o solicitante e os atendentes sobre a criacao de um novo ticket.
     *
     * @param ticket O ticket recém-criado.
     * @param solicitante O usuario que abriu o ticket.
     */
    @Async
    public void enviarNotificacaoNovoTicket(Ticket ticket, Usuario solicitante) {
        if (solicitante == null || solicitante.getEmail() == null) {
            logger.warn("Destinatario nulo ou sem e-mail na notificacao de novo ticket.");
            return;
        }

        try {
            Context context = new Context(LOCALE_PT_BR);
            context.setVariable("nomeDestinatario", solicitante.getNome());
            context.setVariable("ticket", ticket);
            context.setVariable("solicitante", solicitante);
            context.setVariable("linkTicket", montarLinkTicket(ticket.getId()));

            String html = templateEngine.process("emails/notificacao-ticket", context);
            String assunto = String.format("[Ticket #%d] Novo Ticket Aberto - %s", ticket.getId(), ticket.getTitulo());

            enviarEmailHtml(solicitante.getEmail(), assunto, html);
            logger.info("Notificacao de novo ticket #{} enviada para {}", ticket.getId(), solicitante.getEmail());

        } catch (Exception e) {
            logger.error("Erro ao enviar notificacao de novo ticket #{}: {}", ticket.getId(), e.getMessage());
        }
    }

    /**
     * Notifica sobre atualizacao de status de um ticket.
     *
     * @param ticket O ticket atualizado.
     * @param statusAnterior O status anterior (para registro no e-mail).
     * @param destinatario O usuario que deve receber a notificacao.
     */
    @Async
    public void enviarNotificacaoAtualizacaoTicket(Ticket ticket, String statusAnterior, Usuario destinatario) {
        if (destinatario == null || destinatario.getEmail() == null) {
            logger.warn("Destinatario nulo ou sem e-mail na notificacao de atualizacao.");
            return;
        }

        try {
            Context context = new Context(LOCALE_PT_BR);
            context.setVariable("nomeDestinatario", destinatario.getNome());
            context.setVariable("ticket", ticket);
            context.setVariable("statusAnterior", statusAnterior);
            context.setVariable("linkTicket", montarLinkTicket(ticket.getId()));

            String html = templateEngine.process("emails/notificacao-atualizacao-ticket", context);
            String assunto = String.format("[Ticket #%d] Status Atualizado: %s", ticket.getId(), ticket.getStatus());

            enviarEmailHtml(destinatario.getEmail(), assunto, html);
            logger.info("Notificacao de atualizacao do ticket #{} enviada para {}", ticket.getId(), destinatario.getEmail());

        } catch (Exception e) {
            logger.error("Erro ao enviar notificacao de atualizacao do ticket #{}: {}", ticket.getId(), e.getMessage());
        }
    }

    /**
     * Notifica sobre um novo comentario adicionado ao ticket.
     *
     * @param ticket O ticket comentado.
     * @param comentario O comentario adicionado.
     * @param destinatario O usuario que deve receber a notificacao.
     */
    @Async
    public void enviarNotificacaoComentarioTicket(Ticket ticket, ComentarioTicket comentario, Usuario destinatario) {
        if (destinatario == null || destinatario.getEmail() == null) {
            logger.warn("Destinatario nulo ou sem e-mail na notificacao de comentario.");
            return;
        }

        try {
            Context context = new Context(LOCALE_PT_BR);
            context.setVariable("nomeDestinatario", destinatario.getNome());
            context.setVariable("ticket", ticket);
            context.setVariable("comentario", comentario);
            context.setVariable("autorComentario", comentario.getAutor());
            context.setVariable("linkTicket", montarLinkTicket(ticket.getId()));

            String html = templateEngine.process("emails/notificacao-comentario-ticket", context);
            String assunto = String.format("[Ticket #%d] Novo Comentario - %s", ticket.getId(), ticket.getTitulo());

            enviarEmailHtml(destinatario.getEmail(), assunto, html);
            logger.info("Notificacao de comentario no ticket #{} enviada para {}", ticket.getId(), destinatario.getEmail());

        } catch (Exception e) {
            logger.error("Erro ao enviar notificacao de comentario do ticket #{}: {}", ticket.getId(), e.getMessage());
        }
    }

    /**
     * Notifica sobre o encerramento de um ticket.
     *
     * @param ticket O ticket encerrado.
     * @param destinatario O usuario que deve receber a notificacao.
     */
    @Async
    public void enviarNotificacaoEncerramentoTicket(Ticket ticket, Usuario destinatario) {
        if (destinatario == null || destinatario.getEmail() == null) {
            logger.warn("Destinatario nulo ou sem e-mail na notificacao de encerramento.");
            return;
        }

        try {
            Context context = new Context(LOCALE_PT_BR);
            context.setVariable("nomeDestinatario", destinatario.getNome());
            context.setVariable("ticket", ticket);
            context.setVariable("linkTicket", montarLinkTicket(ticket.getId()));

            String html = templateEngine.process("emails/notificacao-encerramento-ticket", context);
            String assunto = String.format("[Ticket #%d] Ticket Encerrado - %s", ticket.getId(), ticket.getTitulo());

            enviarEmailHtml(destinatario.getEmail(), assunto, html);
            logger.info("Notificacao de encerramento do ticket #{} enviada para {}", ticket.getId(), destinatario.getEmail());

        } catch (Exception e) {
            logger.error("Erro ao enviar notificacao de encerramento do ticket #{}: {}", ticket.getId(), e.getMessage());
        }
    }

    /**
     * Notifica um atendente sobre a atribuicao de um ticket.
     *
     * @param ticket O ticket atribuido.
     * @param atendente O atendente designado.
     */
    @Async
    public void enviarNotificacaoAtribuicaoTicket(Ticket ticket, Usuario atendente) {
        if (atendente == null || atendente.getEmail() == null) {
            logger.warn("Atendente nulo ou sem e-mail na notificacao de atribuicao.");
            return;
        }

        try {
            Context context = new Context(LOCALE_PT_BR);
            context.setVariable("nomeDestinatario", atendente.getNome());
            context.setVariable("ticket", ticket);
            context.setVariable("linkTicket", montarLinkTicket(ticket.getId()));

            String html = templateEngine.process("emails/notificacao-atribuicao-ticket", context);
            String assunto = String.format("[Ticket #%d] Ticket Atribuido a Voce - %s", ticket.getId(), ticket.getTitulo());

            enviarEmailHtml(atendente.getEmail(), assunto, html);
            logger.info("Notificacao de atribuicao do ticket #{} enviada para {}", ticket.getId(), atendente.getEmail());

        } catch (Exception e) {
            logger.error("Erro ao enviar notificacao de atribuicao do ticket #{}: {}", ticket.getId(), e.getMessage());
        }
    }

    /**
     * Notifica sobre a reabertura de um ticket.
     *
     * @param ticket O ticket reaberto.
     * @param destinatario O usuario que deve receber a notificacao.
     */
    @Async
    public void enviarNotificacaoReaberturaTicket(Ticket ticket, Usuario destinatario) {
        if (destinatario == null || destinatario.getEmail() == null) {
            logger.warn("Destinatario nulo ou sem e-mail na notificacao de reabertura.");
            return;
        }

        try {
            Context context = new Context(LOCALE_PT_BR);
            context.setVariable("nomeDestinatario", destinatario.getNome());
            context.setVariable("ticket", ticket);
            context.setVariable("linkTicket", montarLinkTicket(ticket.getId()));

            String html = templateEngine.process("emails/notificacao-ticket", context);
            String assunto = String.format("[Ticket #%d] Ticket Reaberto - %s", ticket.getId(), ticket.getTitulo());

            enviarEmailHtml(destinatario.getEmail(), assunto, html);
            logger.info("Notificacao de reabertura do ticket #{} enviada para {}", ticket.getId(), destinatario.getEmail());

        } catch (Exception e) {
            logger.error("Erro ao enviar notificacao de reabertura do ticket #{}: {}", ticket.getId(), e.getMessage());
        }
    }

    /**
     * Metodo generico de notificacao de ticket (mantido para compatibilidade).
     * Pode ser usado para acoes nao mapeadas pelos metodos especificos.
     *
     * @param ticket O ticket que sofreu a alteracao.
     * @param acao Uma descricao da acao ocorrida.
     * @param destinatario O usuario que deve receber a notificacao.
     */
    @Async
    public void enviarNotificacaoTicket(Ticket ticket, String acao, Usuario destinatario) {
        if (destinatario == null || destinatario.getEmail() == null) {
            logger.warn("Tentativa de enviar notificacao de ticket para um destinatario nulo ou sem e-mail.");
            return;
        }

        try {
            Context context = new Context(LOCALE_PT_BR);
            context.setVariable("nomeDestinatario", destinatario.getNome());
            context.setVariable("ticket", ticket);
            context.setVariable("acao", acao);
            context.setVariable("linkTicket", montarLinkTicket(ticket.getId()));

            String html = templateEngine.process("emails/notificacao-ticket", context);
            String assunto = String.format("[Ticket #%d] %s - %s", ticket.getId(), acao, ticket.getTitulo());

            enviarEmailHtml(destinatario.getEmail(), assunto, html);
            logger.info("Notificacao generica de ticket enviada para {} referente ao Ticket #{}", destinatario.getEmail(), ticket.getId());

        } catch (Exception e) {
            logger.error("Erro ao enviar notificacao generica do ticket #{} para {}: {}", ticket.getId(), destinatario.getEmail(), e.getMessage());
        }
    }

    /**
     * Monta o link absoluto para a pagina de detalhes do ticket.
     * Em producao, recomenda-se externalizar para uma propriedade de configuracao.
     */
    private String montarLinkTicket(Long ticketId) {
        return "http://localhost:8080/tickets/detalhar/" + ticketId;
    }

    /**
     * Metodo central e privado para a montagem e envio real da mensagem via SMTP.
     * Em ambientes de teste/dev sem SMTP, apenas loga o e-mail.
     */
    private void enviarEmailHtml(String para, String assunto, String html) throws MessagingException, UnsupportedEncodingException {
        if (isModoSimulado() && "localhost".equals(remetente)) {
            logger.info("[MODO SIMULADO] E-mail nao enviado. Destino: {} | Assunto: {}", para, assunto);
            logger.debug("[MODO SIMULADO] Corpo HTML: {}", html);
            return;
        }

        MimeMessage message = mailSender.createMimeMessage();
        MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

        helper.setFrom(remetente, nomeRemetente);
        helper.setTo(para);
        helper.setSubject(assunto);
        helper.setText(html, true);

        mailSender.send(message);
    }
}