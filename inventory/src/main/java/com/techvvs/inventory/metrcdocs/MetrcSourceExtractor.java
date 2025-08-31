package com.techvvs.inventory.metrcdocs;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Helpers to pull citations & hints from the MCP markdown.
 * - Source URLs (from "**Source:** [https://...]")
 * - State codes (from headings like "**Packages** (CA)" or api-ca.metrc.com)
 * - Endpoints (from lines like "### PUT /packages/v2/unfinish")
 */
public final class MetrcSourceExtractor {

    private MetrcSourceExtractor() {}

    // e.g., "**Source:** [https://api-ca.metrc.com/Document...]"
    private static final Pattern SOURCE_URL =
            Pattern.compile("\\*\\*Source:\\*\\* \\[(?<url>https?://[^\\]\\s]+)", Pattern.CASE_INSENSITIVE);

    // e.g., "**Packages** (CA)" OR "https://api-ca.metrc.com/"
    private static final Pattern STATE_HINT =
            Pattern.compile("\\*\\*Packages\\*\\* \\((?<st>[A-Z]{2})\\)|https?://api-(?<st2>[a-z]{2})\\.metrc\\.com",
                    Pattern.CASE_INSENSITIVE);

    // e.g., "### PUT /packages/v2/unfinish"
    private static final Pattern ENDPOINT =
            Pattern.compile("###\\s*(GET|POST|PUT|DELETE)\\s+(?<path>\\S+)", Pattern.CASE_INSENSITIVE);

    public static List<String> extractSourceUrls(String markdown) {
        if (markdown == null || markdown.isBlank()) return List.of();
        Set<String> urls = new LinkedHashSet<>();
        Matcher m = SOURCE_URL.matcher(markdown);
        while (m.find()) {
            urls.add(m.group("url"));
        }
        return new ArrayList<>(urls);
    }

    public static List<String> extractStates(String markdown) {
        if (markdown == null || markdown.isBlank()) return List.of();
        Set<String> states = new TreeSet<>();
        Matcher m = STATE_HINT.matcher(markdown);
        while (m.find()) {
            String st = m.group("st");
            if (st == null) {
                String st2 = m.group("st2");
                if (st2 != null) st = st2.toUpperCase(Locale.ROOT);
            }
            if (st != null && st.length() == 2) {
                states.add(st);
            }
        }
        return new ArrayList<>(states);
    }

    public static List<String> extractEndpoints(String markdown) {
        if (markdown == null || markdown.isBlank()) return List.of();
        Set<String> eps = new LinkedHashSet<>();
        Matcher m = ENDPOINT.matcher(markdown);
        while (m.find()) {
            String method = m.group(1).toUpperCase(Locale.ROOT);
            String path = m.group("path");
            eps.add(method + " " + path);
        }
        return new ArrayList<>(eps);
    }
}
