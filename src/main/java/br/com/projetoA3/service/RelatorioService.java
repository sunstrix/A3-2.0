package br.com.projetoA3.service;

import br.com.projetoA3.model.Equipe;
import br.com.projetoA3.model.Projeto;
import br.com.projetoA3.model.Tarefa;
import com.lowagie.text.Document;
import com.lowagie.text.DocumentException;
import com.lowagie.text.Element;
import com.lowagie.text.Font;
import com.lowagie.text.FontFactory;
import com.lowagie.text.Paragraph;
import com.lowagie.text.Phrase;
import com.lowagie.text.pdf.PdfPCell;
import com.lowagie.text.pdf.PdfPTable;
import com.lowagie.text.pdf.PdfWriter;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;

/**
 * Service responsável pela geração de relatórios em Excel (XLSX) e PDF.
 * 
 * Utiliza:
 * - Apache POI para geração de planilhas Excel
 * - OpenPDF (fork livre do iText) para geração de PDFs
 * 
 * Cada método retorna um array de bytes que pode ser:
 * - Enviado como download via ResponseEntity no Controller
 * - Anexado em e-mails via EmailService
 */
@Service
public class RelatorioService {

    private static final DateTimeFormatter DATE_FMT = DateTimeFormatter.ofPattern("dd/MM/yyyy");

    // ==========================================
    // RELATÓRIOS DE EQUIPES
    // ==========================================

    /**
     * Gera relatório de Equipes em formato Excel (XLSX)
     */
    public byte[] gerarRelatorioEquipesExcel(List<Equipe> equipes) throws IOException {
        try (Workbook workbook = new XSSFWorkbook();
             ByteArrayOutputStream out = new ByteArrayOutputStream()) {

            Sheet sheet = workbook.createSheet("Equipes");

            // Cabeçalho
            Row header = sheet.createRow(0);
            String[] colunas = {"ID", "Nome", "Descrição", "Líder", "Total de Membros"};
            CellStyle headerStyle = criarEstiloCabeçalho(workbook);
            for (int i = 0; i < colunas.length; i++) {
                Cell cell = header.createCell(i);
                cell.setCellValue(colunas[i]);
                cell.setCellStyle(headerStyle);
            }

            // Dados
            int rowNum = 1;
            for (Equipe equipe : equipes) {
                Row row = sheet.createRow(rowNum++);
                row.createCell(0).setCellValue(equipe.getId());
                row.createCell(1).setCellValue(equipe.getNome() != null ? equipe.getNome() : "");
                row.createCell(2).setCellValue(equipe.getDescricao() != null ? equipe.getDescricao() : "");
                row.createCell(3).setCellValue(
                    equipe.getLider() != null ? equipe.getLider().getNome() : "Sem líder"
                );
                row.createCell(4).setCellValue(
                    equipe.getMembros() != null ? equipe.getMembros().size() : 0
                );
            }

            // Auto-size das colunas
            for (int i = 0; i < colunas.length; i++) {
                sheet.autoSizeColumn(i);
            }

            workbook.write(out);
            return out.toByteArray();
        }
    }

    /**
     * Gera relatório de Equipes em formato PDF
     */
    public byte[] gerarRelatorioEquipesPdf(List<Equipe> equipes) throws IOException, DocumentException {
        try (ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            Document document = new Document();
            PdfWriter.getInstance(document, out);
            document.open();

            adicionarTitulo(document, "Relatório de Equipes");
            adicionarDataGeracao(document);

            PdfPTable table = new PdfPTable(5);
            table.setWidthPercentage(100);
            table.setWidths(new float[]{10, 25, 30, 20, 15});

            adicionarCelulaCabeçalho(table, "ID");
            adicionarCelulaCabeçalho(table, "Nome");
            adicionarCelulaCabeçalho(table, "Descrição");
            adicionarCelulaCabeçalho(table, "Líder");
            adicionarCelulaCabeçalho(table, "Membros");

            for (Equipe equipe : equipes) {
                table.addCell(String.valueOf(equipe.getId()));
                table.addCell(equipe.getNome() != null ? equipe.getNome() : "");
                table.addCell(equipe.getDescricao() != null ? equipe.getDescricao() : "");
                table.addCell(equipe.getLider() != null ? equipe.getLider().getNome() : "Sem líder");
                table.addCell(String.valueOf(equipe.getMembros() != null ? equipe.getMembros().size() : 0));
            }

            document.add(table);
            document.close();
            return out.toByteArray();
        }
    }

    // ==========================================
    // RELATÓRIOS DE PROJETOS
    // ==========================================

    /**
     * Gera relatório de Projetos em formato Excel (XLSX)
     */
    public byte[] gerarRelatorioProjetosExcel(List<Projeto> projetos) throws IOException {
        try (Workbook workbook = new XSSFWorkbook();
             ByteArrayOutputStream out = new ByteArrayOutputStream()) {

            Sheet sheet = workbook.createSheet("Projetos");

            Row header = sheet.createRow(0);
            String[] colunas = {"ID", "Nome", "Descrição", "Status", "Início", "Término Previsto", "Equipe"};
            CellStyle headerStyle = criarEstiloCabeçalho(workbook);
            for (int i = 0; i < colunas.length; i++) {
                Cell cell = header.createCell(i);
                cell.setCellValue(colunas[i]);
                cell.setCellStyle(headerStyle);
            }

            int rowNum = 1;
            for (Projeto p : projetos) {
                Row row = sheet.createRow(rowNum++);
                row.createCell(0).setCellValue(p.getId());
                row.createCell(1).setCellValue(p.getNome() != null ? p.getNome() : "");
                row.createCell(2).setCellValue(p.getDescricao() != null ? p.getDescricao() : "");
                row.createCell(3).setCellValue(p.getStatus() != null ? p.getStatus().name() : "");
                row.createCell(4).setCellValue(formatarData(p.getDataInicio()));
                row.createCell(5).setCellValue(formatarData(p.getDataTerminoPrevista()));
                row.createCell(6).setCellValue(
                    p.getEquipe() != null ? p.getEquipe().getNome() : "Sem equipe"
                );
            }

            for (int i = 0; i < colunas.length; i++) {
                sheet.autoSizeColumn(i);
            }

            workbook.write(out);
            return out.toByteArray();
        }
    }

    /**
     * Gera relatório de Projetos em formato PDF
     */
    public byte[] gerarRelatorioProjetosPdf(List<Projeto> projetos) throws IOException, DocumentException {
        try (ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            Document document = new Document();
            PdfWriter.getInstance(document, out);
            document.open();

            adicionarTitulo(document, "Relatório de Projetos");
            adicionarDataGeracao(document);

            PdfPTable table = new PdfPTable(6);
            table.setWidthPercentage(100);
            table.setWidths(new float[]{10, 20, 25, 15, 15, 15});

            adicionarCelulaCabeçalho(table, "ID");
            adicionarCelulaCabeçalho(table, "Nome");
            adicionarCelulaCabeçalho(table, "Descrição");
            adicionarCelulaCabeçalho(table, "Status");
            adicionarCelulaCabeçalho(table, "Início");
            adicionarCelulaCabeçalho(table, "Término");

            for (Projeto p : projetos) {
                table.addCell(String.valueOf(p.getId()));
                table.addCell(p.getNome() != null ? p.getNome() : "");
                table.addCell(p.getDescricao() != null ? p.getDescricao() : "");
                table.addCell(p.getStatus() != null ? p.getStatus().name() : "");
                table.addCell(formatarData(p.getDataInicio()));
                table.addCell(formatarData(p.getDataTerminoPrevista()));
            }

            document.add(table);
            document.close();
            return out.toByteArray();
        }
    }

    // ==========================================
    // RELATÓRIOS DE TAREFAS
    // ==========================================

    /**
     * Gera relatório de Tarefas em formato Excel (XLSX)
     */
    public byte[] gerarRelatorioTarefasExcel(List<Tarefa> tarefas) throws IOException {
        try (Workbook workbook = new XSSFWorkbook();
             ByteArrayOutputStream out = new ByteArrayOutputStream()) {

            Sheet sheet = workbook.createSheet("Tarefas");

            Row header = sheet.createRow(0);
            String[] colunas = {"ID", "Título", "Status", "Prioridade", "Vencimento", "Projeto", "Responsável"};
            CellStyle headerStyle = criarEstiloCabeçalho(workbook);
            for (int i = 0; i < colunas.length; i++) {
                Cell cell = header.createCell(i);
                cell.setCellValue(colunas[i]);
                cell.setCellStyle(headerStyle);
            }

            int rowNum = 1;
            for (Tarefa t : tarefas) {
                Row row = sheet.createRow(rowNum++);
                row.createCell(0).setCellValue(t.getId());
                row.createCell(1).setCellValue(t.getTitulo() != null ? t.getTitulo() : "");
                row.createCell(2).setCellValue(t.getStatus() != null ? t.getStatus().name() : "");
                row.createCell(3).setCellValue(t.getPrioridade() != null ? t.getPrioridade().name() : "");
                row.createCell(4).setCellValue(formatarData(t.getDataVencimento()));
                row.createCell(5).setCellValue(
                    t.getProjeto() != null ? t.getProjeto().getNome() : ""
                );
                row.createCell(6).setCellValue(
                    t.getResponsavel() != null ? t.getResponsavel().getNome() : "Não atribuído"
                );
            }

            for (int i = 0; i < colunas.length; i++) {
                sheet.autoSizeColumn(i);
            }

            workbook.write(out);
            return out.toByteArray();
        }
    }

    /**
     * Gera relatório de Tarefas em formato PDF
     */
    public byte[] gerarRelatorioTarefasPdf(List<Tarefa> tarefas) throws IOException, DocumentException {
        try (ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            Document document = new Document();
            PdfWriter.getInstance(document, out);
            document.open();

            adicionarTitulo(document, "Relatório de Tarefas");
            adicionarDataGeracao(document);

            PdfPTable table = new PdfPTable(6);
            table.setWidthPercentage(100);
            table.setWidths(new float[]{10, 25, 15, 15, 15, 20});

            adicionarCelulaCabeçalho(table, "ID");
            adicionarCelulaCabeçalho(table, "Título");
            adicionarCelulaCabeçalho(table, "Status");
            adicionarCelulaCabeçalho(table, "Prioridade");
            adicionarCelulaCabeçalho(table, "Vencimento");
            adicionarCelulaCabeçalho(table, "Responsável");

            for (Tarefa t : tarefas) {
                table.addCell(String.valueOf(t.getId()));
                table.addCell(t.getTitulo() != null ? t.getTitulo() : "");
                table.addCell(t.getStatus() != null ? t.getStatus().name() : "");
                table.addCell(t.getPrioridade() != null ? t.getPrioridade().name() : "");
                table.addCell(formatarData(t.getDataVencimento()));
                table.addCell(
                    t.getResponsavel() != null ? t.getResponsavel().getNome() : "Não atribuído"
                );
            }

            document.add(table);
            document.close();
            return out.toByteArray();
        }
    }

    // ==========================================
    // MÉTODOS AUXILIARES PRIVADOS
    // ==========================================

    private CellStyle criarEstiloCabeçalho(Workbook workbook) {
        CellStyle style = workbook.createCellStyle();
        org.apache.poi.ss.usermodel.Font font = workbook.createFont();
        font.setBold(true);
        font.setFontHeightInPoints((short) 11);
        style.setFont(font);
        style.setFillForegroundColor(IndexedColors.LIGHT_CORNFLOWER_BLUE.getIndex());
        style.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        style.setBorderBottom(BorderStyle.THIN);
        style.setBorderTop(BorderStyle.THIN);
        style.setBorderLeft(BorderStyle.THIN);
        style.setBorderRight(BorderStyle.THIN);
        return style;
    }

    private void adicionarTitulo(Document document, String titulo) throws DocumentException {
        Font tituloFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 18);
        Paragraph p = new Paragraph(titulo, tituloFont);
        p.setAlignment(Element.ALIGN_CENTER);
        p.setSpacingAfter(15);
        document.add(p);
    }

    private void adicionarDataGeracao(Document document) throws DocumentException {
        Font dataFont = FontFactory.getFont(FontFactory.HELVETICA, 9);
        Paragraph p = new Paragraph(
            "Gerado em: " + LocalDate.now().format(DATE_FMT),
            dataFont
        );
        p.setAlignment(Element.ALIGN_RIGHT);
        p.setSpacingAfter(20);
        document.add(p);
    }

    private void adicionarCelulaCabeçalho(PdfPTable table, String texto) {
        PdfPCell cell = new PdfPCell(new Phrase(texto, FontFactory.getFont(FontFactory.HELVETICA_BOLD, 10)));
        cell.setBackgroundColor(new java.awt.Color(220, 230, 241));
        cell.setHorizontalAlignment(Element.ALIGN_CENTER);
        cell.setPadding(5);
        table.addCell(cell);
    }

    private String formatarData(LocalDate data) {
        return data != null ? data.format(DATE_FMT) : "-";
    }
}