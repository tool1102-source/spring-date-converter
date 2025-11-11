package com.example.dateconverter.controller;

import com.example.dateconverter.service.PdfToolService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

@Controller
public class PdfToolController {

    @Autowired
    private PdfToolService pdfToolService;

    @GetMapping("/pdf-tools")
    public String showPdfTools(Model model) {
        model.addAttribute("pageTitle", "PDFツール");
        model.addAttribute("content", "pdf-tools");
        return "layout";
    }

    /** テキスト → PDF (修正: 戻り値をObjectにし、エラー時にビューに戻る) */
    @PostMapping("/text-to-pdf")
    public Object textToPdf(@RequestParam("textFile") MultipartFile textFile, Model model) {
        
        try {
            byte[] pdfBytes = pdfToolService.convertTextToPdf(textFile);

            String originalFilename = textFile.getOriginalFilename();
            String filename = (originalFilename != null && !originalFilename.isEmpty()) ? 
                              originalFilename.replaceAll("\\.txt$", "") + ".pdf" : "converted.pdf";

            HttpHeaders header = new HttpHeaders();
            header.add(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + filename + "\"");
            
            ByteArrayResource resource = new ByteArrayResource(pdfBytes);
            
            // 正常終了時は ResponseEntity (ファイルダウンロード) を返す
            return ResponseEntity.ok()
                    .headers(header)
                    .contentLength(pdfBytes.length)
                    .contentType(MediaType.APPLICATION_PDF)
                    .body(resource);

        } catch (IllegalArgumentException e) {
            // 🚨 ファイル未選択/ファイル空などのエラー時
            model.addAttribute("pageTitle", "PDFツール");
            model.addAttribute("content", "pdf-tools");
            model.addAttribute("error", e.getMessage()); // Serviceから受け取った具体的なメッセージを表示
            return "layout"; 
        } catch (Exception e) {
            // その他の変換エラー
            model.addAttribute("pageTitle", "PDFツール");
            model.addAttribute("content", "pdf-tools");
            model.addAttribute("error", "PDF変換中に予期せぬエラーが発生しました。");
            return "layout";
        }
    }

    /** PDF → テキスト (テキスト結果をビューに戻す) */
    @PostMapping("/pdf-to-text")
    public String pdfToText(@RequestParam("pdfFile") MultipartFile pdfFile, Model model) {
        model.addAttribute("pageTitle", "PDFツール");
        model.addAttribute("content", "pdf-tools");

        try {
            String text = pdfToolService.convertPdfToText(pdfFile); 
            model.addAttribute("message", text);
        } catch (IllegalArgumentException e) {
            model.addAttribute("error", e.getMessage());
        } catch (IOException e) {
            model.addAttribute("error", "PDFからのテキスト抽出中にエラーが発生しました。");
        }

        return "layout";
    }
}