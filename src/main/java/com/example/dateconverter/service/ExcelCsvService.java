package com.example.dateconverter.service;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVPrinter;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;

@Service
public class ExcelCsvService {

    private final Path uploadDir = Path.of("uploads");

    public ExcelCsvService() throws IOException {
        if (!Files.exists(uploadDir)) {
            Files.createDirectories(uploadDir);
        }
    }

    // Excel -> CSV
    public String excelToCsv(MultipartFile file) throws Exception {
        try (Workbook workbook = new XSSFWorkbook(file.getInputStream())) {
            Sheet sheet = workbook.getSheetAt(0);
            String csvFileName = "converted_" + System.currentTimeMillis() + ".csv";
            Path csvPath = uploadDir.resolve(csvFileName);

            try (BufferedWriter writer = Files.newBufferedWriter(csvPath);
                 CSVPrinter csvPrinter = new CSVPrinter(writer, CSVFormat.DEFAULT)) {

                for (Row row : sheet) {
                    for (Cell cell : row) {
                        switch (cell.getCellType()) {
                            case STRING -> csvPrinter.print(cell.getStringCellValue());
                            case NUMERIC -> csvPrinter.print(cell.getNumericCellValue());
                            case BOOLEAN -> csvPrinter.print(cell.getBooleanCellValue());
                            default -> csvPrinter.print("");
                        }
                    }
                    csvPrinter.println();
                }
            }
            return "/uploads/" + csvFileName;
        }
    }

    // CSV -> Excel
    public String csvToExcel(MultipartFile file) throws Exception {
        String excelFileName = "converted_" + System.currentTimeMillis() + ".xlsx";
        Path excelPath = uploadDir.resolve(excelFileName);

        try (BufferedReader reader = new BufferedReader(new InputStreamReader(file.getInputStream()));
             Workbook workbook = new XSSFWorkbook()) {

            Sheet sheet = workbook.createSheet("Sheet1");
            String line;
            int rowNum = 0;
            while ((line = reader.readLine()) != null) {
                String[] values = line.split(",");
                Row row = sheet.createRow(rowNum++);
                for (int i = 0; i < values.length; i++) {
                    row.createCell(i).setCellValue(values[i]);
                }
            }

            try (FileOutputStream fos = new FileOutputStream(excelPath.toFile())) {
                workbook.write(fos);
            }
        }
        return "/uploads/" + excelFileName;
    }
}
