package br.com.projetoA3.config;

import br.com.projetoA3.service.CustomUserDetailsService;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

/**
 * Configuração do Spring Security para Spring Boot 3.x / Spring Security 6.x
 * 
 * ✅ Refatoração Sênior: Mantém toda a lógica original de rotas, mas adiciona 
 * suporte a @PreAuthorize e atualiza para criptografia robusta.
 */
@Configuration
@EnableWebSecurity
@EnableMethodSecurity // ✅ Necessário para as correções de autorização nos Services/Controllers
public class SecurityConfig {

    private final CustomUserDetailsService userDetailsService;

    public SecurityConfig(CustomUserDetailsService userDetailsService) {
        this.userDetailsService = userDetailsService;
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        return http
            // ==========================================
            // AUTORIZAÇÃO DE REQUISIÇÕES
            // ==========================================
            .authorizeHttpRequests(auth -> auth
                // Recursos estáticos públicos (CSS, JS, imagens)
                .requestMatchers("/css/**", "/js/**", "/img/**", "/webjars/**").permitAll()
                
                // Páginas de autenticação públicas
                .requestMatchers("/login", "/registro", "/erro/**").permitAll()
                
                // ✅ Áreas restritas (Mantendo sua lógica original de roles)
                .requestMatchers("/usuarios/**").hasRole("ADMINISTRADOR")
                .requestMatchers("/relatorios/**").hasAnyRole("ADMINISTRADOR", "GERENTE")
                .requestMatchers("/configuracoes/**").hasRole("ADMINISTRADOR")
                
                // Novas rotas administrativas protegidas (conforme análise)
                .requestMatchers("/admin/**").hasRole("ADMINISTRADOR")
                
                // Demais endpoints exigem autenticação
                .anyRequest().authenticated()
            )
            
            // ==========================================
            // FORMULÁRIO DE LOGIN PERSONALIZADO
            // ==========================================
            .formLogin(form -> form
                .loginPage("/login")
                .loginProcessingUrl("/login")
                .defaultSuccessUrl("/menu", true) // ✅ Mantido seu redirecionamento para /menu
                .failureUrl("/login?error=true")
                .usernameParameter("login")      // ✅ Mantido seu parâmetro 'login'
                .passwordParameter("senha")      // ✅ Mantido seu parâmetro 'senha'
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
            // GERENCIAMENTO DE SESSÃO
            // ==========================================
            .sessionManagement(session -> session
                .maximumSessions(1)
                .expiredUrl("/login?expired=true")
                .maxSessionsPreventsLogin(false)
            )
            
            // ==========================================
            // PROTEÇÃO CSRF
            // ==========================================
            .csrf(csrf -> {
                // Em desenvolvimento/H2 costuma-se ignorar, mas manteremos o padrão seguro
            })
            
            // ==========================================
            // HEADERS DE SEGURANÇA
            // ==========================================
            .headers(headers -> headers
                .frameOptions(frame -> frame.sameOrigin()) // ✅ Alterado de 'deny' para 'sameOrigin' para permitir H2/Frames internos se necessário
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
     * ✅ CORREÇÃO DE SEGURANÇA: Alterado de NoOp para BCrypt.
     * O NoOp era um bug de segurança crítico apontado na análise.
     */
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}