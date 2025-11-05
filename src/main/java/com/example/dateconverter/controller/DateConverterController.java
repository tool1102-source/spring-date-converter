package com.example.dateconverter.controller;

import com.example.dateconverter.service.DateConverterService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

@Controller
public class DateConverterController {

    private final DateConverterService service;

    public DateConverterController(DateConverterService service) {
        this.service = service;
    }

    @GetMapping("/date-converter")
    public String showPage() {
        return "date-converter";
    }

    @PostMapping("/date-converter")
    public String convert(
            @RequestParam String mode,
            @RequestParam String value,
            @RequestParam(defaultValue = "Asia/Tokyo") String tz,
            @RequestParam(defaultValue = "yyyy-MM-dd HH:mm:ss") String format,
            Model model
    ) {
        String result = service.convert(mode, value, tz, format);
        model.addAttribute("input", value);
        model.addAttribute("output", result);
        model.addAttribute("mode", mode);
        model.addAttribute("tz", tz);
        model.addAttribute("format", format);
        return "date-converter";
    }
}
