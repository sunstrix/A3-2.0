package br.com.projetoA3.mapper;

import br.com.projetoA3.dto.TarefaDTO;
import br.com.projetoA3.enums.Prioridade;
import br.com.projetoA3.enums.StatusTarefa;
import br.com.projetoA3.model.Tarefa;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Mapper responsável por converter entre a entidade {@link Tarefa} e o DTO {@link TarefaDTO}.
 * 
 * Esta classe centraliza a lógica de mapeamento, garantindo:
 * - Conversão null-safe de relacionamentos (responsavel, projeto)
 * - Cálculo automático do flag 'atrasada' baseado em dataVencimento e status
 * - Extração de IDs e nomes para evitar problemas de lazy loading
 * 
 * O {@link TarefaDTO} é implementado como um Record Java, o que significa
 * que possui um construtor canônico com todos os campos como parâmetros.
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
     * 
     * @param tarefas Lista de entidades a serem convertidas
     * @return Lista de DTOs correspondentes
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
     * 
     * Este método é usado para atualizações parciais, onde apenas alguns campos
     * do DTO são aplicados à entidade existente.
     * 
     * @param dto DTO com os novos dados
     * @param tarefa Entidade existente a ser atualizada
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
    // MÉTODOS AUXILIARES PRIVADOS
    // ==========================================

    /**
     * Extrai o nome do responsável de forma null-safe.
     */
    private String extrairNomeResponsavel(Tarefa tarefa) {
        if (tarefa.getResponsavel() == null) {
            return null;
        }
        return tarefa.getResponsavel().getNome();
    }

    /**
     * Extrai o ID do responsável de forma null-safe.
     */
    private Long extrairIdResponsavel(Tarefa tarefa) {
        if (tarefa.getResponsavel() == null) {
            return null;
        }
        return tarefa.getResponsavel().getId();
    }

    /**
     * Extrai o ID do projeto de forma null-safe.
     */
    private Long extrairIdProjeto(Tarefa tarefa) {
        if (tarefa.getProjeto() == null) {
            return null;
        }
        return tarefa.getProjeto().getId();
    }

    /**
     * Extrai o nome do projeto de forma null-safe.
     */
    private String extrairNomeProjeto(Tarefa tarefa) {
        if (tarefa.getProjeto() == null) {
            return null;
        }
        return tarefa.getProjeto().getNome();
    }

    /**
     * Calcula se a tarefa está atrasada com base na data de vencimento e status.
     * 
     * Uma tarefa é considerada atrasada se:
     * - Tem data de vencimento definida
     * - A data de vencimento é anterior à data atual
     * - NÃO está concluída (CONCLUIDA)
     * - NÃO está cancelada (CANCELADA)
     */
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