// ClaudeClient.java
package com.techvvs.inventory.claude;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.*;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.util.List;

@Component
public class ClaudeClient {

    private final RestTemplate rt;
    private final String apiBase;
    private final String apiKey;
    private final String version;
    private final String model;
    private final int maxTokens;
    private final double temperature;

    public ClaudeClient(
        RestTemplate anthropicRestTemplate,
        @Value("${anthropic.apiBase}") String apiBase,
        @Value("${anthropic.apiKey}") String apiKey,
        @Value("${anthropic.version}") String version,
        @Value("${anthropic.model}") String model,
        @Value("${anthropic.maxTokens:800}") int maxTokens,
        @Value("${anthropic.temperature:0.2}") double temperature
    ) {
        this.rt = anthropicRestTemplate;
        this.apiBase = apiBase;
        this.apiKey = apiKey;
        this.version = version;
        this.model = model;
        this.maxTokens = maxTokens;
        this.temperature = temperature;
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
        var req = new ClaudeMessagesRequest();
        req.setModel(model);
        req.setMax_tokens(maxTokens);
        req.setTemperature(temperature);
        req.setSystem(systemPrompt);

        var msg = new ClaudeMessagesRequest.Message();
        msg.setRole("user");
        msg.setContent(List.of(new ClaudeMessagesRequest.ContentBlock(userText)));
        req.setMessages(List.of(msg));

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.set("x-api-key", apiKey);
        headers.set("anthropic-version", version);

        var entity = new HttpEntity<>(req, headers);
        var typeRef = new ParameterizedTypeReference<ClaudeMessagesResponse>() {};
        var resp = rt.exchange(apiBase + "/v1/messages", HttpMethod.POST, entity, typeRef);

        var body = resp.getBody();
        if (body == null || body.getContent() == null || body.getContent().isEmpty())
            throw new RuntimeException("Empty Claude response");

        var sb = new StringBuilder();
        for (var c : body.getContent()) {
            if ("text".equalsIgnoreCase(c.getType()) && c.getText() != null) sb.append(c.getText());
        }
        return sb.toString().trim();
    }

}
