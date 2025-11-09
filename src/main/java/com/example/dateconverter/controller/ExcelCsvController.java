package com.example.dateconverter.controller;

import com.example.dateconverter.service.ExcelCsvService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;

@Controller
public class ExcelCsvController {

    @Autowired
    private ExcelCsvService excelCsvService;

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
}
