package br.com.projetoA3.repository;

import br.com.projetoA3.model.Projeto;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ProjetoRepository extends JpaRepository<Projeto, Long> {
    
    Optional<Projeto> findByNome(String nome);
    
    List<Projeto> findByAtivoTrue();
    
    @Query("SELECT p FROM Projeto p LEFT JOIN FETCH p.tarefas WHERE p.id = :id")
    Optional<Projeto> findByIdComTarefas(@Param("id") Long id);
    
    @Query("SELECT p FROM Projeto p LEFT JOIN FETCH p.equipe WHERE p.id = :id")
    Optional<Projeto> findByIdComEquipe(@Param("id") Long id);
    
    @Query("SELECT p FROM Projeto p LEFT JOIN FETCH p.tarefas LEFT JOIN FETCH p.equipe WHERE p.id = :id")
    Optional<Projeto> findByIdCompleto(@Param("id") Long id);
    
    @Query("SELECT DISTINCT p FROM Projeto p LEFT JOIN FETCH p.tarefas WHERE p.ativo = true")
    List<Projeto> findAllAtivosComTarefas();
}