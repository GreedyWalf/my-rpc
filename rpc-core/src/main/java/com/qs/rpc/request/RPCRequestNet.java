package com.qs.rpc.request;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.qs.rpc.config.ServerConfig;
import com.qs.rpc.core.RPC;
import io.netty.bootstrap.Bootstrap;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.codec.LineBasedFrameDecoder;
import io.netty.handler.codec.string.StringDecoder;
import org.springframework.util.StringUtils;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public class RPCRequestNet {

    //全局map，每个请求对应的锁，用于同步等待每个异步的RPC请求
    private static Map<String, RPCRequest> requestLockMap = new ConcurrentHashMap<>(0);

    //阻塞等待连接成功的锁
    private static Lock connectLock = new ReentrantLock();
    private static Condition condition = connectLock.newCondition();

    private static volatile RPCRequestNet instance;

    public static Lock getConnectLock() {
        return connectLock;
    }

    public static Condition getCondition() {
        return condition;
    }

    public static Map<String, RPCRequest> getRequestLockMap() {
        return requestLockMap;
    }

    //私有化构造方法
    private RPCRequestNet() {
        //netty线程组
        EventLoopGroup group = new NioEventLoopGroup();
        Bootstrap bootstrap = new Bootstrap();
        bootstrap.group(group)
                .channel(NioSocketChannel.class)
                .option(ChannelOption.TCP_NODELAY, true)
                .handler(new ChannelInitializer<SocketChannel>() {
                    @Override
                    protected void initChannel(SocketChannel socketChannel) throws Exception {
                        socketChannel.pipeline()
                                //以换行符分包，防止念包、半包，2048为最大长度，超出最大长度则抛出异常
                                .addLast(new LineBasedFrameDecoder(2048))
                                //将接受到的对象转化为字符串
                                .addLast(new StringDecoder())
                                //自定义实现ChannelHandler
                                .addLast(new RPCRequestHandler());
                    }
                });

        try {
            //连接实现端服务地址端口（连接netty服务端）
            ServerConfig serverConfig = RPC.getServerConfig();
            ChannelFuture future = bootstrap.connect(serverConfig.getIp(), serverConfig.getPort()).sync();
            future.addListener(new ChannelFutureListener() {
                @Override
                public void operationComplete(ChannelFuture channelFuture) throws Exception {
                }
            });
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }


    //单例模式，获取唯一的RPCRequestNet实例
    public static RPCRequestNet connect() {
        if (instance == null) {
            synchronized (RPCRequest.class) {
                if (instance == null) {
                    instance = new RPCRequestNet();
                }
            }
        }

        return instance;
    }

    /**
     * 向实现端发送请求
     */
    public void send(RPCRequest request) {
        ChannelHandlerContext chlContext = RPCRequestHandler.getChlContext();
        try {
            if (chlContext == null) {
                connectLock.lock();
                System.out.println("正在等待连接服务实现端。。");
                condition.await();
                connectLock.unlock();
            }

            String requestJson = null;
            try {
                requestJson = RPC.requestEncode(request);
            } catch (JsonProcessingException e) {
                e.printStackTrace();
            }

            if (StringUtils.isEmpty(requestJson)) {
                System.out.println("请求内容为空！");
                return;
            }

            //请求端，发送request请求报文
            ByteBuf requestBuf = Unpooled.copiedBuffer(requestJson.getBytes());
            RPCRequestHandler.getChlContext().writeAndFlush(requestBuf);
            System.out.println("调用" + request.getRequestId() + "已发送");

            //挂起等待实现端处理完毕返回
            synchronized (request) {
                //放弃对象锁，阻塞等待notify
                request.wait();
            }

            System.out.println("调用" + request.getRequestId() + "接收完毕");
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
}
