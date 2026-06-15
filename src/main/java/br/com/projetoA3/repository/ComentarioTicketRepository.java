package br.com.projetoA3.repository;

import br.com.projetoA3.model.ComentarioTicket;
import br.com.projetoA3.model.Ticket;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Repositorio responsavel pelas operacoes de persistencia da entidade ComentarioTicket.
 * Herda o CRUD padrao do Spring Data JPA e adiciona metodos para listar o historico
 * de interacoes de um chamado especifico.
 */
@Repository
public interface ComentarioTicketRepository extends JpaRepository<ComentarioTicket, Long> {

    /**
     * Busca todos os comentarios de um ticket, ordenados do mais antigo para o mais recente.
     * Util para exibir a linha do tempo completa do chamado (incluindo notas internas).
     */
    List<ComentarioTicket> findByTicketOrderByDataCriacaoAsc(Ticket ticket);

    /**
     * Busca apenas os comentarios publicos de um ticket (onde notaInterna = false).
     * Essencial para a visualizacao do historico na area do solicitante/cliente.
     */
    List<ComentarioTicket> findByTicketAndNotaInternaFalseOrderByDataCriacaoAsc(Ticket ticket);

    /**
     * Conta o numero total de interacoes (comentarios) em um ticket especifico.
     * Util para exibir badges de atividade nas listas de chamados.
     */
    long countByTicket(Ticket ticket);
}