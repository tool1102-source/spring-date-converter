package com.example.dateconverter.service;

import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;

@Service
public class DateToolsService {

    public String calculate(String date, String startDate, String endDate, String operation) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");

        switch (operation) {
            case "weekday":
                LocalDate d = LocalDate.parse(date, formatter);
                return d.getDayOfWeek().toString(); // 曜日取得
            case "period":
                LocalDate start = LocalDate.parse(startDate, formatter);
                LocalDate end = LocalDate.parse(endDate, formatter);
                long days = ChronoUnit.DAYS.between(start, end);
                return days + "日間";
            default:
                throw new RuntimeException("不明な操作です");
        }
    }
}
