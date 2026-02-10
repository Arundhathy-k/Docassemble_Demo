package com.kovan.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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

    private static final String DOCUPILOT_BASE_URL =
            "https://kovan-labs.docupilot.app/dashboard/documents/create/8fecdb46/a9c9cb62";

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

        // 1. Load signature image and convert to base64 data URI
        byte[] signatureBytes = new ClassPathResource("static/images/signature.png")
                .getInputStream().readAllBytes();
        String signatureDataUri = "data:image/png;base64,"
                + Base64.getEncoder().encodeToString(signatureBytes);

        log.info("Loaded signature.png ({} bytes, base64 length: {})",
                signatureBytes.length, signatureDataUri.length());

        // 2. Send only the signature as merge data — it's the only {{token}} in the template
        Map<String, Object> mergeData = new HashMap<>();
        mergeData.put("signature", signatureDataUri);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<Map<String, Object>> request = new HttpEntity<>(mergeData, headers);

        String downloadUrl = DOCUPILOT_BASE_URL + "?download=file";
        log.info("Calling Docupilot: {}", downloadUrl);

        ResponseEntity<byte[]> response = restTemplate.postForEntity(downloadUrl, request, byte[].class);

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

        // Docupilot may return Python-style single quotes — normalize first
        String jsonText = bodyText.replace("'", "\"");
        try {
            @SuppressWarnings("unchecked")
            Map<String, Object> json = objectMapper.readValue(jsonText, Map.class);
            String fileUrl = extractFileUrl(json);

            if (fileUrl != null) {
                log.info("Downloading PDF from: {}", fileUrl);
                // Use URI to prevent RestTemplate from double-encoding %20 etc.
                byte[] pdfBytes = restTemplate.getForObject(URI.create(fileUrl), byte[].class);
                if (pdfBytes != null && pdfBytes.length > 0) {
                    log.info("Downloaded PDF ({} bytes)", pdfBytes.length);
                    return pdfResponse(pdfBytes);
                }
            }
            return errorResponse("No downloadable file URL in response: " + bodyText);

        } catch (Exception e) {
            log.error("Failed to parse Docupilot response: {}", bodyText, e);
            return errorResponse("Unexpected Docupilot response: " + bodyText);
        }
    }

    private boolean isPdf(byte[] body, MediaType contentType) {
        if (contentType != null && "application".equals(contentType.getType()) && "pdf".equals(contentType.getSubtype())) {
            return true;
        }
        return body.length >= 4 && body[0] == 0x25 && body[1] == 0x50 && body[2] == 0x44 && body[3] == 0x46;
    }

    @SuppressWarnings("unchecked")
    private String extractFileUrl(Map<String, Object> json) {
        if (json.get("data") instanceof Map) {
            Map<String, Object> data = (Map<String, Object>) json.get("data");
            if (data.get("file_url") != null) return data.get("file_url").toString();
            if (data.get("download_url") != null) return data.get("download_url").toString();
        }
        if (json.get("file_url") != null) return json.get("file_url").toString();
        if (json.get("download_url") != null) return json.get("download_url").toString();
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

