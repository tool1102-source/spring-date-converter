package com.example.dateconverter.controller;

import com.example.dateconverter.service.JsonCsvService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

@Controller
public class JsonCsvController {

    @Autowired
    private JsonCsvService jsonCsvService;

    @GetMapping("/json-csv")
    public String showPage(Model model) {
        model.addAttribute("pageTitle", "JSON ⇄ CSV 変換ツール");
        model.addAttribute("content", "json-csv");
        return "layout";
    }

    @PostMapping("/json-csv/convert")
    public String convert(
            @RequestParam("inputText") String inputText,
            @RequestParam("mode") String mode,
            Model model) {

        String result = "";
        String error = null;

        try {
            if ("jsonToCsv".equals(mode)) {
                result = jsonCsvService.jsonToCsv(inputText);
            } else {
                result = jsonCsvService.csvToJson(inputText);
            }
        } catch (Exception e) {
            error = "変換中にエラーが発生しました: " + e.getMessage();
        }

        model.addAttribute("pageTitle", "JSON ⇄ CSV 変換ツール");
        model.addAttribute("content", "json-csv");
        model.addAttribute("inputText", inputText);
        model.addAttribute("result", result);
        model.addAttribute("mode", mode);
        model.addAttribute("error", error);

        return "layout";
    }
}
