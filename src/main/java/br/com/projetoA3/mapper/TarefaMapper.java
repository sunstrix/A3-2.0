package br.com.projetoA3.mapper;

import br.com.projetoA3.dto.TarefaDTO;
import br.com.projetoA3.enums.StatusTarefa;
import br.com.projetoA3.model.Tarefa;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Mapper responsável por converter entre a entidade {@link Tarefa} e o DTO {@link TarefaDTO}.
 * 
 * Refatorado para suportar TarefaDTO como Record, mantendo a lógica de 
 * extração null-safe e cálculo de atraso original.
 */
@Component
public class TarefaMapper {

    /**
     * Converte uma entidade {@link Tarefa} para {@link TarefaDTO}.
     * 
     * @param tarefa Entidade JPA a ser convertida (pode ser null)
     * @return DTO correspondente, ou null se a entrada for null
     */
    public TarefaDTO toDTO(Tarefa tarefa) {
        if (tarefa == null) {
            return null;
        }

        return new TarefaDTO(
            tarefa.getId(),
            tarefa.getTitulo(),
            tarefa.getDescricao(),
            tarefa.getPrioridade(),
            tarefa.getStatus(),
            extrairNomeResponsavel(tarefa),
            extrairIdResponsavel(tarefa),
            tarefa.getDataVencimento(),
            calcularAtraso(tarefa),
            extrairIdProjeto(tarefa),
            extrairNomeProjeto(tarefa)
        );
    }

    /**
     * Converte uma lista de entidades {@link Tarefa} para uma lista de {@link TarefaDTO}.
     */
    public List<TarefaDTO> toDTOList(List<Tarefa> tarefas) {
        if (tarefas == null) {
            return List.of();
        }
        return tarefas.stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }

    /**
     * Atualiza uma entidade {@link Tarefa} existente com os dados de um {@link TarefaDTO}.
     * Ajustado para acessar campos do Record (sem o prefixo 'get').
     */
    public void updateEntityFromDTO(TarefaDTO dto, Tarefa tarefa) {
        if (dto == null || tarefa == null) {
            return;
        }

        if (dto.titulo() != null) {
            tarefa.setTitulo(dto.titulo());
        }
        if (dto.descricao() != null) {
            tarefa.setDescricao(dto.descricao());
        }
        if (dto.prioridade() != null) {
            tarefa.setPrioridade(dto.prioridade());
        }
        if (dto.status() != null) {
            tarefa.setStatus(dto.status());
        }
        if (dto.dataVencimento() != null) {
            tarefa.setDataVencimento(dto.dataVencimento());
        }
    }

    // ==========================================
    // MÉTODOS AUXILIARES PRIVADOS (Restaurados)
    // ==========================================

    private String extrairNomeResponsavel(Tarefa tarefa) {
        if (tarefa.getResponsavel() == null) {
            return null;
        }
        return tarefa.getResponsavel().getNome();
    }

    private Long extrairIdResponsavel(Tarefa tarefa) {
        if (tarefa.getResponsavel() == null) {
            return null;
        }
        return tarefa.getResponsavel().getId();
    }

    private Long extrairIdProjeto(Tarefa tarefa) {
        if (tarefa.getProjeto() == null) {
            return null;
        }
        return tarefa.getProjeto().getId();
    }

    private String extrairNomeProjeto(Tarefa tarefa) {
        if (tarefa.getProjeto() == null) {
            return null;
        }
        return tarefa.getProjeto().getNome();
    }

    private boolean calcularAtraso(Tarefa tarefa) {
        if (tarefa.getDataVencimento() == null) {
            return false;
        }
        
        StatusTarefa statusAtual = tarefa.getStatus();
        if (statusAtual == StatusTarefa.CONCLUIDA || statusAtual == StatusTarefa.CANCELADA) {
            return false;
        }
        
        return tarefa.getDataVencimento().isBefore(LocalDate.now());
    }
}