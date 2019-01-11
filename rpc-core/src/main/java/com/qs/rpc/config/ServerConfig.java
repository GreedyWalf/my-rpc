package com.qs.rpc.config;

import com.qs.rpc.core.RPC;
import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;

import java.util.Map;

/**
 * 配置实现端提供服务类，运行时实例bean会注入到spring容器中
 * （实现ApplicationContextAware接口目的是在将bean加载到spring容器中，会保存applicationContext对象）
 */
public class ServerConfig implements ApplicationContextAware {

    private String ip;

    //监听端口
    private int port;

    //开放暴露的serviceImpl类
    private Map<String, String> serverImplMap;

    public int getPort() {
        return port;
    }

    public void setPort(int port) {
        this.port = port;
    }

    public Map<String, String> getServerImplMap() {
        return serverImplMap;
    }

    public void setServerImplMap(Map<String, String> serverImplMap) {
        this.serverImplMap = serverImplMap;
    }

    public String getIp() {
        return ip;
    }

    public void setIp(String ip) {
        this.ip = ip;
    }

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        RPC.serverContext = applicationContext;
    }
}
