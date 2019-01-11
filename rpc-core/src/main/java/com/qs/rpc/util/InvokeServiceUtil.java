package com.qs.rpc.util;

import com.qs.rpc.core.RPC;
import com.qs.rpc.request.RPCRequest;

import java.lang.reflect.Method;
import java.util.Map;

public class InvokeServiceUtil {

    /**
     * 根据配置的接口类和实现类映射关系，执行实现类中实现的接口方法，返回结果
     */
    @SuppressWarnings({"unchecked", "rawtypes"})
    public static Object invoke(RPCRequest request) {
        Object result = null;
        //获取接口和实现类map
        Map<String, String> serverImplMap = RPC.getServerConfig().getServerImplMap();

        try {
            //获取调用接口的方法名、参数信息（获取参数包装类类型）
            String className = request.getClassName();
            Object[] parameters = request.getParameters();
            int parameterLength = parameters.length;
            Class[] parameterTypes = new Class[parameterLength];
            for (int i = 0; i < parameterLength; i++) {
                parameterTypes[i] = parameters[i].getClass();
            }

            //使用反射，根据接口方法名、参数，调用实际实现类方法
            Class implClazz = Class.forName(serverImplMap.get(className));
            Method method = implClazz.getDeclaredMethod(request.getMethodName(), parameterTypes);
            Object implObj = implClazz.newInstance();
            result = method.invoke(implObj, parameters);
        } catch (Exception e) {
            e.printStackTrace();
        }

        return result;
    }
}
