package br.com.projetoA3.service;

import br.com.projetoA3.model.Equipe;
import br.com.projetoA3.repository.EquipeRepository;
import jakarta.persistence.EntityNotFoundException;
import org.hibernate.Hibernate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

/**
 * Service responsável pela lógica de negócio das Equipes.
 * 
 * ✅ BUG FIX: Adicionado Hibernate.initialize() para resolver 
 * LazyInitializationException no Thymeleaf ao acessar equipe.membros.
 */
@Service
@Transactional(readOnly = true)
public class EquipeService {

    private final EquipeRepository equipeRepository;

    public EquipeService(EquipeRepository equipeRepository) {
        this.equipeRepository = equipeRepository;
    }

    /**
     * Lista todas as equipes com suas coleções lazy já inicializadas.
     */
    public List<Equipe> findAll() {
        List<Equipe> equipes = equipeRepository.findAll();
        
        // ✅ CORREÇÃO: Força o carregamento das coleções lazy DENTRO da transação.
        // Isso evita o LazyInitializationException quando o Thymeleaf tenta
        // acessar equipe.membros.size() na view após a transação ser fechada.
        for (Equipe equipe : equipes) {
            Hibernate.initialize(equipe.getMembros());
            Hibernate.initialize(equipe.getLider());
        }
        
        return equipes;
    }

    /**
     * Busca uma equipe por ID, garantindo que as coleções estejam inicializadas.
     */
    public Optional<Equipe> findById(Long id) {
        Optional<Equipe> optEquipe = equipeRepository.findById(id);
        optEquipe.ifPresent(equipe -> {
            Hibernate.initialize(equipe.getMembros());
            Hibernate.initialize(equipe.getLider());
        });
        return optEquipe;
    }

    /**
     * Salva uma nova equipe.
     */
    @Transactional
    public Equipe save(Equipe equipe) {
        return equipeRepository.save(equipe);
    }

    /**
     * Atualiza uma equipe existente.
     */
    @Transactional
    public Equipe update(Long id, Equipe dadosAtualizados) {
        Equipe existente = equipeRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Equipe não encontrada: " + id));
        
        existente.setNome(dadosAtualizados.getNome());
        existente.setDescricao(dadosAtualizados.getDescricao());
        
        // Atualiza o líder se foi enviado
        if (dadosAtualizados.getLider() != null) {
            existente.setLider(dadosAtualizados.getLider());
        }
        
        return equipeRepository.save(existente);
    }

    /**
     * Remove uma equipe do sistema.
     */
    @Transactional
    public void deleteById(Long id) {
        if (!equipeRepository.existsById(id)) {
            throw new EntityNotFoundException("Equipe não encontrada: " + id);
        }
        equipeRepository.deleteById(id);
    }
}