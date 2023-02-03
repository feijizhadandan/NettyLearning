package cn.Netty_1.demo_hello;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.codec.string.StringDecoder;

/**
 *  cn.Netty 入门案例：服务端 HelloWorld，处理简单的读取
 */
public class HelloServer {
    public static void main(String[] args) {

        // 1.启动器，负责组装netty组件，启动服务器
        new ServerBootstrap()
                // 2.多个EventLoop(一个selector + 单线程执行器)，可以监听各种事件，并进行处理
                .group(new NioEventLoopGroup())
                // 3.选择服务器的ServerSocketChannel实现
                .channel(NioServerSocketChannel.class)
                // 4.boss负责处理连接; worker(child) 负责处理读写。
                // 因为boss的操作比较固定，只负责accept，因此netty帮我们进行了封装，我们只需要关注child的处理方式
                // 决定了worker(child)需要执行哪些操作(handler)
                .childHandler(
                        // 5.服务端和客户端进行数据读写通道的初始化器ChannelInitializer
                        // 其中有初始化方法initChannel，在初始化方法中添加处理器ChannelInboundHandlerAdapter，在处理器中定义处理方法
                        new ChannelInitializer<NioSocketChannel>() {
                            // 数据连接建立后被调用
                            @Override
                            protected void initChannel(NioSocketChannel nsc) throws Exception {
                                // 6.添加具体handler
                                nsc.pipeline().addLast(new StringDecoder());    // 将ByteBuf转换为字符串
                                nsc.pipeline().addLast(new ChannelInboundHandlerAdapter() { // 自定义 handler
                                    @Override  // 处理读事件
                                    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
                                        // 输出转换完成的字符串
                                        System.out.println(msg);
                                    }
                                });
                            }
                        }
                )
                // 6.绑定监听端口
                .bind(8888);
        
    }
}
