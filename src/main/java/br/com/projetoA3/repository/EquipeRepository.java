package br.com.projetoA3.repository;

import br.com.projetoA3.model.Equipe;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface EquipeRepository extends JpaRepository<Equipe, Long> {
    
    // ✅ Buscar equipes filtradas pelo líder responsável
    List<Equipe> findByLiderId(Long liderId);
}