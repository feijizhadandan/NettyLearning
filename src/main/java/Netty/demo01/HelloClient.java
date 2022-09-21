package Netty.demo01;

import io.netty.bootstrap.Bootstrap;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.codec.string.StringEncoder;

import java.net.InetSocketAddress;

/**
 * Netty 入门案例：客户端 HelloWorld，处理简单的写、发送操作
 */
public class HelloClient {
    public static void main(String[] args) throws InterruptedException {
        
        // 1.客户端启动器
        new Bootstrap()
                // 2.多个EventLoop(一个selector + 单线程执行器)，可以监听各种事件，并进行处理
                .group(new NioEventLoopGroup())
                // 3.选择客户端 channel 实现
                .channel(NioSocketChannel.class)
                // 4.在连接建立后，会调用ChannelInitializer初始化器的初始化方法initChannel，
                // 在初始化方法中添加处理器ChannelInboundHandlerAdapter，在处理器中定义处理方法
                .handler(new ChannelInitializer<NioSocketChannel>() {
                    @Override
                    protected void initChannel(NioSocketChannel nsc) throws Exception {
                        nsc.pipeline().addLast(new StringEncoder());
                    }
                })
                // 5.连接到服务器
                .connect(new InetSocketAddress(8888))
                // 阻塞方法，直到连接建立  
                .sync()
                // 获取socketChannel连接对象
                .channel()
                // 向服务器发送数据
                .writeAndFlush("hello world");
        
    }
}
