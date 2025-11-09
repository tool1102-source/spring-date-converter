package com.example.dateconverter.service;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.font.PDType0Font;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;

@Service
public class PdfService {

    private final Path uploadDir = Path.of("uploads");
    private final Path fontPath = Path.of("fonts/NotoSansCJKjp-Regular.ttf");

    public PdfService() throws IOException {
        if (!Files.exists(uploadDir)) Files.createDirectories(uploadDir);
    }

    // テキストファイル -> PDF
    public String textToPdf(MultipartFile textFile) throws Exception {
        String pdfFileName = "converted_" + System.currentTimeMillis() + ".pdf";
        Path pdfPath = uploadDir.resolve(pdfFileName);

        String content = new String(textFile.getBytes());

        try (PDDocument document = new PDDocument()) {
            PDPage page = new PDPage();
            document.addPage(page);

            PDType0Font font = PDType0Font.load(document, fontPath.toFile());
            try (PDPageContentStream contentStream = new PDPageContentStream(document, page)) {
                contentStream.beginText();
                contentStream.setFont(font, 12);
                contentStream.newLineAtOffset(50, 750);

                for (String line : content.split("\n")) {
                    contentStream.showText(line);
                    contentStream.newLineAtOffset(0, -15);
                }

                contentStream.endText();
            }

            document.save(pdfPath.toFile());
        }
        return "/uploads/" + pdfFileName;
    }

    // PDF -> テキスト
    public String pdfToText(MultipartFile pdfFile) throws Exception {
        try (PDDocument document = PDDocument.load(pdfFile.getInputStream())) {
            StringBuilder sb = new StringBuilder();
            document.getPages().forEach(page -> {
                try {
                    sb.append(new org.apache.pdfbox.text.PDFTextStripper().getText(document));
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            });
            return sb.toString().trim();
        }
    }
}
