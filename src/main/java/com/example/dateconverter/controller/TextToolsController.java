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
        // 🚨 SEO修正: pageTitleを最適化
        model.addAttribute("pageTitle", "テキスト・エンコード/デコード変換ツール");
        
        // 🚨 新規追加: metaDescriptionを追加
        model.addAttribute("metaDescription", "Base64、URLエンコード/デコード、大文字・小文字、全角・半角変換など、様々な文字列のエンコーディングと変換を一括で行える無料オンラインツール。");
        
        // 🚨 独自ドメイン設定: Canonical URLを設定
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

        // 🚨 POST処理後もメタデータを再設定
        model.addAttribute("pageTitle", "テキスト・エンコード/デコード変換ツール");
        model.addAttribute("metaDescription", "Base64、URLエンコード/デコード、大文字・小文字、全角・半角変換など、様々な文字列のエンコーディングと変換を一括で行える無料オンラインツール。");
        model.addAttribute("canonicalUrl", "https://convertertools.jp/text-tools");
        
        model.addAttribute("content", "text-tools");
        model.addAttribute("inputText", inputText);
        model.addAttribute("operation", operation);
        model.addAttribute("result", result);
        model.addAttribute("error", error);

        return "layout";
    }
}