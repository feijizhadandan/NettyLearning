package Netty.ChannelFutureConnect;

import io.netty.bootstrap.Bootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.codec.string.StringEncoder;
import lombok.extern.slf4j.Slf4j;

import java.net.InetSocketAddress;
import java.util.Scanner;

@Slf4j
public class HelloClient {
    public static void main(String[] args) throws InterruptedException {

        ChannelFuture channelFuture = new Bootstrap()
                .group(new NioEventLoopGroup())
                .channel(NioSocketChannel.class)
                .handler(new ChannelInitializer<NioSocketChannel>() {
                    @Override
                    protected void initChannel(NioSocketChannel nsc) throws Exception {
                        nsc.pipeline().addLast(new StringEncoder());
                    }
                })
                // connect()是异步非阻塞的: 主线程负责发起 connect 请求，真正执行 connect 的是 nio 线程(EventLoop)
                .connect(new InetSocketAddress(8888));
        

        /**
         *  处理连接建立好后才执行操作
         *      方法1：使用 sync 方法"同步"处理结果
         *      方法2：使用 addListener(回调对象) 方法"异步"处理结果
         *              异步：不是指异步IO，而是强调不在主线程中进行处理
         */
        /*
            (1):阻塞当前线程，直到 nio 线程连接建立完毕
            channelFuture.sync();  
            Channel channel = channelFuture.channel();
            channel.writeAndFlush("hello world");
         */
        
        // (2):在 nio 线程建立好后，会调用 operationComplete() 函数，因此可以在 nio 线程中发送数据
        //     通过 channelFuture 来添加 Listener 进行监听
        channelFuture.addListener(new ChannelFutureListener() {
            @Override
            public void operationComplete(ChannelFuture channelFuture) throws Exception {
                Channel channel = channelFuture.channel();
                channel.writeAndFlush("连接成功");
            }
        });


    }
}
