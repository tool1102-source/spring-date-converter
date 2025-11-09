package com.example.dateconverter.service;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.font.PDType0Font;
import org.apache.pdfbox.pdmodel.font.PDType1Font;
import org.springframework.stereotype.Service;

import java.io.*;

@Service
public class PdfService {

    public File textToPdf(String text) throws IOException {
        File pdfFile = File.createTempFile("converted_", ".pdf");

        try (PDDocument doc = new PDDocument()) {
            PDPage page = new PDPage();
            doc.addPage(page);

            PDType0Font font = null;

            // フォントを読み込み（なければ Helvetica を使用）
            InputStream fontStream = PdfService.class.getResourceAsStream("/fonts/NotoSansCJKjp-Regular.otf");
            if (fontStream != null) {
                font = PDType0Font.load(doc, fontStream);
            } else {
                font = PDType0Font.load(doc, new File("C:/Windows/Fonts/msgothic.ttc")); // Windows用フォールバック
            }

            try (PDPageContentStream contentStream = new PDPageContentStream(doc, page)) {
                contentStream.beginText();
                contentStream.setFont(font != null ? font : PDType1Font.HELVETICA, 12);
                contentStream.newLineAtOffset(50, 750);

                for (String line : text.split("\n")) {
                    contentStream.showText(line);
                    contentStream.newLineAtOffset(0, -15);
                }

                contentStream.endText();
            }

            doc.save(pdfFile);
        }

        return pdfFile;
    }
}
