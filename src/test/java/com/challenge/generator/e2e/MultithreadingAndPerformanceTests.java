package com.challenge.generator.e2e;

import com.challenge.generator.expression.api.dto.EmailData;
import com.challenge.generator.expression.api.dto.EmailListResponse;
import com.challenge.generator.base.BaseTest;
import org.junit.jupiter.api.Test;
import org.springframework.http.ResponseEntity;

import java.net.URI;
import java.time.Duration;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.*;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import static org.junit.jupiter.api.Assertions.*;

class MultithreadingAndPerformanceTests extends BaseTest {

    @Test
    void concurrentGenerateRequests_shouldBeThreadSafe() {
        int threads = 16;
        try (BaseTest.AutoCloseableExecutor pool = createFixedThreadPool(threads)) {
            ExecutorService exec = pool.executor();
            CountDownLatch start = new CountDownLatch(1);

            List<CompletableFuture<String>> futures = IntStream.range(0, threads)
                    .mapToObj(i -> CompletableFuture.supplyAsync(() -> {
                        try {
                            start.await(5, TimeUnit.SECONDS);
                        } catch (InterruptedException e) {
                            throw new RuntimeException(e);
                        }
                        String expression = "firstName & '.' & lastName & '@example.com'";
                        URI uri = buildUri(expression, Map.of("firstName", "user" + i, "lastName", "smith" + i));
                        EmailListResponse body = getOk(uri);
                        assertNotNull(body);
                        List<EmailData> data = body.data();
                        assertNotNull(data);
                        assertEquals(1, data.size());
                        return data.getFirst().value();
                    }, exec))
                    .toList();

            start.countDown();

            List<String> results = futures.stream()
                    .map(f -> {
                        try {
                            return f.get(20, TimeUnit.SECONDS);
                        } catch (Exception e) {
                            throw new RuntimeException(e);
                        }
                    })
                    .toList();

            for (int i = 0; i < threads; i++) {
                assertEquals("user" + i + ".smith" + i + "@example.com", results.get(i));
            }
            assertEquals(threads, Set.copyOf(results).size());
        }
    }

    @Test
    void performance_heavyCrossJoin_shouldCompleteWithinTime() {
        String listA = IntStream.range(0, 50).mapToObj(i -> "a" + i).collect(Collectors.joining(","));
        String listB = IntStream.range(0, 50).mapToObj(i -> "b" + i).collect(Collectors.joining(","));
        String listC = IntStream.range(0, 50).mapToObj(i -> "c" + i).collect(Collectors.joining(","));
        String expression = "cross_join(split(a,','),split(b,','),split(c,','))";

        URI uri = buildUri(expression, Map.of("a", listA, "b", listB, "c", listC));

        long start = System.nanoTime();
        ResponseEntity<EmailListResponse> response = assertTimeoutPreemptively(Duration.ofSeconds(20),
                () -> restTemplate.getForEntity(uri, EmailListResponse.class),
                "Cross join should finish within 20 seconds");
        long elapsedMs = (System.nanoTime() - start) / 1_000_000L;
        assertTrue(response.getStatusCode().is2xxSuccessful(), "Expected 2xx but was: " + response.getStatusCode());
        EmailListResponse body = response.getBody();
        assertNotNull(body);
        List<EmailData> data = body.data();
        assertNotNull(data);
        assertEquals(125_000, data.size());
        assertTrue(data.getFirst().value().startsWith("a0b0c0"));
        assertTrue(elapsedMs < 20_000, "Took too long: " + elapsedMs + "ms");
    }
}
