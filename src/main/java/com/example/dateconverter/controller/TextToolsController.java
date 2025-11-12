package com.example.dateconverter.controller;

// 🚨 削除: TextToolsService 関連のインポート
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/text-tools")
public class TextToolsController {

    // 🚨 削除: サービス関連のフィールドとAutowired
    // @Autowired
    // private TextToolsService textToolsService;

    @GetMapping
    public String showTextTools(Model model) {
        model.addAttribute("pageTitle", "テキスト表記変換ツール (全角・半角・かな・大文字)");
        model.addAttribute("metaDescription", "全角/半角、ひらがな/カタカナ、大文字/小文字、HTMLエスケープなど、テキストの表記形式を瞬時に変換します。サーバー負荷ゼロで動作する高速ツールです。");
        model.addAttribute("canonicalUrl", "https://convertertools.jp/text-tools");
        model.addAttribute("content", "text-tools");
        
        return "layout";
    }

    // 🚨 以前の @PostMapping で実行されていたメソッドは全て削除してください。
}