package cr.una.reservas_municipales.service;

import cr.una.reservas_municipales.dto.ReservationWithSpaceDto;
import cr.una.reservas_municipales.dto.ReservationSummaryDto;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class ReservationExportService {

    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");
    private static final DateTimeFormatter DATE_ONLY_FORMATTER = DateTimeFormatter.ofPattern("dd/MM/yyyy");

    public byte[] generateReservationsExcel(List<ReservationWithSpaceDto> reservations, 
                                           ReservationSummaryDto summary) throws IOException {
        
        log.info("Generando reporte Excel para {} reservaciones del usuario: {}", 
                reservations.size(), summary.getUserName());
        
        try (Workbook workbook = new XSSFWorkbook()) {
            
            // Hoja 1: Datos de reservaciones
            Sheet reservationsSheet = workbook.createSheet("Reservaciones");
            createReservationsSheet(reservationsSheet, reservations, workbook);
            
            // Hoja 2: Resumen estadístico
            Sheet summarySheet = workbook.createSheet("Resumen");
            createSummarySheet(summarySheet, summary, workbook);
            
            // Convertir a bytes
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            workbook.write(outputStream);
            
            log.info("Reporte Excel generado exitosamente. Tamaño: {} bytes", outputStream.size());
            return outputStream.toByteArray();
        }
    }

    private void createReservationsSheet(Sheet sheet, List<ReservationWithSpaceDto> reservations, Workbook workbook) {
        // Crear estilos
        CellStyle headerStyle = createHeaderStyle(workbook);
        CellStyle dataStyle = createDataStyle(workbook);
        CellStyle currencyStyle = createCurrencyStyle(workbook);
        CellStyle dateStyle = createDateStyle(workbook);

        // Headers
        Row headerRow = sheet.createRow(0);
        String[] headers = {
            "ID Reserva", "Espacio", "Fecha Inicio", "Fecha Fin", 
            "Estado", "Monto Total", "Moneda", "Fecha Creación", "Observaciones"
        };
        
        for (int i = 0; i < headers.length; i++) {
            Cell cell = headerRow.createCell(i);
            cell.setCellValue(headers[i]);
            cell.setCellStyle(headerStyle);
        }

        // Datos
        int rowNum = 1;
        for (ReservationWithSpaceDto reservation : reservations) {
            Row row = sheet.createRow(rowNum++);
            
            // ID Reserva
            Cell cell0 = row.createCell(0);
            cell0.setCellValue(reservation.getReservationId().toString());
            cell0.setCellStyle(dataStyle);
            
            // Espacio
            Cell cell1 = row.createCell(1);
            cell1.setCellValue(reservation.getSpaceName());
            cell1.setCellStyle(dataStyle);
            
            // Fecha Inicio
            Cell cell2 = row.createCell(2);
            cell2.setCellValue(reservation.getStartsAt().format(DATE_FORMATTER));
            cell2.setCellStyle(dateStyle);
            
            // Fecha Fin
            Cell cell3 = row.createCell(3);
            cell3.setCellValue(reservation.getEndsAt().format(DATE_FORMATTER));
            cell3.setCellStyle(dateStyle);
            
            // Estado
            Cell cell4 = row.createCell(4);
            cell4.setCellValue(getStatusDisplayName(reservation.getStatus()));
            cell4.setCellStyle(dataStyle);
            
            // Monto Total
            Cell cell5 = row.createCell(5);
            if (reservation.getTotalAmount() != null) {
                cell5.setCellValue(reservation.getTotalAmount().doubleValue());
            } else {
                cell5.setCellValue(0.0);
            }
            cell5.setCellStyle(currencyStyle);
            
            // Moneda
            Cell cell6 = row.createCell(6);
            cell6.setCellValue(reservation.getCurrency() != null ? reservation.getCurrency() : "CRC");
            cell6.setCellStyle(dataStyle);
            
            // Fecha Creación
            Cell cell7 = row.createCell(7);
            cell7.setCellValue(reservation.getCreatedAt().format(DATE_ONLY_FORMATTER));
            cell7.setCellStyle(dateStyle);
            
            // Observaciones
            Cell cell8 = row.createCell(8);
            cell8.setCellValue(reservation.getObservations());
            cell8.setCellStyle(dataStyle);
        }

        // Auto-ajustar columnas
        for (int i = 0; i < headers.length; i++) {
            sheet.autoSizeColumn(i);
        }
    }

    private void createSummarySheet(Sheet sheet, ReservationSummaryDto summary, Workbook workbook) {
        CellStyle headerStyle = createHeaderStyle(workbook);
        CellStyle dataStyle = createDataStyle(workbook);
        CellStyle currencyStyle = createCurrencyStyle(workbook);
        CellStyle titleStyle = createTitleStyle(workbook);

        int rowNum = 0;

        // Título principal
        Row titleRow = sheet.createRow(rowNum++);
        Cell titleCell = titleRow.createCell(0);
        titleCell.setCellValue("REPORTE DE RESERVACIONES");
        titleCell.setCellStyle(titleStyle);
        
        // Espacio
        rowNum++;
        
        // Información del usuario
        Row userRow = sheet.createRow(rowNum++);
        Cell userLabelCell = userRow.createCell(0);
        userLabelCell.setCellValue("Usuario:");
        userLabelCell.setCellStyle(headerStyle);
        Cell userValueCell = userRow.createCell(1);
        userValueCell.setCellValue(summary.getUserName());
        userValueCell.setCellStyle(dataStyle);
        
        Row emailRow = sheet.createRow(rowNum++);
        Cell emailLabelCell = emailRow.createCell(0);
        emailLabelCell.setCellValue("Email:");
        emailLabelCell.setCellStyle(headerStyle);
        Cell emailValueCell = emailRow.createCell(1);
        emailValueCell.setCellValue(summary.getUserEmail());
        emailValueCell.setCellStyle(dataStyle);
        
        // Espacio
        rowNum++;
        
        // Estadísticas
        Row statsHeaderRow = sheet.createRow(rowNum++);
        Cell statsHeaderCell = statsHeaderRow.createCell(0);
        statsHeaderCell.setCellValue("ESTADÍSTICAS");
        statsHeaderCell.setCellStyle(titleStyle);
        
        // Total de reservas
        Row totalRow = sheet.createRow(rowNum++);
        Cell totalLabelCell = totalRow.createCell(0);
        totalLabelCell.setCellValue("Total de Reservas:");
        totalLabelCell.setCellStyle(headerStyle);
        Cell totalValueCell = totalRow.createCell(1);
        totalValueCell.setCellValue(summary.getTotalReservations());
        totalValueCell.setCellStyle(dataStyle);
        
        // Reservas confirmadas
        Row confirmedRow = sheet.createRow(rowNum++);
        Cell confirmedLabelCell = confirmedRow.createCell(0);
        confirmedLabelCell.setCellValue("Reservas Confirmadas:");
        confirmedLabelCell.setCellStyle(headerStyle);
        Cell confirmedValueCell = confirmedRow.createCell(1);
        confirmedValueCell.setCellValue(summary.getConfirmedReservations());
        confirmedValueCell.setCellStyle(dataStyle);
        
        // Reservas canceladas
        Row cancelledRow = sheet.createRow(rowNum++);
        Cell cancelledLabelCell = cancelledRow.createCell(0);
        cancelledLabelCell.setCellValue("Reservas Canceladas:");
        cancelledLabelCell.setCellStyle(headerStyle);
        Cell cancelledValueCell = cancelledRow.createCell(1);
        cancelledValueCell.setCellValue(summary.getCancelledReservations());
        cancelledValueCell.setCellStyle(dataStyle);
        
        // Reservas pendientes
        Row pendingRow = sheet.createRow(rowNum++);
        Cell pendingLabelCell = pendingRow.createCell(0);
        pendingLabelCell.setCellValue("Reservas Pendientes:");
        pendingLabelCell.setCellStyle(headerStyle);
        Cell pendingValueCell = pendingRow.createCell(1);
        pendingValueCell.setCellValue(summary.getPendingReservations());
        pendingValueCell.setCellStyle(dataStyle);
        
        // Reservas completadas
        Row completedRow = sheet.createRow(rowNum++);
        Cell completedLabelCell = completedRow.createCell(0);
        completedLabelCell.setCellValue("Reservas Completadas:");
        completedLabelCell.setCellStyle(headerStyle);
        Cell completedValueCell = completedRow.createCell(1);
        completedValueCell.setCellValue(summary.getCompletedReservations());
        completedValueCell.setCellStyle(dataStyle);
        
        // Total dinero pagado
        Row amountRow = sheet.createRow(rowNum++);
        Cell amountLabelCell = amountRow.createCell(0);
        amountLabelCell.setCellValue("Total Dinero Pagado:");
        amountLabelCell.setCellStyle(headerStyle);
        Cell amountValueCell = amountRow.createCell(1);
        if (summary.getTotalAmountPaid() != null) {
            amountValueCell.setCellValue(summary.getTotalAmountPaid().doubleValue());
        } else {
            amountValueCell.setCellValue(0.0);
        }
        amountValueCell.setCellStyle(currencyStyle);
        
        // Moneda
        Row currencyRow = sheet.createRow(rowNum++);
        Cell currencyLabelCell = currencyRow.createCell(0);
        currencyLabelCell.setCellValue("Moneda:");
        currencyLabelCell.setCellStyle(headerStyle);
        Cell currencyValueCell = currencyRow.createCell(1);
        currencyValueCell.setCellValue(summary.getCurrency() != null ? summary.getCurrency() : "CRC");
        currencyValueCell.setCellStyle(dataStyle);

        // Auto-ajustar columnas
        sheet.autoSizeColumn(0);
        sheet.autoSizeColumn(1);
    }

    private CellStyle createHeaderStyle(Workbook workbook) {
        CellStyle style = workbook.createCellStyle();
        Font font = workbook.createFont();
        font.setBold(true);
        font.setColor(IndexedColors.WHITE.getIndex());
        style.setFont(font);
        style.setFillForegroundColor(IndexedColors.DARK_BLUE.getIndex());
        style.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        style.setBorderBottom(BorderStyle.THIN);
        style.setBorderTop(BorderStyle.THIN);
        style.setBorderRight(BorderStyle.THIN);
        style.setBorderLeft(BorderStyle.THIN);
        style.setAlignment(HorizontalAlignment.CENTER);
        return style;
    }

    private CellStyle createDataStyle(Workbook workbook) {
        CellStyle style = workbook.createCellStyle();
        style.setBorderBottom(BorderStyle.THIN);
        style.setBorderTop(BorderStyle.THIN);
        style.setBorderRight(BorderStyle.THIN);
        style.setBorderLeft(BorderStyle.THIN);
        return style;
    }

    private CellStyle createCurrencyStyle(Workbook workbook) {
        CellStyle style = createDataStyle(workbook);
        style.setDataFormat(workbook.createDataFormat().getFormat("#,##0.00"));
        return style;
    }

    private CellStyle createDateStyle(Workbook workbook) {
        CellStyle style = createDataStyle(workbook);
        style.setAlignment(HorizontalAlignment.CENTER);
        return style;
    }

    private CellStyle createTitleStyle(Workbook workbook) {
        CellStyle style = workbook.createCellStyle();
        Font font = workbook.createFont();
        font.setBold(true);
        font.setFontHeightInPoints((short) 14);
        font.setColor(IndexedColors.DARK_BLUE.getIndex());
        style.setFont(font);
        style.setAlignment(HorizontalAlignment.CENTER);
        return style;
    }

    private String getStatusDisplayName(String status) {
        switch (status) {
            case "PENDING":
                return "Pendiente";
            case "CONFIRMED":
                return "Confirmada";
            case "CANCELLED":
                return "Cancelada";
            case "COMPLETED":
                return "Completada";
            default:
                return status;
        }
    }
}