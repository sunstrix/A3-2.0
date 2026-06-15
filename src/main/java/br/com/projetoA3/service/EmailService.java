package br.com.projetoA3.service;

import br.com.projetoA3.model.Ticket;
import br.com.projetoA3.model.Usuario;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

import java.io.UnsupportedEncodingException;
import java.util.Locale;

/**
 * Servico responsavel pelo processamento e envio de e-mails do sistema.
 * Utiliza o Thymeleaf para renderizar templates HTML dinamicos e o JavaMailSender
 * para a transmissao via SMTP.
 */
@Service
public class EmailService {

    private static final Logger logger = LoggerFactory.getLogger(EmailService.class);

    @Autowired
    private JavaMailSender mailSender;

    @Autowired
    private TemplateEngine templateEngine;

    @Value("${spring.mail.username}")
    private String remetente;

    /**
     * Envia o e-mail de boas-vindas para um novo usuario cadastrado no sistema.
     * O envio e assincrono para nao bloquear a thread principal da aplicacao.
     *
     * @param usuario O usuario recem-cadastrado.
     */
    @Async
    public void enviarEmailBoasVindas(Usuario usuario) {
        try {
            Context context = new Context(new Locale("pt", "BR"));
            context.setVariable("nome", usuario.getNome());
            context.setVariable("email", usuario.getEmail());
            
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
     * Envia notificacoes sobre movimentacoes em um ticket (criacao, mudanca de status, novos comentarios).
     *
     * @param ticket O ticket que sofreu a alteracao.
     * @param acao Uma descricao da acao ocorrida (ex: "Novo comentario", "Status alterado para EM_ANDAMENTO").
     * @param destinatario O usuario que deve receber a notificacao.
     */
    @Async
    public void enviarNotificacaoTicket(Ticket ticket, String acao, Usuario destinatario) {
        if (destinatario == null || destinatario.getEmail() == null) {
            logger.warn("Tentativa de enviar notificacao de ticket para um destinatario nulo ou sem e-mail.");
            return;
        }

        try {
            Context context = new Context(new Locale("pt", "BR"));
            context.setVariable("nomeDestinatario", destinatario.getNome());
            context.setVariable("ticket", ticket);
            context.setVariable("acao", acao);
            // Link ficticio para acesso rapido, sera adaptado no Controller/Template
            context.setVariable("linkTicket", "http://localhost:8080/tickets/detalhar/" + ticket.getId()); 
            
            String html = templateEngine.process("emails/notificacao-ticket", context);
            
            String assunto = String.format("[Ticket #%d] %s - %s", ticket.getId(), acao, ticket.getTitulo());
            
            enviarEmailHtml(
                destinatario.getEmail(), 
                assunto, 
                html
            );
            logger.info("Notificacao de ticket enviada para {} referente ao Ticket #{}", destinatario.getEmail(), ticket.getId());
            
        } catch (Exception e) {
            logger.error("Erro ao enviar notificacao do ticket #{} para {}: {}", ticket.getId(), destinatario.getEmail(), e.getMessage());
        }
    }

    /**
     * Metodo central e privado para a montagem e envio real da mensagem via SMTP.
     */
    private void enviarEmailHtml(String para, String assunto, String html) throws MessagingException, UnsupportedEncodingException {
        MimeMessage message = mailSender.createMimeMessage();
        MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

        helper.setFrom(remetente, "A3-2.0 Help Desk");
        helper.setTo(para);
        helper.setSubject(assunto);
        helper.setText(html, true); // true indica que o texto e HTML

        mailSender.send(message);
    }
}