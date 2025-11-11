package com.example.dateconverter.controller;

import org.springframework.beans.factory.annotation.Autowired; 
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import com.example.dateconverter.service.ExcelToolService;

@Controller
@RequestMapping("/excel-tools")
public class ExcelToolController {

    private final ExcelToolService excelToolService;

    @Autowired 
    public ExcelToolController(ExcelToolService excelToolService) {
        this.excelToolService = excelToolService;
    }

    /**
     * 画面表示 (GET /excel-tools)
     */
    @GetMapping
    public String showExcelTools(Model model) {
        // 🚨 SEO修正: pageTitleを設定
        model.addAttribute("pageTitle", "Excel ⇄ CSV 相互変換ツール");
        // 🚨 新規追加: metaDescriptionを追加
        model.addAttribute("metaDescription", "CSVファイルからExcel (xlsx) への変換、またはExcelからCSVへの変換をオンラインで行う無料ツール。開発時のデータ操作を効率化します。");
        // 🚨 独自ドメイン設定: Canonical URLを設定
        model.addAttribute("canonicalUrl", "https://convertertools.jp/excel-tools");
        
        model.addAttribute("content", "excel-tools");
        return "layout";
    }

    /**
     * CSV → Excel (ResponseEntityでファイルとして返す)
     */
    @PostMapping("/csv-to-excel")
    public Object csvToExcel(@RequestParam("csvFile") MultipartFile csvFile, Model model) {
        try {
            byte[] excelBytes = excelToolService.convertCsvToExcel(csvFile);
            
            String originalFilename = csvFile.getOriginalFilename();
            String filename = (originalFilename != null && !originalFilename.isEmpty()) ? 
                              originalFilename.replaceAll("\\.csv$", "") + ".xlsx" : "converted.xlsx";

            HttpHeaders header = new HttpHeaders();
            header.add(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + filename + "\"");

            ByteArrayResource resource = new ByteArrayResource(excelBytes);

            return ResponseEntity.ok()
                    .headers(header)
                    .contentLength(excelBytes.length)
                    .contentType(MediaType.parseMediaType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"))
                    .body(resource);
                    
        } catch (IllegalArgumentException e) {
            // 🚨 エラー時: ビューに戻るためにメタデータを再設定
            model.addAttribute("pageTitle", "Excel ⇄ CSV 相互変換ツール");
            model.addAttribute("metaDescription", "CSVファイルからExcel (xlsx) への変換、またはExcelからCSVへの変換をオンラインで行う無料ツール。開発時のデータ操作を効率化します。");
            model.addAttribute("canonicalUrl", "https://convertertools.jp/excel-tools");
            model.addAttribute("error", e.getMessage()); 
            model.addAttribute("content", "excel-tools");
            return "layout";
        } catch (Exception e) {
            // 🚨 エラー時: ビューに戻るためにメタデータを再設定
            model.addAttribute("pageTitle", "Excel ⇄ CSV 相互変換ツール");
            model.addAttribute("metaDescription", "CSVファイルからExcel (xlsx) への変換、またはExcelからCSVへの変換をオンラインで行う無料ツール。開発時のデータ操作を効率化します。");
            model.addAttribute("canonicalUrl", "https://convertertools.jp/excel-tools");
            model.addAttribute("error", "Excel変換中に予期せぬエラーが発生しました。");
            model.addAttribute("content", "excel-tools");
            return "layout";
        }
    }

    /**
     * Excel → CSV (ResponseEntityでファイルとして返す)
     */
    @PostMapping("/excel-to-csv")
    public Object excelToCsv(@RequestParam("excelFile") MultipartFile excelFile, Model model) {
        try {
            byte[] csvBytes = excelToolService.convertExcelToCsv(excelFile);
            
            String originalFilename = excelFile.getOriginalFilename();
            String filename = (originalFilename != null && !originalFilename.isEmpty()) ? 
                              originalFilename.replaceAll("\\.xlsx?$", "") + ".csv" : "converted.csv";

            HttpHeaders header = new HttpHeaders();
            header.add(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + filename + "\"");

            ByteArrayResource resource = new ByteArrayResource(csvBytes);

            return ResponseEntity.ok()
                    .headers(header)
                    .contentLength(csvBytes.length)
                    .contentType(MediaType.parseMediaType("text/csv"))
                    .body(resource);
                    
        } catch (IllegalArgumentException e) {
            // 🚨 エラー時: ビューに戻るためにメタデータを再設定
            model.addAttribute("pageTitle", "Excel ⇄ CSV 相互変換ツール");
            model.addAttribute("metaDescription", "CSVファイルからExcel (xlsx) への変換、またはExcelからCSVへの変換をオンラインで行う無料ツール。開発時のデータ操作を効率化します。");
            model.addAttribute("canonicalUrl", "https://convertertools.jp/excel-tools");
            model.addAttribute("error", e.getMessage());
            model.addAttribute("content", "excel-tools");
            return "layout";
        } catch (Exception e) {
            // 🚨 エラー時: ビューに戻るためにメタデータを再設定
            model.addAttribute("pageTitle", "Excel ⇄ CSV 相互変換ツール");
            model.addAttribute("metaDescription", "CSVファイルからExcel (xlsx) への変換、またはExcelからCSVへの変換をオンラインで行う無料ツール。開発時のデータ操作を効率化します。");
            model.addAttribute("canonicalUrl", "https://convertertools.jp/excel-tools");
            model.addAttribute("error", "CSV変換中に予期せぬエラーが発生しました。");
            model.addAttribute("content", "excel-tools");
            return "layout";
        }
    }
}