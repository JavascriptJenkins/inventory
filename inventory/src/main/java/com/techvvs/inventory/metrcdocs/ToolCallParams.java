// ToolCallParams.java
package com.techvvs.inventory.metrcdocs;

import java.util.Map;

public class ToolCallParams {
    private String name;
    private Map<String, Object> arguments;

    public ToolCallParams() {}
    public ToolCallParams(String name, Map<String, Object> arguments) {
        this.name = name; this.arguments = arguments;
    }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public Map<String, Object> getArguments() { return arguments; }
    public void setArguments(Map<String, Object> arguments) { this.arguments = arguments; }
}
