package br.com.projetoA3.service;

import br.com.projetoA3.model.EquipeMembro;
import br.com.projetoA3.repository.EquipeMembroRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class EquipeMembroService {

    private final EquipeMembroRepository equipeMembroRepository;

    public EquipeMembroService(EquipeMembroRepository equipeMembroRepository) {
        this.equipeMembroRepository = equipeMembroRepository;
    }

    // ✅ Listar todos os membros de todas as equipes
    public List<EquipeMembro> findAll() {
        return equipeMembroRepository.findAll();
    }

    // ✅ Buscar membro por ID
    public Optional<EquipeMembro> findById(Long id) {
        return equipeMembroRepository.findById(id);
    }

    // ✅ Buscar membros de uma equipe específica
    public List<EquipeMembro> findByEquipeId(Long equipeId) {
        return equipeMembroRepository.findByEquipeId(equipeId);
    }

    // ✅ Buscar todas as alocações de um usuário
    public List<EquipeMembro> findByUsuarioId(Long usuarioId) {
        return equipeMembroRepository.findByUsuarioId(usuarioId);
    }

    // ✅ Salvar ou atualizar membro
    public EquipeMembro save(EquipeMembro membro) {
        return equipeMembroRepository.save(membro);
    }

    // ✅ Deletar membro por ID
    public void deleteById(Long id) {
        equipeMembroRepository.deleteById(id);
    }
}