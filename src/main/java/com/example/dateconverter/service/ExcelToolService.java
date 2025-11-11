package com.example.dateconverter.service;

import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.apache.commons.csv.*;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.*;
import java.nio.charset.StandardCharsets;

@Service
public class ExcelToolService {

    /**
     * CSV → Excel (結果をバイト配列で返す)
     */
    public byte[] convertCsvToExcel(MultipartFile csvFile) throws Exception {
        if (csvFile.isEmpty()) {
            throw new IllegalArgumentException("CSVファイルが選択されていません。");
        }

        try (
            // BOM付きCSVファイル対応のため、InputStreamReaderを使用
            Reader reader = new InputStreamReader(csvFile.getInputStream(), StandardCharsets.UTF_8); 
            Workbook workbook = new XSSFWorkbook();
            ByteArrayOutputStream bos = new ByteArrayOutputStream() // メモリにExcelを書き出す
        ) {
            Sheet sheet = workbook.createSheet("Sheet1");
            CSVParser csvParser = CSVFormat.DEFAULT.parse(reader);

            int rowIndex = 0;
            for (CSVRecord record : csvParser) {
                Row row = sheet.createRow(rowIndex++);
                for (int i = 0; i < record.size(); i++) {
                    row.createCell(i).setCellValue(record.get(i));
                }
            }

            workbook.write(bos);
            
            return bos.toByteArray(); 
        }
    }

    /**
     * Excel → CSV (結果をバイト配列で返す)
     */
    public byte[] convertExcelToCsv(MultipartFile excelFile) throws Exception {
        if (excelFile.isEmpty()) {
            throw new IllegalArgumentException("Excelファイルが選択されていません。");
        }

        try (
            InputStream is = excelFile.getInputStream();
            Workbook workbook = WorkbookFactory.create(is);
            ByteArrayOutputStream bos = new ByteArrayOutputStream(); // メモリにCSVを書き出す
            // WriterとしてOutputStreamWriterを使用
            BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(bos, StandardCharsets.UTF_8)); 
            CSVPrinter csvPrinter = new CSVPrinter(writer, CSVFormat.DEFAULT) 
        ) {
            Sheet sheet = workbook.getSheetAt(0);
            for (Row row : sheet) {
                for (Cell cell : row) {
                    // セルの値を取得し、文字列としてCSVに出力
                    csvPrinter.print(getCellValueAsString(cell));
                }
                csvPrinter.println();
            }
            csvPrinter.flush();
            
            return bos.toByteArray();
        }
    }

    // Excelセルの内容を安全に文字列として取得するヘルパーメソッド
    private String getCellValueAsString(Cell cell) {
        if (cell == null) {
            return "";
        }
        
        // 以下のswitch式を修正し、defaultケースを追加しました
        return switch (cell.getCellType()) {
            case STRING -> cell.getStringCellValue();
            case NUMERIC -> {
                if (DateUtil.isCellDateFormatted(cell)) {
                    yield cell.getDateCellValue().toString();
                } else {
                    // 数値の小数点以下のゼロを省略するためにフォーマット
                    DataFormatter formatter = new DataFormatter();
                    yield formatter.formatCellValue(cell);
                }
            }
            case BOOLEAN -> String.valueOf(cell.getBooleanCellValue());
            case FORMULA -> cell.getCellFormula();
            // BLANK, ERROR, およびその他の未定義のケースをdefaultで処理
            default -> ""; 
        };
    }
}