// DocsService.java (update/extend)
package com.techvvs.inventory.metrcdocs;


import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.techvvs.inventory.claude.AnthropicOverloadedException;
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
                       @org.springframework.beans.factory.annotation.Value("${metrc.mcp.fallbackQuery:packages}") String fallbackQuery) {
        this.mcp = mcp;
        this.claude = claude;
        this.fallbackQuery = fallbackQuery;
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

    public Map<String, Object> askWithConnectorAndClaude(String userQuestion) throws Exception {
        String markdown = retrieveMarkdown(userQuestion);
        if (markdown.isBlank()) {
            return Map.of("query", userQuestion, "answer", "No snippets found in connector.", "sources", List.of());
        }

        var urls = new ArrayList<>(MetrcSourceExtractor.extractSourceUrls(markdown));
        var states = new ArrayList<>(MetrcSourceExtractor.extractStates(markdown));
        var endpoints = extractEndpoints(markdown);

        String system = "Use ONLY the provided METRC snippets. If insufficient, reply INSUFFICIENT_CONTEXT. Be concise; cite endpoints & sources.";
        String user    = "Question: " + userQuestion + "\n\nSnippets:\n" + shrink(markdown, 12000) + "\n\nSources:\n" + String.join("\n", urls);

        String answer;
        try {
            answer = claude.answer(system, user);
        } catch (AnthropicOverloadedException ex) {
            // deterministic fallback: return what we can prove from connector
            return Map.of(
                    "query", userQuestion,
                    "answer", "Service is temporarily overloaded. Returning connector-derived facts.",
                    "states", states,
                    "endpoints", endpoints,
                    "sources", urls
            );
        }

        return Map.of("query", userQuestion, "answer", answer, "states", states, "endpoints", endpoints, "sources", urls);

    }

    private static List<String> extractEndpoints(String md) {
        var p = java.util.regex.Pattern.compile("###\\s*(GET|POST|PUT|DELETE)\\s+(\\S+)");
        var m = p.matcher(md);
        var out = new java.util.LinkedList<String>();
        while (m.find()) out.add((m.group(1) + " " + m.group(2)).trim());
        return out;
    }

    // keep the prompt under control
    private static String shrink(String s, int maxChars) {
        if (s == null || s.length() <= maxChars) return s == null ? "" : s;
        return s.substring(0, maxChars) + "\n\n[...truncated for brevity...]";
    }

    // DocsService.java  (inside class)
    private String retrieveMarkdown(String question) throws Exception {
        // 1) try the user's question
        String q1 = sanitizeQuery(question);
        String md = mcp.searchDocsMarkdown(q1);
        if (!md.isBlank()) return md;

        // 2) try the configured fallback (property: metrc.mcp.fallbackQuery)
        String fb = sanitizeQuery(fallbackQuery); // injected in the constructor earlier
        if (!fb.isBlank() && !fb.equalsIgnoreCase(q1)) {
            md = mcp.searchDocsMarkdown(fb);
            if (!md.isBlank()) return md;
        }

        // 3) last-ditch built-ins (still via the connector)
        for (String alt : new String[]{"packages", "transfers", "sales"}) {
            if (!alt.equalsIgnoreCase(q1) && !alt.equalsIgnoreCase(fb)) {
                md = mcp.searchDocsMarkdown(alt);
                if (!md.isBlank()) return md;
            }
        }
        return "";
    }

    private static String sanitizeQuery(String q) {
        if (q == null) return "";
        // strip inline comments and collapse whitespace (prevents FTS5 errors on '#')
        String cleaned = q.replace("\r", " ").replace("\n", " ").trim();
        int hash = cleaned.indexOf('#');
        if (hash >= 0) cleaned = cleaned.substring(0, hash).trim();
        return cleaned.replaceAll("\\s{2,}", " ");
    }

}
