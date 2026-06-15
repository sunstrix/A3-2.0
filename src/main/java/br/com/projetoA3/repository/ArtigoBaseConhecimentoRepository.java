package br.com.projetoA3.repository;

import br.com.projetoA3.model.ArtigoBaseConhecimento;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Repositorio responsavel pelas operacoes de persistencia da entidade ArtigoBaseConhecimento.
 * Herda o CRUD padrao do Spring Data JPA e adiciona metodos para listar, filtrar e buscar
 * artigos da base de conhecimento (FAQ/Documentacao) do Help Desk.
 */
@Repository
public interface ArtigoBaseConhecimentoRepository extends JpaRepository<ArtigoBaseConhecimento, Long> {

    /**
     * Busca todos os artigos que estao ativos (publicados e visiveis para os usuarios).
     */
    List<ArtigoBaseConhecimento> findByAtivoTrue();

    /**
     * Busca artigos ativos filtrados por uma categoria especifica.
     */
    List<ArtigoBaseConhecimento> findByCategoriaAndAtivoTrue(String categoria);

    /**
     * Realiza uma busca textual (case-insensitive) no titulo ou no conteudo do artigo.
     * Essencial para a barra de pesquisa da Base de Conhecimento.
     * Retorna apenas artigos ativos.
     */
    List<ArtigoBaseConhecimento> findByTituloContainingIgnoreCaseOrConteudoContainingIgnoreCaseAndAtivoTrue(String titulo, String conteudo);

    /**
     * Recupera os 5 artigos mais populares (com maior numero de visualizacoes).
     * Util para exibir em destaque na pagina inicial do portal do cliente.
     */
    List<ArtigoBaseConhecimento> findTop5ByAtivoTrueOrderByVisualizacoesDesc();
    
    /**
     * Recupera todos os artigos ordenados do mais recente para o mais antigo.
     */
    List<ArtigoBaseConhecimento> findAllByOrderByDataCriacaoDesc();
}