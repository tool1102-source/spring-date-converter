package com.example.dateconverter.service;

import org.springframework.stereotype.Service;
import org.json.JSONArray;
import org.json.JSONObject;

import java.util.*;

@Service
public class JsonCsvService {

    // JSON → CSV
    public String jsonToCsv(String jsonString) {
        JSONArray jsonArray = new JSONArray(jsonString);

        if (jsonArray.isEmpty()) {
            return "";
        }

        JSONObject first = jsonArray.getJSONObject(0);
        Set<String> keys = first.keySet();

        StringBuilder sb = new StringBuilder();
        // ヘッダー
        sb.append(String.join(",", keys)).append("\n");

        // 各行
        for (int i = 0; i < jsonArray.length(); i++) {
            JSONObject obj = jsonArray.getJSONObject(i);
            List<String> row = new ArrayList<>();
            for (String key : keys) {
                row.add(escapeCsv(obj.optString(key, "")));
            }
            sb.append(String.join(",", row)).append("\n");
        }

        return sb.toString();
    }

    // CSV → JSON
    public String csvToJson(String csvString) {
        String[] lines = csvString.split("\\r?\\n");
        if (lines.length < 2) {
            return "[]";
        }

        String[] headers = lines[0].split(",");
        JSONArray jsonArray = new JSONArray();

        for (int i = 1; i < lines.length; i++) {
            if (lines[i].trim().isEmpty()) continue;
            String[] values = lines[i].split(",");
            JSONObject obj = new JSONObject();
            for (int j = 0; j < headers.length; j++) {
                obj.put(headers[j], j < values.length ? values[j] : "");
            }
            jsonArray.put(obj);
        }

        return jsonArray.toString(2); // 整形出力
    }

    private String escapeCsv(String value) {
        if (value.contains(",") || value.contains("\"")) {
            value = "\"" + value.replace("\"", "\"\"") + "\"";
        }
        return value;
    }
}
