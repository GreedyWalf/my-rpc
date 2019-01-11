package com.qs.rpc.response;

import com.qs.rpc.core.RPC;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.codec.LineBasedFrameDecoder;
import io.netty.handler.codec.string.StringDecoder;

/**
 * 实现端netty（开启服务端netty，监听9999端口）
 */
public class RPCResponseNet {

    public static void connect() {
        EventLoopGroup boss = new NioEventLoopGroup();
        EventLoopGroup worker = new NioEventLoopGroup();

        try {
            ServerBootstrap serverBootstrap = new ServerBootstrap();
            serverBootstrap.group(boss, worker)
                    .channel(NioServerSocketChannel.class)
                    .option(ChannelOption.SO_BACKLOG, 1024)
                    .childHandler(new ChannelInitializer<SocketChannel>() {
                        @Override
                        protected void initChannel(SocketChannel socketChannel) throws Exception {
                            socketChannel.pipeline()
                                    .addLast(new LineBasedFrameDecoder(2048))
                                    .addLast(new StringDecoder())
                                    .addLast(new RPCResponseHandler());
                        }
                    });

            //获取配置文件地址端口信息
            String remoteIp = RPC.getServerConfig().getIp();
            int port = RPC.getServerConfig().getPort();
            //绑定服务端口
            ChannelFuture future = serverBootstrap.bind(remoteIp, port).sync();
            System.out.println("server start on port:9999");
            //同步等待服务端监听端口关闭
            future.channel().closeFuture().sync();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            boss.shutdownGracefully();
            worker.shutdownGracefully();
        }
    }
}
