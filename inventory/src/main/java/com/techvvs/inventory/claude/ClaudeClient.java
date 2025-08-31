// ClaudeClient.java
package com.techvvs.inventory.claude;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.*;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClientResponseException;
import org.springframework.web.client.RestTemplate;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

@Component
public class ClaudeClient {

    private final RestTemplate rt;
    private final String apiBase;
    private final String apiKey;
    private final String version;
    private final String model;
    private final int maxTokens;
    private final double temperature;

    // Retry config
    private final int maxAttempts;
    private final long baseBackoffMs;
    private final long maxBackoffMs;
    private final long jitterMs;
    private final List<Integer> tokenFallbacks;

    public ClaudeClient(
            RestTemplate anthropicRestTemplate,
            @Value("${anthropic.apiBase}") String apiBase,
            @Value("${anthropic.apiKey}") String apiKey,
            @Value("${anthropic.version}") String version,
            @Value("${anthropic.model}") String model,
            @Value("${anthropic.maxTokens:800}") int maxTokens,
            @Value("${anthropic.temperature:0.2}") double temperature,
            @Value("${anthropic.retry.maxAttempts:4}") int maxAttempts,
            @Value("${anthropic.retry.baseBackoffMillis:400}") long baseBackoffMs,
            @Value("${anthropic.retry.maxBackoffMillis:4000}") long maxBackoffMs,
            @Value("${anthropic.retry.jitterMillis:250}") long jitterMs,
            @Value("${anthropic.retry.maxTokensFallbacks:512,256}") String tokenFallbacksCsv, List<Integer> tokenFallbacks

    ) {
        this.rt = anthropicRestTemplate;
        this.apiBase = apiBase;
        this.apiKey = apiKey;
        this.version = version;
        this.model = model;
        this.maxTokens = maxTokens;
        this.temperature = temperature;

        this.maxAttempts = Math.max(1, maxAttempts);
        this.baseBackoffMs = baseBackoffMs;
        this.maxBackoffMs = maxBackoffMs;
        this.jitterMs = jitterMs;
        this.tokenFallbacks = tokenFallbacks;
    }

    public String summarize(String userQuery, String markdown) {
        // 1) Build request
        ClaudeMessagesRequest req = new ClaudeMessagesRequest();
        req.setModel(model);
        req.setMax_tokens(maxTokens);
        req.setTemperature(temperature);
        req.setSystem("You are a compliance assistant. Read METRC API documentation snippets and answer the user's question clearly. " +
                      "Cite specific endpoints (method + path) and any state-specific differences when relevant. " +
                      "If something is unknown, say so explicitly. Keep answers concise.");

        ClaudeMessagesRequest.Message m = new ClaudeMessagesRequest.Message();
        m.setRole("user");
        m.setContent(List.of(new ClaudeMessagesRequest.ContentBlock(
            "User question:\n" + userQuery + "\n\n" +
            "Source (METRC tool output; markdown):\n" + markdown + "\n\n" +
            "Task: Answer the question strictly from the source. If multiple states differ, summarize the differences. " +
            "Include a short list of the most relevant endpoints involved."
        )));
        req.setMessages(List.of(m));

        // 2) Headers (Anthropic Messages API)
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.set("x-api-key", apiKey);
        headers.set("anthropic-version", version); // required per docs
        // headers.set("anthropic-beta", "prompt-caching-2024-07-31"); // optional

        HttpEntity<ClaudeMessagesRequest> entity = new HttpEntity<>(req, headers);

        // 3) Call API
        ParameterizedTypeReference<ClaudeMessagesResponse> typeRef = new ParameterizedTypeReference<>() {};
        ResponseEntity<ClaudeMessagesResponse> resp = rt.exchange(
            apiBase + "/v1/messages", HttpMethod.POST, entity, typeRef);

        ClaudeMessagesResponse body = resp.getBody();
        if (body == null || body.getContent() == null || body.getContent().isEmpty()) {
            throw new RuntimeException("Empty Claude response");
        }

        // 4) Join all text blocks
        StringBuilder out = new StringBuilder();
        for (ClaudeMessagesResponse.ContentBlock cb : body.getContent()) {
            if ("text".equalsIgnoreCase(cb.getType()) && cb.getText() != null) {
                out.append(cb.getText());
            }
        }
        return out.toString().trim();
    }

    public String answer(String systemPrompt, String userText) {
        int attempt = 0;
        List<Integer> tokenPlan = new ArrayList<>();
        tokenPlan.add(maxTokens);
        tokenPlan.addAll(tokenFallbacks); // e.g., 800 -> 512 -> 256

        while (true) {
            attempt++;
            int tokens = tokenPlan.get(Math.min(attempt - 1, tokenPlan.size() - 1));

            try {
                String out = callOnce(systemPrompt, userText, tokens);
                return out;
            } catch (RestClientResponseException e) {
                int code = e.getRawStatusCode();
                if (code == 529 || code == 429 || code == 503) {
                    if (attempt >= maxAttempts) {
                        throw new AnthropicOverloadedException(
                                "Anthropic overloaded (status " + code + ") after " + attempt + " attempt(s).");
                    }
                    sleepBackoff(attempt, e.getResponseHeaders());
                    continue;
                }
                throw e; // other errors bubble up
            } catch (Exception e) {
                // network hiccup: retry
                if (attempt >= maxAttempts) throw new AnthropicOverloadedException(
                        "Anthropic request failed after " + attempt + " attempt(s): " + e.getMessage());
                sleepBackoff(attempt, null);
            }
        }
    }


    private String callOnce(String systemPrompt, String userText, int maxTokensThisAttempt) {
        ClaudeMessagesRequest req = new ClaudeMessagesRequest();
        req.setModel(model);
        req.setMax_tokens(maxTokensThisAttempt);
        req.setTemperature(temperature);
        req.setSystem(systemPrompt);

        ClaudeMessagesRequest.Message m = new ClaudeMessagesRequest.Message();
        m.setRole("user");
        m.setContent(List.of(new ClaudeMessagesRequest.ContentBlock(userText)));
        req.setMessages(List.of(m));

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.set("x-api-key", apiKey);
        headers.set("anthropic-version", version);

        ResponseEntity<ClaudeMessagesResponse> resp = rt.exchange(
                apiBase + "/v1/messages", HttpMethod.POST, new HttpEntity<>(req, headers),
                new ParameterizedTypeReference<ClaudeMessagesResponse>() {}
        );

        ClaudeMessagesResponse body = resp.getBody();
        if (body == null || body.getContent() == null || body.getContent().isEmpty())
            throw new RuntimeException("Empty Claude response");

        StringBuilder out = new StringBuilder();
        for (ClaudeMessagesResponse.ContentBlock cb : body.getContent()) {
            if ("text".equalsIgnoreCase(cb.getType()) && cb.getText() != null) out.append(cb.getText());
        }
        return out.toString().trim();
    }


    private void sleepBackoff(int attempt, HttpHeaders hdrs) {
        long retryAfterMs = 0L;
        if (hdrs != null) {
            String ra = hdrs.getFirst("retry-after");
            if (ra != null) {
                try {
                    // numeric seconds only; date format omitted for brevity
                    retryAfterMs = Long.parseLong(ra.trim()) * 1000L;
                } catch (NumberFormatException ignored) {}
            }
        }
        long exp = Math.min(maxBackoffMs, (long) (baseBackoffMs * Math.pow(2, attempt - 1)));
        long jitter = ThreadLocalRandom.current().nextLong(0, Math.max(1, jitterMs));
        long wait = Math.max(retryAfterMs, exp + jitter);
        try { Thread.sleep(wait); } catch (InterruptedException ignored) { Thread.currentThread().interrupt(); }
    }





}
