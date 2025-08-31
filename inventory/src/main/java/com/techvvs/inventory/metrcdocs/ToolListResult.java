// ToolListResult.java
package com.techvvs.inventory.metrcdocs;

import java.util.List;
import java.util.Map;

public class ToolListResult {
    private List<McpTool> tools;
    public List<McpTool> getTools() { return tools; }
    public void setTools(List<McpTool> tools) { this.tools = tools; }

    public static class McpTool {
        private String name;
        private String description;
        private Map<String, Object> inputSchema;
        public String getName() { return name; }
        public void setName(String name) { this.name = name; }
        public String getDescription() { return description; }
        public void setDescription(String description) { this.description = description; }
        public Map<String, Object> getInputSchema() { return inputSchema; }
        public void setInputSchema(Map<String, Object> inputSchema) { this.inputSchema = inputSchema; }
    }
}
