package com.qs.rpc.core;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.qs.rpc.config.ServerConfig;
import com.qs.rpc.request.RPCProxyHandler;
import com.qs.rpc.request.RPCRequest;
import com.qs.rpc.response.RPCResponse;
import com.qs.rpc.response.RPCResponseNet;
import org.springframework.context.ApplicationContext;

import java.io.IOException;
import java.lang.reflect.Proxy;

public class RPC {

    private static ObjectMapper objectMapper = new ObjectMapper();

    public static ApplicationContext serverContext;

    public static Object call(Class clazz) {
        RPCProxyHandler handler = new RPCProxyHandler();
        return Proxy.newProxyInstance(clazz.getClassLoader(), new Class[]{clazz}, handler);
    }

    public static void start() {
        System.out.println("正在开启rpc服务。。");
        RPCResponseNet.connect();
    }


    public static ServerConfig getServerConfig() {
        return (ServerConfig) serverContext.getBean("serverConfig");
    }


    /**
     * 将request请求对象转换为json串（注意使用换行符分包）
     */
    public static String requestEncode(RPCRequest request) throws JsonProcessingException {
        return objectMapper.writeValueAsString(request) + System.getProperty("line.separator");
    }

    public static RPCRequest requestDecode(String requestJson) throws IOException {
        return objectMapper.readValue(requestJson, RPCRequest.class);
    }

    public static String responseEncode(RPCResponse response) throws JsonProcessingException {
        return objectMapper.writeValueAsString(response) + System.getProperty("line.separator");
    }

    public static RPCResponse responseDecode(String responseJson) throws IOException {
        return objectMapper.readValue(responseJson, RPCResponse.class);
    }
}
