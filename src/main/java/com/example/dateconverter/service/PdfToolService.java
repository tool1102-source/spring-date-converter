package com.example.dateconverter.service;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.common.PDRectangle;
import org.apache.pdfbox.pdmodel.font.PDType1Font;
import org.apache.pdfbox.text.PDFTextStripper;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;

@Service
public class PdfToolService {

    /** ✅ PDF → テキスト変換 */
    public String convertPdfToText(MultipartFile pdfFile) throws IOException {
        try (PDDocument document = PDDocument.load(pdfFile.getInputStream())) {
            PDFTextStripper stripper = new PDFTextStripper();
            return stripper.getText(document);
        }
    }

    /** ✅ テキスト → PDF変換 */
    public File convertTextToPdf(MultipartFile textFile) throws IOException {
        String text = new String(textFile.getBytes(), "UTF-8");
        File pdfFile = File.createTempFile("converted-", ".pdf");

        try (PDDocument document = new PDDocument()) {
            PDPage page = new PDPage(PDRectangle.A4);
            document.addPage(page);

            try (PDPageContentStream cs = new PDPageContentStream(document, page)) {
                cs.beginText();
                cs.setFont(PDType1Font.HELVETICA, 12);
                cs.newLineAtOffset(50, 750);

                float y = 750;
                float leading = 14.5f;

                for (String line : text.split("\n")) {
                    if (y < 50) {
                        cs.endText();
                        cs.close();
                        page = new PDPage(PDRectangle.A4);
                        document.addPage(page);
                        try (PDPageContentStream newCs = new PDPageContentStream(document, page)) {
                            newCs.beginText();
                            newCs.setFont(PDType1Font.HELVETICA, 12);
                            newCs.newLineAtOffset(50, 750);
                        }
                        y = 750;
                    }
                    cs.showText(line);
                    cs.newLineAtOffset(0, -leading);
                    y -= leading;
                }

                cs.endText();
            }

            document.save(pdfFile);
        }

        return pdfFile;
    }
}
