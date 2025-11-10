package com.example.dateconverter.controller;

import com.example.dateconverter.service.PdfToolService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;

@Controller
public class PdfToolController {

    @Autowired
    private PdfToolService pdfToolService;

    /** ✅ 画面表示（GET） */
    @GetMapping("/pdf-tools")
    public String showPdfTools(Model model) {
        model.addAttribute("pageTitle", "PDFツール");
        model.addAttribute("content", "pdf-tools");
        return "layout";
    }

    /** ✅ テキスト → PDF */
    @PostMapping("/text-to-pdf")
    public String textToPdf(@RequestParam("textFile") MultipartFile textFile, Model model) {
        model.addAttribute("pageTitle", "PDFツール");
        model.addAttribute("content", "pdf-tools");

        try {
            File pdfFile = pdfToolService.convertTextToPdf(textFile);

            // ✅ 修正：File オブジェクトではなくファイル名のみ渡す
            String downloadFileName = pdfFile.getName(); 
            model.addAttribute("message", "PDFに変換しました！");
            model.addAttribute("downloadLink", "/download/" + downloadFileName);

        } catch (IOException e) {
            model.addAttribute("error", "変換中にエラーが発生しました: " + e.getMessage());
        }

        return "layout";
    }

    /** ✅ PDF → テキスト */
    @PostMapping("/pdf-to-text")
    public String pdfToText(@RequestParam("pdfFile") MultipartFile pdfFile, Model model) {
        model.addAttribute("pageTitle", "PDFツール");
        model.addAttribute("content", "pdf-tools");

        try {
            String text = pdfToolService.convertPdfToText(pdfFile);
            model.addAttribute("message", text);
        } catch (IOException e) {
            model.addAttribute("error", "変換中にエラーが発生しました: " + e.getMessage());
        }

        return "layout";
    }
}
