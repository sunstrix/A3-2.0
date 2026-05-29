package br.com.projetoA3.service;

import br.com.projetoA3.model.Equipe;
import br.com.projetoA3.model.EquipeMembro;
import br.com.projetoA3.model.Usuario;
import br.com.projetoA3.repository.EquipeMembroRepository;
import br.com.projetoA3.repository.UsuarioRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class EquipeMembroService {

    private final EquipeMembroRepository equipeMembroRepository;
    private final UsuarioRepository usuarioRepository; // ✅ Novo: injetado via construtor

    public EquipeMembroService(EquipeMembroRepository equipeMembroRepository, UsuarioRepository usuarioRepository) {
        this.equipeMembroRepository = equipeMembroRepository;
        this.usuarioRepository = usuarioRepository;
    }

    // ✅ Métodos existentes (NÃO REMOVER)
    public List<EquipeMembro> findAll() {
        return equipeMembroRepository.findAll();
    }

    public Optional<EquipeMembro> findById(Long id) {
        return equipeMembroRepository.findById(id);
    }

    public List<EquipeMembro> findByEquipeId(Long equipeId) {
        return equipeMembroRepository.findByEquipeId(equipeId);
    }

    public List<EquipeMembro> findByUsuarioId(Long usuarioId) {
        return equipeMembroRepository.findByUsuarioId(usuarioId);
    }

    public EquipeMembro save(EquipeMembro membro) {
        return equipeMembroRepository.save(membro);
    }

    public void deleteById(Long id) {
        equipeMembroRepository.deleteById(id);
    }

    // ✅ NOVO MÉTODO: Sincroniza membros de uma equipe (adiciona/remove conforme lista)
    @Transactional
    public void sincronizarMembros(Equipe equipe, List<Long> novosMembrosIds) {
        // Se não vierem IDs, remove todos os membros existentes
        if (novosMembrosIds == null || novosMembrosIds.isEmpty()) {
            equipeMembroRepository.deleteByEquipeId(equipe.getId());
            return;
        }

        // IDs que já estão salvos
        List<Long> idsJaSalvos = equipeMembroRepository.findByEquipeId(equipe.getId())
            .stream()
            .map(m -> m.getUsuario().getId())
            .collect(Collectors.toList());

        // Remover membros que saíram da lista
        for (EquipeMembro membro : equipeMembroRepository.findByEquipeId(equipe.getId())) {
            if (!novosMembrosIds.contains(membro.getUsuario().getId())) {
                equipeMembroRepository.delete(membro);
            }
        }

        // Adicionar novos membros
        for (Long usuarioId : novosMembrosIds) {
            if (!idsJaSalvos.contains(usuarioId)) {
                Usuario usuario = usuarioRepository.findById(usuarioId).orElse(null);
                if (usuario != null) {
                    EquipeMembro novoMembro = new EquipeMembro(equipe, usuario, "Membro");
                    equipeMembroRepository.save(novoMembro);
                }
            }
        }
    }
}