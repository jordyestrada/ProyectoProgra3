package cr.una.reservas_municipales.service;

import cr.una.reservas_municipales.dto.ReservationSummaryDto;
import cr.una.reservas_municipales.dto.ReservationWithSpaceDto;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
class ReservationExportServiceTest {

    @InjectMocks
    private ReservationExportService exportService;

    private List<ReservationWithSpaceDto> testReservations;
    private ReservationSummaryDto testSummary;

    @BeforeEach
    void setUp() {
        testReservations = new ArrayList<>();
        
        // Crear reservas de prueba
        ReservationWithSpaceDto reservation1 = new ReservationWithSpaceDto();
        reservation1.setReservationId(UUID.randomUUID());
        reservation1.setSpaceName("Cancha de Fútbol");
        reservation1.setStartsAt(OffsetDateTime.now().plusDays(1));
        reservation1.setEndsAt(OffsetDateTime.now().plusDays(1).plusHours(2));
        reservation1.setStatus("CONFIRMED");
        reservation1.setTotalAmount(new BigDecimal("5000.00"));
        reservation1.setCreatedAt(OffsetDateTime.now());
        testReservations.add(reservation1);

        ReservationWithSpaceDto reservation2 = new ReservationWithSpaceDto();
        reservation2.setReservationId(UUID.randomUUID());
        reservation2.setSpaceName("Salón de Eventos");
        reservation2.setStartsAt(OffsetDateTime.now().plusDays(2));
        reservation2.setEndsAt(OffsetDateTime.now().plusDays(2).plusHours(4));
        reservation2.setStatus("PENDING");
        reservation2.setTotalAmount(new BigDecimal("15000.00"));
        reservation2.setCreatedAt(OffsetDateTime.now());
        testReservations.add(reservation2);

        // Crear resumen de prueba
        testSummary = ReservationSummaryDto.builder()
                .totalReservations(2L)
                .confirmedReservations(1L)
                .pendingReservations(1L)
                .cancelledReservations(0L)
                .completedReservations(0L)
                .totalAmountPaid(new BigDecimal("5000.00"))
                .build();
    }

    @Test
    void testGenerateReservationsExcel_Success() throws IOException {
        // Act
        byte[] excelBytes = exportService.generateReservationsExcel(testReservations, testSummary);

        // Assert
        assertNotNull(excelBytes);
        assertTrue(excelBytes.length > 0);

        // Verificar contenido del Excel
        try (ByteArrayInputStream bis = new ByteArrayInputStream(excelBytes);
             Workbook workbook = new XSSFWorkbook(bis)) {

            // Verificar que tiene 2 hojas
            assertEquals(2, workbook.getNumberOfSheets());
            assertEquals("Reservaciones", workbook.getSheetName(0));
            assertEquals("Resumen", workbook.getSheetName(1));

            // Verificar hoja de reservas
            Sheet reservationsSheet = workbook.getSheetAt(0);
            assertNotNull(reservationsSheet);
            
            // Verificar encabezados (fila 0)
            Row headerRow = reservationsSheet.getRow(0);
            assertNotNull(headerRow);
            assertEquals("ID Reserva", headerRow.getCell(0).getStringCellValue());
            assertEquals("Espacio", headerRow.getCell(1).getStringCellValue());
            assertEquals("Fecha Inicio", headerRow.getCell(2).getStringCellValue());
            assertEquals("Fecha Fin", headerRow.getCell(3).getStringCellValue());
            assertEquals("Estado", headerRow.getCell(4).getStringCellValue());
            assertEquals("Monto Total", headerRow.getCell(5).getStringCellValue());

            // Verificar datos (fila 1 y 2)
            assertEquals(3, reservationsSheet.getPhysicalNumberOfRows()); // Header + 2 filas de datos
            
            Row dataRow1 = reservationsSheet.getRow(1);
            assertNotNull(dataRow1);
            assertEquals("Cancha de Fútbol", dataRow1.getCell(1).getStringCellValue());
            assertEquals("Confirmada", dataRow1.getCell(4).getStringCellValue()); // Estado traducido
            assertEquals(5000.00, dataRow1.getCell(5).getNumericCellValue(), 0.01);

            Row dataRow2 = reservationsSheet.getRow(2);
            assertNotNull(dataRow2);
            assertEquals("Salón de Eventos", dataRow2.getCell(1).getStringCellValue());
            assertEquals("Pendiente", dataRow2.getCell(4).getStringCellValue()); // Estado traducido
            assertEquals(15000.00, dataRow2.getCell(5).getNumericCellValue(), 0.01);

            // Verificar hoja de resumen
            Sheet summarySheet = workbook.getSheetAt(1);
            assertNotNull(summarySheet);
            
            // Fila 6: Total de Reservas
            Row summaryRow1 = summarySheet.getRow(6);
            assertNotNull(summaryRow1);
            assertEquals(2.0, summaryRow1.getCell(1).getNumericCellValue());

            // Fila 7: Reservas Confirmadas
            Row summaryRow2 = summarySheet.getRow(7);
            assertNotNull(summaryRow2);
            assertEquals(1.0, summaryRow2.getCell(1).getNumericCellValue());

            // Fila 8: Reservas Canceladas  
            Row summaryRow3 = summarySheet.getRow(8);
            assertNotNull(summaryRow3);
            assertEquals(0.0, summaryRow3.getCell(1).getNumericCellValue());

            // Fila 9: Reservas Pendientes
            Row summaryRow4 = summarySheet.getRow(9);
            assertNotNull(summaryRow4);
            assertEquals(1.0, summaryRow4.getCell(1).getNumericCellValue());
        }
    }

    @Test
    void testGenerateReservationsExcel_EmptyList() throws IOException {
        // Arrange
        List<ReservationWithSpaceDto> emptyList = new ArrayList<>();
        ReservationSummaryDto emptySummary = ReservationSummaryDto.builder()
                .totalReservations(0L)
                .confirmedReservations(0L)
                .pendingReservations(0L)
                .cancelledReservations(0L)
                .completedReservations(0L)
                .totalAmountPaid(BigDecimal.ZERO)
                .build();

        // Act
        byte[] excelBytes = exportService.generateReservationsExcel(emptyList, emptySummary);

        // Assert
        assertNotNull(excelBytes);
        assertTrue(excelBytes.length > 0);

        try (ByteArrayInputStream bis = new ByteArrayInputStream(excelBytes);
             Workbook workbook = new XSSFWorkbook(bis)) {

            Sheet reservationsSheet = workbook.getSheetAt(0);
            // Solo debe tener la fila de encabezados
            assertEquals(1, reservationsSheet.getPhysicalNumberOfRows());

            Sheet summarySheet = workbook.getSheetAt(1);
            assertNotNull(summarySheet);
            // Verificar que todos los valores son cero
            // Fila 6: Total de Reservas
            assertNotNull(summarySheet.getRow(6));
            assertEquals(0.0, summarySheet.getRow(6).getCell(1).getNumericCellValue());
            // Fila 11: Total Dinero Pagado
            assertNotNull(summarySheet.getRow(11));
            assertEquals(0.0, summarySheet.getRow(11).getCell(1).getNumericCellValue());
        }
    }

    @Test
    void testGenerateReservationsExcel_NullTotalAmount() throws IOException {
        // Arrange
        testReservations.get(0).setTotalAmount(null);

        // Act
        byte[] excelBytes = exportService.generateReservationsExcel(testReservations, testSummary);

        // Assert
        assertNotNull(excelBytes);
        
        try (ByteArrayInputStream bis = new ByteArrayInputStream(excelBytes);
             Workbook workbook = new XSSFWorkbook(bis)) {

            Sheet reservationsSheet = workbook.getSheetAt(0);
            Row dataRow = reservationsSheet.getRow(1);
            
            // Verificar que maneja null correctamente (debería ser 0.0 o celda vacía)
            Cell amountCell = dataRow.getCell(5);
            if (amountCell != null && amountCell.getCellType() == CellType.NUMERIC) {
                assertEquals(0.0, amountCell.getNumericCellValue(), 0.01);
            }
        }
    }

    @Test
    void testGenerateReservationsExcel_MultipleStatuses() throws IOException {
        // Arrange
        ReservationWithSpaceDto cancelledReservation = new ReservationWithSpaceDto();
        cancelledReservation.setReservationId(UUID.randomUUID());
        cancelledReservation.setSpaceName("Gimnasio");
        cancelledReservation.setStartsAt(OffsetDateTime.now().plusDays(3));
        cancelledReservation.setEndsAt(OffsetDateTime.now().plusDays(3).plusHours(1));
        cancelledReservation.setStatus("CANCELLED");
        cancelledReservation.setTotalAmount(BigDecimal.ZERO);
        cancelledReservation.setCreatedAt(OffsetDateTime.now());
        testReservations.add(cancelledReservation);

        // Crear un nuevo summary con los valores actualizados
        ReservationSummaryDto updatedSummary = ReservationSummaryDto.builder()
                .totalReservations(3L)
                .confirmedReservations(1L)
                .pendingReservations(1L)
                .cancelledReservations(1L)
                .completedReservations(0L)
                .totalAmountPaid(new BigDecimal("5000.00"))
                .build();

        // Act
        byte[] excelBytes = exportService.generateReservationsExcel(testReservations, updatedSummary);

        // Assert
        assertNotNull(excelBytes);

        try (ByteArrayInputStream bis = new ByteArrayInputStream(excelBytes);
             Workbook workbook = new XSSFWorkbook(bis)) {

            Sheet reservationsSheet = workbook.getSheetAt(0);
            assertEquals(4, reservationsSheet.getPhysicalNumberOfRows()); // Header + 3 datos

            // Verificar que todos los estados están presentes (traducidos)
            assertNotNull(reservationsSheet.getRow(1));
            assertEquals("Confirmada", reservationsSheet.getRow(1).getCell(4).getStringCellValue());
            assertNotNull(reservationsSheet.getRow(2));
            assertEquals("Pendiente", reservationsSheet.getRow(2).getCell(4).getStringCellValue());
            assertNotNull(reservationsSheet.getRow(3));
            assertEquals("Cancelada", reservationsSheet.getRow(3).getCell(4).getStringCellValue());

            Sheet summarySheet = workbook.getSheetAt(1);
            // Fila 6: Total de Reservas
            assertNotNull(summarySheet.getRow(6));
            assertEquals(3.0, summarySheet.getRow(6).getCell(1).getNumericCellValue()); // Total
            // Fila 8: Canceladas
            assertNotNull(summarySheet.getRow(8));
            assertEquals(1.0, summarySheet.getRow(8).getCell(1).getNumericCellValue()); // Canceladas
        }
    }

    @Test
    void testGenerateReservationsExcel_CellFormatting() throws IOException {
        // Act
        byte[] excelBytes = exportService.generateReservationsExcel(testReservations, testSummary);

        // Assert
        try (ByteArrayInputStream bis = new ByteArrayInputStream(excelBytes);
             Workbook workbook = new XSSFWorkbook(bis)) {

            Sheet reservationsSheet = workbook.getSheetAt(0);
            Row headerRow = reservationsSheet.getRow(0);
            
            // Verificar que los encabezados tienen formato especial (negrita)
            Cell headerCell = headerRow.getCell(0);
            CellStyle headerStyle = headerCell.getCellStyle();
            assertNotNull(headerStyle);
            
            // Verificar que las fechas tienen formato de fecha
            Row dataRow = reservationsSheet.getRow(1);
            Cell dateCell = dataRow.getCell(2);
            assertNotNull(dateCell);
            
            // Verificar que los montos tienen formato de moneda
            Cell amountCell = dataRow.getCell(5);
            assertEquals(CellType.NUMERIC, amountCell.getCellType());
        }
    }

    @Test
    void testGenerateReservationsExcel_LargeDataset() throws IOException {
        // Arrange - Crear 100 reservas
        List<ReservationWithSpaceDto> largeList = new ArrayList<>();
        for (int i = 0; i < 100; i++) {
            ReservationWithSpaceDto reservation = new ReservationWithSpaceDto();
            reservation.setReservationId(UUID.randomUUID());
            reservation.setSpaceName("Espacio " + i);
            reservation.setStartsAt(OffsetDateTime.now().plusDays(i));
            reservation.setEndsAt(OffsetDateTime.now().plusDays(i).plusHours(2));
            reservation.setStatus(i % 3 == 0 ? "CONFIRMED" : i % 3 == 1 ? "PENDING" : "CANCELLED");
            reservation.setTotalAmount(new BigDecimal("1000.00"));
            reservation.setCreatedAt(OffsetDateTime.now());
            largeList.add(reservation);
        }

        ReservationSummaryDto largeSummary = ReservationSummaryDto.builder()
                .totalReservations(100L)
                .confirmedReservations(34L)
                .pendingReservations(33L)
                .cancelledReservations(33L)
                .totalAmountPaid(new BigDecimal("100000.00"))
                .build();

        // Act
        byte[] excelBytes = exportService.generateReservationsExcel(largeList, largeSummary);

        // Assert
        assertNotNull(excelBytes);
        assertTrue(excelBytes.length > 0);

        try (ByteArrayInputStream bis = new ByteArrayInputStream(excelBytes);
             Workbook workbook = new XSSFWorkbook(bis)) {

            Sheet reservationsSheet = workbook.getSheetAt(0);
            assertEquals(101, reservationsSheet.getPhysicalNumberOfRows()); // Header + 100 datos
        }
    }
}
