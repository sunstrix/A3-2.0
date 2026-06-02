package br.com.projetoA3.repository;

import br.com.projetoA3.model.Projeto;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Repository responsável pelas operações de persistência da entidade Projeto.
 * Atualizado com consultas analíticas otimizadas para o Dashboard.
 */
@Repository
public interface ProjetoRepository extends JpaRepository<Projeto, Long> {

    // ==========================================
    // MÉTODOS EXISTENTES (PRESERVADOS)
    // ==========================================

    List<Projeto> findByStatus(Projeto.StatusProjeto status);

    List<Projeto> findByNomeContainingIgnoreCase(String nome);

    List<Projeto> findByStatusAndNomeContainingIgnoreCase(Projeto.StatusProjeto status, String nome);

    // ==========================================
    // ✅ NOVOS MÉTODOS ANALÍTICOS (DASHBOARD)
    // ==========================================

    /**
     * Agrupa projetos por status para métricas globais.
     * Retorna: [Status, Quantidade]
     */
    @Query("SELECT p.status, COUNT(p) FROM Projeto p GROUP BY p.status")
    List<Object[]> countProjectsByStatusGrouped();

    /**
     * Agrupa a quantidade de projetos por nome da Equipe.
     * Útil para o Gráfico de Barras de distribuição de carga por time.
     * Retorna: [NomeEquipe, Quantidade]
     */
    @Query("SELECT e.nome, COUNT(p) FROM Projeto p JOIN p.equipe e GROUP BY e.nome")
    List<Object[]> countProjectsByEquipeGrouped();
    
    /**
     * Conta o total de projetos de forma direta (mais rápido que count de lista).
     */
    long count();
}