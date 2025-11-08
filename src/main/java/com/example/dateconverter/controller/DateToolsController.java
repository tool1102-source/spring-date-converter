package com.example.dateconverter.controller;

import com.example.dateconverter.service.DateToolsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

@Controller
public class DateToolsController {

    @Autowired
    private DateToolsService dateToolsService;

    @GetMapping("/date-tools")
    public String showPage(Model model) {
        model.addAttribute("pageTitle", "日付操作ツール");
        model.addAttribute("content", "date-tools");
        return "layout";
    }

    @PostMapping("/date-tools/calc")
    public String calculate(
            @RequestParam(required = false) String date,
            @RequestParam(required = false) String startDate,
            @RequestParam(required = false) String endDate,
            @RequestParam String operation,
            Model model) {

        String result = "";
        String error = null;

        try {
            result = dateToolsService.calculate(date, startDate, endDate, operation);
        } catch (Exception e) {
            error = "処理に失敗しました: " + e.getMessage();
        }

        model.addAttribute("pageTitle", "日付操作ツール");
        model.addAttribute("content", "date-tools");
        model.addAttribute("date", date);
        model.addAttribute("startDate", startDate);
        model.addAttribute("endDate", endDate);
        model.addAttribute("operation", operation);
        model.addAttribute("result", result);
        model.addAttribute("error", error);

        return "layout";
    }
}
