package br.com.projetoA3.repository;

import br.com.projetoA3.model.Ticket;
import br.com.projetoA3.model.Usuario;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Repositorio responsavel pelas operacoes de persistencia da entidade Ticket.
 * Estende o JpaRepository para herdar todos os metodos padroes de CRUD, 
 * preservando a integracao com o Spring Data JPA e o banco SQLite.
 */
@Repository
public interface TicketRepository extends JpaRepository<Ticket, Long> {

    /**
     * Busca todos os tickets que possuem um determinado status 
     * (ex: ABERTO, EM_ANDAMENTO, RESOLVIDO, FECHADO).
     */
    List<Ticket> findByStatus(String status);

    /**
     * Busca todos os tickets abertos por um usuario especifico (Solicitante).
     * Util para a area "Meus Chamados" do portal do cliente/colaborador.
     */
    List<Ticket> findBySolicitante(Usuario solicitante);

    /**
     * Busca todos os tickets atribuidos a um atendente especifico.
     * Util para o painel de controle do atendente.
     */
    List<Ticket> findByAtendente(Usuario atendente);

    /**
     * Conta a quantidade total de tickets com um determinado status.
     * Util para exibir badges e metricas no dashboard do Help Desk.
     */
    long countByStatus(String status);

    /**
     * Realiza uma busca textual (case-insensitive) no titulo ou na descricao do ticket.
     * Essencial para a barra de pesquisa global do sistema de chamados.
     */
    List<Ticket> findByTituloContainingIgnoreCaseOrDescricaoContainingIgnoreCase(String titulo, String descricao);

    /**
     * Recupera todos os tickets ordenados do mais recente para o mais antigo.
     * Padrao ideal para exibicao em listas de acompanhamento.
     */
    List<Ticket> findAllByOrderByDataCriacaoDesc();
    
    /**
     * Recupera tickets de um determinado status ordenados por data de criacao (mais antigos primeiro).
     * Util para a fila de atendimento (SLA), priorizando os chamados que estao abertos ha mais tempo.
     */
    List<Ticket> findByStatusOrderByDataCriacaoAsc(String status);
}