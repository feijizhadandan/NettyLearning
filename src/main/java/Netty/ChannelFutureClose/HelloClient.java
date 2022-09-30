package Netty.ChannelFutureClose;

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

        NioEventLoopGroup eventLoopGroup = new NioEventLoopGroup();
        ChannelFuture channelFuture = new Bootstrap()
                .group(eventLoopGroup)
                .channel(NioSocketChannel.class)
                .handler(new ChannelInitializer<NioSocketChannel>() {
                    @Override
                    protected void initChannel(NioSocketChannel nsc) throws Exception {
                        nsc.pipeline().addLast(new StringEncoder());
                    }
                })
                .connect(new InetSocketAddress(8888));

        Channel channel = channelFuture.sync().channel();
        log.debug("主线程");

        // 输入线程
        new Thread(()-> {
            log.debug("输入线程");

            Scanner scanner = new Scanner(System.in);
            while(true) {
                String line = scanner.nextLine();
                if("q".equals(line)) {
                    channel.close();
                    break;
                }
                channel.writeAndFlush(line);
            }
        }, "InputThread").start();

        /**
         *  处理连接关闭后才执行的善后操作
         *      方法1：在主线程同步处理关闭
         *      方法2：异步处理关闭
         *             异步：不是指异步IO，而是强调不在主线程中进行处理
         */
        
        /*
            (1): 在主线程同步处理关闭, 获取 closeFuture 进行阻塞
            ChannelFuture closeFuture = channel.closeFuture();
            closeFuture.sync();
            System.out.println("closing..");
            System.out.println("连接关闭完成");
         */
        
        /*
            (2): 异步处理关闭，通过 closeFuture 添加 listener
         */
        ChannelFuture closeFuture = channel.closeFuture();
        closeFuture.addListener(new ChannelFutureListener() {
            @Override
            public void operationComplete(ChannelFuture channelFuture) throws Exception {
                log.debug("closing");
                System.out.println("连接关闭完成");
                // 善后处理（关闭 EventLoopGroup 中的线程）
                eventLoopGroup.shutdownGracefully();
            }
        });

    }
}
