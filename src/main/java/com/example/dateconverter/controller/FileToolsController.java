package com.example.dateconverter.controller;

import com.example.dateconverter.service.ExcelCsvService;
import com.example.dateconverter.service.PdfService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@Controller
public class FileToolsController {

    @Autowired
    private ExcelCsvService excelCsvService;

    @Autowired
    private PdfService pdfService;

    /* =====================================
       Excel ⇄ CSV
       ===================================== */

    @GetMapping("/excel-csv")
    public String excelCsvPage() {
        return "excel-csv"; // templates/excel-csv.html
    }

    @PostMapping("/excel-to-csv")
    public String excelToCsv(@RequestParam("file") MultipartFile file, Model model) {
        try {
            String downloadLink = excelCsvService.excelToCsv(file);
            model.addAttribute("result", "変換完了");
            model.addAttribute("downloadLink", downloadLink);
        } catch (Exception e) {
            model.addAttribute("error", "変換中にエラーが発生しました: " + e.getMessage());
        }
        return "excel-csv";
    }

    @PostMapping("/csv-to-excel")
    public String csvToExcel(@RequestParam("file") MultipartFile file, Model model) {
        try {
            String downloadLink = excelCsvService.csvToExcel(file);
            model.addAttribute("result", "変換完了");
            model.addAttribute("downloadLink", downloadLink);
        } catch (Exception e) {
            model.addAttribute("error", "変換中にエラーが発生しました: " + e.getMessage());
        }
        return "excel-csv";
    }

    /* =====================================
       PDFツール
       ===================================== */

    @GetMapping("/text-to-pdf")
    public String textToPdfPage() {
        return "text-to-pdf"; // templates/text-to-pdf.html
    }

    @PostMapping("/text-to-pdf")
    public String textToPdf(@RequestParam("textFile") MultipartFile textFile, Model model) {
        try {
            String downloadLink = pdfService.textToPdf(textFile);
            model.addAttribute("result", "変換完了");
            model.addAttribute("downloadLink", downloadLink);
        } catch (Exception e) {
            model.addAttribute("error", "変換中にエラーが発生しました: " + e.getMessage());
        }
        return "text-to-pdf";
    }

    @PostMapping("/pdf-to-text")
    public String pdfToText(@RequestParam("pdfFile") MultipartFile pdfFile, Model model) {
        try {
            String text = pdfService.pdfToText(pdfFile);
            model.addAttribute("result", text);
        } catch (Exception e) {
            model.addAttribute("error", "変換中にエラーが発生しました: " + e.getMessage());
        }
        return "text-to-pdf";
    }
}
