package com.challenge.generator.integration;

import com.challenge.generator.expression.api.dto.CurlGenerationRequest;
import com.challenge.generator.expression.api.dto.CurlGenerationResponse;
import com.challenge.generator.base.BaseTest;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;

import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class CurlGenerationEndpointTest extends BaseTest {

    @Test
    void shouldGenerateCurlRequestForValidEmails() {

        var request = new CurlGenerationRequest(
                "firstName & '.' & lastName & '@' & domain",
                Map.of(
                        "firstName", "John",
                        "lastName", "Doe", 
                        "domain", "example.com"
                )
        );


        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<CurlGenerationRequest> httpEntity = new HttpEntity<>(request, headers);
        
        ResponseEntity<CurlGenerationResponse> response = restTemplate.postForEntity(
                "/generate-curl", 
                httpEntity, 
                CurlGenerationResponse.class
        );


        assertTrue(response.getStatusCode().is2xxSuccessful());
        assertNotNull(response.getBody());
        
        CurlGenerationResponse body = response.getBody();

        String curlRequest = body.curlRequest();
        assertTrue(curlRequest.contains("curl --location --request GET"));
        assertTrue(curlRequest.contains("http://localhost:8081/generate"));
        assertTrue(curlRequest.contains("expression=firstName+%26+%27.%27+%26+lastName+%26+%27%40%27+%26+domain"));
        assertTrue(curlRequest.contains("firstName=John"));
        assertTrue(curlRequest.contains("lastName=Doe"));
        assertTrue(curlRequest.contains("domain=example.com"));
        assertNull(body.hint());
    }

    @Test
    void shouldGenerateCurlRequestWithHintForInvalidEmails() {

        var request = new CurlGenerationRequest(
                "name & '@invalid'",
                Map.of("name", "test")
        );


        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<CurlGenerationRequest> httpEntity = new HttpEntity<>(request, headers);
        
        ResponseEntity<CurlGenerationResponse> response = restTemplate.postForEntity(
                "/generate-curl", 
                httpEntity, 
                CurlGenerationResponse.class
        );


        assertTrue(response.getStatusCode().is2xxSuccessful());
        assertNotNull(response.getBody());
        
        CurlGenerationResponse body = response.getBody();
        String expectedCurl = "curl --location --request GET \\\n" +
                "  'http://localhost:8081/generate?" +
                "expression=name+%26+%27%40invalid%27&name=test'";
        assertEquals(expectedCurl, body.curlRequest());
        assertEquals("Warning: This curl request will produce 1 invalid email address(es): test@invalid", body.hint());
    }

    @Test
    void shouldHandleMultipleInvalidEmails() {

        var request = new CurlGenerationRequest(
                "zip(split(names, ','), '@invalid')",
                Map.of("names", "user1,user2")
        );


        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<CurlGenerationRequest> httpEntity = new HttpEntity<>(request, headers);
        
        ResponseEntity<CurlGenerationResponse> response = restTemplate.postForEntity(
                "/generate-curl", 
                httpEntity, 
                CurlGenerationResponse.class
        );


        assertTrue(response.getStatusCode().is2xxSuccessful());
        assertNotNull(response.getBody());
        
        CurlGenerationResponse body = response.getBody();
        assertNotNull(body.curlRequest());
        assertEquals("Warning: This curl request will produce 2 invalid email address(es): user1@invalid, user2@invalid", body.hint());
    }

    @Test
    void shouldReturnErrorForMissingExpression() {

        String invalidJson = "{\"inputs\":{\"name\":\"test\"}}";


        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<String> httpEntity = new HttpEntity<>(invalidJson, headers);
        
        ResponseEntity<Map> response = restTemplate.postForEntity(
                "/generate-curl", 
                httpEntity, 
                Map.class
        );


        assertTrue(response.getStatusCode().is4xxClientError() || response.getStatusCode().is5xxServerError());
        assertNotNull(response.getBody());
    }

    @Test
    void shouldReturnErrorForNullInputs() {

        String invalidJson = "{\"expression\":\"name & '@test.com'\"}";


        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<String> httpEntity = new HttpEntity<>(invalidJson, headers);
        
        ResponseEntity<Map> response = restTemplate.postForEntity(
                "/generate-curl", 
                httpEntity, 
                Map.class
        );


        assertTrue(response.getStatusCode().is4xxClientError() || response.getStatusCode().is5xxServerError());
        assertNotNull(response.getBody());
    }


    @Test
    void shouldGenerateValidEmailsForExample1_StringManipulation() {

        var request = new CurlGenerationRequest(
                "substring(firstName,1,1) & lastName & '@' & domain",
                Map.of(
                        "firstName", "Alice",
                        "lastName", "Smith", 
                        "domain", "example.com"
                )
        );


        ResponseEntity<CurlGenerationResponse> response = callGenerateCurl(request);


        assertTrue(response.getStatusCode().is2xxSuccessful());
        assertNotNull(response.getBody());
        
        CurlGenerationResponse body = response.getBody();
        assertNull(body.hint());
        assertNotNull(body.curlRequest());
        assertTrue(body.curlRequest().contains("substring%28firstName%2C1%2C1%29"));
    }


    @Test
    void shouldGenerateValidEmailsForExample2_ListOperations() {

        var request = new CurlGenerationRequest(
                "zip(split(aliases,','),'.',lastName,'@',domain)",
                Map.of(
                        "aliases", "jean,j,jeannot",
                        "lastName", "Mignard", 
                        "domain", "peoplespheres.io"
                )
        );


        ResponseEntity<CurlGenerationResponse> response = callGenerateCurl(request);


        assertTrue(response.getStatusCode().is2xxSuccessful());
        assertNotNull(response.getBody());
        
        CurlGenerationResponse body = response.getBody();
        assertNull(body.hint());
        assertNotNull(body.curlRequest());
    }


    @Test
    void shouldGenerateValidEmailsForExample3_ConditionalLogic() {


        var request = new CurlGenerationRequest(
                "firstName & (age < '30' ? '.young' : '.mature') & '@' & domain",
                Map.of(
                        "firstName", "John",
                        "age", "25",
                        "domain", "example.com"
                )
        );


        ResponseEntity<CurlGenerationResponse> response = callGenerateCurl(request);


        assertTrue(response.getStatusCode().is2xxSuccessful());
        assertNotNull(response.getBody());
        
        CurlGenerationResponse body = response.getBody();
        assertNull(body.hint());
        assertNotNull(body.curlRequest());
    }


    @Test
    void shouldGenerateValidResultForExample4_ArithmeticOperations() {


        var request = new CurlGenerationRequest(
                "base + bonus * multiplier - deduction / factor",
                Map.of(
                        "base", "100",
                        "bonus", "20",
                        "multiplier", "3",
                        "deduction", "40",
                        "factor", "4"
                )
        );


        ResponseEntity<CurlGenerationResponse> response = callGenerateCurl(request);


        assertTrue(response.getStatusCode().is2xxSuccessful());
        assertNotNull(response.getBody());
        
        CurlGenerationResponse body = response.getBody();
        assertNotNull(body.hint());
        assertTrue(body.hint().contains("invalid email address"));
        assertNotNull(body.curlRequest());
    }


    @Test
    void shouldGenerateValidEmailsForExample16_Fixed() {

        var request = new CurlGenerationRequest(
                "zip(cross_join(split(userIds,','), split(departments,';')), '.', substring(split(roles,','), 1, 3), (equals(substring(split(departments,';'), 1, 2), 'IT') ? (baseSalary + bonus > '100000' & experience >= '5' & equals(substring(certifications, 1, 3), 'AWS') ? '_senior_tech' : '_tech') : (experience > '10' ? '_senior_ops' : '_ops')), '@', domain)",
                Map.of(
                        "userIds", "1001,1002,1003",
                        "departments", "IT;HR;Finance",
                        "roles", "Developer,Manager,Analyst",
                        "baseSalary", "95000",
                        "bonus", "15000",
                        "experience", "7",
                        "certifications", "AWS,Docker,K8s",
                        "domain", "company.com"
                )
        );


        ResponseEntity<CurlGenerationResponse> response = callGenerateCurl(request);


        assertTrue(response.getStatusCode().is2xxSuccessful());
        assertNotNull(response.getBody());
        
        CurlGenerationResponse body = response.getBody();
        assertNull(body.hint());
        assertNotNull(body.curlRequest());
    }


    @Test
    void shouldGenerateValidEmailsForExample17_Fixed() {

        Map<String, Object> inputs = new HashMap<>();
        inputs.put("assetCodes", "AAPL,MSFT,GOOGL,TSLA");
        inputs.put("assetTypes", "Stock;Bond;ETF;Crypto");
        inputs.put("currentPrice", "180");
        inputs.put("purchasePrice", "150");
        inputs.put("weight", "25");
        inputs.put("totalWeight", "100");
        inputs.put("dividendYield", "1.5");
        inputs.put("riskThreshold", "15");
        inputs.put("sectors", "Tech,Healthcare,Energy,Finance");
        inputs.put("marketCap", "2500000000");
        inputs.put("peRatio", "22");
        inputs.put("debtRatio", "0.2");
        inputs.put("domain", "portfolio.com");
        
        var request = new CurlGenerationRequest(
                "zip(split(assetCodes,','), '.', substring(split(assetTypes,';'), 1, 4), (((currentPrice - purchasePrice) / purchasePrice * 100 * weight / totalWeight + dividendYield * 0.3) > riskThreshold ? (equals(substring(split(sectors,','), 1, 4), 'Tech') & marketCap > '1000000000' & peRatio < '25' & debtRatio < '0.4' ? '_premium_growth' : '_growth') : (currentPrice / purchasePrice > '0.9' ? '_stable' : '_risk')), '@', domain)",
                inputs
        );


        ResponseEntity<CurlGenerationResponse> response = callGenerateCurl(request);


        assertTrue(response.getStatusCode().is2xxSuccessful());
        assertNotNull(response.getBody());
        
        CurlGenerationResponse body = response.getBody();
        assertNull(body.hint());
        assertNotNull(body.curlRequest());
    }


    @Test
    void shouldDetectInvalidEmailsForExample22_EmptyValues() {


        var request = new CurlGenerationRequest(
                "zip(split(aliases,','), '.', middleName, '.', lastName)",
                Map.of(
                        "aliases", "test,,demo",
                        "middleName", "",
                        "lastName", "User"
                )
        );


        ResponseEntity<CurlGenerationResponse> response = callGenerateCurl(request);


        assertTrue(response.getStatusCode().is2xxSuccessful());
        assertNotNull(response.getBody());
        
        CurlGenerationResponse body = response.getBody();
        assertNotNull(body.hint());
        assertTrue(body.hint().contains("invalid email address"));
        assertNotNull(body.curlRequest());
    }

    private ResponseEntity<CurlGenerationResponse> callGenerateCurl(CurlGenerationRequest request) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<CurlGenerationRequest> httpEntity = new HttpEntity<>(request, headers);
        
        return restTemplate.postForEntity(
                "/generate-curl", 
                httpEntity, 
                CurlGenerationResponse.class
        );
    }
}
