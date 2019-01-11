package com.qs.rpc.response;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.qs.rpc.core.RPC;
import com.qs.rpc.request.RPCRequest;
import com.qs.rpc.util.InvokeServiceUtil;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelHandlerAdapter;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelPromise;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

public class RPCResponseHandler extends ChannelHandlerAdapter {

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        String requestJson = (String) msg;
        System.out.println("实现端netty接收：" + requestJson);

        RPCRequest request = json2Obj(requestJson);
        Object result = InvokeServiceUtil.invoke(request);
        System.out.println("使用反射和动态代理已经执行了实现类对应方法，返回值为：" + result);

        RPCResponse response = new RPCResponse();
        String requestId = request.getRequestId();
        response.setRequestId(requestId);
        response.setResult(result);

        String responseJson = RPC.responseEncode(response);
        ByteBuf byteBuf = Unpooled.copiedBuffer(responseJson.getBytes(StandardCharsets.UTF_8));
        ctx.writeAndFlush(byteBuf);
    }

    @Override
    public void channelReadComplete(ChannelHandlerContext ctx) throws Exception {
        ctx.flush();
    }

    @Override
    public void close(ChannelHandlerContext ctx, ChannelPromise promise) throws Exception {
        ctx.close();
    }

    private RPCRequest json2Obj(String json) throws IOException {
        ObjectMapper objectMapper = new ObjectMapper();
        return objectMapper.readValue(json, RPCRequest.class);
    }

    private String obj2Json(Object obj) throws JsonProcessingException {
        ObjectMapper objectMapper = new ObjectMapper();
        return objectMapper.writeValueAsString(obj);
    }
}
