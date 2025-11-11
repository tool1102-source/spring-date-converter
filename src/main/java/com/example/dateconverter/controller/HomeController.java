package com.example.dateconverter.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class HomeController {

    @GetMapping("/")
    public String showHome(Model model) {
        // 🚨 修正: ページタイトルとメタディスクリプションを最適化
        model.addAttribute("pageTitle", "開発者向け無料データ変換ツール集");
        model.addAttribute("metaDescription", "日付/Epoch、JSON/CSV、Excel/PDFなどの多様なデータ形式を相互変換する無料オンラインツール集。開発効率を大幅に向上させます。");
        model.addAttribute("canonicalUrl", "https://convertertools.jp/");
        model.addAttribute("content", "index");
        return "layout";
    }
}