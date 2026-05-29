package br.com.projetoA3.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.crypto.password.NoOpPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
            // Desabilitar CSRF para simplificar o desenvolvimento
            .csrf(csrf -> csrf.disable())
            // Configurar permissões de acesso
            .authorizeHttpRequests(auth -> auth
                .requestMatchers("/login", "/css/**", "/js/**", "/images/**", "/", "/error").permitAll()
                .anyRequest().authenticated()
            )
            // Configurar formulário de login nativo do Spring Security
            .formLogin(form -> form
                .loginPage("/login")
                .defaultSuccessUrl("/menu", true)
                .failureUrl("/login?error=true")
                .permitAll()
            )
            // Configurar logout
            .logout(logout -> logout
                .logoutUrl("/logout")
                .logoutSuccessUrl("/login")
                .invalidateHttpSession(true)
                .deleteCookies("JSESSIONID")
                .permitAll()
            );
        
        return http.build();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        // ⚠️ APENAS PARA DESENVOLVIMENTO: permite senhas em texto puro
        // Para produção, substitua por: return new BCryptPasswordEncoder();
        return NoOpPasswordEncoder.getInstance();
    }
}