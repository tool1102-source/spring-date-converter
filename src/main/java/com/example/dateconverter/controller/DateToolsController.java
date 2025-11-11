package com.example.dateconverter.controller;

import com.example.dateconverter.service.DateToolsService; // 🚨 DateToolsService をインポート
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping; // 🚨 RequestMapping をインポート
import org.springframework.web.bind.annotation.RequestParam;

@Controller
@RequestMapping("/date-tools") // 🚨 クラス全体に /date-tools のマッピングを設定
public class DateToolsController { // 🚨 クラス名を DateToolsController に変更

    private final DateToolsService dateToolsService; // 🚨 サービス名を変更

    @Autowired 
    public DateToolsController(DateToolsService dateToolsService) { // 🚨 サービス名を変更
        this.dateToolsService = dateToolsService;
    }

    // 画面表示 (GET /date-tools)
    @GetMapping
    public String showDateTools(Model model) {
        model.addAttribute("pageTitle", "日付/時刻ツール");
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
        // 🚨 サービスメソッドを呼び出す
        String result = dateToolsService.convertToCustomFormat(inputDate, inputFormat, outputFormat);
        
        model.addAttribute("inputDate", inputDate);
        model.addAttribute("inputFormat", inputFormat);
        model.addAttribute("outputFormat", outputFormat);
        
        if (result.startsWith("エラー:") || result.endsWith("されていません。")) {
            model.addAttribute("error", result);
        } else {
            model.addAttribute("resultMessage", result);
        }
        
        model.addAttribute("pageTitle", "日付/時刻ツール");
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
        String result = dateToolsService.epochToStandardDate(epochMilli); // 🚨 サービスメソッドを呼び出す
        model.addAttribute("epochResultMessage", result); 
        model.addAttribute("pageTitle", "日付/時刻ツール");
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
        String result = dateToolsService.standardDateToEpoch(standardDate); // 🚨 サービスメソッドを呼び出す
        model.addAttribute("epochConvertResult", result); 
        model.addAttribute("pageTitle", "日付/時刻ツール");
        model.addAttribute("content", "date-tools");
        return "layout";
    }
}