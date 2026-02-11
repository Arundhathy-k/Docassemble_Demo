package com.kovan.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.kovan.model.AgreementRequest;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.net.URI;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Generates NDR agreement PDF via Docassemble API.
 *
 * Docassemble does not have POST /api/interview. Correct flow:
 * 1. GET /api/session/new?i=<interview> — create session
 * 2. POST /api/session — set variables and run (question=1)
 * 3. GET /api/session — read session state for attachments
 * 4. GET attachment URL — download PDF
 */
@Service
public class DocassembleService {

    private static final String BASE_URL = "http://localhost:8000";
    private static final String API_KEY =
            "f2Ra4eCoJQlxugCJGwzt4gAmoM2tnQpH";
    private static final String INTERVIEW = "docassemble.playground1:generate_agreement.yml";

    private final RestTemplate restTemplate = new RestTemplate();
    private final ObjectMapper objectMapper = new ObjectMapper();

    public byte[] generateAgreementPdf(AgreementRequest request) {

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.set("X-API-Key", API_KEY);

        /* ---------- 1. Create session: GET /api/session/new?i=...&first_name=...&last_name=... ---------- */
        String iEncoded = URLEncoder.encode(INTERVIEW, StandardCharsets.UTF_8);
        String newUrl = BASE_URL + "/api/session/new?i=" + iEncoded
                + "&first_name=" + URLEncoder.encode(nullToEmpty(request.getFirstName()), StandardCharsets.UTF_8)
                + "&last_name=" + URLEncoder.encode(nullToEmpty(request.getLastName()), StandardCharsets.UTF_8)
                + "&address=" + URLEncoder.encode(nullToEmpty(request.getAddress()), StandardCharsets.UTF_8)
                + "&phone=" + URLEncoder.encode(nullToEmpty(request.getPhone()), StandardCharsets.UTF_8)
                + "&agreement_date=" + URLEncoder.encode(nullToEmpty(request.getAgreementDate()), StandardCharsets.UTF_8);
        ResponseEntity<Map> newResponse = restTemplate.exchange(
                URI.create(newUrl),
                HttpMethod.GET,
                new HttpEntity<>(headers),
                Map.class
        );
        Map newBody = newResponse.getBody();
        if (newBody == null || newBody.get("session") == null) {
            throw new RuntimeException("Docassemble session/new did not return session id: " + newBody);
        }
        String sessionId = newBody.get("session").toString();
        String iParam = newBody.containsKey("i") ? newBody.get("i").toString() : INTERVIEW;

        /* ---------- 2. Set variables and run: POST /api/session ---------- */
        Map<String, Object> variables = new HashMap<>();
        variables.put("first_name", nullToEmpty(request.getFirstName()));
        variables.put("last_name", nullToEmpty(request.getLastName()));
        variables.put("address", nullToEmpty(request.getAddress()));
        variables.put("phone", nullToEmpty(request.getPhone()));
        variables.put("agreement_date", nullToEmpty(request.getAgreementDate()));

        Map<String, Object> postBody = new HashMap<>();
        postBody.put("i", iParam);
        postBody.put("session", sessionId);
        postBody.put("variables", variables);
        postBody.put("question", 1);

        String postBodyJson;
        try {
            postBodyJson = objectMapper.writeValueAsString(postBody);
        } catch (Exception e) {
            throw new RuntimeException("Failed to serialize POST body", e);
        }
        HttpEntity<String> postEntity = new HttpEntity<>(postBodyJson, headers);

        restTemplate.exchange(
                URI.create(BASE_URL + "/api/session"),
                HttpMethod.POST,
                postEntity,
                Map.class
        );

        /* ---------- 3. Get session state: GET /api/session?i=...&session=... ---------- */
        String getUrl = BASE_URL + "/api/session?i=" + URLEncoder.encode(iParam, StandardCharsets.UTF_8)
                + "&session=" + URLEncoder.encode(sessionId, StandardCharsets.UTF_8);
        ResponseEntity<Map> getResponse = restTemplate.exchange(
                URI.create(getUrl),
                HttpMethod.GET,
                new HttpEntity<>(headers),
                Map.class
        );
        Map result = getResponse.getBody();
        if (result == null) {
            throw new RuntimeException("Docassemble returned empty session state");
        }

        /* ---------- 4. Resolve PDF: either attachments[0].url or _internal.docvar.0.pdf.number ---------- */
        int attachmentNumber = -1;
        String pdfUrl = null;

        if (result.containsKey("attachments")) {
            @SuppressWarnings("unchecked")
            List<Map<String, Object>> attachments = (List<Map<String, Object>>) result.get("attachments");
            if (attachments != null && !attachments.isEmpty()) {
                Object urlObj = attachments.getFirst().get("url");
                if (urlObj != null) {
                    pdfUrl = urlObj.toString();
                    if (pdfUrl.startsWith("/")) pdfUrl = BASE_URL + pdfUrl;
                }
            }
        }
        if (pdfUrl == null) {
            attachmentNumber = extractPdfAttachmentNumber(result);
            if (attachmentNumber <= 0) {
                throw new RuntimeException("No PDF in session. Session has no 'attachments' and no _internal.docvar PDF number. Keys: " + result.keySet());
            }
            pdfUrl = BASE_URL + "/interview?i=" + URLEncoder.encode(iParam, StandardCharsets.UTF_8)
                    + "&session=" + URLEncoder.encode(sessionId, StandardCharsets.UTF_8)
                    + "&attachment=" + attachmentNumber
                    + "&key=" + URLEncoder.encode(API_KEY, StandardCharsets.UTF_8);
        }

        /* ---------- 5. Download PDF: try /api/file/<number> first, then /interview?attachment= with cookie ---------- */
        byte[] body = null;
        if (attachmentNumber > 0) {
            String apiFileUrl = BASE_URL + "/api/file/" + attachmentNumber
                    + "?i=" + URLEncoder.encode(iParam, StandardCharsets.UTF_8)
                    + "&session=" + URLEncoder.encode(sessionId, StandardCharsets.UTF_8);
            try {
                ResponseEntity<byte[]> apiFileResponse = restTemplate.exchange(
                        URI.create(apiFileUrl),
                        HttpMethod.GET,
                        new HttpEntity<>(headers),
                        byte[].class
                );
                body = apiFileResponse.getBody();
            } catch (Exception ignored) {
                // Fall through to interview URL
            }
        }
        if (body == null) {
            HttpHeaders downloadHeaders = new HttpHeaders();
            downloadHeaders.set("X-API-Key", API_KEY);
            downloadHeaders.set("Cookie", "X-API-Key=" + API_KEY);
            ResponseEntity<byte[]> pdfResponse = restTemplate.exchange(
                    URI.create(pdfUrl),
                    HttpMethod.GET,
                    new HttpEntity<>(downloadHeaders),
                    byte[].class
            );
            body = pdfResponse.getBody();
        }
        if (body == null || body.length == 0) {
            throw new RuntimeException("Empty response from docassemble when downloading PDF");
        }
        if (!isPdf(body)) {
            String preview = body.length > 200 ? new String(body, 0, 200, StandardCharsets.UTF_8) : new String(body, StandardCharsets.UTF_8);
            throw new RuntimeException("Docassemble did not return a PDF (got " + body.length + " bytes). "
                    + "Ensure the interview generates a PDF and the server allows API key for file download. "
                    + "Response preview: " + preview.replaceAll("\\s+", " "));
        }
        return body;
    }

    private static boolean isPdf(byte[] body) {
        if (body == null || body.length < 4) return false;
        return body[0] == '%' && body[1] == 'P' && body[2] == 'D' && body[3] == 'F';
    }

    private static String nullToEmpty(String s) {
        return s == null ? "" : s;
    }

    /**
     * Extract the first PDF attachment number from Docassemble session state.
     * Session stores docs under _internal.docvar["0"].pdf.number (and similar for other indices).
     */
    @SuppressWarnings("unchecked")
    private int extractPdfAttachmentNumber(Map<String, Object> result) {
        Object internalObj = result.get("_internal");
        if (!(internalObj instanceof Map)) return -1;
        Map<String, Object> internal = (Map<String, Object>) internalObj;
        Object docvarObj = internal.get("docvar");
        if (!(docvarObj instanceof Map)) return -1;
        Map<String, Object> docvar = (Map<String, Object>) docvarObj;
        for (String key : docvar.keySet()) {
            Object docObj = docvar.get(key);
            if (!(docObj instanceof Map)) continue;
            Map<String, Object> doc = (Map<String, Object>) docObj;
            Object pdfObj = doc.get("pdf");
            if (!(pdfObj instanceof Map)) continue;
            Map<String, Object> pdf = (Map<String, Object>) pdfObj;
            Object num = pdf.get("number");
            if (num instanceof Number) {
                return ((Number) num).intValue();
            }
        }
        return -1;
    }
}
