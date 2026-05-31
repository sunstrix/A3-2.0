package br.com.projetoA3.service;

import jakarta.mail.*;
import jakarta.mail.internet.*;
import org.springframework.stereotype.Service;

import java.util.Properties;

@Service
public class EmailService {

    private final ConfiguracaoService configuracaoService;

    public EmailService(ConfiguracaoService configuracaoService) {
        this.configuracaoService = configuracaoService;
    }

    /**
     * Envia um e-mail com anexo usando as configurações de SMTP salvas no banco.
     */
    public void enviarRelatorioComAnexo(String assunto, String corpo, byte[] anexoBytes, String nomeAnexo) throws Exception {
        // 1. Obtém as propriedades de SMTP do banco de dados
        Properties props = configuracaoService.getMailProperties();
        if (props == null) {
            throw new Exception("Configurações de SMTP (Host, Porta, Usuário, Senha) estão incompletas no banco.");
        }

        // 2. Obtém o destinatário (precisa ser configurado na tela /configuracoes)
        String destinatario = configuracaoService.getValor("EMAIL_DESTINATARIO");
        if (destinatario == null || destinatario.trim().isEmpty()) {
            throw new Exception("O campo EMAIL_DESTINATARIO não está configurado. Acesse /configuracoes para definir para quem o relatório será enviado.");
        }

        // 3. Cria a sessão de e-mail
        Session session = Session.getInstance(props);
        MimeMessage message = new MimeMessage(session);

        // 4. Configura remetente e destinatário
        message.setFrom(new InternetAddress(configuracaoService.getValor("EMAIL_USUARIO")));
        message.addRecipient(Message.RecipientType.TO, new InternetAddress(destinatario));
        message.setSubject(assunto);

        // 5. Cria o conteúdo multipart (texto + anexo)
        Multipart multipart = new MimeMultipart();

        // Parte de texto
        MimeBodyPart textPart = new MimeBodyPart();
        textPart.setText(corpo);
        multipart.addBodyPart(textPart);

        // Parte de anexo
        MimeBodyPart attachmentPart = new MimeBodyPart();
        attachmentPart.setContent(anexoBytes, "application/octet-stream");
        attachmentPart.setFileName(nomeAnexo);
        multipart.addBodyPart(attachmentPart);

        message.setContent(multipart);

        // 6. Envia o e-mail
        Transport.send(message);
    }
}