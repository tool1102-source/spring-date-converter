package com.example.dateconverter.controller;

import com.example.dateconverter.service.ExcelCsvService;
import com.example.dateconverter.service.PdfService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.InputStreamResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.FileInputStream;
import java.nio.charset.StandardCharsets;

@Controller
public class ToolController {

    @Autowired
    private PdfService pdfService;

    @Autowired
    private ExcelCsvService excelCsvService;

    // ===== PDF変換ページ =====
    @GetMapping("/text-to-pdf")
    public String showTextToPdfPage() {
        return "text-to-pdf";
    }

    @PostMapping("/text-to-pdf")
    public String convertTextToPdf(@RequestParam("textFile") MultipartFile textFile, Model model) {
        try {
            String text = new String(textFile.getBytes(), StandardCharsets.UTF_8);
            File pdfFile = pdfService.textToPdf(text);

            model.addAttribute("downloadLink", "/download?file=" + pdfFile.getAbsolutePath());
            model.addAttribute("message", "PDF変換が完了しました！");
        } catch (Exception e) {
            model.addAttribute("error", "変換中にエラーが発生しました: " + e.getMessage());
        }
        return "text-to-pdf";
    }

    // ===== Excel ⇄ CSVページ =====
    @GetMapping("/excel-csv")
    public String showExcelCsvPage() {
        return "excel-csv";
    }

    @PostMapping("/excel-to-csv")
    public String convertExcelToCsv(@RequestParam("file") MultipartFile file, Model model) {
        try {
            File tempFile = File.createTempFile("upload_", ".xlsx");
            file.transferTo(tempFile);

            File converted = excelCsvService.convertExcelToCsv(tempFile);
            model.addAttribute("downloadLink", "/download?file=" + converted.getAbsolutePath());
            model.addAttribute("message", "Excel → CSV 変換が完了しました！");
        } catch (Exception e) {
            model.addAttribute("error", "変換中にエラーが発生しました: " + e.getMessage());
        }
        return "excel-csv";
    }

    @PostMapping("/csv-to-excel")
    public String convertCsvToExcel(@RequestParam("file") MultipartFile file, Model model) {
        try {
            File tempFile = File.createTempFile("upload_", ".csv");
            file.transferTo(tempFile);

            File converted = excelCsvService.convertCsvToExcel(tempFile);
            model.addAttribute("downloadLink", "/download?file=" + converted.getAbsolutePath());
            model.addAttribute("message", "CSV → Excel 変換が完了しました！");
        } catch (Exception e) {
            model.addAttribute("error", "変換中にエラーが発生しました: " + e.getMessage());
        }
        return "excel-csv";
    }

    // ===== ダウンロード共通 =====
    @GetMapping("/download")
    public ResponseEntity<InputStreamResource> downloadFile(@RequestParam("file") String filePath) throws Exception {
        File file = new File(filePath);
        if (!file.exists()) {
            throw new RuntimeException("File not found: " + filePath);
        }

        InputStreamResource resource = new InputStreamResource(new FileInputStream(file));

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment;filename=" + file.getName())
                .contentType(MediaType.APPLICATION_OCTET_STREAM)
                .contentLength(file.length())
                .body(resource);
    }
}
