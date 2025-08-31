// JsonRpcResponse.java
package com.techvvs.inventory.metrcdocs;

public class JsonRpcResponse<T> {
    private String jsonrpc;
    private Object id;
    private T result;
    private JsonRpcError error;

    public String getJsonrpc() { return jsonrpc; }
    public void setJsonrpc(String jsonrpc) { this.jsonrpc = jsonrpc; }
    public Object getId() { return id; }
    public void setId(Object id) { this.id = id; }
    public T getResult() { return result; }
    public void setResult(T result) { this.result = result; }
    public JsonRpcError getError() { return error; }
    public void setError(JsonRpcError error) { this.error = error; }
}
