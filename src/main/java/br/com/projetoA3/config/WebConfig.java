package br.com.projetoA3.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import org.springframework.lang.NonNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.web.servlet.HandlerInterceptor;

/**
 * Configuração Web MVC do Spring.
 * Define CORS, recursos estáticos e interceptadores.
 */
@Configuration
public class WebConfig implements WebMvcConfigurer {

    private static final Logger logger = LoggerFactory.getLogger(WebConfig.class);

    /**
     * Configuração de CORS para permitir requisições de outras origens.
     * Ajuste conforme necessário para produção.
     */
    @Override
    public void addCorsMappings(@NonNull CorsRegistry registry) {
        registry.addMapping("/**")
                .allowedOrigins("http://localhost:8080", "http://127.0.0.1:8080")
                .allowedMethods("GET", "POST", "PUT", "DELETE", "OPTIONS")
                .allowedHeaders("*")
                .allowCredentials(true)
                .maxAge(3600);
    }

    /**
     * Configuração de recursos estáticos (CSS, JS, imagens).
     */
    @Override
    public void addResourceHandlers(@NonNull ResourceHandlerRegistry registry) {
        registry.addResourceHandler("/css/**")
                .addResourceLocations("classpath:/static/css/", "classpath:/public/css/");
        registry.addResourceHandler("/js/**")
                .addResourceLocations("classpath:/static/js/", "classpath:/public/js/");
        registry.addResourceHandler("/img/**")
                .addResourceLocations("classpath:/static/img/", "classpath:/public/img/");
        registry.addResourceHandler("/webjars/**")
                .addResourceLocations("classpath:/META-INF/resources/webjars/");
    }

    /**
     * Configuração de interceptadores (exemplo: logging de requisições).
     */
    @Override
    public void addInterceptors(@NonNull InterceptorRegistry registry) {
        registry.addInterceptor(new HandlerInterceptor() {
            @Override
            public boolean preHandle(@NonNull HttpServletRequest request, 
                                     @NonNull HttpServletResponse response, 
                                     @NonNull Object handler) {
                long startTime = System.currentTimeMillis();
                request.setAttribute("startTime", startTime);
                logger.info("Requisição: {} {}", request.getMethod(), request.getRequestURI());
                return true;
            }

            @Override
            public void afterCompletion(@NonNull HttpServletRequest request, 
                                        @NonNull HttpServletResponse response, 
                                        @NonNull Object handler, 
                                        Exception ex) {
                long startTime = (Long) request.getAttribute("startTime");
                long duration = System.currentTimeMillis() - startTime;
                logger.info("Resposta: {} {} - {} - {}ms", 
                    request.getMethod(), 
                    request.getRequestURI(), 
                    response.getStatus(), 
                    duration);
            }
        }).addPathPatterns("/**");
    }
}