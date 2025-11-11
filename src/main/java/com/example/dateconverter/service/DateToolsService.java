package com.example.dateconverter.service;

import org.springframework.stereotype.Service;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;

@Service
public class DateToolsService { // 🚨 クラス名を DateToolsService に変更

    private static final ZoneId JST_ZONE_ID = ZoneId.of("Asia/Tokyo");
    
    private DateTimeFormatter getStandardFormatter() {
        return DateTimeFormatter
                .ofPattern("yyyy/MM/dd HH:mm:ss")
                .withZone(JST_ZONE_ID);
    }

    /**
     * 日付文字列を指定された出力形式に変換します。
     */
    public String convertToCustomFormat(String inputDate, String inputFormat, String outputFormat) {
        if (inputDate == null || inputDate.trim().isEmpty()) {
            return "日付が入力されていません。";
        }
        if (inputFormat == null || inputFormat.trim().isEmpty()) {
            return "入力フォーマットが入力されていません。";
        }
        if (outputFormat == null || outputFormat.trim().isEmpty()) {
            return "出力フォーマットが入力されていません。";
        }

        try {
            // 入力形式のFormatterを作成
            DateTimeFormatter inputFormatter = DateTimeFormatter
                    .ofPattern(inputFormat)
                    .withZone(JST_ZONE_ID);
            
            // 出力形式のFormatterを作成
            DateTimeFormatter outputFormatter = DateTimeFormatter
                    .ofPattern(outputFormat)
                    .withZone(JST_ZONE_ID);

            // ZonedDateTimeとしてパースを試みる（Zone情報を含む可能性を考慮）
            ZonedDateTime zonedDateTime;
            
            try {
                // Zone情報を含まないパターンでパースを試み、JSTを付与
                LocalDateTime localDateTime = LocalDateTime.parse(inputDate, inputFormatter);
                zonedDateTime = localDateTime.atZone(JST_ZONE_ID);
            } catch (DateTimeParseException e) {
                // 失敗した場合、Zone情報を含むパターンとしてパースを試みる
                zonedDateTime = ZonedDateTime.parse(inputDate, inputFormatter);
            }
            
            // 指定された出力形式で出力
            return zonedDateTime.format(outputFormatter);

        } catch (DateTimeParseException e) {
            return "エラー: 入力日付またはフォーマットが不正です。";
        } catch (IllegalArgumentException e) {
            return "エラー: フォーマット文字列が不正です。";
        }
    }

    /**
     * Epochミリ秒を標準形式の日付文字列に変換します。
     */
    public String epochToStandardDate(String epochMilli) {
        if (epochMilli == null || epochMilli.trim().isEmpty()) {
            return "Epochミリ秒が入力されていません。";
        }

        try {
            long milli = Long.parseLong(epochMilli);
            Instant instant = Instant.ofEpochMilli(milli);
            
            return instant.atZone(JST_ZONE_ID).format(getStandardFormatter());

        } catch (NumberFormatException e) {
            return "エラー: Epochミリ秒は数値で入力してください。";
        }
    }

    /**
     * 標準形式の日付文字列をEpochミリ秒に変換します。
     */
    public String standardDateToEpoch(String standardDate) {
        if (standardDate == null || standardDate.trim().isEmpty()) {
            return "日付が入力されていません。";
        }

        try {
            // 標準形式でパース
            LocalDateTime localDateTime = LocalDateTime.parse(standardDate, getStandardFormatter());
            // JSTとしてEpochミリ秒を取得
            long epochMilli = localDateTime.atZone(JST_ZONE_ID).toInstant().toEpochMilli();
            
            return String.valueOf(epochMilli);

        } catch (DateTimeParseException e) {
            return "エラー: 日付が標準形式 (yyyy/MM/dd HH:mm:ss) になっていません。";
        }
    }
}