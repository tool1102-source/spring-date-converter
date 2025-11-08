package com.example.dateconverter.controller;

import com.example.dateconverter.service.StringToolsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

@Controller
public class StringToolsController {

    @Autowired
    private StringToolsService stringToolsService;

    @GetMapping("/string-tools")
    public String showPage(Model model) {
        model.addAttribute("pageTitle", "文字列操作ツール");
        model.addAttribute("content", "string-tools");
        return "layout";
    }

    @PostMapping("/string-tools/process")
    public String process(
            @RequestParam("inputText") String inputText,
            @RequestParam("operation") String operation,
            @RequestParam(required = false) String regex,
            @RequestParam(required = false) String replacement,
            Model model) {

        String result = "";
        String error = null;

        try {
            result = stringToolsService.process(inputText, operation, regex, replacement);
        } catch (Exception e) {
            error = "処理に失敗しました: " + e.getMessage();
        }

        model.addAttribute("pageTitle", "文字列操作ツール");
        model.addAttribute("content", "string-tools");
        model.addAttribute("inputText", inputText);
        model.addAttribute("operation", operation);
        model.addAttribute("regex", regex);
        model.addAttribute("replacement", replacement);
        model.addAttribute("result", result);
        model.addAttribute("error", error);

        return "layout";
    }
}
