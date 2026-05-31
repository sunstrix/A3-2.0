package br.com.projetoA3.mapper;

import br.com.projetoA3.dto.TarefaDTO;
import br.com.projetoA3.model.Tarefa;
import br.com.projetoA3.model.Usuario;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Mapper responsável por converter entidades Tarefa em TarefaDTO.
 * 
 * Esta classe centraliza a lógica de transformação, evitando que
 * controllers e services manipulem diretamente os dados de apresentação.
 * O uso de streams e lambdas mantém o código conciso e legível.
 */
@Component
public class TarefaMapper {

    /**
     * Converte uma entidade Tarefa em TarefaDTO.
     * 
     * @param tarefa Entidade JPA a ser convertida
     * @return TarefaDTO com os dados formatados para a view
     */
    public TarefaDTO toDTO(Tarefa tarefa) {
        if (tarefa == null) {
            return null;
        }

        Usuario responsavel = tarefa.getResponsavel();
        String responsavelNome = responsavel != null ? responsavel.getNome() : null;
        Long responsavelId = responsavel != null ? responsavel.getId() : null;

        return new TarefaDTO(
            tarefa.getId(),
            tarefa.getTitulo(),
            tarefa.getDescricao(),
            tarefa.getStatus() != null ? tarefa.getStatus().name() : null,
            tarefa.getPrioridade() != null ? tarefa.getPrioridade().name() : null,
            responsavelNome,
            responsavelId,
            tarefa.getDataVencimento(),
            calcularSeEstaAtrasada(tarefa)
        );
    }

    /**
     * Converte uma lista de entidades Tarefa em uma lista de TarefaDTO.
     * 
     * @param tarefas Lista de entidades a serem convertidas
     * @return Lista de TarefaDTO convertida
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
     * Calcula se uma tarefa está atrasada com base na data de vencimento.
     * Uma tarefa é considerada atrasada quando:
     * - Possui data de vencimento definida
     * - A data de vencimento é anterior à data atual
     * - Não está concluída nem cancelada
     */
    private boolean calcularSeEstaAtrasada(Tarefa tarefa) {
        if (tarefa.getDataVencimento() == null) {
            return false;
        }
        if (tarefa.getStatus() == null) {
            return false;
        }
        
        boolean statusFinal = tarefa.getStatus().name().equals("CONCLUIDA") 
                           || tarefa.getStatus().name().equals("CANCELADA");
        
        if (statusFinal) {
            return false;
        }
        
        return tarefa.getDataVencimento().isBefore(LocalDate.now());
    }
}