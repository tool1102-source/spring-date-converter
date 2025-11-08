package com.example.dateconverter.service;

import org.springframework.stereotype.Service;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.net.URLEncoder;

@Service
public class TextToolsService {

    public String convert(String inputText, String operation) {
        switch (operation) {
            case "uppercase":
                return inputText.toUpperCase();
            case "lowercase":
                return inputText.toLowerCase();
            case "url-encode":
                try {
                    return URLEncoder.encode(inputText, "UTF-8");
                } catch (UnsupportedEncodingException e) {
                    throw new RuntimeException(e);
                }
            case "url-decode":
                try {
                    return URLDecoder.decode(inputText, "UTF-8");
                } catch (UnsupportedEncodingException e) {
                    throw new RuntimeException(e);
                }
            default:
                throw new RuntimeException("不明な操作です");
        }
    }
}
