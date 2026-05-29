package br.com.projetoA3.repository;

import br.com.projetoA3.model.EquipeMembro;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface EquipeMembroRepository extends JpaRepository<EquipeMembro, Long> {
    
    // ✅ Buscar todos os membros de uma equipe específica
    List<EquipeMembro> findByEquipeId(Long equipeId);

    // ✅ Buscar todas as equipes que um determinado usuário participa
    List<EquipeMembro> findByUsuarioId(Long usuarioId);
}