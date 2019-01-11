package com.qs.rpc.request;

import com.qs.rpc.core.RPC;
import com.qs.rpc.response.RPCResponse;
import io.netty.channel.ChannelHandlerAdapter;
import io.netty.channel.ChannelHandlerContext;

import java.util.Map;

public class RPCRequestHandler extends ChannelHandlerAdapter {

    private static ChannelHandlerContext chlContext;

    public static ChannelHandlerContext getChlContext() {
        return chlContext;
    }

    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        chlContext = ctx;
        RPCRequestNet.getConnectLock().lock();
        RPCRequestNet.getCondition().signalAll();
        RPCRequestNet.getConnectLock().unlock();
    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        System.out.println("请求端接收到实现端响应咯。。");
        String responseJson = (String) msg;
        RPCResponse response = RPC.responseDecode(responseJson);
        Map<String, RPCRequest> requestLockMap = RPCRequestNet.getRequestLockMap();
        //将每个请求对象request作为锁
        RPCRequest request = requestLockMap.get(response.getRequestId());
        System.out.println("获取到服务实现端响应，响应内容：" + responseJson);
        synchronized (request) {
            request.setResult(response.getResult());
            System.out.println("一次请求结束，requestId=" + response.getRequestId());
            request.notifyAll();
        }
    }
}
