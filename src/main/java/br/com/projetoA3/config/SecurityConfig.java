package br.com.projetoA3.config;

import br.com.projetoA3.service.CustomUserDetailsService;
import br.com.projetoA3.service.UsuarioService;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.NoOpPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.security.web.csrf.CsrfTokenRequestAttributeHandler;

import java.io.IOException;
import java.time.LocalDateTime;

/**
 * Configuracao do Spring Security para Spring Boot 3.x / Spring Security 6.x
 * 
 * ATENCAO: Usa NoOpPasswordEncoder (senhas em texto puro) apenas para
 * fins academicos/demonstracao. Em producao, SEMPRE use BCrypt ou similar.
 * 
 * Expandido com rotas do Help Desk, perfil ATENDENTE e atualizacao de ultimo acesso.
 */
@Configuration
@EnableWebSecurity
@EnableMethodSecurity
public class SecurityConfig {

    private final CustomUserDetailsService userDetailsService;
    private final UsuarioService usuarioService;

    public SecurityConfig(CustomUserDetailsService userDetailsService, UsuarioService usuarioService) {
        this.userDetailsService = userDetailsService;
        this.usuarioService = usuarioService;
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        // Configuracao do Handler de CSRF para Spring Security 6
        CsrfTokenRequestAttributeHandler requestHandler = new CsrfTokenRequestAttributeHandler();
        requestHandler.setCsrfRequestAttributeName("_csrf");

        return http
            // ==========================================
            // AUTORIZACAO DE REQUISICOES
            // ==========================================
            .authorizeHttpRequests(auth -> auth
                // Recursos estaticos publicos (CSS, JS, imagens, uploads)
                .requestMatchers("/css/**", "/js/**", "/img/**", "/webjars/**", "/uploads/**").permitAll()
                
                // Paginas de autenticacao publicas
                .requestMatchers("/login", "/registro", "/erro/**").permitAll()
                
                // Area de Usuarios restrita ao ADMINISTRADOR
                .requestMatchers("/usuarios/**").hasRole("ADMINISTRADOR")
                
                // Relatorios disponiveis para ADMINISTRADOR e GERENTE
                .requestMatchers("/relatorios/**").hasAnyRole("ADMINISTRADOR", "GERENTE")
                
                // Area de configuracoes restrita ao ADMINISTRADOR
                .requestMatchers("/configuracoes/**").hasRole("ADMINISTRADOR")
                
                // Painel do atendente - acesso para atendentes, gerentes e admins
                .requestMatchers("/painel/atendente").hasAnyRole("ADMINISTRADOR", "GERENTE", "ATENDENTE")
                
                // Rotas de tickets - atendentes podem ver todos, colaboradores apenas os proprios
                .requestMatchers("/tickets", "/tickets/atribuidos", "/tickets/*/atribuir", "/tickets/*/atribuir-me", "/tickets/*/status").hasAnyRole("ADMINISTRADOR", "GERENTE", "ATENDENTE")
                .requestMatchers("/tickets/meus", "/tickets/abrir", "/tickets/detalhar/**", "/tickets/*/comentar").authenticated()
                
                // Base de conhecimento - publica para leitura, restrita para edicao
                .requestMatchers("/base-conhecimento/admin/**").hasAnyRole("ADMINISTRADOR", "GERENTE", "ATENDENTE")
                .requestMatchers("/base-conhecimento").permitAll()
                .requestMatchers("/base-conhecimento/detalhar/**").permitAll()
                
                // Demais endpoints exigem autenticacao
                .anyRequest().authenticated()
            )
            
            // ==========================================
            // FORMULARIO DE LOGIN PERSONALIZADO
            // ==========================================
            .formLogin(form -> form
                .loginPage("/login")
                .loginProcessingUrl("/login")
                .defaultSuccessUrl("/menu", true)
                .failureUrl("/login?error=true")
                .usernameParameter("login")
                .passwordParameter("senha")
                .successHandler(atualizarUltimoAcessoHandler())
                .permitAll()
            )
            
            // ==========================================
            // LOGOUT SEGURO
            // ==========================================
            .logout(logout -> logout
                .logoutUrl("/logout")
                .logoutSuccessUrl("/login?logout=true")
                .invalidateHttpSession(true)
                .deleteCookies("JSESSIONID")
                .permitAll()
            )
            
            // ==========================================
            // GERENCIAMENTO DE SESSAO
            // ==========================================
            .sessionManagement(session -> session
                .maximumSessions(1)
                .expiredUrl("/login?expired=true")
                .maxSessionsPreventsLogin(false)
            )
            
            // ==========================================
            // PROTECAO CSRF
            // ==========================================
            .csrf(csrf -> csrf
                .csrfTokenRequestHandler(requestHandler)
            )
            
            // ==========================================
            // HEADERS DE SEGURANCA
            // ==========================================
            .headers(headers -> headers
                .frameOptions(frame -> frame.deny())
            )
            
            // USA NOSSO AUTHENTICATION PROVIDER PERSONALIZADO
            .authenticationProvider(authenticationProvider())
            
            .build();
    }

    @Bean
    public DaoAuthenticationProvider authenticationProvider() {
        DaoAuthenticationProvider authProvider = new DaoAuthenticationProvider();
        authProvider.setUserDetailsService(userDetailsService);
        authProvider.setPasswordEncoder(passwordEncoder());
        return authProvider;
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration authConfig) throws Exception {
        return authConfig.getAuthenticationManager();
    }

    /**
     * Handler que atualiza o campo ultimoAcesso do usuario no banco de dados
     * apos um login bem-sucedido.
     */
    @Bean
    public AuthenticationSuccessHandler atualizarUltimoAcessoHandler() {
        return new AuthenticationSuccessHandler() {
            @Override
            public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response,
                                              Authentication authentication) throws IOException, ServletException {
                if (authentication.getPrincipal() instanceof UserDetails) {
                    String username = ((UserDetails) authentication.getPrincipal()).getUsername();
                    try {
                        usuarioService.findByLogin(username).ifPresent(usuario -> {
                            usuario.setUltimoAcesso(LocalDateTime.now());
                            usuarioService.save(usuario);
                        });
                    } catch (Exception e) {
                        // Nao bloqueia o login se falhar a atualizacao do ultimo acesso
                    }
                }
                response.sendRedirect("/menu");
            }
        };
    }

    /**
     * ATENCAO: NoOpPasswordEncoder - NAO usar em producao!
     * Aceita senhas em texto puro para facilitar testes academicos.
     */
    @Bean
    @SuppressWarnings("deprecation")
    public PasswordEncoder passwordEncoder() {
        return NoOpPasswordEncoder.getInstance();
    }
}