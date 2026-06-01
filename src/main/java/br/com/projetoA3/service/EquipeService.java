package br.com.projetoA3.service;

import br.com.projetoA3.model.Equipe;
import br.com.projetoA3.model.Usuario;
import br.com.projetoA3.repository.EquipeRepository;
import br.com.projetoA3.repository.UsuarioRepository;
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
 * LazyInitializationException no Thymeleaf ao acessar equipe.membros
 * e equipe.lider na view equipe/list.html.
 * 
 * Princípios aplicados:
 * - Injeção por construtor (sem @Autowired em campo)
 * - @Transactional(readOnly = true) na classe (otimiza transações de leitura)
 * - Override com @Transactional apenas em métodos de escrita
 * - Inicialização eager de coleções lazy dentro do contexto transacional
 */
@Service
@Transactional(readOnly = true)
public class EquipeService {

    private final EquipeRepository equipeRepository;
    private final UsuarioRepository usuarioRepository;

    /**
     * Injeção de dependências via construtor (padrão Spring moderno).
     */
    public EquipeService(EquipeRepository equipeRepository,
                         UsuarioRepository usuarioRepository) {
        this.equipeRepository = equipeRepository;
        this.usuarioRepository = usuarioRepository;
    }

    // ==========================================
    // CONSULTAS (READ-ONLY)
    // ==========================================

    /**
     * Lista todas as equipes com suas coleções lazy já inicializadas.
     * 
     * ✅ CORREÇÃO: Força o carregamento de 'membros' e 'lider' DENTRO
     * da transação, evitando LazyInitializationException quando o
     * Thymeleaf tenta acessar essas coleções na view.
     */
    public List<Equipe> findAll() {
        List<Equipe> equipes = equipeRepository.findAll();
        
        // Força inicialização das coleções lazy dentro do contexto transacional
        for (Equipe equipe : equipes) {
            inicializarColecoes(equipe);
        }
        
        return equipes;
    }

    /**
     * Busca uma equipe por ID, garantindo que as coleções estejam inicializadas.
     */
    public Optional<Equipe> findById(Long id) {
        Optional<Equipe> optEquipe = equipeRepository.findById(id);
        optEquipe.ifPresent(this::inicializarColecoes);
        return optEquipe;
    }

    /**
     * Conta o total de equipes cadastradas.
     */
    public long count() {
        return equipeRepository.count();
    }

    // ==========================================
    // ESCRITA (TRANSACTIONAL) - override do readOnly
    // ==========================================

    /**
     * Salva uma nova equipe.
     */
    @Transactional
    public Equipe save(Equipe equipe) {
        // Se veio um lider apenas com ID, busca a entidade completa
        if (equipe.getLider() != null && equipe.getLider().getId() != null) {
            Usuario lider = usuarioRepository.findById(equipe.getLider().getId())
                    .orElseThrow(() -> new EntityNotFoundException(
                        "Líder não encontrado: " + equipe.getLider().getId()));
            equipe.setLider(lider);
        }
        
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
        if (dadosAtualizados.getLider() != null && dadosAtualizados.getLider().getId() != null) {
            Usuario lider = usuarioRepository.findById(dadosAtualizados.getLider().getId())
                    .orElseThrow(() -> new EntityNotFoundException(
                        "Líder não encontrado: " + dadosAtualizados.getLider().getId()));
            existente.setLider(lider);
        } else {
            existente.setLider(null);
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

    // ==========================================
    // MÉTODOS AUXILIARES PRIVADOS
    // ==========================================

    /**
     * Força a inicialização das coleções lazy da equipe.
     * Deve ser chamado DENTRO de um contexto transacional ativo.
     * 
     * Inicializa:
     * - membros (lista de membros da equipe)
     * - lider (associação ManyToOne)
     */
    private void inicializarColecoes(Equipe equipe) {
        if (equipe == null) return;
        
        // Inicializa a coleção de membros (evita LazyInitializationException)
        if (equipe.getMembros() != null) {
            Hibernate.initialize(equipe.getMembros());
        }
        
        // Inicializa a associação com o líder
        if (equipe.getLider() != null) {
            Hibernate.initialize(equipe.getLider());
        }
    }
}