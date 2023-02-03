package cn.Netty_1.ChannelFutureClose;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.buffer.ByteBuf;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import lombok.extern.slf4j.Slf4j;

import java.nio.charset.StandardCharsets;
import java.util.Scanner;

/**
 *  分工细化 cn.Netty 服务端：
 *      1、boss 和 worker 细化
 *      2、不同的 handler 交给不同的 EventLoop 处理
 */
@Slf4j
public class BossAndWorker {
    public static void main(String[] args) {

        /**
         *  分工细化(2):
         *      如果一个 NioEventLoop 管理几百个 SocketChannel，如果其中一个 handler 处理时间较长，就会导致其他 channel 的信息不能被及时处理（因为是多路复用，只有一个selector）
         *      所以可以让 worker 只负责读出 channel 中的信息，后续 handler 中的操作业务操作交给 DefaultEventLoopGroup 中的线程完成
         *      不同的处理器（handler）可以放在不同的 LoopGroup 中进行处理，在 addLast() 函数的第一个参数指明即可
         *      
         *      channel 和 worker的NioEventLoop 和 handler的DefaultEventLoop 都会进行绑定
         */
        EventLoopGroup loopGroup = new DefaultEventLoopGroup();

        ChannelFuture bind = new ServerBootstrap()
                /**
                 *  分工细化(1):
                 *      boss只负责ServerSocketChannel上的accept事件
                 *      worker只负责SocketChannel上的读写事件
                 */
                .group(new NioEventLoopGroup(), new NioEventLoopGroup(2))
                .channel(NioServerSocketChannel.class)
                .childHandler(
                        new ChannelInitializer<NioSocketChannel>() {
                            @Override
                            protected void initChannel(NioSocketChannel nsc) throws Exception {
                                nsc.pipeline().addLast("origin_handler", new ChannelInboundHandlerAdapter() {
                                    @Override
                                    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
                                        ByteBuf buf = (ByteBuf) msg;
                                        log.debug(buf.toString(StandardCharsets.UTF_8));
                                        ctx.fireChannelRead(msg);
                                    }
                                }).addLast(loopGroup, "additional_handler", new ChannelInboundHandlerAdapter() {
                                    @Override
                                    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
                                        ByteBuf buf = (ByteBuf) msg;
                                        log.debug(buf.toString(StandardCharsets.UTF_8));
                                        ctx.fireChannelRead(msg);
                                    }
                                });
                            }
                        }
                )
                .bind(8888);

        Scanner scanner = new Scanner(System.in);
        String input = scanner.nextLine();
        if(input.equals("q")) { 
            System.out.println(bind.channel());
        }

    }
}
