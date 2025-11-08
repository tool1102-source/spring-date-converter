package com.example.dateconverter.controller;

import com.example.dateconverter.service.TextToolsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

@Controller
public class TextToolsController {

    @Autowired
    private TextToolsService textToolsService;

    @GetMapping("/text-tools")
    public String showPage(Model model) {
        model.addAttribute("pageTitle", "文字列変換ツール");
        model.addAttribute("content", "text-tools");
        return "layout";
    }

    @PostMapping("/text-tools/convert")
    public String convert(
            @RequestParam("inputText") String inputText,
            @RequestParam("operation") String operation,
            Model model) {

        String result = "";
        String error = null;

        try {
            result = textToolsService.convert(inputText, operation);
        } catch (Exception e) {
            error = "変換に失敗しました: " + e.getMessage();
        }

        model.addAttribute("pageTitle", "文字列変換ツール");
        model.addAttribute("content", "text-tools");
        model.addAttribute("inputText", inputText);
        model.addAttribute("operation", operation);
        model.addAttribute("result", result);
        model.addAttribute("error", error);

        return "layout";
    }
}
