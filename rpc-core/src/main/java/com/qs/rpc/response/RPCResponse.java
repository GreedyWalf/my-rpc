package com.qs.rpc.response;

public class RPCResponse {

    //每个请求对应唯一的id
    private String requestId;

    private Object result;

    public String getRequestId() {
        return requestId;
    }

    public void setRequestId(String requestId) {
        this.requestId = requestId;
    }

    public void setResult(Object result) {
        this.result = result;
    }

    public Object getResult() {
        return result;
    }
}
