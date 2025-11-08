package com.example.dateconverter.service;

import org.springframework.stereotype.Service;

@Service
public class StringToolsService {

    public String process(String inputText, String operation, String regex, String replacement) {
        switch (operation) {
            case "regex-replace":
                if (regex == null || replacement == null) {
                    throw new RuntimeException("正規表現と置換文字列を指定してください");
                }
                return inputText.replaceAll(regex, replacement);
            case "count":
                return String.valueOf(inputText.length());
            default:
                throw new RuntimeException("不明な操作です");
        }
    }
}
