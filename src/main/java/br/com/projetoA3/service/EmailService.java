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
     * @param assunto Assunto do e-mail
     * @param corpo Texto do corpo da mensagem
     * @param anexoBytes Conteúdo do arquivo em bytes (PDF ou Excel)
     * @param nomeAnexo Nome do arquivo com extensão (ex: "relatorio.pdf")
     * @throws Exception Caso falhe na conexão ou envio
     */
    public void enviarRelatorioComAnexo(String assunto, String corpo, byte[] anexoBytes, String nomeAnexo) throws Exception {
        // 1. Obtém as propriedades de SMTP do banco de dados
        Properties props = configuracaoService.getMailProperties();
        if (props == null) {
            throw new Exception("Configurações de SMTP (Host, Porta, Usuário, Senha) estão incompletas. Acesse /configuracoes para preenchê-las.");
        }

        // 2. Obtém o destinatário configurado
        String destinatario = configuracaoService.getValor("EMAIL_DESTINATARIO");
        if (destinatario == null || destinatario.trim().isEmpty()) {
            throw new Exception("O campo EMAIL_DESTINATARIO não está configurado na tela de configurações.");
        }

        // 3. Cria a sessão de e-mail
        Session session = Session.getInstance(props);
        MimeMessage message = new MimeMessage(session);

        // 4. Configura remetente e destinatário
        String remetente = configuracaoService.getValor("EMAIL_USUARIO");
        message.setFrom(new InternetAddress(remetente));
        message.addRecipient(Message.RecipientType.TO, new InternetAddress(destinatario));
        message.setSubject(assunto);

        // 5. Cria o conteúdo multipart (texto + anexo)
        Multipart multipart = new MimeMultipart();

        // Parte de texto
        MimeBodyPart textPart = new MimeBodyPart();
        textPart.setText(corpo, "utf-8");
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