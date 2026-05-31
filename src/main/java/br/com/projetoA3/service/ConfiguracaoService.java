package br.com.projetoA3.service;

import br.com.projetoA3.model.Configuracao;
import br.com.projetoA3.repository.ConfiguracaoRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.Properties;

@Service
public class ConfiguracaoService {

    private final ConfiguracaoRepository configuracaoRepository;

    public ConfiguracaoService(ConfiguracaoRepository configuracaoRepository) {
        this.configuracaoRepository = configuracaoRepository;
    }

    /**
     * Retorna todas as configurações cadastradas
     */
    public List<Configuracao> findAll() {
        return configuracaoRepository.findAll();
    }

    /**
     * Retorna uma configuração específica pela chave
     */
    public Optional<Configuracao> findByChave(String chave) {
        return configuracaoRepository.findByChave(chave);
    }

    /**
     * Salva ou atualiza uma configuração
     */
    @Transactional
    public Configuracao save(Configuracao configuracao) {
        return configuracaoRepository.save(configuracao);
    }

    /**
     * Obtém uma propriedade específica (valor) pela chave.
     * Útil para extrair host, porta, usuário, etc.
     */
    public String getValor(String chave) {
        return configuracaoRepository.findByChave(chave)
                .map(Configuracao::getValor)
                .orElse(null);
    }

    /**
     * Gera as propriedades Java (Properties) necessárias para o envio de e-mail
     * baseado nas configurações salvas no banco.
     * Retorna null se as configurações de SMTP não estiverem completas.
     */
    public Properties getMailProperties() {
        String host = getValor("EMAIL_HOST");
        String porta = getValor("EMAIL_PORT");
        String usuario = getValor("EMAIL_USUARIO");
        String senha = getValor("EMAIL_SENHA");

        // Validação básica: se faltar algum dado essencial, retorna null
        if (host == null || porta == null || usuario == null || senha == null) {
            return null;
        }

        Properties props = new Properties();
        props.put("mail.smtp.host", host);
        props.put("mail.smtp.port", porta);
        props.put("mail.smtp.auth", "true");
        props.put("mail.smtp.starttls.enable", "true");
        props.put("mail.smtp.user", usuario);
        props.put("mail.password", senha);
        props.put("mail.smtp.from", usuario); // O remetente será o próprio usuário logado

        return props;
    }
}