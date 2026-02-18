package com.kovan.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.microsoft.playwright.Browser;
import com.microsoft.playwright.Page;
import com.microsoft.playwright.Playwright;
import org.springframework.core.io.ClassPathResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Map;

@RestController
@RequestMapping("/docupilot")
public class DocupilotController {

    private static final ObjectMapper objectMapper = new ObjectMapper();

    // TODO: move API key to configuration or environment variable for production use
    private static final String API_KEY = "9c0e49c1e2347098cfcb9f9e3d9d8c17";

    @GetMapping("/invoice")
    public ResponseEntity<byte[]> generateInvoice() throws IOException {
        String htmlContent = loadDebtAgreementHtml();

        URL url = new URL("https://kovan-labs.docupilot.app/dashboard/documents/create/8fecdb46/274e0885?download=file");
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();

        conn.setRequestMethod("POST");
        conn.setRequestProperty("Content-Type", "application/json");
        conn.setRequestProperty("X-API-KEY", API_KEY);
        conn.setDoOutput(true);

        Map<String, Object> payload = Map.of("my_html_content", htmlContent);

        String requestBody = objectMapper.writeValueAsString(payload);

        try (OutputStream os = conn.getOutputStream()) {
            os.write(requestBody.getBytes());
            os.flush();
        }

        int responseCode = conn.getResponseCode();
        if (responseCode != HttpURLConnection.HTTP_OK) {
            return ResponseEntity
                    .status(responseCode)
                    .build();
        }

        byte[] pdfBytes;
        try (InputStream is = conn.getInputStream();
             ByteArrayOutputStream buffer = new ByteArrayOutputStream()) {

            byte[] data = new byte[8192];
            int nRead;
            while ((nRead = is.read(data, 0, data.length)) != -1) {
                buffer.write(data, 0, nRead);
            }

            pdfBytes = buffer.toByteArray();
        }

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION,
                        "attachment; filename=invoice.pdf")
                .contentType(MediaType.APPLICATION_PDF)
                .body(pdfBytes);
    }

    /**
     * Generates a PDF from the exact Debt_Negotiation_Agreement HTML using headless Chromium (Playwright).
     * Renders the HTML exactly as in a browser (full CSS, flexbox, positioning, images).
     * Requires Chromium: run {@code mvn exec:exec -Dexec.executable=java -Dexec.args="-cp %classpath com.microsoft.playwright.CLI install chromium"} once, or ensure Playwright browsers are installed.
     */
    @GetMapping("/invoice-exact")
    public ResponseEntity<byte[]> generateInvoiceExact() throws IOException {
        String htmlContent = loadDebtAgreementHtml();

        Path pdfPath = null;
        try (Playwright playwright = Playwright.create()) {
            try (Browser browser = playwright.chromium().launch()) {
                try (Page page = browser.newContext().newPage()) {
                    page.setContent(htmlContent);
                    pdfPath = Files.createTempFile("debt-agreement-", ".pdf");
                    page.pdf(new Page.PdfOptions()
                            .setPath(pdfPath)
                            .setPrintBackground(true));
                    byte[] pdfBytes = Files.readAllBytes(pdfPath);
                    return ResponseEntity.ok()
                            .header(HttpHeaders.CONTENT_DISPOSITION,
                                    "attachment; filename=debt_negotiation_agreement.pdf")
                            .contentType(MediaType.APPLICATION_PDF)
                            .body(pdfBytes);
                }
            }
        } finally {
            if (pdfPath != null) {
                try {
                    Files.deleteIfExists(pdfPath);
                } catch (IOException ignored) {
                }
            }
        }
    }

    private String loadDebtAgreementHtml() throws IOException {
        ClassPathResource resource = new ClassPathResource("Debt_Negotiation_Agreement 1.html");
        try (InputStream htmlStream = resource.getInputStream()) {
            ByteArrayOutputStream htmlBuffer = new ByteArrayOutputStream();
            byte[] tmp = new byte[8192];
            int read;
            while ((read = htmlStream.read(tmp)) != -1) {
                htmlBuffer.write(tmp, 0, read);
            }
            return htmlBuffer.toString(StandardCharsets.UTF_8);
        }
    }
}
