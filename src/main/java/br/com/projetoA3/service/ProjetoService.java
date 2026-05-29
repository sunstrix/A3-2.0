package br.com.projetoA3.service;

import br.com.projetoA3.model.Projeto;
import br.com.projetoA3.repository.ProjetoRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class ProjetoService {

    private final ProjetoRepository projetoRepository;

    public ProjetoService(ProjetoRepository projetoRepository) {
        this.projetoRepository = projetoRepository;
    }

    // ✅ Listar todos os projetos
    public List<Projeto> findAll() {
        return projetoRepository.findAll();
    }

    // ✅ Buscar projeto por ID
    public Optional<Projeto> findById(Long id) {
        return projetoRepository.findById(id);
    }

    // ✅ Filtrar projetos por status (ex: EM_ANDAMENTO, CONCLUIDO)
    public List<Projeto> findByStatus(Projeto.StatusProjeto status) {
        return projetoRepository.findByStatus(status);
    }

    // ✅ Buscar projetos atribuídos a um gerente específico
    public List<Projeto> findByGerenteId(Long gerenteId) {
        return projetoRepository.findByGerenteId(gerenteId);
    }

    // ✅ Salvar ou atualizar projeto
    public Projeto save(Projeto projeto) {
        return projetoRepository.save(projeto);
    }

    // ✅ Deletar projeto por ID
    public void deleteById(Long id) {
        projetoRepository.deleteById(id);
    }
}