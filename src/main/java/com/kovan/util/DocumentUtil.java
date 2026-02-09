package com.kovan.util;

import lombok.experimental.UtilityClass;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.Base64;

/**
 * Utility class for working with documents and encoding
 */
@Slf4j
@UtilityClass
public class DocumentUtil {

    private static final Logger log = LoggerFactory.getLogger(DocumentUtil.class);

    /**
     * Encodes HTML content to Base64
     */
    public static String encodeToBase64(String htmlContent) {
        if (htmlContent == null || htmlContent.isEmpty()) {
            return "";
        }
        return Base64.getEncoder().encodeToString(htmlContent.getBytes(StandardCharsets.UTF_8));
    }

    /**
     * Decodes Base64 content to HTML string
     */
    public static String decodeFromBase64(String encodedContent) {
        if (encodedContent == null || encodedContent.isEmpty()) {
            return "";
        }
        try {
            byte[] decodedBytes = Base64.getDecoder().decode(encodedContent);
            return new String(decodedBytes, StandardCharsets.UTF_8);
        } catch (IllegalArgumentException e) {

            log.error("Error decoding Base64 content", e);
            return "";
        }
    }

    /**
     * Generates a download filename from session ID
     */
    public static String generateFilename(String sessionId) {
        return String.format("signed_document_%s.html", sessionId);
    }

    /**
     * Sanitizes HTML content for security
     */
    public static String sanitizeHtml(String htmlContent) {
        if (htmlContent == null) {
            return "";
        }
        // Basic sanitization - can be enhanced with HtmlCleaner or similar libraries
        return htmlContent
            .replaceAll("<script[^>]*>.*?</script>", "")
            .replaceAll("javascript:", "");
    }

    /**
     * Validates if HTML content looks valid
     */
    public static boolean isValidHtml(String htmlContent) {
        if (htmlContent == null || htmlContent.trim().isEmpty()) {
            return false;
        }
        return htmlContent.contains("<html") || htmlContent.contains("<HTML") ||
               htmlContent.contains("<!DOCTYPE");
    }
}
