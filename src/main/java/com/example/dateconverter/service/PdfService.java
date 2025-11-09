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

    /**
     * テキストファイルを PDF に変換
     * @param text 入力テキスト
     * @return 生成された PDF ファイル
     * @throws Exception
     */
    public File textToPdf(String text) throws Exception {
        PDDocument document = new PDDocument();
        PDPage page = new PDPage();
        document.addPage(page);

        // 改行コードを正規化（CR+LF -> LF）
        String normalizedText = text.replace("\r", "");

        // フォント読み込み（プロジェクト内優先、なければ OS フォント）
        PDType0Font font;
        try (InputStream fontStream = getClass().getResourceAsStream("/fonts/NotoSansCJKjp-VF.ttf")) {
            if (fontStream != null) {
                font = PDType0Font.load(document, fontStream);
            } else {
                // Windows または Linux の既存フォントにフォールバック
                File fallbackFont = new File("C:/Windows/Fonts/msgothic.ttc");
                if (!fallbackFont.exists()) {
                    fallbackFont = new File("/usr/share/fonts/truetype/noto/NotoSansCJK-Regular.ttc");
                }
                font = PDType0Font.load(document, fallbackFont);
            }
        }

        // ページにテキストを書き込む
        try (PDPageContentStream contentStream = new PDPageContentStream(document, page)) {
            contentStream.beginText();
            contentStream.setFont(font, 12);
            contentStream.newLineAtOffset(50, 700);

            for (String line : normalizedText.split("\n")) {
                contentStream.showText(line);
                contentStream.newLineAtOffset(0, -15);
            }

            contentStream.endText();
        }

        // 一時ファイルとして PDF 保存
        File tempFile = Files.createTempFile("text-to-pdf-", ".pdf").toFile();
        try (FileOutputStream out = new FileOutputStream(tempFile)) {
            document.save(out);
        }

        document.close();
        return tempFile;
    }
}
