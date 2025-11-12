package com.example.dateconverter.controller;

// 🚨 削除: StringToolsService 関連のインポート
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/string-tools")
public class StringToolsController {

    // 🚨 削除: サービス関連のフィールドとAutowired
    // @Autowired
    // private StringToolsService stringToolsService;

    @GetMapping
    public String showStringTools(Model model) {
        model.addAttribute("pageTitle", "文字列操作・変換ユーティリティ");
        model.addAttribute("metaDescription", "正規表現、Base64、URLエンコード/デコード、ハッシュ生成など、開発者向けの高度な文字列操作をブラウザ側で実行できる高速ツールです。");
        model.addAttribute("canonicalUrl", "https://convertertools.jp/string-tools");
        model.addAttribute("content", "string-tools");
        
        return "layout";
    }

    // 🚨 以前の @PostMapping で実行されていたメソッドは全て削除してください。
}