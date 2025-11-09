package com.example.dateconverter.service;

import com.opencsv.CSVReader;
import com.opencsv.CSVWriter;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.stereotype.Service;

import java.io.*;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;

@Service
public class ExcelCsvService {

    // ===== Excel → CSV =====
    public File convertExcelToCsv(File excelFile) throws Exception {
        try (FileInputStream fis = new FileInputStream(excelFile);
             Workbook workbook = new XSSFWorkbook(fis)) {

            Sheet sheet = workbook.getSheetAt(0);
            File tempCsv = Files.createTempFile("excel-to-csv-", ".csv").toFile();

            try (CSVWriter writer = new CSVWriter(new FileWriter(tempCsv))) {
                for (Row row : sheet) {
                    List<String> cells = new ArrayList<>();
                    for (Cell cell : row) {
                        cell.setCellType(CellType.STRING);
                        cells.add(cell.getStringCellValue());
                    }
                    writer.writeNext(cells.toArray(new String[0]));
                }
            }
            return tempCsv;
        }
    }

    // ===== CSV → Excel =====
    public File convertCsvToExcel(File csvFile) throws Exception {
        File tempExcel = Files.createTempFile("csv-to-excel-", ".xlsx").toFile();

        try (CSVReader reader = new CSVReader(new FileReader(csvFile));
             Workbook workbook = new XSSFWorkbook()) {

            Sheet sheet = workbook.createSheet("Sheet1");
            String[] line;
            int rowNum = 0;

            while ((line = reader.readNext()) != null) {
                Row row = sheet.createRow(rowNum++);
                for (int i = 0; i < line.length; i++) {
                    Cell cell = row.createCell(i);
                    cell.setCellValue(line[i]);
                }
            }

            try (FileOutputStream fos = new FileOutputStream(tempExcel)) {
                workbook.write(fos);
            }
        }
        return tempExcel;
    }
}
