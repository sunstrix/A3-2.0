package br.com.projetoA3.exception;

import jakarta.persistence.EntityNotFoundException;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.ui.Model;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.servlet.ModelAndView;

import java.util.stream.Collectors;

/**
 * Centralizador de tratamento de exceções da aplicação.
 * ✅ Refatoração Sênior: Evita exposição de stack traces e melhora a UX.
 */
@ControllerAdvice
public class GlobalExceptionHandler {

    /**
     * Trata erros de recurso não encontrado (404).
     */
    @ExceptionHandler(EntityNotFoundException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public ModelAndView handleEntityNotFound(EntityNotFoundException ex) {
        ModelAndView mav = new ModelAndView("erro/404");
        mav.addObject("mensagem", "O recurso solicitado não foi encontrado em nosso banco de dados.");
        mav.addObject("detalhe", ex.getMessage());
        return mav;
    }

    /**
     * Trata erros de permissão negada (403).
     */
    @ExceptionHandler(AccessDeniedException.class)
    @ResponseStatus(HttpStatus.FORBIDDEN)
    public ModelAndView handleAccessDenied(AccessDeniedException ex) {
        ModelAndView mav = new ModelAndView("erro/403");
        mav.addObject("mensagem", "Você não possui permissão suficiente para realizar esta ação.");
        return mav;
    }

    /**
     * Trata erros de validação de formulários (400).
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ModelAndView handleValidationErrors(MethodArgumentNotValidException ex) {
        ModelAndView mav = new ModelAndView("erro/400");
        String erros = ex.getBindingResult().getFieldErrors().stream()
                .map(error -> error.getField() + ": " + error.getDefaultMessage())
                .collect(Collectors.joining(", "));
        
        mav.addObject("mensagem", "Existem erros no preenchimento dos campos.");
        mav.addObject("erros", erros);
        return mav;
    }

    /**
     * Handler genérico para qualquer outra exceção não tratada (500).
     * ✅ SEGURANÇA: Oculta detalhes técnicos em produção.
     */
    @ExceptionHandler(Exception.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public ModelAndView handleGenericException(Exception ex) {
        ModelAndView mav = new ModelAndView("erro/500");
        mav.addObject("mensagem", "Ocorreu um erro interno inesperado.");
        // Em um cenário real de produção, aqui faríamos o LOG do erro mas não mostraríamos o stacktrace no View
        return mav;
    }
}