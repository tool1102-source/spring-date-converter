package com.example.dateconverter.controller;

// 🚨 削除: com.example.dateconverter.service.JsonCsvService;
// 🚨 削除: org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

@Controller
public class JsonCsvController {

    // 🚨 削除: サービス関連のフィールドとAutowiredを削除
    // @Autowired
    // private JsonCsvService jsonCsvService;

    @GetMapping("/json-csv")
    public String showPage(Model model) {
        model.addAttribute("pageTitle", "JSON ⇄ CSV 変換ツール");
        model.addAttribute("metaDescription", "無料で使える高速なJSONとCSVの相互変換ツール。ネストされたJSONにも対応し、開発やデータ分析の効率を大幅に向上させます。");
        model.addAttribute("canonicalUrl", "https://convertertools.jp/json-csv");
        model.addAttribute("content", "json-csv");
        
        // 🚨 以前のPOST処理で追加されていた可能性のあるaddAttributeは全て削除されています

        return "layout";
    }

    // 🚨 以前の @PostMapping("/json-csv/convert") メソッドは全て削除してください。
}