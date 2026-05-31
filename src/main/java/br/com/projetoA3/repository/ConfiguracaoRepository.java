package br.com.projetoA3.repository;

import br.com.projetoA3.model.Configuracao;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface ConfiguracaoRepository extends JpaRepository<Configuracao, Long> {

    /**
     * Busca uma configuração específica pela sua chave única (ex: "EMAIL_HOST", "EMAIL_PORT")
     */
    Optional<Configuracao> findByChave(String chave);
}