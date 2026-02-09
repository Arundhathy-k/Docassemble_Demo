package com.kovan.service;

import com.kovan.model.AgreementRequest;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class DocassembleService {

    private static final String DOCASSEMBLE_URL =
            "http://localhost:8000/api/interview";

    private static final String API_KEY = "YOUR_API_KEY_HERE";

    public byte[] generateAgreementPdf(AgreementRequest request) {

        RestTemplate restTemplate = new RestTemplate();

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setBearerAuth(API_KEY);

        Map<String, Object> body = new HashMap<>();
        body.put("interview", "ndragreement:generate_agreement.yml");
        body.put("variables", request);

        HttpEntity<Map<String, Object>> entity =
                new HttpEntity<>(body, headers);

        ResponseEntity<Map> response = restTemplate.exchange(
                DOCASSEMBLE_URL,
                HttpMethod.POST,
                entity,
                Map.class
        );

        // Extract PDF URL
        Map interview = (Map) response.getBody().get("interview");
        List<Map> attachments = (List<Map>) interview.get("attachments");
        String pdfUrl = attachments.getFirst().get("url").toString();

        // Download PDF
        return restTemplate.getForObject(pdfUrl, byte[].class);
    }
}

