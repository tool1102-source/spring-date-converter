package com.example.dateconverter.service;

import org.springframework.stereotype.Service;
import java.time.*;
import java.time.format.DateTimeFormatter;

@Service
public class DateConverterService {

    public String convert(String mode, String value, String tz, String format) {
        try {
            ZoneId zone = ZoneId.of(tz);
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern(format).withZone(zone);

            if ("unix2date".equalsIgnoreCase(mode)) {
                long epoch = Long.parseLong(value);
                Instant instant = Instant.ofEpochSecond(epoch);
                return formatter.format(instant);
            } else if ("date2unix".equalsIgnoreCase(mode)) {
                LocalDateTime dateTime = LocalDateTime.parse(value, formatter);
                return String.valueOf(dateTime.atZone(zone).toEpochSecond());
            }
        } catch (Exception e) {
            return "変換エラー: " + e.getMessage();
        }
        return "不明なモード";
    }
}
