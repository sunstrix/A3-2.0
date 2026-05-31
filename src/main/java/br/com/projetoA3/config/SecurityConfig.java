package br.com.projetoA3.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

/**
 * Configuração moderna do Spring Security para Spring Boot 3.x / Spring Security 6.x
 * 
 * Substitui o antigo WebSecurityConfigurerAdapter (removido no Spring Security 5.7+).
 * Utiliza a nova API lambda-based e expõe o SecurityFilterChain como @Bean.
 * 
 * Características:
 * - Autenticação baseada em formulário personalizado
 * - Autorização por URL e por método (@PreAuthorize)
 * - Proteção CSRF ativa (padrão seguro)
 * - Gerenciamento de sessão com máximo de 1 sessão por usuário
 * - BCrypt com fator de custo 12 para hashing de senhas
 * - Logout com invalidação de sessão e limpeza de cookies
 */
@Configuration
@EnableWebSecurity
@EnableMethodSecurity // Permite uso de @PreAuthorize em controllers e services
public class SecurityConfig {

    /**
     * Configuração principal da cadeia de filtros de segurança.
     * Define regras de autenticação, autorização, login, logout e sessões.
     */
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
                
                // Área administrativa restrita ao perfil ADMINISTRADOR
                .requestMatchers("/configuracoes/**").hasRole("ADMINISTRADOR")
                
                // Relatórios disponíveis para ADMIN e GERENTE
                .requestMatchers("/relatorios/**").hasAnyRole("ADMINISTRADOR", "GERENTE")
                
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
                .maxSessionsPreventsLogin(false) // Nova sessão expulsa a antiga
            )
            
            // ==========================================
            // PROTEÇÃO CSRF (mantida ativa por padrão)
            // ==========================================
            // O CSRF é essencial para aplicações web com formulários e sessões.
            // Para requisições AJAX (ex: mover tarefas no Kanban), o token CSRF
            // deve ser incluído no header da requisição. Veja csrf-meta.html.
            .csrf(csrf -> csrf
                // Se houver endpoints de API REST pura no futuro, excluí-los aqui:
                // .ignoringRequestMatchers("/api/**")
            )
            
            // ==========================================
            // HEADERS DE SEGURANÇA
            // ==========================================
            .headers(headers -> headers
                .frameOptions(frame -> frame.deny()) // Previne clickjacking
                .contentTypeOptions(content -> {}) // X-Content-Type-Options: nosniff
                .xssProtection(xss -> {}) // X-XSS-Protection
            )
            
            .build();
    }

    /**
     * Encoder de senhas usando BCrypt.
     * 
     * Fator de custo 12 oferece bom equilíbrio entre segurança e performance.
     * Cada incremento dobra o tempo de hashing (13 = 2x mais lento que 12).
     * 
     * IMPORTANTE: Senhas nunca devem ser armazenadas em texto plano.
     * Sempre use passwordEncoder.encode(senha) antes de salvar no banco.
     */
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder(12);
    }
}