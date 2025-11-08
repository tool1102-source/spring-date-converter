package com.example.dateconverter.controller;

import com.example.dateconverter.service.DateFormatService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

@Controller
public class DateFormatController {

    @Autowired
    private DateFormatService dateFormatService;

    @GetMapping("/date-format")
    public String showPage(Model model) {
        model.addAttribute("pageTitle", "日付フォーマット変換ツール");
        model.addAttribute("content", "date-format");
        return "layout";
    }

    @PostMapping("/date-format/convert")
    public String convert(
            @RequestParam("inputDate") String inputDate,
            @RequestParam("inputFormat") String inputFormat,
            @RequestParam("outputFormat") String outputFormat,
            Model model) {

        String result = "";
        String error = null;

        try {
            result = dateFormatService.convertDateFormat(inputDate, inputFormat, outputFormat);
        } catch (Exception e) {
            error = "変換に失敗しました: " + e.getMessage();
        }

        model.addAttribute("pageTitle", "日付フォーマット変換ツール");
        model.addAttribute("content", "date-format");
        model.addAttribute("inputDate", inputDate);
        model.addAttribute("inputFormat", inputFormat);
        model.addAttribute("outputFormat", outputFormat);
        model.addAttribute("result", result);
        model.addAttribute("error", error);

        return "layout";
    }
}
