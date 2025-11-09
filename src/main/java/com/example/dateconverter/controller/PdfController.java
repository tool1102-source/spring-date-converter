package com.example.dateconverter.controller;

import com.example.dateconverter.service.PdfService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.nio.charset.StandardCharsets;

@Controller
public class PdfController {

    @Autowired
    private PdfService pdfService;

    @GetMapping("/text-to-pdf")
    public String showTextToPdfPage() {
        return "text-to-pdf";
    }

    @PostMapping("/text-to-pdf")
    public String convertTextToPdf(@RequestParam("textFile") MultipartFile textFile, Model model) {
        try {
            // アップロードされたテキストファイルを文字列として読み込み
            String text = new String(textFile.getBytes(), StandardCharsets.UTF_8);

            // サービスでPDFへ変換
            File pdfFile = pdfService.textToPdf(text);

            // ダウンロードリンクをモデルへセット
            model.addAttribute("downloadLink", "/download?file=" + pdfFile.getAbsolutePath());
            model.addAttribute("message", "PDF変換が完了しました！");

        } catch (Exception e) {
            e.printStackTrace();
            model.addAttribute("error", "変換中にエラーが発生しました: " + e.getMessage());
        }

        return "text-to-pdf";
    }
}
