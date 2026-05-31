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
 * Configuração moderna do Spring Security para Spring Boot 3.x / Spring Security 6.x
 */
@Configuration
@EnableWebSecurity
@EnableMethodSecurity
public class SecurityConfig {

    private final CustomUserDetailsService userDetailsService;

    // ✅ Injeção do nosso UserDetailsService personalizado
    public SecurityConfig(CustomUserDetailsService userDetailsService) {
        this.userDetailsService = userDetailsService;
    }

    /**
     * Configuração principal da cadeia de filtros de segurança.
     */
    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        return http
            // ==========================================
            // AUTORIZAÇÃO DE REQUISIÇÕES
            // ==========================================
            .authorizeHttpRequests(auth -> auth
                .requestMatchers("/css/**", "/js/**", "/img/**", "/webjars/**").permitAll()
                .requestMatchers("/login", "/registro", "/erro/**").permitAll()
                .requestMatchers("/configuracoes/**").hasRole("ADMINISTRADOR")
                .requestMatchers("/relatorios/**").hasAnyRole("ADMINISTRADOR", "GERENTE")
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
                .usernameParameter("login")    // Campo do formulário HTML
                .passwordParameter("senha")    // Campo do formulário HTML
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
            .csrf(csrf -> {})
            
            // ==========================================
            // HEADERS DE SEGURANÇA
            // ==========================================
            .headers(headers -> headers
                .frameOptions(frame -> frame.deny())
            )
            
            // ✅ USA NOSSO AUTHENTICATION PROVIDER PERSONALIZADO
            .authenticationProvider(authenticationProvider())
            
            .build();
    }

    /**
     * ✅ CRÍTICO: Configura o DaoAuthenticationProvider que conecta:
     * - Nosso CustomUserDetailsService (busca usuário no banco)
     * - Nosso PasswordEncoder (BCrypt para validar a senha)
     */
    @Bean
    public DaoAuthenticationProvider authenticationProvider() {
        DaoAuthenticationProvider authProvider = new DaoAuthenticationProvider();
        authProvider.setUserDetailsService(userDetailsService);
        authProvider.setPasswordEncoder(passwordEncoder());
        return authProvider;
    }

    /**
     * AuthenticationManager usado em alguns casos de teste ou login programático.
     */
    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration authConfig) throws Exception {
        return authConfig.getAuthenticationManager();
    }

    /**
     * Encoder de senhas usando BCrypt com fator de custo 12.
     */
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder(12);
    }
}