package com.example.dateconverter.controller;

import com.example.dateconverter.service.DateToolsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
@RequestMapping("/date-tools")
public class DateToolsController {

    private final DateToolsService dateToolsService;

    @Autowired 
    public DateToolsController(DateToolsService dateToolsService) {
        this.dateToolsService = dateToolsService;
    }

    // 画面表示 (GET /date-tools)
    @GetMapping
    public String showDateTools(Model model) {
        // 🚨 SEO修正: pageTitleを最適化
        model.addAttribute("pageTitle", "日付/時刻・Epoch Time 相互変換ツール");
        
        // 🚨 新規追加: metaDescriptionを追加
        model.addAttribute("metaDescription", "日付フォーマット（yyyy/MM/dd HH:mm:ss）の相互変換、Epochミリ秒と標準日付の双方向変換を瞬時に行える無料オンラインツール。開発者・データ分析に最適です。");
        
        // 🚨 独自ドメイン設定: Canonical URLを追加
        model.addAttribute("canonicalUrl", "https://convertertools.jp/date-tools"); 

        model.addAttribute("content", "date-tools");
        return "layout";
    }

    // 日付文字列の相互変換 (POST /date-tools/convert-date)
    @PostMapping("/convert-date")
    public String convertDate(
            @RequestParam("inputDate") String inputDate,
            @RequestParam("inputFormat") String inputFormat,
            @RequestParam("outputFormat") String outputFormat,
            Model model
    ) {
        String result = dateToolsService.convertToCustomFormat(inputDate, inputFormat, outputFormat);

        model.addAttribute("inputDate", inputDate);
        model.addAttribute("inputFormat", inputFormat);
        model.addAttribute("outputFormat", outputFormat);
        
        if (result.startsWith("エラー:") || result.endsWith("されていません。")) {
            model.addAttribute("error", result);
        } else {
            model.addAttribute("resultMessage", result);
        }
        
        // 🚨 POST処理後もメタデータを再設定
        model.addAttribute("pageTitle", "日付/時刻・Epoch Time 相互変換ツール");
        model.addAttribute("metaDescription", "日付フォーマット（yyyy/MM/dd HH:mm:ss）の相互変換、Epochミリ秒と標準日付の双方向変換を瞬時に行える無料オンラインツール。開発者・データ分析に最適です。");
        model.addAttribute("canonicalUrl", "https://convertertools.jp/date-tools"); 
        
        model.addAttribute("content", "date-tools");
        return "layout";
    }

    /**
     * Epochミリ秒 → 日付文字列 変換 (POST /date-tools/epoch-to-date)
     */
    @PostMapping("/epoch-to-date")
    public String epochToDate(
            @RequestParam("epochMilli") String epochMilli,
            Model model
    ) {
        String result = dateToolsService.epochToStandardDate(epochMilli);
        model.addAttribute("epochResultMessage", result); 
        
        // 🚨 POST処理後もメタデータを再設定
        model.addAttribute("pageTitle", "日付/時刻・Epoch Time 相互変換ツール");
        model.addAttribute("metaDescription", "日付フォーマット（yyyy/MM/dd HH:mm:ss）の相互変換、Epochミリ秒と標準日付の双方向変換を瞬時に行える無料オンラインツール。開発者・データ分析に最適です。");
        model.addAttribute("canonicalUrl", "https://convertertools.jp/date-tools"); 
        
        model.addAttribute("content", "date-tools");
        return "layout";
    }

    /**
     * 日付文字列 → Epochミリ秒 変換 (POST /date-tools/date-to-epoch)
     */
    @PostMapping("/date-to-epoch")
    public String dateToEpoch(
            @RequestParam("standardDate") String standardDate,
            Model model
    ) {
        String result = dateToolsService.standardDateToEpoch(standardDate);
        model.addAttribute("epochResultMessage", result); // 結果メッセージは同じフィールドを使用
        
        // 🚨 POST処理後もメタデータを再設定
        model.addAttribute("pageTitle", "日付/時刻・Epoch Time 相互変換ツール");
        model.addAttribute("metaDescription", "日付フォーマット（yyyy/MM/dd HH:mm:ss）の相互変換、Epochミリ秒と標準日付の双方向変換を瞬時に行える無料オンラインツール。開発者・データ分析に最適です。");
        model.addAttribute("canonicalUrl", "https://convertertools.jp/date-tools"); 
        
        model.addAttribute("content", "date-tools");
        return "layout";
    }
}