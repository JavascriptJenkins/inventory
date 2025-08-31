// DocsService.java (update/extend)
package com.techvvs.inventory.metrcdocs;


import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.techvvs.inventory.claude.ClaudeClient;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.regex.*;

@Service
public class DocsService {
    private final MetrcMcpClient mcp;
    private final ClaudeClient claude;
    private final String fallbackQuery;
    private final ObjectMapper om = new ObjectMapper();

    private static final Pattern SOURCE = Pattern.compile("\\*\\*Source:\\*\\* \\[(?<url>https?://[^\\] \\t\\n\\r]+)");
    private static final Pattern STATE  = Pattern.compile("\\*\\*Packages\\*\\* \\((?<st>[A-Z]{2})\\)|https?://api-(?<st2>[a-z]{2})\\.metrc\\.com");
    private static final Pattern ENDPT  = Pattern.compile("###\\s*(GET|POST|PUT|DELETE)\\s+(?<path>\\S+)");


    public DocsService(MetrcMcpClient mcp, ClaudeClient claude,
                       @Value("${metrc.mcp.fallbackQuery:packages}") String fallbackQuery) {
        this.mcp = mcp; this.claude = claude; this.fallbackQuery = fallbackQuery;
    }
    // 1) Raw joined markdown
// DocsService.java
    public String searchPackagesRaw(String q) {
        return mcp.callSearchDocsRaw(q);
    }


    // 2) Structured parse from markdown
    private static final Pattern BLOCK = Pattern.compile(
            "\\*\\*Packages\\*\\* \\((?<state>[A-Z]{2})\\)[\\s\\S]*?###\\s*Server:\\s*(?<server>\\S+)[\\s\\S]*?###\\s*(?<method>GET|POST|PUT|DELETE)\\s+(?<path>\\S+)[\\s\\S]*?(?:\\*\\*Source:\\*\\* \\[(?<source>[^\\]]+)])?",
            Pattern.MULTILINE
    );

    public Map<String,Object> searchPackagesStructured(String query) {
        String markdown = searchPackagesRaw(query);
        List<MetrcPackageDoc> items = new ArrayList<>();
        Matcher m = BLOCK.matcher(markdown);
        while (m.find()) {
            items.add(new MetrcPackageDoc(
                    m.group("state"),
                    m.group("server"),
                    m.group("method"),
                    m.group("path"),
                    m.group("source")
            ));
        }
        return Map.of("count", items.size(), "items", items);
    }

    // Reuse your existing raw call to MCP
    private String getMarkdownFromMcp(String query) throws Exception {
        String body = mcp.callSearchDocsRaw(query);
        JsonNode root = om.readTree(body);
        JsonNode content = root.path("result").path("content");

        StringBuilder md = new StringBuilder();
        if (content.isArray()) {
            for (JsonNode item : content) {
                if ("text".equalsIgnoreCase(item.path("type").asText())) {
                    if (md.length() > 0) md.append("\n\n---\n\n");
                    md.append(item.path("text").asText(""));
                }
            }
        } else if (content.isObject() && "text".equalsIgnoreCase(content.path("type").asText())) {
            md.append(content.path("text").asText(""));
        }
        return md.toString();
    }

    public Map<String, Object> searchPackagesAI(String userQuery) throws Exception {
        String markdown = getMarkdownFromMcp(userQuery); // we pass the same query through MCP
        String answer = claude.summarize(userQuery, markdown);
        return Map.of(
                "query", userQuery,
                "answer", answer
        );
    }

    public Map<String,Object> askWithConnectorAndClaude(String userQuestion) throws Exception {
        // 1) Retrieve exclusively via the MCP connector
        String markdown = mcp.searchDocsMarkdown(userQuestion);
        if (markdown.isBlank() && fallbackQuery != null && !fallbackQuery.isBlank()) {
            markdown = mcp.searchDocsMarkdown(fallbackQuery); // still via connector
        }
        if (markdown.isBlank()) {
            return Map.of(
                    "query", userQuestion,
                    "answer", "I couldn’t retrieve any METRC snippets from the connector for this question.",
                    "states", List.of(),
                    "endpoints", List.of(),
                    "sources", List.of()
            );
        }

        // 2) Extract sources/states/endpoints for citations & UI
        Set<String> sources = new LinkedHashSet<>();
        Matcher ms = SOURCE.matcher(markdown);
        while (ms.find()) sources.add(ms.group("url"));

        Set<String> states = new TreeSet<>();
        Matcher st = STATE.matcher(markdown);
        while (st.find()) {
            String s = st.group("st");
            if (s == null) s = Optional.ofNullable(st.group("st2")).map(String::toUpperCase).orElse(null);
            if (s != null) states.add(s);
        }

        Set<String> endpoints = new LinkedHashSet<>();
        Matcher me = ENDPT.matcher(markdown);
        while (me.find()) endpoints.add(me.group(0).replace("###", "").trim()); // e.g., "PUT /packages/v2/unfinish"

        // 3) Ask Claude with strict grounding to the connector content
        String system = "You are a compliance assistant. Use ONLY the provided METRC connector snippets. " +
                "Do NOT use outside knowledge. If the snippets don't contain the answer, say so. " +
                "Cite the provided Source URLs (if any). Keep answers concise.";

        String user = "User question:\n" + userQuestion + "\n\n" +
                "Source (markdown from METRC connector):\n" + markdown + "\n\n" +
                "If answering, include a short bullet list of relevant endpoints and cite the matching Source URLs.";

        String answer = claude.answer(system, user);

        return Map.of(
                "query", userQuestion,
                "answer", answer,
                "states", new ArrayList<>(states),
                "endpoints", new ArrayList<>(endpoints),
                "sources", new ArrayList<>(sources) // citations all come from connector
        );
    }

}
