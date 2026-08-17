package com.hrapp.util;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

/**
 * Utility class for extracting plain text from uploaded PDF resumes.
 * Uses Apache PDFBox 3.x API.
 */
public class PdfTextExtractor {

    // Private constructor — utility class, not meant to be instantiated
    private PdfTextExtractor() {}

    /**
     * Extract all text content from a PDF MultipartFile.
     *
     * @param file  the uploaded resume PDF
     * @return      extracted plain text, or empty string if extraction fails
     * @throws IOException if the file cannot be read or is not a valid PDF
     */
    public static String extractText(MultipartFile file) throws IOException {
        if (file == null || file.isEmpty()) {
            return "";
        }

        // PDFBox 3.x: use Loader.loadPDF() — PDDocument.load() is deprecated
        try (PDDocument document = org.apache.pdfbox.Loader.loadPDF(file.getBytes())) {

            if (document.isEncrypted()) {
                throw new IOException("Cannot extract text from an encrypted PDF");
            }

            PDFTextStripper stripper = new PDFTextStripper();

            // Strip all pages
            stripper.setStartPage(1);
            stripper.setEndPage(document.getNumberOfPages());
            stripper.setSortByPosition(true);  // preserves reading order

            String rawText = stripper.getText(document);

            // Normalise: collapse multiple blank lines and trim
            return rawText.replaceAll("\\n{3,}", "\n\n").trim();
        }
    }

    /**
     * Truncate extracted text to a maximum character count.
     * Groq API has a token limit — keep the prompt manageable.
     *
     * @param text      raw extracted text
     * @param maxChars  maximum characters to include (recommended: 6000)
     * @return          truncated text
     */
    public static String truncate(String text, int maxChars) {
        if (text == null) return "";
        if (text.length() <= maxChars) return text;
        return text.substring(0, maxChars) + "\n[... resume truncated ...]";
    }
}
