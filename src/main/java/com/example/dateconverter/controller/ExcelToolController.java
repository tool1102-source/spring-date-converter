package com.example.dateconverter.controller;

import org.springframework.beans.factory.annotation.Autowired; // 🚨 追加
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

    // 🚨 修正: @Autowired を明示的に追加
    @Autowired 
    public ExcelToolController(ExcelToolService excelToolService) {
        this.excelToolService = excelToolService;
    }

    @GetMapping
    public String showExcelTools(Model model) {
        model.addAttribute("pageTitle", "Excelツール");
        model.addAttribute("content", "excel-tools");
        return "layout";
    }

    // CSV → Excel (ResponseEntityでファイルとして返す)
    @PostMapping("/csv-to-excel")
    public ResponseEntity<ByteArrayResource> csvToExcel(@RequestParam("csvFile") MultipartFile csvFile) {
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
            return ResponseEntity.badRequest().build();
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }

    // Excel → CSV (ResponseEntityでファイルとして返す)
    @PostMapping("/excel-to-csv")
    public ResponseEntity<ByteArrayResource> excelToCsv(@RequestParam("excelFile") MultipartFile excelFile) {
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
            return ResponseEntity.badRequest().build();
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }
}