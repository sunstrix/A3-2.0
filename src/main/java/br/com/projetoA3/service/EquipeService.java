package br.com.projetoA3.service;

import br.com.projetoA3.model.Equipe;
import br.com.projetoA3.repository.EquipeRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class EquipeService {

    private final EquipeRepository equipeRepository;

    public EquipeService(EquipeRepository equipeRepository) {
        this.equipeRepository = equipeRepository;
    }

    // ✅ Listar todas as equipes
    public List<Equipe> findAll() {
        return equipeRepository.findAll();
    }

    // ✅ Buscar equipe por ID
    public Optional<Equipe> findById(Long id) {
        return equipeRepository.findById(id);
    }

    // ✅ Filtrar equipes pelo líder responsável
    public List<Equipe> findByLiderId(Long liderId) {
        return equipeRepository.findByLiderId(liderId);
    }

    // ✅ Salvar ou atualizar equipe
    public Equipe save(Equipe equipe) {
        return equipeRepository.save(equipe);
    }

    // ✅ Deletar equipe por ID
    public void deleteById(Long id) {
        equipeRepository.deleteById(id);
    }
}