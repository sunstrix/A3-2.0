package br.com.projetoA3.repository;

import br.com.projetoA3.model.EquipeMembro;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface EquipeMembroRepository extends JpaRepository<EquipeMembro, Long> {
    
    // ✅ Métodos existentes (NÃO REMOVER)
    List<EquipeMembro> findByEquipeId(Long equipeId);
    List<EquipeMembro> findByUsuarioId(Long usuarioId);
    
    // ✅ NOVO: Deleta todos os membros de uma equipe (usado na sincronização)
    void deleteByEquipeId(Long equipeId);
}