package com.example.dateconverter.service;

import org.springframework.stereotype.Service;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;

@Service
public class DateFormatService {

    public String convertDateFormat(String inputDate, String inputFormat, String outputFormat) {
        try {
            DateTimeFormatter inputFormatter = DateTimeFormatter.ofPattern(inputFormat);
            DateTimeFormatter outputFormatter = DateTimeFormatter.ofPattern(outputFormat);

            LocalDateTime dateTime = LocalDateTime.parse(inputDate, inputFormatter);
            return dateTime.format(outputFormatter);

        } catch (DateTimeParseException e) {
            throw new RuntimeException("入力またはフォーマットが不正です。例: yyyy-MM-dd HH:mm:ss");
        }
    }
}
