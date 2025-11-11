package com.example.dateconverter.controller;

import com.example.dateconverter.service.StringToolsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

@Controller
public class StringToolsController {

    @Autowired
    private StringToolsService stringToolsService;

    @GetMapping("/string-tools")
    public String showPage(Model model) {
        // 🚨 修正: ページタイトルとメタディスクリプションを最適化
        model.addAttribute("pageTitle", "文字列操作・整形ツール（正規表現・エンコード/デコード）");
        model.addAttribute("metaDescription", "正規表現による検索/置換、Base64、URLエンコード/デコード、ハッシュ生成など、開発者が必要とする高度な文字列操作をオンラインで提供します。");
        model.addAttribute("canonicalUrl", "https://convertertools.jp/string-tools");
        model.addAttribute("content", "string-tools");
        return "layout";
    }

    @PostMapping("/string-tools/process")
    public String process(
            @RequestParam("inputText") String inputText,
            @RequestParam("operation") String operation,
            @RequestParam(required = false) String regex,
            @RequestParam(required = false) String replacement,
            Model model) {

        String result = "";
        String error = null;

        try {
            result = stringToolsService.process(inputText, operation, regex, replacement);
        } catch (Exception e) {
            error = "処理に失敗しました: " + e.getMessage();
        }

        // 🚨 POST処理後もメタデータを再設定 (変更なし)
        model.addAttribute("pageTitle", "文字列操作・整形ツール（正規表現・エンコード/デコード）");
        model.addAttribute("metaDescription", "正規表現による検索/置換、Base64、URLエンコード/デコード、ハッシュ生成など、開発者が必要とする高度な文字列操作をオンラインで提供します。");
        model.addAttribute("canonicalUrl", "https://convertertools.jp/string-tools");

        model.addAttribute("content", "string-tools");
        model.addAttribute("inputText", inputText);
        model.addAttribute("operation", operation);
        model.addAttribute("regex", regex);
        model.addAttribute("replacement", replacement);
        model.addAttribute("result", result);
        model.addAttribute("error", error);

        return "layout";
    }
}