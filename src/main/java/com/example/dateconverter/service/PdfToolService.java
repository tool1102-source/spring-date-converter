package com.example.dateconverter.service;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.common.PDRectangle;
import org.apache.pdfbox.pdmodel.font.PDType0Font;
import org.apache.pdfbox.text.PDFTextStripper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;

@Service
public class PdfToolService {

    @Value("classpath:/fonts/NotoSansCJKjp-VF.ttf")
    private Resource japaneseFontResource;

    /**
     * PDF → テキスト変換 (メモリ処理)
     */
    public String convertPdfToText(MultipartFile pdfFile) throws IOException {
        if (pdfFile.isEmpty()) {
            throw new IllegalArgumentException("PDFファイルが選択されていません。");
        }
        
        try (PDDocument document = PDDocument.load(pdfFile.getInputStream())) {
            PDFTextStripper stripper = new PDFTextStripper();
            return stripper.getText(document);
        }
    }

    /**
     * テキスト → PDF変換 (日本語フォント対応とメモリ処理)
     * @return PDFのバイト配列
     */
    public byte[] convertTextToPdf(MultipartFile textFile) throws IOException {
        
        if (textFile.isEmpty()) {
            throw new IllegalArgumentException("テキストファイルが選択されていません。");
        }
        if (textFile.getSize() == 0) {
            throw new IllegalArgumentException("アップロードされたファイルの内容が空です。");
        }
        
        String text = new String(textFile.getBytes(), StandardCharsets.UTF_8);
        
        // 🚨 修正: CR (U+000D) 文字を全て除去し、改行コードをLF (\n) のみに統一
        String cleanText = text.replaceAll("\r", "");
        
        try (PDDocument document = new PDDocument();
             ByteArrayOutputStream bos = new ByteArrayOutputStream()) { 
            
            PDType0Font font = PDType0Font.load(document, japaneseFontResource.getInputStream());

            PDPage page = new PDPage(PDRectangle.A4);
            document.addPage(page);

            try (PDPageContentStream cs = new PDPageContentStream(document, page)) {
                
                cs.setFont(font, 12); 
                cs.beginText();
                
                float margin = 50;
                float y = 750;
                float leading = 14.5f;

                cs.newLineAtOffset(margin, y);

                // 修正後の cleanText を使用して、LF (\n) で分割
                for (String line : cleanText.split("\n")) {
                    
                    if (y < margin) {
                        cs.endText();
                        cs.close();
                        page = new PDPage(PDRectangle.A4);
                        document.addPage(page);
                        
                        // 新しい ContentStream を開く
                        try (PDPageContentStream newCs = new PDPageContentStream(document, page)) {
                            newCs.setFont(font, 12);
                            newCs.beginText();
                            y = 750;
                            newCs.newLineAtOffset(margin, y);
                            // 新しい ContentStream で処理を続行するため、現在の try ブロックを抜ける
                            break; 
                        }
                    }
                    
                    // 行の描画
                    cs.showText(line);
                    cs.newLineAtOffset(0, -leading);
                    y -= leading;
                }
                
                cs.endText();
            }

            document.save(bos); 
            return bos.toByteArray();

        } catch (IllegalArgumentException e) {
             throw e;
        } catch (Exception e) {
             throw new IOException("PDF変換処理中に予期せぬエラーが発生しました: " + e.getMessage(), e);
        }
    }
}