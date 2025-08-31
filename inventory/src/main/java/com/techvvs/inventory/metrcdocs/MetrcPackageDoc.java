// MetrcPackageDoc.java
package com.techvvs.inventory.metrcdocs;

public class MetrcPackageDoc {
    private String state;
    private String server;
    private String method;
    private String path;
    private String source;

    public MetrcPackageDoc() {}
    public MetrcPackageDoc(String state, String server, String method, String path, String source) {
        this.state = state; this.server = server; this.method = method; this.path = path; this.source = source;
    }
    public String getState() { return state; }
    public void setState(String state) { this.state = state; }
    public String getServer() { return server; }
    public void setServer(String server) { this.server = server; }
    public String getMethod() { return method; }
    public void setMethod(String method) { this.method = method; }
    public String getPath() { return path; }
    public void setPath(String path) { this.path = path; }
    public String getSource() { return source; }
    public void setSource(String source) { this.source = source; }
}

