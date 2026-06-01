package br.com.projetoA3.controller;

import br.com.projetoA3.service.EquipeService;
import br.com.projetoA3.service.ProjetoService;
import br.com.projetoA3.service.RelatorioService;
import br.com.projetoA3.service.TarefaService;
import br.com.projetoA3.service.UsuarioService;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import java.io.IOException;
import java.util.List;

/**
 * Controller responsável pela geração de relatórios em Excel e PDF.
 * 
 * ✅ Refatoração Sênior: Proteção de acesso, injeção por construtor 
 * e preservação completa da lógica de index e filtros.
 */
@Controller
@RequestMapping("/relatorios")
@PreAuthorize("hasAnyRole('ADMINISTRADOR', 'GERENTE')") // ✅ Hardening de Segurança
public class RelatorioController {

    private final RelatorioService relatorioService;
    private final EquipeService equipeService;
    private final ProjetoService projetoService;
    private final TarefaService tarefaService;
    private final UsuarioService usuarioService;

    /**
     * Injeção de dependências via construtor (Padrão Spring Moderno).
     */
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
    // 📋 PÁGINA ÍNDICE DE RELATÓRIOS (PRESERVADO)
    // ==========================================

    /**
     * Exibe a página de listagem de relatórios disponíveis.
     * ✅ BUG 2 FIX PRESERVADO: Evita erro 500 ao acessar /relatorios.
     */
    @GetMapping
    public String index(Model model) {
        try {
            model.addAttribute("equipes", equipeService.findAll());
            model.addAttribute("projetos", projetoService.findAll());
            model.addAttribute("usuarios", usuarioService.findAll());
            model.addAttribute("activePage", "relatorios");
            
            return "relatorios/index";
        } catch (Exception e) {
            model.addAttribute("equipes", List.of());
            model.addAttribute("projetos", List.of());
            model.addAttribute("usuarios", List.of());
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
        // ✅ Utiliza o método findAllEntities() restaurado no TarefaService
        byte[] excelBytes = relatorioService.gerarRelatorioTarefasExcel(tarefaService.findAllEntities());
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=relatorio_tarefas.xlsx")
                .contentType(MediaType.parseMediaType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"))
                .body(excelBytes);
    }

    @GetMapping("/tarefas/pdf")
    public ResponseEntity<byte[]> baixarTarefasPdf() throws Exception {
        // ✅ Utiliza o método findAllEntities() restaurado no TarefaService
        byte[] pdfBytes = relatorioService.gerarRelatorioTarefasPdf(tarefaService.findAllEntities());
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=relatorio_tarefas.pdf")
                .contentType(MediaType.APPLICATION_PDF)
                .body(pdfBytes);
    }

    @GetMapping("/usuarios/excel")
    public ResponseEntity<byte[]> baixarUsuariosExcel() throws IOException {
        byte[] excelBytes = relatorioService.gerarRelatorioEquipesExcel(equipeService.findAll());
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=relatorio_usuarios.xlsx")
                .contentType(MediaType.parseMediaType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"))
                .body(excelBytes);
    }
}