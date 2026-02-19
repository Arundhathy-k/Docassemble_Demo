package com.kovan.controller;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ClassPathResource;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;

import java.io.IOException;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.HashMap;
import java.util.Map;

/**
 * Generates a PDF via the Docupilot merge endpoint.
 *
 * All static data (name, date, company) is hardcoded in agreement.html.
 * Only {{signature}} is a token — the actual signature PNG is loaded from
 * static/images/signature.png, converted to base64, and sent to Docupilot.
 */
@RestController
@RequestMapping("/pdf")
public class PdfController {

    private static final Logger log = LoggerFactory.getLogger(PdfController.class);

    @Value("${docupilot.api.key}")
    private String apiKey;

    private static final String MERGE_URL = "https://api.docupilot.app/api/v1/templates/103642/merge";

    private final RestTemplate restTemplate = new RestTemplate();
    private final ObjectMapper objectMapper = new ObjectMapper();

    /**
     * POST /pdf/generate
     *
     * Loads signature.png, converts to base64, sends it as the {{signature}} token
     * to Docupilot. All other data (name, date, company) is in the HTML template.
     */
    @PostMapping(value = "/generate", produces = MediaType.APPLICATION_PDF_VALUE)
    public ResponseEntity<byte[]> generatePdf() throws IOException {

        // 1. Load Debt_Negotiation_Agreement HTML (template 103642 expects my_html_content)
        String htmlContent;
        try (var is = new ClassPathResource("Debt_Negotiation_Agreement 1.html").getInputStream()) {
            htmlContent = new String(is.readAllBytes(), StandardCharsets.UTF_8);
        }

        // 2. Load signature image and convert to base64 (optional, if template uses {{signature}})
        String signatureDataUri = null;
        try {
            byte[] signatureBytes = new ClassPathResource("static/images/signature.png")
                    .getInputStream().readAllBytes();
            signatureDataUri = "data:image/png;base64," + Base64.getEncoder().encodeToString(signatureBytes);
        } catch (Exception e) {
            log.warn("Could not load signature.png, omitting: {}", e.getMessage());
        }

        // 3. Merge data: full HTML + optional signature + placeholder values for {{tokens}}
        Map<String, Object> mergeData = new HashMap<>();
        mergeData.put("my_html_content", htmlContent);
        if (signatureDataUri != null) mergeData.put("signature", signatureDataUri);
        mergeData.put("agreement_date", "02/19/2026");
        mergeData.put("client_name", "John Doe");
        mergeData.put("client_address", "123 Main St, New York, NY 10001");
        mergeData.put("client_phone", "555-123-4567");
        mergeData.put("estimated_months", "36");
        mergeData.put("total_enrolled_debt", "25000");
        mergeData.put("savings_percent", "50");
        mergeData.put("savings_needed", "12500");
        mergeData.put("fee_percent", "25");
        mergeData.put("negotiation_fee", "6250");
        mergeData.put("program_cost", "18750");
        mergeData.put("total_savings", "6250");
        mergeData.put("program_start_date", "02/19/2026");
        mergeData.put("monthly_payment", "520");
        mergeData.put("program_length", "36");
        mergeData.put("current_date", "02/19/2026");
        mergeData.put("co_client_name", "");

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.set("apikey", apiKey);
        HttpEntity<Map<String, Object>> request = new HttpEntity<>(mergeData, headers);

        log.info("Calling Docupilot merge: {}", MERGE_URL);

        ResponseEntity<byte[]> response = restTemplate.postForEntity(MERGE_URL, request, byte[].class);

        byte[] body = response.getBody();
        MediaType contentType = response.getHeaders().getContentType();

        log.info("Docupilot response: status={}, contentType={}, bodyLength={}",
                response.getStatusCode(), contentType, body != null ? body.length : 0);

        if (body == null || body.length == 0) {
            log.error("Docupilot returned empty response");
            return errorResponse("Docupilot returned an empty response");
        }

        // If it's already a PDF, return directly
        if (isPdf(body, contentType)) {
            log.info("Received valid PDF ({} bytes)", body.length);
            return pdfResponse(body);
        }

        // Otherwise try to extract file_url from JSON response
        String bodyText = new String(body, StandardCharsets.UTF_8);
        log.info("Docupilot returned non-PDF response, trying JSON file_url extraction");

        String fileUrl;
        try {
            // Docupilot may return Python-style single quotes — normalize to valid JSON
            String jsonText = bodyText.replace("'", "\"");
            JsonNode root = objectMapper.readTree(jsonText);
            fileUrl = extractFileUrl(root);
        } catch (Exception e) {
            log.error("Failed to parse Docupilot JSON response", e);
            return errorResponse("Failed to parse Docupilot response: " + e.getMessage());
        }

        if (fileUrl == null || fileUrl.isBlank()) {
            return errorResponse("No file_url in Docupilot response: " + bodyText);
        }

        try {
            log.info("Downloading PDF from: {}", fileUrl);
            HttpHeaders downloadHeaders = new HttpHeaders();
            downloadHeaders.set("User-Agent", "Mozilla/5.0 (compatible; DocupilotClient/1.0)");
            HttpEntity<Void> downloadRequest = new HttpEntity<>(downloadHeaders);
            ResponseEntity<byte[]> downloadResponse = restTemplate.exchange(
                    URI.create(fileUrl),
                    HttpMethod.GET,
                    downloadRequest,
                    byte[].class);
            byte[] pdfBytes = downloadResponse.getBody();
            if (pdfBytes != null && pdfBytes.length > 0) {
                log.info("Downloaded PDF ({} bytes)", pdfBytes.length);
                return pdfResponse(pdfBytes);
            }
            return errorResponse("Downloaded PDF is empty");
        } catch (Exception e) {
            log.error("Failed to download PDF from file_url: {}", fileUrl, e);
            return errorResponse("Failed to download PDF: " + e.getMessage());
        }
    }

    private boolean isPdf(byte[] body, MediaType contentType) {
        if (contentType != null && "application".equals(contentType.getType()) && "pdf".equals(contentType.getSubtype())) {
            return true;
        }
        return body.length >= 4 && body[0] == 0x25 && body[1] == 0x50 && body[2] == 0x44 && body[3] == 0x46;
    }

    private String extractFileUrl(JsonNode root) {
        JsonNode data = root.path("data");
        if (data.isObject()) {
            JsonNode url = data.path("file_url");
            if (!url.isMissingNode() && url.isTextual()) return url.asText();
            url = data.path("download_url");
            if (!url.isMissingNode() && url.isTextual()) return url.asText();
        }
        JsonNode url = root.path("file_url");
        if (!url.isMissingNode() && url.isTextual()) return url.asText();
        url = root.path("download_url");
        if (!url.isMissingNode() && url.isTextual()) return url.asText();
        return null;
    }

    private ResponseEntity<byte[]> pdfResponse(byte[] pdfBytes) {
        HttpHeaders outHeaders = new HttpHeaders();
        outHeaders.setContentType(MediaType.APPLICATION_PDF);
        outHeaders.setContentDisposition(
                ContentDisposition.inline().filename("agreement.pdf").build()
        );
        return new ResponseEntity<>(pdfBytes, outHeaders, HttpStatus.OK);
    }

    private ResponseEntity<byte[]> errorResponse(String message) {
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .contentType(MediaType.APPLICATION_JSON)
                .body(("{\"success\":false,\"error\":\"" + message.replace("\"", "'") + "\"}").getBytes(StandardCharsets.UTF_8));
    }
}

