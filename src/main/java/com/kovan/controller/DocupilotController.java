package com.kovan.controller;

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

    private final RestTemplate restTemplate = new RestTemplate();

    /**
     * Converts the Debt_Negotiation_Agreement HTML file to PDF using DocuPilot convert API.
     * POST /docupilot/convert-html-to-pdf
     *
     * @return PDF bytes with Content-Type application/pdf
     */
    @GetMapping(value = "/convert-html-to-pdf", produces = MediaType.APPLICATION_PDF_VALUE)
    public ResponseEntity<byte[]> convertHtmlToPdf() throws IOException {
        byte[] htmlBytes;
        try (var is = new ClassPathResource(HTML_RESOURCE).getInputStream()) {
            htmlBytes = is.readAllBytes();
        }

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
}