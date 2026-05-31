package br.com.projetoA3.controller;

import br.com.projetoA3.service.ConfiguracaoService;
import br.com.projetoA3.service.EmailService;
import br.com.projetoA3.service.EquipeService;
import br.com.projetoA3.service.ProjetoService;
import br.com.projetoA3.service.RelatorioService;
import br.com.projetoA3.service.TarefaService;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.io.IOException;

@Controller
@RequestMapping("/relatorios")
public class RelatorioController {

    private final RelatorioService relatorioService;
    private final EquipeService equipeService;
    private final ProjetoService projetoService;
    private final TarefaService tarefaService;
    private final ConfiguracaoService configuracaoService;
    private final EmailService emailService; 

    public RelatorioController(RelatorioService relatorioService, EquipeService equipeService, 
                               ProjetoService projetoService, TarefaService tarefaService, 
                               ConfiguracaoService configuracaoService, EmailService emailService) {
        this.relatorioService = relatorioService;
        this.equipeService = equipeService;
        this.projetoService = projetoService;
        this.tarefaService = tarefaService;
        this.configuracaoService = configuracaoService;
        this.emailService = emailService;
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
        // ✅ CORREÇÃO: Usa findAllEntities() para obter List<Tarefa> em vez de List<TarefaDTO>
        byte[] excelBytes = relatorioService.gerarRelatorioTarefasExcel(tarefaService.findAllEntities());
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=relatorio_tarefas.xlsx")
                .contentType(MediaType.parseMediaType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"))
                .body(excelBytes);
    }

    @GetMapping("/tarefas/pdf")
    public ResponseEntity<byte[]> baixarTarefasPdf() throws Exception {
        // ✅ CORREÇÃO: Usa findAllEntities() para obter List<Tarefa> em vez de List<TarefaDTO>
        byte[] pdfBytes = relatorioService.gerarRelatorioTarefasPdf(tarefaService.findAllEntities());
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=relatorio_tarefas.pdf")
                .contentType(MediaType.APPLICATION_PDF)
                .body(pdfBytes);
    }

    // ==========================================
    // 📧 ENVIO POR E-MAIL (COMUNICAÇÃO)
    // ==========================================
    
    @PostMapping("/enviar-teste")
    public String enviarRelatorioTeste(RedirectAttributes attributes) {
        try {
            byte[] pdfBytes = relatorioService.gerarRelatorioEquipesPdf(equipeService.findAll());
            
            emailService.enviarRelatorioComAnexo(
                "Relatório de Equipes - Teste", 
                "Segue em anexo o relatório de equipes.", 
                pdfBytes, 
                "relatorio_equipes.pdf"
            );
            
            attributes.addFlashAttribute("sucesso", "Relatório enviado por e-mail com sucesso!");
        } catch (Exception e) {
            attributes.addFlashAttribute("erro", "Erro ao enviar e-mail: " + e.getMessage());
        }
        return "redirect:/configuracoes";
    }
}