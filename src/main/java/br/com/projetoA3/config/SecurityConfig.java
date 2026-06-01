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
import org.springframework.security.crypto.password.NoOpPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

/**
 * Configuração do Spring Security para Spring Boot 3.x / Spring Security 6.x
 * 
 * ✅ BUG 1 FIX: Autorização de /usuarios/** apenas para ADMINISTRADOR
 * ✅ BUG 2 FIX: Autorização de /relatorios/** para ADMINISTRADOR e GERENTE
 * 
 * ⚠️ ATENÇÃO: Usa NoOpPasswordEncoder (senhas em texto puro) apenas para
 * fins acadêmicos/demonstração. Em produção, SEMPRE use BCrypt ou similar.
 */
@Configuration
@EnableWebSecurity
@EnableMethodSecurity
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
                // Recursos estáticos públicos
                .requestMatchers("/css/**", "/js/**", "/img/**", "/webjars/**").permitAll()
                
                // Páginas de autenticação públicas
                .requestMatchers("/login", "/registro", "/erro/**").permitAll()
                
                // ✅ BUG 1 FIX: Área de Usuários restrita ao ADMINISTRADOR
                .requestMatchers("/usuarios/**").hasRole("ADMINISTRADOR")
                
                // ✅ BUG 2 FIX: Relatórios disponíveis para ADMINISTRADOR e GERENTE
                .requestMatchers("/relatorios/**").hasAnyRole("ADMINISTRADOR", "GERENTE")
                
                // Área de configurações restrita ao ADMINISTRADOR
                .requestMatchers("/configuracoes/**").hasRole("ADMINISTRADOR")
                
                // Demais endpoints exigem autenticação
                .anyRequest().authenticated()
            )
            
            // ==========================================
            // FORMULÁRIO DE LOGIN PERSONALIZADO
            // ==========================================
            .formLogin(form -> form
                .loginPage("/login")
                .loginProcessingUrl("/login")
                .defaultSuccessUrl("/menu", true)
                .failureUrl("/login?error=true")
                .usernameParameter("login")
                .passwordParameter("senha")
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
            // PROTEÇÃO CSRF - Desabilitada para APIs REST (se necessário, reative para forms)
            // ==========================================
            .csrf(csrf -> csrf.disable())  // CORREÇÃO: sintaxe correta para Spring Security 6.x
            
            // ==========================================
            // HEADERS DE SEGURANÇA
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
     * ⚠️ NoOpPasswordEncoder - NÃO usar em produção!
     * Aceita senhas em texto puro para facilitar testes acadêmicos.
     */
    @Bean
    @SuppressWarnings("deprecation")
    public PasswordEncoder passwordEncoder() {
        return NoOpPasswordEncoder.getInstance();
    }
}