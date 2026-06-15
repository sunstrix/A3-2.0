package br.com.projetoA3.config;

import org.springframework.context.MessageSource;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.support.ReloadableResourceBundleMessageSource;
import org.springframework.validation.beanvalidation.LocalValidatorFactoryBean;
import org.springframework.web.servlet.LocaleResolver;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import org.springframework.web.servlet.i18n.LocaleChangeInterceptor;
import org.springframework.web.servlet.i18n.SessionLocaleResolver;

import java.util.Locale;

/**
 * Classe de configuracao para internacionalizacao (i18n) e validacoes.
 * Garante que o sistema e as mensagens de erro do Hibernate Validator 
 * estejam sempre em Portugues do Brasil (pt_BR).
 */
@Configuration
public class MessageConfig implements WebMvcConfigurer {

    /**
     * Configura o leitor de arquivos de mensagens (messages.properties).
     */
    @Bean
    public MessageSource messageSource() {
        ReloadableResourceBundleMessageSource messageSource = new ReloadableResourceBundleMessageSource();
        messageSource.setBasenames("classpath:messages");
        messageSource.setDefaultEncoding("UTF-8");
        messageSource.setCacheSeconds(3600); // Recarrega a cada 1 hora (util em desenvolvimento)
        return messageSource;
    }

    /**
     * Injeta o MessageSource no Validator do Spring.
     * Isso faz com que as anotacoes @NotNull, @Size, @Email, etc., 
     * retornem as mensagens de erro em PT-BR.
     */
    @Bean
    public LocalValidatorFactoryBean getValidator() {
        LocalValidatorFactoryBean bean = new LocalValidatorFactoryBean();
        bean.setValidationMessageSource(messageSource());
        return bean;
    }

    /**
     * Define o Locale padrao da sessao como Portugues do Brasil.
     */
    @Bean
    public LocaleResolver localeResolver() {
        SessionLocaleResolver slr = new SessionLocaleResolver();
        slr.setDefaultLocale(new Locale("pt", "BR"));
        return slr;
    }

    /**
     * Permite alteracao dinamica do idioma via parametro URL (ex: ?lang=en_US).
     * Util para futuros modulos de preferencia do usuario no Help Desk.
     */
    @Bean
    public LocaleChangeInterceptor localeChangeInterceptor() {
        LocaleChangeInterceptor lci = new LocaleChangeInterceptor();
        lci.setParamName("lang");
        return lci;
    }

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(localeChangeInterceptor());
    }
}