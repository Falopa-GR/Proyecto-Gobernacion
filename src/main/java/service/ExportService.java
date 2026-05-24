package service;

import model.AdministrativeSituation;
import model.AdministrativeSituation.SituationType;
import model.PublicServer;
import model.VacationPeriod;

import com.itextpdf.text.*;
import com.itextpdf.text.pdf.*;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.ss.usermodel.Font;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import javax.swing.*;
import javax.swing.filechooser.FileNameExtensionFilter;
import java.io.*;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;

/**
 * RF-07 — Servicio de exportación de reportes
 *
 * Genera archivos Excel (.xlsx) y PDF de los principales reportes:
 *   - Planta activa de servidores
 *   - Situaciones administrativas activas hoy
 *   - Historial de situaciones por servidor
 *   - Servidores en deuda de vacaciones
 *
 * Uso:
 *   ExportService.exportToExcel(parent, "vacaciones_deuda", headers, rows);
 *   ExportService.exportToPdf(parent, "planta_activa", titulo, headers, rows);
 */
public class ExportService {

    private static final DateTimeFormatter FMT = DateTimeFormatter.ofPattern("dd/MM/yyyy");
    private static final String FECHA_HOY = LocalDate.now().format(FMT);

    // ─────────────────────────────────────────────────────────────────────
    // API GENÉRICA — cualquier tabla con headers + filas de Strings
    // ─────────────────────────────────────────────────────────────────────

    /**
     * Abre un JFileChooser y exporta a Excel (.xlsx).
     *
     * @param parent    ventana padre para el diálogo
     * @param suggested nombre de archivo sugerido (sin extensión)
     * @param headers   nombres de las columnas
     * @param rows      filas de datos (cada fila es un array de Strings)
     */
    public static void exportToExcel(java.awt.Component parent,
                                     String suggested,
                                     String[] headers,
                                     List<String[]> rows) {
        File file = chooseFile(parent, suggested, "xlsx",
                new FileNameExtensionFilter("Excel (*.xlsx)", "xlsx"));
        if (file == null) return;

        try (Workbook wb = new XSSFWorkbook()) {
            Sheet sheet = wb.createSheet("Reporte");

            // Estilo encabezado
            CellStyle headerStyle = wb.createCellStyle();
            Font headerFont = wb.createFont();
            headerFont.setBold(true);
            headerFont.setFontHeightInPoints((short) 11);
            headerStyle.setFont(headerFont);
            headerStyle.setFillForegroundColor(IndexedColors.DARK_BLUE.getIndex());
            headerStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
            headerFont.setColor(IndexedColors.WHITE.getIndex());
            headerStyle.setAlignment(HorizontalAlignment.CENTER);

            // Estilo filas pares
            CellStyle evenStyle = wb.createCellStyle();
            evenStyle.setFillForegroundColor(IndexedColors.PALE_BLUE.getIndex());
            evenStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);

            // Fila de título
            Row titleRow = sheet.createRow(0);
            Cell titleCell = titleRow.createCell(0);
            titleCell.setCellValue("Reporte: " + suggested.replace("_", " ").toUpperCase()
                    + "  —  Generado: " + FECHA_HOY);
            CellStyle titleStyle = wb.createCellStyle();
            Font titleFont = wb.createFont();
            titleFont.setBold(true);
            titleFont.setFontHeightInPoints((short) 13);
            titleStyle.setFont(titleFont);
            titleCell.setCellStyle(titleStyle);
            sheet.addMergedRegion(new org.apache.poi.ss.util.CellRangeAddress(0, 0, 0, headers.length - 1));

            // Fila de encabezados (fila 2)
            Row headerRow = sheet.createRow(1);
            for (int i = 0; i < headers.length; i++) {
                Cell c = headerRow.createCell(i);
                c.setCellValue(headers[i]);
                c.setCellStyle(headerStyle);
            }

            // Filas de datos
            for (int r = 0; r < rows.size(); r++) {
                Row row = sheet.createRow(r + 2);
                String[] rowData = rows.get(r);
                for (int c = 0; c < rowData.length; c++) {
                    Cell cell = row.createCell(c);
                    cell.setCellValue(rowData[c] != null ? rowData[c] : "");
                    if (r % 2 == 0) cell.setCellStyle(evenStyle);
                }
            }

            // Autoajustar columnas
            for (int i = 0; i < headers.length; i++) sheet.autoSizeColumn(i);

            // Fila de total al final
            Row totalRow = sheet.createRow(rows.size() + 2);
            Cell totalCell = totalRow.createCell(0);
            totalCell.setCellValue("Total: " + rows.size() + " registro(s)");
            CellStyle totalStyle = wb.createCellStyle();
            Font totalFont = wb.createFont();
            totalFont.setItalic(true);
            totalStyle.setFont(totalFont);
            totalCell.setCellStyle(totalStyle);

            try (FileOutputStream fos = new FileOutputStream(file)) {
                wb.write(fos);
            }

            JOptionPane.showMessageDialog(parent,
                    "Excel exportado correctamente:\n" + file.getAbsolutePath(),
                    "Exportación exitosa", JOptionPane.INFORMATION_MESSAGE);

        } catch (Exception ex) {
            JOptionPane.showMessageDialog(parent,
                    "Error al exportar Excel:\n" + ex.getMessage(),
                    "Error", JOptionPane.ERROR_MESSAGE);
            ex.printStackTrace();
        }
    }

    /**
     * Abre un JFileChooser y exporta a PDF.
     *
     * @param parent    ventana padre
     * @param suggested nombre sugerido (sin extensión)
     * @param titulo    título que aparece en la cabecera del PDF
     * @param headers   nombres de las columnas
     * @param rows      filas de datos
     */
    public static void exportToPdf(java.awt.Component parent,
                                   String suggested,
                                   String titulo,
                                   String[] headers,
                                   List<String[]> rows) {
        File file = chooseFile(parent, suggested, "pdf",
                new FileNameExtensionFilter("PDF (*.pdf)", "pdf"));
        if (file == null) return;

        try {
            Document doc = new Document(PageSize.A4.rotate(), 20, 20, 30, 30);
            PdfWriter.getInstance(doc, new FileOutputStream(file));
            doc.open();

            // Fuentes
            com.itextpdf.text.Font titleFont  = new com.itextpdf.text.Font(
                    com.itextpdf.text.Font.FontFamily.HELVETICA, 14, com.itextpdf.text.Font.BOLD,
                    new BaseColor(17, 24, 39));
            com.itextpdf.text.Font subFont    = new com.itextpdf.text.Font(
                    com.itextpdf.text.Font.FontFamily.HELVETICA, 9, com.itextpdf.text.Font.NORMAL,
                    new BaseColor(107, 114, 128));
            com.itextpdf.text.Font headerFont = new com.itextpdf.text.Font(
                    com.itextpdf.text.Font.FontFamily.HELVETICA, 9, com.itextpdf.text.Font.BOLD,
                    BaseColor.WHITE);
            com.itextpdf.text.Font cellFont   = new com.itextpdf.text.Font(
                    com.itextpdf.text.Font.FontFamily.HELVETICA, 8, com.itextpdf.text.Font.NORMAL,
                    new BaseColor(33, 37, 41));

            // Título y subtítulo
            Paragraph pTitulo = new Paragraph(titulo, titleFont);
            pTitulo.setSpacingAfter(2);
            doc.add(pTitulo);

            Paragraph pSub = new Paragraph(
                    "Sistema de Talento Humano  —  Generado: " + FECHA_HOY
                            + "  —  Total: " + rows.size() + " registro(s)", subFont);
            pSub.setSpacingAfter(12);
            doc.add(pSub);

            // Tabla
            PdfPTable table = new PdfPTable(headers.length);
            table.setWidthPercentage(100);

            // Encabezados
            BaseColor headerBg = new BaseColor(17, 24, 39);
            for (String h : headers) {
                PdfPCell cell = new PdfPCell(new Phrase(h, headerFont));
                cell.setBackgroundColor(headerBg);
                cell.setPadding(6);
                cell.setHorizontalAlignment(Element.ALIGN_CENTER);
                cell.setBorderColor(new BaseColor(55, 65, 81));
                table.addCell(cell);
            }

            // Filas de datos con color alterno
            BaseColor evenBg = new BaseColor(243, 244, 246);
            for (int r = 0; r < rows.size(); r++) {
                String[] rowData = rows.get(r);
                for (String val : rowData) {
                    PdfPCell cell = new PdfPCell(new Phrase(val != null ? val : "", cellFont));
                    cell.setPadding(5);
                    if (r % 2 == 0) cell.setBackgroundColor(evenBg);
                    cell.setBorderColor(new BaseColor(209, 213, 219));
                    table.addCell(cell);
                }
            }

            doc.add(table);
            doc.close();

            JOptionPane.showMessageDialog(parent,
                    "PDF exportado correctamente:\n" + file.getAbsolutePath(),
                    "Exportación exitosa", JOptionPane.INFORMATION_MESSAGE);

        } catch (Exception ex) {
            JOptionPane.showMessageDialog(parent,
                    "Error al exportar PDF:\n" + ex.getMessage(),
                    "Error", JOptionPane.ERROR_MESSAGE);
            ex.printStackTrace();
        }
    }

    // ─────────────────────────────────────────────────────────────────────
    // CONVERTIDORES DE MODELOS A FILAS — un método por tipo de reporte
    // ─────────────────────────────────────────────────────────────────────

    public static final String[] HEADERS_PLANTA = {
            "Cédula", "Nombre", "Apellido", "Cargo", "Dependencia",
            "Tipo vinculación", "Fecha ingreso", "Salario"
    };

    public static String[] serverToRow(PublicServer s) {
        return new String[]{
                s.getIdNumber(),
                s.getFirstName(),
                s.getLastName(),
                s.getPosition()    != null ? s.getPosition().getName()    : "—",
                s.getDependency()  != null ? s.getDependency().getName()  : "—",
                s.getVinculationType() != null ? s.getVinculationType()     : "—",
                s.getAdmissionDate()  != null ? s.getAdmissionDate().format(FMT) : "—",
                s.getMonthlySalary()  != null
                        ? String.format("$%,.0f", s.getMonthlySalary())       : "—"
        };
    }

    public static final String[] HEADERS_SITUACIONES = {
            "Cédula", "Nombre", "Tipo", "Fecha inicio", "Fecha fin", "Días", "Acto administrativo"
    };

    public static String[] situacionToRow(AdministrativeSituation a) {
        long dias = 0;
        if (a.getStartDate() != null && a.getEndDate() != null)
            dias = a.getStartDate().until(a.getEndDate()).getDays() + 1;
        return new String[]{
                a.getServer() != null ? a.getServer().getIdNumber()                 : "—",
                a.getServer() != null
                        ? a.getServer().getFirstName() + " " + a.getServer().getLastName() : "—",
                a.getType()   != null ? a.getType().name()                          : "—",
                a.getStartDate() != null ? a.getStartDate().format(FMT)             : "—",
                a.getEndDate()   != null ? a.getEndDate().format(FMT)               : "—",
                String.valueOf(dias),
                a.getAdministrativeAct() != null ? a.getAdministrativeAct()         : "—"
        };
    }

    public static final String[] HEADERS_VACACIONES_DEUDA = {
            "Cédula", "Nombre", "Fecha ingreso", "Años servicio",
            "Días acumulados", "Días usados", "Días pendientes", "Períodos pendientes"
    };

    // ─────────────────────────────────────────────────────────────────────
    // SELECTOR DE ARCHIVO
    // ─────────────────────────────────────────────────────────────────────

    private static File chooseFile(java.awt.Component parent, String suggested,
                                   String ext, FileNameExtensionFilter filter) {
        JFileChooser fc = new JFileChooser();
        fc.setSelectedFile(new File(suggested + "_" + LocalDate.now() + "." + ext));
        fc.setFileFilter(filter);
        fc.setDialogTitle("Guardar reporte");
        int res = fc.showSaveDialog(parent);
        if (res != JFileChooser.APPROVE_OPTION) return null;
        File file = fc.getSelectedFile();
        if (!file.getName().endsWith("." + ext)) file = new File(file.getAbsolutePath() + "." + ext);
        return file;
    }
}