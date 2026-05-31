package br.com.projetoA3.exception;

/**
 * Exceção lançada quando um usuário tenta acessar ou executar uma operação
 * para a qual não possui permissão adequada no contexto da aplicação.
 * Complementa o tratamento de segurança do Spring Security com regras de negócio.
 */
public class AcessoNegadoException extends RuntimeException {

    public AcessoNegadoException(String mensagem) {
        super(mensagem);
    }

    public AcessoNegadoException(String mensagem, Throwable causa) {
        super(mensagem, causa);
    }
}