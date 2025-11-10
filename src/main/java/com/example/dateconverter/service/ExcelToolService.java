package com.example.dateconverter.service;

import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.apache.commons.csv.*;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.*;
import java.nio.file.*;

@Service
public class ExcelToolService {

    private static final String DOWNLOAD_DIR = "downloads";

    public ExcelToolService() throws IOException {
        Files.createDirectories(Paths.get(DOWNLOAD_DIR));
    }

    /**
     * CSV → Excel
     */
    public String convertCsvToExcel(MultipartFile csvFile) throws Exception {
        if (csvFile.isEmpty()) {
            throw new IllegalArgumentException("CSVファイルが選択されていません。");
        }

        File outputFile = new File(DOWNLOAD_DIR, csvFile.getOriginalFilename().replaceAll("\\.csv$", "") + ".xlsx");

        try (
            Reader reader = new InputStreamReader(csvFile.getInputStream(), "UTF-8");
            Workbook workbook = new XSSFWorkbook()
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

            try (FileOutputStream fos = new FileOutputStream(outputFile)) {
                workbook.write(fos);
            }
        }

        return "/" + DOWNLOAD_DIR + "/" + outputFile.getName();
    }

    /**
     * Excel → CSV
     */
    public String convertExcelToCsv(MultipartFile excelFile) throws Exception {
        if (excelFile.isEmpty()) {
            throw new IllegalArgumentException("Excelファイルが選択されていません。");
        }

        File outputFile = new File(DOWNLOAD_DIR, excelFile.getOriginalFilename().replaceAll("\\.xlsx?$", "") + ".csv");

        try (
            InputStream is = excelFile.getInputStream();
            Workbook workbook = WorkbookFactory.create(is);
            BufferedWriter writer = Files.newBufferedWriter(outputFile.toPath());
            CSVPrinter csvPrinter = new CSVPrinter(writer, CSVFormat.DEFAULT)
        ) {
            Sheet sheet = workbook.getSheetAt(0);
            for (Row row : sheet) {
                for (Cell cell : row) {
                    csvPrinter.print(cell.toString());
                }
                csvPrinter.println();
            }
        }

        return "/" + DOWNLOAD_DIR + "/" + outputFile.getName();
    }
}
