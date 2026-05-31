package br.com.projetoA3.controller;

import br.com.projetoA3.service.*;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/relatorios")
public class RelatorioController {

    private final RelatorioService relatorioService;
    private final EquipeService equipeService;
    private final ProjetoService projetoService;
    private final TarefaService tarefaService;
    private final EmailService emailService;

    public RelatorioController(RelatorioService relatorioService, EquipeService equipeService,
                               ProjetoService projetoService, TarefaService tarefaService, EmailService emailService) {
        this.relatorioService = relatorioService;
        this.equipeService = equipeService;
        this.projetoService = projetoService;
        this.tarefaService = tarefaService;
        this.emailService = emailService;
    }

    // ==========================================
    // 📥 DOWNLOAD EXCEL
    // ==========================================

    @GetMapping("/equipes/excel")
    public ResponseEntity<byte[]> baixarEquipesExcel() throws Exception {
        byte[] bytes = relatorioService.gerarRelatorioEquipesExcel(equipeService.findAll());
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=relatorio_equipes.xlsx")
                .contentType(MediaType.parseMediaType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"))
                .body(bytes);
    }

    @GetMapping("/projetos/excel")
    public ResponseEntity<byte[]> baixarProjetosExcel() throws Exception {
        byte[] bytes = relatorioService.gerarRelatorioProjetosExcel(projetoService.findAll());
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=relatorio_projetos.xlsx")
                .contentType(MediaType.parseMediaType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"))
                .body(bytes);
    }

    @GetMapping("/tarefas/excel")
    public ResponseEntity<byte[]> baixarTarefasExcel() throws Exception {
        byte[] bytes = relatorioService.gerarRelatorioTarefasExcel(tarefaService.findAll());
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=relatorio_tarefas.xlsx")
                .contentType(MediaType.parseMediaType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"))
                .body(bytes);
    }

    // ==========================================
    // 📄 DOWNLOAD PDF
    // ==========================================

    @GetMapping("/equipes/pdf")
    public ResponseEntity<byte[]> baixarEquipesPdf() throws Exception {
        byte[] bytes = relatorioService.gerarRelatorioEquipesPdf(equipeService.findAll());
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=relatorio_equipes.pdf")
                .contentType(MediaType.APPLICATION_PDF)
                .body(bytes);
    }

    @GetMapping("/projetos/pdf")
    public ResponseEntity<byte[]> baixarProjetosPdf() throws Exception {
        byte[] bytes = relatorioService.gerarRelatorioProjetosPdf(projetoService.findAll());
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=relatorio_projetos.pdf")
                .contentType(MediaType.APPLICATION_PDF)
                .body(bytes);
    }

    @GetMapping("/tarefas/pdf")
    public ResponseEntity<byte[]> baixarTarefasPdf() throws Exception {
        byte[] bytes = relatorioService.gerarRelatorioTarefasPdf(tarefaService.findAll());
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=relatorio_tarefas.pdf")
                .contentType(MediaType.APPLICATION_PDF)
                .body(bytes);
    }

    // ==========================================
    // 📧 ENVIO DE E-MAIL (TESTE)
    // ==========================================

    @PostMapping("/enviar-relatorio-equipe")
    public String enviarRelatorioEquipeEmail(RedirectAttributes attributes) {
        try {
            // Gera o PDF
            byte[] pdfBytes = relatorioService.gerarRelatorioEquipesPdf(equipeService.findAll());
            
            // Envia o e-mail
            emailService.enviarRelatorioComAnexo(
                "Relatório de Equipes - A3 Sistema", 
                "Segue em anexo o relatório atualizado de equipes.", 
                pdfBytes, 
                "relatorio_equipes.pdf"
            );
            
            attributes.addFlashAttribute("sucesso", "Relatório de Equipes enviado com sucesso para o e-mail cadastrado!");
        } catch (Exception e) {
            attributes.addFlashAttribute("erro", "Erro ao enviar e-mail: " + e.getMessage());
        }
        return "redirect:/configuracoes";
    }
}