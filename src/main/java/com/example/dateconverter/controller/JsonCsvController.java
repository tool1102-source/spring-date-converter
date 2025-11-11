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
        // 🚨 SEO修正: pageTitleにTSVも追加してキーワードを強化
        model.addAttribute("pageTitle", "JSON ⇄ CSV/TSV 相互変換ツール"); 
        
        // 🚨 新規追加: metaDescriptionを追加
        model.addAttribute("metaDescription", "JSON形式のデータをCSV形式、またはその逆の形式に変換する無料オンラインツール。データの整形や確認、開発時のデータ処理に最適です。");
        
        // 🚨 独自ドメイン設定: Canonical URLを設定
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

        // 🚨 POST処理後もメタデータを再設定
        model.addAttribute("pageTitle", "JSON ⇄ CSV/TSV 相互変換ツール");
        model.addAttribute("metaDescription", "JSON形式のデータをCSV形式、またはその逆の形式に変換する無料オンラインツール。データの整形や確認、開発時のデータ処理に最適です。");
        model.addAttribute("canonicalUrl", "https://convertertools.jp/json-csv");
        
        model.addAttribute("content", "json-csv");
        model.addAttribute("inputText", inputText);
        model.addAttribute("result", result);
        model.addAttribute("error", error);
        model.addAttribute("mode", mode);

        return "layout";
    }
}