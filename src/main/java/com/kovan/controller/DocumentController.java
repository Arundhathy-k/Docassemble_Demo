package com.kovan.controller;

import com.kovan.model.AgreementRequest;
import com.kovan.service.DocassembleService;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/documents")
public class DocumentController {

    private final DocassembleService service;

    public DocumentController(DocassembleService service) {
        this.service = service;
    }

    @PostMapping("/ndr-agreement")
    public ResponseEntity<byte[]> generate(
            @RequestBody AgreementRequest request) {

        byte[] pdf = service.generateAgreementPdf(request);

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION,
                        "attachment; filename=ndr_agreement.pdf")
                .contentType(MediaType.APPLICATION_PDF)
                .body(pdf);
    }
}
