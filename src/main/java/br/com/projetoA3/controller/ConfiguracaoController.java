package br.com.projetoA3.controller;

import br.com.projetoA3.model.Configuracao;
import br.com.projetoA3.service.ConfiguracaoService;
import jakarta.validation.Valid;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;

@Controller
@RequestMapping("/configuracoes")
public class ConfiguracaoController {

    private final ConfiguracaoService configuracaoService;

    public ConfiguracaoController(ConfiguracaoService configuracaoService) {
        this.configuracaoService = configuracaoService;
    }

    // ✅ Listar todas as configurações
    @GetMapping
    public String listar(Model model) {
        List<Configuracao> configs = configuracaoService.findAll();
        
        // Se a tabela estiver vazia, podemos adicionar alguns valores padrão para facilitar
        // Mas o ideal é que o admin preencha pela primeira vez.
        // Aqui passamos a lista para o template.
        model.addAttribute("configuracoes", configs);
        
        return "configuracao/list";
    }

    // ✅ Salvar/Atualizar configurações
    @PostMapping("/salvar")
    public String salvar(@RequestParam(value = "id", required = false) List<Long> ids,
                         @RequestParam(value = "chave", required = false) List<String> chaves,
                         @RequestParam(value = "valor", required = false) List<String> valores,
                         RedirectAttributes attributes) {
        try {
            if (ids != null && chaves != null && valores != null) {
                for (int i = 0; i < ids.size(); i++) {
                    Long id = ids.get(i);
                    String valor = valores.get(i);
                    String chave = chaves.get(i);
                    
                    // Busca se já existe
                    Configuracao conf = configuracaoService.findByChave(chave).orElse(null);
                    
                    if (conf == null) {
                        // Cria nova
                        conf = new Configuracao(chave, valor, "Configuração do sistema");
                    } else {
                        // Atualiza existente
                        conf.setValor(valor);
                    }
                    
                    configuracaoService.save(conf);
                }
            }
            attributes.addFlashAttribute("sucesso", "Configurações salvas com sucesso!");
        } catch (Exception e) {
            attributes.addFlashAttribute("erro", "Erro ao salvar: " + e.getMessage());
        }
        return "redirect:/configuracoes";
    }
}