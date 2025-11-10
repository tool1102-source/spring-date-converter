package com.example.dateconverter.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import com.example.dateconverter.service.ExcelToolService;

@Controller
@RequestMapping("/excel-tools")
public class ExcelToolController {

    private final ExcelToolService excelToolService;

    public ExcelToolController(ExcelToolService excelToolService) {
        this.excelToolService = excelToolService;
    }

    // 画面表示
    @GetMapping
    public String showExcelTools(Model model) {
        model.addAttribute("pageTitle", "Excelツール");
        model.addAttribute("content", "excel-tools");
        return "layout";
    }

    // CSV → Excel
    @PostMapping("/csv-to-excel")
    public String csvToExcel(@RequestParam("csvFile") MultipartFile csvFile, Model model) {
        model.addAttribute("pageTitle", "Excelツール");
        model.addAttribute("content", "excel-tools");

        try {
            String downloadLink = excelToolService.convertCsvToExcel(csvFile);
            model.addAttribute("message", "Excelファイルに変換しました！");
            model.addAttribute("downloadLink", downloadLink);
        } catch (Exception e) {
            model.addAttribute("error", "変換中にエラーが発生しました: " + e.getMessage());
        }

        return "layout";
    }



    // Excel → CSV
    @PostMapping("/excel-to-csv")
    public String excelToCsv(@RequestParam("excelFile") MultipartFile excelFile, Model model) {
        model.addAttribute("pageTitle", "Excelツール");
        model.addAttribute("content", "excel-tools");

        try {
            String downloadLink = excelToolService.convertExcelToCsv(excelFile);
            model.addAttribute("message", "CSVファイルに変換しました！");
            model.addAttribute("downloadLink", downloadLink);
        } catch (Exception e) {
            model.addAttribute("error", "変換中にエラーが発生しました: " + e.getMessage());
        }

        return "layout";
    }
}
