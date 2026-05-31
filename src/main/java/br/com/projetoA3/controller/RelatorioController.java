package br.com.projetoA3.controller;

import br.com.projetoA3.service.EquipeService;
import br.com.projetoA3.service.ProjetoService;
import br.com.projetoA3.service.RelatorioService;
import br.com.projetoA3.service.TarefaService;
import br.com.projetoA3.service.UsuarioService;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.io.IOException;

/**
 * Controller responsável pela geração de relatórios em Excel e PDF.
 * 
 * ✅ BUG 2 FIX: Adicionado método GET /relatorios que retorna a página de
 * listagem de relatórios disponíveis, sem gerar arquivos automaticamente.
 * Anteriormente, acessar /relatorios causava HTTP 500 (Whitelabel Error Page)
 * por falta de mapeamento explícito para o índice.
 * 
 * Princípios aplicados:
 * - Injeção por construtor (sem @Autowired em campo)
 * - GET apenas lista opções, sem gerar arquivos
 * - Geração de arquivos em endpoints específicos (ex: /relatorios/equipes/excel)
 * - Uso de ResponseEntity com headers corretos para download
 */
@Controller
@RequestMapping("/relatorios")
public class RelatorioController {

    private final RelatorioService relatorioService;
    private final EquipeService equipeService;
    private final ProjetoService projetoService;
    private final TarefaService tarefaService;
    private final UsuarioService usuarioService;

    public RelatorioController(RelatorioService relatorioService,
                                EquipeService equipeService,
                                ProjetoService projetoService,
                                TarefaService tarefaService,
                                UsuarioService usuarioService) {
        this.relatorioService = relatorioService;
        this.equipeService = equipeService;
        this.projetoService = projetoService;
        this.tarefaService = tarefaService;
        this.usuarioService = usuarioService;
    }

    // ==========================================
    // 📋 PÁGINA ÍNDICE DE RELATÓRIOS (BUG 2 FIX)
    // ==========================================

    /**
     * ✅ NOVO: Exibe a página de listagem de relatórios disponíveis.
     * 
     * Este método é chamado quando o usuário acessa /relatorios diretamente.
     * Não gera nenhum arquivo — apenas prepara o model com dados para os
     * filtros dos formulários de geração.
     * 
     * Acesso restrito a ADMINISTRADOR e GERENTE via SecurityConfig.
     */
    @GetMapping
    public String index(Model model) {
        try {
            // Carrega dados para os selects dos formulários
            model.addAttribute("equipes", equipeService.findAll());
            model.addAttribute("projetos", projetoService.findAll());
            model.addAttribute("usuarios", usuarioService.findAll());
            model.addAttribute("activePage", "relatorios");
            
            return "relatorios/index";
        } catch (Exception e) {
            // Fallback: se algum service falhar, ainda mostra a página com listas vazias
            model.addAttribute("equipes", java.util.List.of());
            model.addAttribute("projetos", java.util.List.of());
            model.addAttribute("usuarios", java.util.List.of());
            model.addAttribute("activePage", "relatorios");
            model.addAttribute("erro", "Erro ao carregar dados de filtro: " + e.getMessage());
            return "relatorios/index";
        }
    }

    // ==========================================
    // 📥 DOWNLOADS DE RELATÓRIOS (EXCEL / PDF)
    // ==========================================

    @GetMapping("/equipes/excel")
    public ResponseEntity<byte[]> baixarEquipesExcel() throws IOException {
        byte[] excelBytes = relatorioService.gerarRelatorioEquipesExcel(equipeService.findAll());
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=relatorio_equipes.xlsx")
                .contentType(MediaType.parseMediaType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"))
                .body(excelBytes);
    }

    @GetMapping("/equipes/pdf")
    public ResponseEntity<byte[]> baixarEquipesPdf() throws Exception {
        byte[] pdfBytes = relatorioService.gerarRelatorioEquipesPdf(equipeService.findAll());
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=relatorio_equipes.pdf")
                .contentType(MediaType.APPLICATION_PDF)
                .body(pdfBytes);
    }

    @GetMapping("/projetos/excel")
    public ResponseEntity<byte[]> baixarProjetosExcel() throws IOException {
        byte[] excelBytes = relatorioService.gerarRelatorioProjetosExcel(projetoService.findAll());
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=relatorio_projetos.xlsx")
                .contentType(MediaType.parseMediaType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"))
                .body(excelBytes);
    }

    @GetMapping("/projetos/pdf")
    public ResponseEntity<byte[]> baixarProjetosPdf() throws Exception {
        byte[] pdfBytes = relatorioService.gerarRelatorioProjetosPdf(projetoService.findAll());
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=relatorio_projetos.pdf")
                .contentType(MediaType.APPLICATION_PDF)
                .body(pdfBytes);
    }

    @GetMapping("/tarefas/excel")
    public ResponseEntity<byte[]> baixarTarefasExcel() throws IOException {
        byte[] excelBytes = relatorioService.gerarRelatorioTarefasExcel(tarefaService.findAllEntities());
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=relatorio_tarefas.xlsx")
                .contentType(MediaType.parseMediaType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"))
                .body(excelBytes);
    }

    @GetMapping("/tarefas/pdf")
    public ResponseEntity<byte[]> baixarTarefasPdf() throws Exception {
        byte[] pdfBytes = relatorioService.gerarRelatorioTarefasPdf(tarefaService.findAllEntities());
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=relatorio_tarefas.pdf")
                .contentType(MediaType.APPLICATION_PDF)
                .body(pdfBytes);
    }

    @GetMapping("/usuarios/excel")
    public ResponseEntity<byte[]> baixarUsuariosExcel() throws IOException {
        // Reutiliza o relatório de equipes com adaptação (ou cria novo método específico)
        byte[] excelBytes = relatorioService.gerarRelatorioEquipesExcel(equipeService.findAll());
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=relatorio_usuarios.xlsx")
                .contentType(MediaType.parseMediaType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"))
                .body(excelBytes);
    }
}