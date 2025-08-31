// ClaudeMessagesRequest.java
package com.techvvs.inventory.claude;

import java.util.List;

public class ClaudeMessagesRequest {
    private String model;
    private int max_tokens;
    private Double temperature; // optional
    private String system;      // optional
    private List<Message> messages;

    public static class Message {
        private String role; // "user"
        private List<ContentBlock> content;

        public String getRole() { return role; }
        public void setRole(String role) { this.role = role; }
        public List<ContentBlock> getContent() { return content; }
        public void setContent(List<ContentBlock> content) { this.content = content; }
    }

    public static class ContentBlock {
        private String type = "text";
        private String text;

        public ContentBlock() {}
        public ContentBlock(String text) { this.text = text; }
        public String getType() { return type; }
        public void setType(String type) { this.type = type; }
        public String getText() { return text; }
        public void setText(String text) { this.text = text; }
    }

    public String getModel() { return model; }
    public void setModel(String model) { this.model = model; }
    public int getMax_tokens() { return max_tokens; }
    public void setMax_tokens(int max_tokens) { this.max_tokens = max_tokens; }
    public Double getTemperature() { return temperature; }
    public void setTemperature(Double temperature) { this.temperature = temperature; }
    public String getSystem() { return system; }
    public void setSystem(String system) { this.system = system; }
    public List<Message> getMessages() { return messages; }
    public void setMessages(List<Message> messages) { this.messages = messages; }
}
