package com.example.dateconverter.controller;

// 🚨 不要になったDateToolsService, PostMapping, RequestParamのインポートを削除
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/date-tools")
public class DateToolsController { 

    // 🚨 サービスが不要になったため、コンストラクタとAutowiredを削除
    // private final DateToolsService dateToolsService;
    // @Autowired 
    // public DateToolsController(DateToolsService dateToolsService) {
    //     this.dateToolsService = dateToolsService;
    // }

    // 画面表示 (GET /date-tools) のみ残す
    @GetMapping
    public String showDateTools(Model model) {
        // 以前の会話で修正されたSEOメタデータを適用
        model.addAttribute("pageTitle", "日付/時刻・Epoch Time 相互変換ツール");
        model.addAttribute("metaDescription", "日付フォーマット（yyyy/MM/dd HH:mm:ss）の相互変換、Epochミリ秒と標準日付の双方向変換を瞬時に行える無料オンラインツール。開発者・データ分析に最適です。");
        model.addAttribute("canonicalUrl", "https://convertertools.jp/date-tools"); 
        model.addAttribute("content", "date-tools");
        return "layout";
    }

    // 🚨 以前のconvertDate, epochToDate, dateToEpoch の各POSTメソッドは全て削除してください。
}