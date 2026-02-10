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

    private static final String DOCASSEMBLE_API =
            "http://localhost:8000/api/interview";

    private static final String API_KEY =
            "f2Ra4eCoJQlxugCJGwzt4gAmoM2tnQpH";

    public byte[] generateAgreementPdf(AgreementRequest request) {

        RestTemplate restTemplate = new RestTemplate();

        /* ---------- Headers ---------- */
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.set("X-API-Key", API_KEY);

        /* ---------- Variables ---------- */
        Map<String, Object> variables = new HashMap<>();
        variables.put("first_name", request.getFirstName());
        variables.put("last_name", request.getLastName());
        variables.put("address", request.getAddress());
        variables.put("phone", request.getPhone());
        variables.put("agreement_date", request.getAgreementDate());

        /* ---------- Body ---------- */
        Map<String, Object> body = new HashMap<>();
        body.put("i", "docassemble.playground1:generate_agreement.yml");
        body.put("variables", variables);

        HttpEntity<Map<String, Object>> entity =
                new HttpEntity<>(body, headers);

        /* ---------- Call docassemble ---------- */
        ResponseEntity<Map> response = restTemplate.postForEntity(
                DOCASSEMBLE_API,
                entity,
                Map.class
        );

        Map result = response.getBody();

        if (result == null || !result.containsKey("attachments")) {
            throw new RuntimeException("No attachments returned by docassemble");
        }

        List<Map<String, Object>> attachments =
                (List<Map<String, Object>>) result.get("attachments");

        if (attachments.isEmpty()) {
            throw new RuntimeException("No PDF generated");
        }

        String pdfUrl = attachments.get(0).get("url").toString();

        /* ---------- Download PDF ---------- */
        HttpHeaders pdfHeaders = new HttpHeaders();
        pdfHeaders.set("X-API-Key", API_KEY);

        HttpEntity<Void> pdfRequest = new HttpEntity<>(pdfHeaders);

        ResponseEntity<byte[]> pdfResponse =
                restTemplate.exchange(
                        "http://localhost:8000" + pdfUrl,
                        HttpMethod.GET,
                        pdfRequest,
                        byte[].class
                );

        return pdfResponse.getBody();
    }
}
