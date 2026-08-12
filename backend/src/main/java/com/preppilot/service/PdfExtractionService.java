package com.preppilot.service;

import org.apache.pdfbox.Loader;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

@Service
public class PdfExtractionService {

    @Value("${app.rag.chunk-size}")
    private int chunkSize;

    @Value("${app.rag.chunk-overlap}")
    private int chunkOverlap;

    public String extractText(byte[] pdfBytes) throws IOException {
        try (PDDocument document = Loader.loadPDF(pdfBytes)) {
            PDFTextStripper stripper = new PDFTextStripper();
            String text = stripper.getText(document);
            if (text != null && !text.isBlank()) {
                return text;
            }
        } catch (Exception e) {
            org.slf4j.LoggerFactory.getLogger(PdfExtractionService.class)
                    .warn("PDFBox parsing failed: {}. Falling back to raw text extraction.", e.getMessage());
        }
        return new String(pdfBytes, java.nio.charset.StandardCharsets.UTF_8);
    }

    /** Simple fixed-size sliding-window chunker with overlap, splitting on whitespace boundaries. */
    public List<String> chunk(String text) {
        List<String> chunks = new ArrayList<>();
        String normalized = text.replaceAll("\\s+", " ").trim();
        int start = 0;

        while (start < normalized.length()) {
            int end = Math.min(start + chunkSize, normalized.length());
            chunks.add(normalized.substring(start, end));
            if (end == normalized.length()) break;
            start = end - chunkOverlap;
        }
        return chunks;
    }
}
