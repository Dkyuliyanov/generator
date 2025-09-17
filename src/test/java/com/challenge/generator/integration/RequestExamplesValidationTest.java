package com.challenge.generator.integration;

import com.challenge.generator.base.BaseTest;
import com.challenge.generator.expression.api.dto.CurlGenerationResponse;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class RequestExamplesValidationTest extends BaseTest {


    private HttpEntity<String> createHttpEntityFromFile(String fileName) throws IOException {
        Path path = Path.of(fileName);
        assertTrue(Files.exists(path), "Example file missing: " + fileName);
        
        String json = Files.readString(path);
        assertNotNull(json);
        assertFalse(json.isBlank(), "JSON file is empty: " + fileName);
        
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        return new HttpEntity<>(json, headers);
    }


    private void testValidExample(String fileName) throws IOException {
        HttpEntity<String> httpEntity = createHttpEntityFromFile(fileName);
        
        ResponseEntity<CurlGenerationResponse> response = restTemplate.postForEntity(
                "/generate-curl",
                httpEntity,
                CurlGenerationResponse.class
        );
        
        assertTrue(response.getStatusCode().is2xxSuccessful(),
                () -> "Expected 2xx for " + fileName + " but got " + response.getStatusCode());
        assertNotNull(response.getBody(), "Response body null for " + fileName);
        assertNotNull(response.getBody().curlRequest(), "Curl request null for " + fileName);

    }


    private void testErrorExample(String fileName) throws IOException {
        HttpEntity<String> httpEntity = createHttpEntityFromFile(fileName);
        
        ResponseEntity<Map> response = restTemplate.postForEntity(
                "/generate-curl",
                httpEntity,
                Map.class
        );
        
        assertTrue(response.getStatusCode().is4xxClientError() || response.getStatusCode().is5xxServerError(),
                () -> "Expected 4xx/5xx for " + fileName + " but got " + response.getStatusCode());
        assertNotNull(response.getBody(), "Error body null for " + fileName);
        assertTrue(response.getBody().containsKey("error"), "Error body should contain 'error' field for " + fileName);
    }

    @Test
    void ex001ShouldBeValidAndExecutable() throws IOException {
        testValidExample("request-examples/ex-001.json");
    }

    @Test
    void ex002ShouldBeValidAndExecutable() throws IOException {
        testValidExample("request-examples/ex-002.json");
    }

    @Test
    void ex003ShouldBeValidAndExecutable() throws IOException {
        testValidExample("request-examples/ex-003.json");
    }

    @Test
    void ex004ShouldBeValidAndExecutable() throws IOException {
        testValidExample("request-examples/ex-004.json");
    }

    @Test
    void ex005ShouldBeValidAndExecutable() throws IOException {
        testValidExample("request-examples/ex-005.json");
    }

    @Test
    void ex006ShouldBeValidAndExecutable() throws IOException {
        testValidExample("request-examples/ex-006.json");
    }

    @Test
    void ex007ShouldBeValidAndExecutable() throws IOException {
        testValidExample("request-examples/ex-007.json");
    }

    @Test
    void ex008ShouldBeValidAndExecutable() throws IOException {
        testValidExample("request-examples/ex-008.json");
    }

    @Test
    void ex009ShouldBeValidAndExecutable() throws IOException {
        testValidExample("request-examples/ex-009.json");
    }

    @Test
    void ex010ShouldBeValidAndExecutable() throws IOException {
        testValidExample("request-examples/ex-010.json");
    }

    @Test
    void ex011ShouldReturnError() throws IOException {
        testErrorExample("request-examples/ex-011.json");
    }

    @Test
    void ex012ShouldReturnError() throws IOException {
        testErrorExample("request-examples/ex-012.json");
    }

    @Test
    void ex013ShouldReturnError() throws IOException {
        testErrorExample("request-examples/ex-013.json");
    }

    @Test
    void ex014ShouldReturnError() throws IOException {
        testErrorExample("request-examples/ex-014.json");
    }

    @Test
    void ex015ShouldReturnError() throws IOException {
        testErrorExample("request-examples/ex-015.json");
    }

    @Test
    void ex016ShouldReturnError() throws IOException {
        testErrorExample("request-examples/ex-016.json");
    }

    @Test
    void ex017ShouldReturnError() throws IOException {
        testErrorExample("request-examples/ex-017.json");
    }

    @Test
    void ex019ShouldReturnError() throws IOException {
        testErrorExample("request-examples/ex-019.json");
    }
}
