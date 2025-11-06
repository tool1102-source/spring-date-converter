package com.example.dateconverter;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

@Controller
public class DateConverterController {

    @PostMapping("/convert")
    public String convertDate(
            @RequestParam String inputDate,
            @RequestParam String outputFormat,
            Model model) {

        try {
            // まず一般的な日付形式でパースを試みる
            String[] patterns = {
                "yyyy/MM/dd HH:mm",
                "yyyy-MM-dd HH:mm",
                "yyyy/MM/dd",
                "yyyy-MM-dd"
            };

            Date parsed = null;
            for (String pattern : patterns) {
                try {
                    parsed = new SimpleDateFormat(pattern).parse(inputDate);
                    break;
                } catch (ParseException ignored) {}
            }

            if (parsed == null) {
                model.addAttribute("error", "日付形式を認識できませんでした。");
                return "index";
            }

            // 指定フォーマットで整形
            String formatted = new SimpleDateFormat(outputFormat).format(parsed);
            model.addAttribute("result", formatted);

        } catch (Exception e) {
            model.addAttribute("error", "変換中にエラーが発生しました: " + e.getMessage());
        }

        return "index";
    }
}
