package com.kovan.controller;

import com.kovan.service.HtmlMergeService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.ClassPathResource;
import org.springframework.http.*;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/docupilot")
public class DocupilotController {

    @Value("${docupilot.api.key}")
    private String apiKey;

    @Value("${docupilot.api.secret}")
    private String apiSecret;

    @Value("${docupilot.workspace.id}")
    private String workspaceId;

    private static final String CONVERT_API_URL = "https://api.docupilot.app/api/v2/convert/?response_type=stream";

    private static final String HTML_RESOURCE = "Debt_Negotiation_Agreement 2.html";

    private static final String SIGNATURE_RESOURCE = "static/images/signature.png";

    private final RestTemplate restTemplate = new RestTemplate();
    private final HtmlMergeService htmlMergeService;

    public DocupilotController(HtmlMergeService htmlMergeService) {
        this.htmlMergeService = htmlMergeService;
    }

    /**
     * Returns merged HTML with signature embedded in {{ client_signature }}.
     * GET /docupilot/merged-html
     *
     * @return merged HTML as text/html
     */
    @GetMapping(value = "/merged-html", produces = MediaType.TEXT_HTML_VALUE)
    public ResponseEntity<String> getMergedHtml() throws IOException {
        byte[] signatureBytes = new ClassPathResource(SIGNATURE_RESOURCE).getInputStream().readAllBytes();
        String merged = htmlMergeService.mergeSignaturesIntoHtml(HTML_RESOURCE, signatureBytes, defaultPlaceholders());
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.TEXT_HTML);
        headers.setContentDisposition(
                ContentDisposition.attachment().filename("merged-agreement.html").build());
        return new ResponseEntity<>(merged, headers, HttpStatus.OK);
    }

    /**
     * Converts the Debt_Negotiation_Agreement HTML file to PDF using DocuPilot convert API.
     * Merges signature into HTML first, then sends to DocuPilot.
     * GET /docupilot/convert-html-to-pdf
     *
     * @return PDF bytes with Content-Type application/pdf
     */
    @GetMapping(value = "/convert-html-to-pdf", produces = MediaType.APPLICATION_PDF_VALUE)
    public ResponseEntity<byte[]> convertHtmlToPdf() throws IOException {
        byte[] signatureBytes = new ClassPathResource(SIGNATURE_RESOURCE).getInputStream().readAllBytes();
        String mergedHtml = htmlMergeService.mergeSignaturesIntoHtml(HTML_RESOURCE, signatureBytes, defaultPlaceholders());
        byte[] htmlBytes = mergedHtml.getBytes(StandardCharsets.UTF_8);

        MultiValueMap<String, Object> body = new LinkedMultiValueMap<>();
        body.add("file", new ByteArrayResource(htmlBytes) {
            @Override
            public String getFilename() {
                return "document.html";
            }
        });
        body.add("output_type", "pdf");
        body.add("output_file_name", "agreement.pdf");

        HttpHeaders headers = new HttpHeaders();
        String credentials = Base64.getEncoder().encodeToString((apiKey + ":" + apiSecret).getBytes(StandardCharsets.UTF_8));
        headers.set("Authorization", "Bearer " + credentials);
        headers.set("X-Workspace", workspaceId);

        HttpEntity<MultiValueMap<String, Object>> request = new HttpEntity<>(body, headers);

        ResponseEntity<byte[]> response = restTemplate.postForEntity(CONVERT_API_URL, request, byte[].class);

        byte[] pdfBytes = response.getBody();
        if (pdfBytes == null || pdfBytes.length == 0) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(("{\"error\":\"DocuPilot returned empty response\"}").getBytes(StandardCharsets.UTF_8));
        }

        HttpHeaders outHeaders = new HttpHeaders();
        outHeaders.setContentType(MediaType.APPLICATION_PDF);
        outHeaders.setContentDisposition(
                ContentDisposition.attachment().filename("agreement.pdf").build());

        return new ResponseEntity<>(pdfBytes, outHeaders, HttpStatus.OK);
    }

    private Map<String, String> defaultPlaceholders() {
        Map<String, String> m = new HashMap<>();
        m.put("agreement_date", "02/19/2026");
        m.put("client_name", "John Doe");
        m.put("client_address", "123 Main St, New York, NY 10001");
        m.put("client_phone", "555-123-4567");
        m.put("estimated_months", "36");
        m.put("total_enrolled_debt", "25000");
        m.put("savings_percent", "50");
        m.put("savings_needed", "12500");
        m.put("fee_percent", "25");
        m.put("negotiation_fee", "6250");
        m.put("program_cost", "18750");
        m.put("total_savings", "6250");
        m.put("program_start_date", "02/19/2026");
        m.put("monthly_payment", "520");
        m.put("program_length", "36");
        m.put("current_date", "02/19/2026");
        m.put("co_client_name", "");
        return m;
    }
}