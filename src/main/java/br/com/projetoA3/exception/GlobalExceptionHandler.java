package br.com.projetoA3.exception;

import jakarta.persistence.EntityNotFoundException;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

/**
 * Handler global de exceções utilizando @ControllerAdvice.
 * Centraliza o tratamento de erros, evitando exposição de stack traces
 * e garantindo feedback amigável ao usuário final.
 */
@ControllerAdvice
public class GlobalExceptionHandler {

    /**
     * Captura entidades não encontradas no banco (ex: Projeto, Tarefa, Equipe)
     */
    @ExceptionHandler(EntityNotFoundException.class)
    public String handleNotFound(EntityNotFoundException ex, Model model) {
        model.addAttribute("mensagem", ex.getMessage());
        model.addAttribute("codigo", 404);
        return "error/404";
    }

    /**
     * Captura negações de acesso pelo Spring Security
     */
    @ExceptionHandler(AccessDeniedException.class)
    public String handleForbidden(AccessDeniedException ex, Model model) {
        model.addAttribute("mensagem", "Acesso negado. Você não tem permissão para realizar esta ação.");
        model.addAttribute("codigo", 403);
        return "error/403";
    }

    /**
     * Captura violações de regras de negócio definidas no sistema
     */
    @ExceptionHandler(RegraDeNegocioException.class)
    public String handleBusinessRule(RegraDeNegocioException ex, RedirectAttributes attributes) {
        attributes.addFlashAttribute("erro", ex.getMessage());
        return "redirect:/menu";
    }

    /**
     * Captura tentativas de acesso bloqueadas por lógica de negócio
     */
    @ExceptionHandler(AcessoNegadoException.class)
    public String handleBusinessAccess(AcessoNegadoException ex, RedirectAttributes attributes) {
        attributes.addFlashAttribute("erro", ex.getMessage());
        return "redirect:/menu";
    }

    /**
     * Captura qualquer exceção não tratada (fallback seguro)
     */
    @ExceptionHandler(Exception.class)
    public String handleGeneric(Exception ex, Model model) {
        // Em produção, adicione logging: log.error("Erro inesperado", ex);
        model.addAttribute("mensagem", "Ocorreu um erro interno no sistema. Tente novamente mais tarde.");
        model.addAttribute("codigo", 500);
        return "error/500";
    }
}