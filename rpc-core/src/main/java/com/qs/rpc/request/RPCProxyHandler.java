package com.qs.rpc.request;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.atomic.AtomicLong;

/**
 * 代理类，获取远程调用service的类名、方法名、参数信息
 */
public class RPCProxyHandler implements InvocationHandler {

    private static AtomicLong requestTimes = new AtomicLong(0);

    @Override
    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
        String methodName = method.getName();
        System.out.println("代理类执行了：" + methodName);
        if ("toString".equals(methodName) || "equals".equals(methodName) || "hashCode".equals(methodName)) {
            return null;
        }

        //代理，获取接口请求方法名、参数类型、参数信息，封装为RPCRequest对象，发送给服务端netty，获取实现类的该方法请求结果
        RPCRequest request = new RPCRequest();
        request.setRequestId(getRequestId(method.getName()));
        request.setClassName(method.getDeclaringClass().getName());
        request.setMethodName(method.getName());
        request.setParameters(args);

        //请求端向实现端发起请求
        Map<String, RPCRequest> requestLockMap = RPCRequestNet.getRequestLockMap();
        requestLockMap.put(request.getRequestId(), request);
        RPCRequestNet.connect().send(request);
        requestLockMap.remove(request.getRequestId());

        return request.getResult();
    }

    private String getRequestId(String methodName) {
        StringBuilder sb = new StringBuilder();
        sb.append(requestTimes.incrementAndGet());
        sb.append(System.currentTimeMillis());
        sb.append(methodName);
        Random random = new Random();
        sb.append(random.nextInt(1000));
        return sb.toString();
    }
}
