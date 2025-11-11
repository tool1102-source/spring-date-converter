package com.example.dateconverter.controller;

import com.example.dateconverter.service.JsonCsvService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

@Controller
public class JsonCsvController {

    @Autowired
    private JsonCsvService jsonCsvService;

    @GetMapping("/json-csv")
    public String showPage(Model model) {
        // 🚨 修正: ページタイトルとメタディスクリプションを最適化
        model.addAttribute("pageTitle", "JSON ⇄ CSV/TSV 相互変換ツール");
        model.addAttribute("metaDescription", "JSONデータとCSV/TSVデータを双方向に変換する無料オンラインツール。ネストされたJSON構造にも対応し、データ分析やAPI連携を効率化します。");
        model.addAttribute("canonicalUrl", "https://convertertools.jp/json-csv");
        model.addAttribute("content", "json-csv");
        return "layout";
    }

    @PostMapping("/json-csv/convert")
    public String convert(
            @RequestParam("inputText") String inputText,
            @RequestParam("mode") String mode,
            Model model) {

        String result = "";
        String error = null;

        try {
            if ("jsonToCsv".equals(mode)) {
                result = jsonCsvService.jsonToCsv(inputText);
            } else {
                result = jsonCsvService.csvToJson(inputText);
            }
        } catch (Exception e) {
            error = "変換中にエラーが発生しました: " + e.getMessage();
        }

        // 🚨 POST処理後もメタデータを再設定 (変更なし)
        model.addAttribute("pageTitle", "JSON ⇄ CSV/TSV 相互変換ツール");
        model.addAttribute("metaDescription", "JSONデータとCSV/TSVデータを双方向に変換する無料オンラインツール。ネストされたJSON構造にも対応し、データ分析やAPI連携を効率化します。");
        model.addAttribute("canonicalUrl", "https://convertertools.jp/json-csv");
        
        model.addAttribute("content", "json-csv");
        model.addAttribute("inputText", inputText);
        model.addAttribute("result", result);
        model.addAttribute("error", error);

        return "layout";
    }
}