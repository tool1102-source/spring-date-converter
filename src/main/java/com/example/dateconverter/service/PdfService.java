package com.example.dateconverter.service;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.font.PDType0Font;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.nio.file.Files;

@Service
public class PdfService {

    public File textToPdf(String text) throws Exception {
        PDDocument document = new PDDocument();
        PDPage page = new PDPage();
        document.addPage(page);

        // フォント読み込み（クラウド・ローカル対応）
        PDType0Font font;
        try (InputStream fontStream = getClass().getResourceAsStream("/fonts/NotoSansCJKjp-VF.ttf")) {
            if (fontStream != null) {
                font = PDType0Font.load(document, fontStream);
            } else {
                // ローカル・OSフォントにフォールバック
                File fontFile = new File("C:/Windows/Fonts/msgothic.ttc");
                font = PDType0Font.load(document, fontFile);
            }
        }

        PDPageContentStream contentStream = new PDPageContentStream(document, page);
        contentStream.beginText();
        contentStream.setFont(font, 12);
        contentStream.newLineAtOffset(50, 700);

        for (String line : text.split("\n")) {
            contentStream.showText(line);
            contentStream.newLineAtOffset(0, -15);
        }

        contentStream.endText();
        contentStream.close();

        // 一時ファイルとしてPDF保存
        File tempFile = Files.createTempFile("text-to-pdf-", ".pdf").toFile();
        try (FileOutputStream out = new FileOutputStream(tempFile)) {
            document.save(out);
        }
        document.close();


        return tempFile;
    }
}
