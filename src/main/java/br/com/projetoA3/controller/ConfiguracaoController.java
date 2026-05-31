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
        model.addAttribute("configuracoes", configuracaoService.findAll());
        return "configuracao/list";
    }

    // ✅ Formulário de edição (usa a tela de listagem com campos editáveis para simplicidade)
    @PostMapping("/salvar")
    public String salvarTodas(@RequestParam(value = "id", required = false) List<Long> ids,
                              @RequestParam(value = "chave", required = false) List<String> chaves,
                              @RequestParam(value = "valor", required = false) List<String> valores,
                              RedirectAttributes attributes) {
        try {
            if (ids != null && chaves != null && valores != null) {
                for (int i = 0; i < ids.size(); i++) {
                    Long id = ids.get(i);
                    String valor = valores.get(i);
                    
                    if (id != null && id > 0) {
                        // Atualizar existente
                        Configuracao conf = configuracaoService.findByChave(chaves.get(i)).orElse(null);
                        if (conf != null) {
                            conf.setValor(valor);
                            configuracaoService.save(conf);
                        }
                    } else {
                        // Criar novo (caso precise de lógica de criação dinâmica, mas aqui focamos em update)
                    }
                }
            }
            attributes.addFlashAttribute("sucesso", "Configurações salvas com sucesso!");
        } catch (Exception e) {
            attributes.addFlashAttribute("erro", "Erro ao salvar configurações: " + e.getMessage());
        }
        return "redirect:/configuracoes";
    }
}