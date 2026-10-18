package com.hrapp.util;

import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;

/**
 * Checks an uploaded resume before we read or save it.
 * Rules: not empty, max 5 MB, .pdf extension, PDF content type, real PDF bytes.
 */
public class ResumeFileValidator {

    public static final long MAX_SIZE_BYTES = 5 * 1024 * 1024; // 5 MB

    private ResumeFileValidator() {}

    public static void validate(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new IllegalArgumentException("Resume file is required and cannot be empty");
        }
        if (file.getSize() > MAX_SIZE_BYTES) {
            throw new IllegalArgumentException("Resume is too large. Maximum size is 5 MB");
        }

        String name = file.getOriginalFilename();
        if (name == null || !name.toLowerCase().endsWith(".pdf")) {
            throw new IllegalArgumentException("Only .pdf files are accepted for resumes");
        }

        String type = file.getContentType();
        if (!"application/pdf".equals(type) && !"application/x-pdf".equals(type)) {
            throw new IllegalArgumentException("Invalid file type. Only PDF resumes are accepted");
        }

        // Every real PDF starts with "%PDF" - stops renamed .exe/.docx files
        try (InputStream in = file.getInputStream()) {
            byte[] header = in.readNBytes(4);
            if (!"%PDF".equals(new String(header))) {
                throw new IllegalArgumentException("File content is not a valid PDF");
            }
        } catch (IOException e) {
            throw new IllegalArgumentException("Could not read the uploaded file");
        }
    }
}
