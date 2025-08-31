// JsonRpcRequest.java
package com.techvvs.inventory.metrcdocs;

public class JsonRpcRequest<T> {
    private String jsonrpc;
    private Object id;
    private String method;
    private T params;

    public JsonRpcRequest() {}
    public JsonRpcRequest(String jsonrpc, Object id, String method, T params) {
        this.jsonrpc = jsonrpc; this.id = id; this.method = method; this.params = params;
    }
    public String getJsonrpc() { return jsonrpc; }
    public void setJsonrpc(String jsonrpc) { this.jsonrpc = jsonrpc; }
    public Object getId() { return id; }
    public void setId(Object id) { this.id = id; }
    public String getMethod() { return method; }
    public void setMethod(String method) { this.method = method; }
    public T getParams() { return params; }
    public void setParams(T params) { this.params = params; }
}
