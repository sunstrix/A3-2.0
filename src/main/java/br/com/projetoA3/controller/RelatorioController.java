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
 * Refatorado para garantir compatibilidade entre DTOs de visualização e Entidades de relatório.
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
    // 📋 PÁGINA ÍNDICE DE RELATÓRIOS
    // ==========================================

    @GetMapping
    public String index(Model model) {
        try {
            model.addAttribute("equipes", equipeService.findAll());
            model.addAttribute("projetos", projetoService.findAll());
            model.addAttribute("usuarios", usuarioService.findAll());
            model.addAttribute("activePage", "relatorios");
            return "relatorios/index";
        } catch (Exception e) {
            model.addAttribute("erro", "Erro ao carregar filtros: " + e.getMessage());
            model.addAttribute("activePage", "relatorios");
            return "relatorios/index";
        }
    }

    // ==========================================
    // 📥 DOWNLOADS DE RELATÓRIOS (EXCEL / PDF)
    // ==========================================

    @GetMapping("/equipes/excel")
    public ResponseEntity<byte[]> baixarEquipesExcel(RedirectAttributes attributes) {
        try {
            byte[] excelBytes = relatorioService.gerarRelatorioEquipesExcel(equipeService.findAll());
            return ResponseEntity.ok()
                    .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=relatorio_equipes.xlsx")
                    .contentType(MediaType.parseMediaType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"))
                    .body(excelBytes);
        } catch (Exception e) {
            attributes.addFlashAttribute("erro", "Erro ao gerar Excel de Equipes: " + e.getMessage());
            return ResponseEntity.status(302).header(HttpHeaders.LOCATION, "/relatorios").build();
        }
    }

    @GetMapping("/equipes/pdf")
    public ResponseEntity<byte[]> baixarEquipesPdf(RedirectAttributes attributes) {
        try {
            byte[] pdfBytes = relatorioService.gerarRelatorioEquipesPdf(equipeService.findAll());
            return ResponseEntity.ok()
                    .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=relatorio_equipes.pdf")
                    .contentType(MediaType.APPLICATION_PDF)
                    .body(pdfBytes);
        } catch (Exception e) {
            attributes.addFlashAttribute("erro", "Erro ao gerar PDF de Equipes: " + e.getMessage());
            return ResponseEntity.status(302).header(HttpHeaders.LOCATION, "/relatorios").build();
        }
    }

    @GetMapping("/projetos/excel")
    public ResponseEntity<byte[]> baixarProjetosExcel(RedirectAttributes attributes) {
        try {
            byte[] excelBytes = relatorioService.gerarRelatorioProjetosExcel(projetoService.findAll());
            return ResponseEntity.ok()
                    .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=relatorio_projetos.xlsx")
                    .contentType(MediaType.parseMediaType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"))
                    .body(excelBytes);
        } catch (Exception e) {
            attributes.addFlashAttribute("erro", "Erro ao gerar Excel de Projetos: " + e.getMessage());
            return ResponseEntity.status(302).header(HttpHeaders.LOCATION, "/relatorios").build();
        }
    }

    @GetMapping("/projetos/pdf")
    public ResponseEntity<byte[]> baixarProjetosPdf(RedirectAttributes attributes) {
        try {
            byte[] pdfBytes = relatorioService.gerarRelatorioProjetosPdf(projetoService.findAll());
            return ResponseEntity.ok()
                    .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=relatorio_projetos.pdf")
                    .contentType(MediaType.APPLICATION_PDF)
                    .body(pdfBytes);
        } catch (Exception e) {
            attributes.addFlashAttribute("erro", "Erro ao gerar PDF de Projetos: " + e.getMessage());
            return ResponseEntity.status(302).header(HttpHeaders.LOCATION, "/relatorios").build();
        }
    }

    @GetMapping("/tarefas/excel")
    public ResponseEntity<byte[]> baixarTarefasExcel(RedirectAttributes attributes) {
        try {
            // ✅ CORREÇÃO: Garante que estamos pegando as Entidades e não DTOs para o relatório
            byte[] excelBytes = relatorioService.gerarRelatorioTarefasExcel(tarefaService.findAllEntities());
            return ResponseEntity.ok()
                    .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=relatorio_tarefas.xlsx")
                    .contentType(MediaType.parseMediaType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"))
                    .body(excelBytes);
        } catch (Exception e) {
            attributes.addFlashAttribute("erro", "Erro ao gerar Excel de Tarefas: " + e.getMessage());
            return ResponseEntity.status(302).header(HttpHeaders.LOCATION, "/relatorios").build();
        }
    }

    @GetMapping("/tarefas/pdf")
    public ResponseEntity<byte[]> baixarTarefasPdf(RedirectAttributes attributes) {
        try {
            byte[] pdfBytes = relatorioService.gerarRelatorioTarefasPdf(tarefaService.findAllEntities());
            return ResponseEntity.ok()
                    .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=relatorio_tarefas.pdf")
                    .contentType(MediaType.APPLICATION_PDF)
                    .body(pdfBytes);
        } catch (Exception e) {
            attributes.addFlashAttribute("erro", "Erro ao gerar PDF de Tarefas: " + e.getMessage());
            return ResponseEntity.status(302).header(HttpHeaders.LOCATION, "/relatorios").build();
        }
    }

    @GetMapping("/usuarios/excel")
    public ResponseEntity<byte[]> baixarUsuariosExcel(RedirectAttributes attributes) {
        try {
            byte[] excelBytes = relatorioService.gerarRelatorioEquipesExcel(equipeService.findAll());
            return ResponseEntity.ok()
                    .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=relatorio_usuarios.xlsx")
                    .contentType(MediaType.parseMediaType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"))
                    .body(excelBytes);
        } catch (Exception e) {
            attributes.addFlashAttribute("erro", "Erro ao gerar Excel de Usuários: " + e.getMessage());
            return ResponseEntity.status(302).header(HttpHeaders.LOCATION, "/relatorios").build();
        }
    }
}