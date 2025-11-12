package com.example.dateconverter.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

@Controller
public class HomeController {

    @GetMapping("/")
    public String showHomePage(Model model) {
        model.addAttribute("pageTitle", "開発者向け変換ツール集 | JSON, CSV, Epoch, PDF, 文字列操作");
        model.addAttribute("metaDescription", "開発やデータ分析に必要な各種変換・操作ツールを無料で提供。JSON-CSV相互変換、Epoch Time計算、PDF処理、文字列操作など、高速かつ安全なオンラインユーティリティです。");
        model.addAttribute("canonicalUrl", "https://convertertools.jp/");
        model.addAttribute("content", "index");

        // 🚨 主要ツールリストをモデルに追加
        List<Map<String, String>> tools = Arrays.asList(
            Map.of("name", "日付/Epoch 変換", "url", "/date-tools", "description", "Epochミリ秒と標準時刻の相互変換を瞬時に行えます。", "icon", "📅"),
            Map.of("name", "JSON ⇄ CSV 変換", "url", "/json-csv", "description", "JSONとCSV/TSVを双方向変換。サーバー負荷ゼロで高速動作。", "icon", "📊"),
            Map.of("name", "文字列操作", "url", "/string-tools", "description", "Base64、URLエンコード/デコード、大文字/小文字変換など。", "icon", "⚙️"),
            Map.of("name", "テキスト変換", "url", "/text-tools", "description", "全角/半角、ひらがな/カタカナ、HTMLエスケープ変換。", "icon", "🔠"),
            Map.of("name", "Excel/CSV 処理", "url", "/excel-tools", "description", "ExcelファイルやCSVの結合・分割・形式変換を行います。", "icon", "📑"),
            Map.of("name", "PDF 処理", "url", "/pdf-tools", "description", "PDFファイルの結合、分割、パスワード解除など。", "icon", "📄")
        );
        model.addAttribute("tools", tools);

        return "layout";
    }
}