package com.example.dateconverter.service;

import com.opencsv.CSVReader;
import com.opencsv.CSVWriter;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.stereotype.Service;

import java.io.*;
import java.util.ArrayList;
import java.util.List;

@Service
public class ExcelCsvService {

    // CSV → Excel
    public File convertCsvToExcel(File csvFile) throws IOException {
        File excelFile = File.createTempFile("converted_", ".xlsx");
        try (
            CSVReader reader = new CSVReader(new FileReader(csvFile));
            Workbook workbook = new XSSFWorkbook();
        ) {
            Sheet sheet = workbook.createSheet("Sheet1");
            String[] nextLine;
            int rowNum = 0;

            while ((nextLine = reader.readNext()) != null) {
                Row row = sheet.createRow(rowNum++);
                for (int i = 0; i < nextLine.length; i++) {
                    row.createCell(i).setCellValue(nextLine[i]);
                }
            }

            try (FileOutputStream fileOut = new FileOutputStream(excelFile)) {
                workbook.write(fileOut);
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        return excelFile;
    }

    // Excel → CSV
    public File convertExcelToCsv(File excelFile) throws IOException {
        File csvFile = File.createTempFile("converted_", ".csv");

        try (
            Workbook workbook = new XSSFWorkbook(new FileInputStream(excelFile));
            FileWriter writer = new FileWriter(csvFile);
            CSVWriter csvWriter = new CSVWriter(writer)
        ) {
            Sheet sheet = workbook.getSheetAt(0);
            for (Row row : sheet) {
                List<String> cellValues = new ArrayList<>();
                for (Cell cell : row) {
                    cell.setCellType(CellType.STRING);
                    cellValues.add(cell.getStringCellValue());
                }
                csvWriter.writeNext(cellValues.toArray(new String[0]));
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        return csvFile;
    }
}
