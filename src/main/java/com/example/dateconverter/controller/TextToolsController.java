package com.example.dateconverter.controller;

import com.example.dateconverter.service.TextToolsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

@Controller
public class TextToolsController {

    @Autowired
    private TextToolsService textToolsService;

    @GetMapping("/text-tools")
    public String showPage(Model model) {
        // 🚨 修正: ページタイトルとメタディスクリプションを最適化
        model.addAttribute("pageTitle", "テキスト・表記形式変換ツール（全角/半角、大文字/小文字）");
        model.addAttribute("metaDescription", "全角/半角カタカナ、大文字/小文字、HTMLエスケープなど、テキストの表記揺れを一括で修正・変換する無料オンラインツール。");
        model.addAttribute("canonicalUrl", "https://convertertools.jp/text-tools");
        model.addAttribute("content", "text-tools");
        return "layout";
    }

    @PostMapping("/text-tools/convert")
    public String convert(
            @RequestParam("inputText") String inputText,
            @RequestParam("operation") String operation,
            Model model) {

        String result = "";
        String error = null;

        try {
            result = textToolsService.convert(inputText, operation);
        } catch (Exception e) {
            error = "変換に失敗しました: " + e.getMessage();
        }

        // 🚨 POST処理後もメタデータを再設定 (変更なし)
        model.addAttribute("pageTitle", "テキスト・表記形式変換ツール（全角/半角、大文字/小文字）");
        model.addAttribute("metaDescription", "全角/半角カタカナ、大文字/小文字、HTMLエスケープなど、テキストの表記揺れを一括で修正・変換する無料オンラインツール。");
        model.addAttribute("canonicalUrl", "https://convertertools.jp/text-tools");
        
        model.addAttribute("content", "text-tools");
        model.addAttribute("inputText", inputText);
        model.addAttribute("operation", operation);
        model.addAttribute("result", result);
        model.addAttribute("error", error);

        return "layout";
    }
}