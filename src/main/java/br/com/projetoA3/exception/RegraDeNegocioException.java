package br.com.projetoA3.exception;

/**
 * Exceção lançada quando uma regra de negócio é violada.
 * Utilizada para sinalizar condições específicas do domínio que impedem 
 * a continuidade da operação, separando erros lógicos de falhas técnicas.
 */
public class RegraDeNegocioException extends RuntimeException {

    public RegraDeNegocioException(String mensagem) {
        super(mensagem);
    }

    public RegraDeNegocioException(String mensagem, Throwable causa) {
        super(mensagem, causa);
    }
}