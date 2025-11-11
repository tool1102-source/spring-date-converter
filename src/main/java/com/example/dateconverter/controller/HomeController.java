package com.example.dateconverter.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class HomeController {

    @GetMapping("/")
    public String showHome(Model model) {
        // 🚨 SEO修正: サイト全体を表すキーワードを強調
        model.addAttribute("pageTitle", "開発者向け多機能オンラインツール集 | convertertools.jp"); 
        
        // 🚨 新規追加: metaDescriptionを追加
        model.addAttribute("metaDescription", "日付/時刻変換、JSON/CSV相互変換、PDF/Excelファイル処理など、開発者の日常業務を効率化する無料の多機能オンラインコンバーターツール集。");
        
        // 🚨 独自ドメイン設定: Canonical URLを追加（トップページなのでルートパス）
        model.addAttribute("canonicalUrl", "https://convertertools.jp/"); 

        model.addAttribute("content", "index");
        return "layout";
    }
}