package br.com.projetoA3.repository;

import br.com.projetoA3.model.Projeto;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface ProjetoRepository extends JpaRepository<Projeto, Long> {
    
    // ✅ Buscar projetos filtrados por status
    List<Projeto> findByStatus(Projeto.StatusProjeto status);
    
    // ✅ Buscar projetos atribuídos a um gerente específico
    List<Projeto> findByGerenteId(Long gerenteId);
}