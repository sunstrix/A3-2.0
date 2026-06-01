package br.com.projetoA3.repository;

import br.com.projetoA3.model.Projeto;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Repository responsável pelas operações de persistência da entidade Projeto.
 * 
 * Inclui métodos personalizados para filtragem por status e busca textual.
 */
@Repository
public interface ProjetoRepository extends JpaRepository<Projeto, Long> {

    /**
     * Busca projetos filtrados por status.
     * 
     * @param status O status do projeto a ser buscado (Enum StatusProjeto).
     * @return Lista de projetos com o status especificado.
     */
    List<Projeto> findByStatus(Projeto.StatusProjeto status);

    /**
     * Busca projetos pelo nome contendo a string informada (case insensitive).
     * 
     * @param nome Nome ou parte do nome do projeto.
     * @return Lista de projetos que contêm o nome.
     */
    List<Projeto> findByNomeContainingIgnoreCase(String nome);

    /**
     * Busca projetos filtrados por status E contendo o nome informado.
     * 
     * @param status Status do projeto.
     * @param nome Nome ou parte do nome.
     * @return Lista de projetos filtrados.
     */
    List<Projeto> findByStatusAndNomeContainingIgnoreCase(Projeto.StatusProjeto status, String nome);
}