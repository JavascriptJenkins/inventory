// ToolCallResult.java
package com.techvvs.inventory.metrcdocs;

import com.fasterxml.jackson.annotation.JsonFormat;
import java.util.List;

public class ToolCallResult {

    // The server sometimes returns a single object instead of array.
    // Tell Jackson to accept both.
    @JsonFormat(with = JsonFormat.Feature.ACCEPT_SINGLE_VALUE_AS_ARRAY)
    private List<ContentItem> content;

    public List<ContentItem> getContent() { return content; }
    public void setContent(List<ContentItem> content) { this.content = content; }

    public static class ContentItem {
        private String type; // "text"
        private String text; // markdown blob
        public String getType() { return type; }
        public void setType(String type) { this.type = type; }
        public String getText() { return text; }
        public void setText(String text) { this.text = text; }
    }
}
