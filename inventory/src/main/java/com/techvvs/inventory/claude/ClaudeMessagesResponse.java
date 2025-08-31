// ClaudeMessagesResponse.java
package com.techvvs.inventory.claude;

import java.util.List;

public class ClaudeMessagesResponse {
    private String id;
    private String model;
    private String role;
    private List<ContentBlock> content; // [{type:"text", text:"..."}]
    private String stop_reason;

    public static class ContentBlock {
        private String type; // "text"
        private String text;
        public String getType() { return type; }
        public void setType(String type) { this.type = type; }
        public String getText() { return text; }
        public void setText(String text) { this.text = text; }
    }

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    public String getModel() { return model; }
    public void setModel(String model) { this.model = model; }
    public String getRole() { return role; }
    public void setRole(String role) { this.role = role; }
    public List<ContentBlock> getContent() { return content; }
    public void setContent(List<ContentBlock> content) { this.content = content; }
    public String getStop_reason() { return stop_reason; }
    public void setStop_reason(String stop_reason) { this.stop_reason = stop_reason; }
}
