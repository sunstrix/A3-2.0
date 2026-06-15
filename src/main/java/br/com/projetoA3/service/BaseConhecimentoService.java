package br.com.projetoA3.service;

import br.com.projetoA3.model.ArtigoBaseConhecimento;
import br.com.projetoA3.model.Usuario;
import br.com.projetoA3.repository.ArtigoBaseConhecimentoRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

/**
 * Servico responsavel pela regra de negocio do modulo de Base de Conhecimento.
 * Gerencia artigos, tutoriais, FAQs e documentacoes do sistema de Help Desk.
 */
@Service
public class BaseConhecimentoService {

    private static final Logger logger = LoggerFactory.getLogger(BaseConhecimentoService.class);

    @Autowired
    private ArtigoBaseConhecimentoRepository artigoRepository;

    /**
     * Cria e salva um novo artigo na base de conhecimento.
     */
    @Transactional
    public ArtigoBaseConhecimento criarArtigo(ArtigoBaseConhecimento artigo, Usuario autor) {
        artigo.setAutor(autor);
        ArtigoBaseConhecimento novoArtigo = artigoRepository.save(artigo);
        logger.info("Artigo de Base de Conhecimento '{}' (ID: {}) criado por {}", 
                novoArtigo.getTitulo(), novoArtigo.getId(), autor.getNome());
        return novoArtigo;
    }

    /**
     * Atualiza as informacoes de um artigo existente.
     */
    @Transactional
    public ArtigoBaseConhecimento atualizarArtigo(Long id, ArtigoBaseConhecimento dadosAtualizados) {
        ArtigoBaseConhecimento artigoExistente = artigoRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Artigo não encontrado na base de conhecimento."));
        
        artigoExistente.setTitulo(dadosAtualizados.getTitulo());
        artigoExistente.setConteudo(dadosAtualizados.getConteudo());
        artigoExistente.setCategoria(dadosAtualizados.getCategoria());
        artigoExistente.setTags(dadosAtualizados.getTags());
        artigoExistente.setAtivo(dadosAtualizados.isAtivo());
        
        ArtigoBaseConhecimento artigoSalvo = artigoRepository.save(artigoExistente);
        logger.info("Artigo ID {} atualizado com sucesso.", id);
        return artigoSalvo;
    }

    /**
     * Remove logicamente ou fisicamente um artigo da base.
     * Neste caso, optamos pela remocao fisica, mas pode ser adaptado para desativacao.
     */
    @Transactional
    public void excluirArtigo(Long id) {
        if (!artigoRepository.existsById(id)) {
            throw new RuntimeException("Artigo não encontrado para exclusão.");
        }
        artigoRepository.deleteById(id);
        logger.info("Artigo ID {} excluído da base de conhecimento.", id);
    }

    /**
     * Recupera um artigo pelo ID e incrementa o contador de visualizacoes.
     * Essencial para metricas de artigos mais acessados.
     */
    @Transactional
    public ArtigoBaseConhecimento buscarPorIdComVisualizacao(Long id) {
        ArtigoBaseConhecimento artigo = artigoRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Artigo não encontrado."));
        
        // Incrementa visualizacao apenas se o artigo estiver ativo
        if (artigo.isAtivo()) {
            artigo.incrementarVisualizacoes();
            artigoRepository.save(artigo);
        }
        
        return artigo;
    }

    // ==========================================
    // Metodos de Leitura e Listagem
    // ==========================================

    public List<ArtigoBaseConhecimento> listarTodos() {
        return artigoRepository.findAllByOrderByDataCriacaoDesc();
    }

    public List<ArtigoBaseConhecimento> listarArtigosAtivos() {
        return artigoRepository.findByAtivoTrue();
    }

    public List<ArtigoBaseConhecimento> listarTop5Populares() {
        return artigoRepository.findTop5ByAtivoTrueOrderByVisualizacoesDesc();
    }

    public List<ArtigoBaseConhecimento> buscarPorCategoria(String categoria) {
        return artigoRepository.findByCategoriaAndAtivoTrue(categoria);
    }

    public List<ArtigoBaseConhecimento> buscarPorTexto(String termo) {
        if (termo == null || termo.trim().isEmpty()) {
            return listarArtigosAtivos();
        }
        return artigoRepository.findByTituloContainingIgnoreCaseOrConteudoContainingIgnoreCaseAndAtivoTrue(termo, termo);
    }

    public Optional<ArtigoBaseConhecimento> buscarPorId(Long id) {
        return artigoRepository.findById(id);
    }
}