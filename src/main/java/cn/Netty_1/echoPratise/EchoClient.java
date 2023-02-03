package cn.Netty_1.echoPratise;

import io.netty.bootstrap.Bootstrap;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufAllocator;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioSocketChannel;
import lombok.extern.slf4j.Slf4j;

import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.util.Scanner;

/**
 * 回声双向通信的客户端
 */
@Slf4j
public class EchoClient {
    public static void main(String[] args) throws InterruptedException {

        ChannelFuture channelFuture = new Bootstrap()
                .group(new NioEventLoopGroup())
                .channel(NioSocketChannel.class)
                .handler(new ChannelInitializer<NioSocketChannel>() {
                    @Override
                    protected void initChannel(NioSocketChannel nsc) throws Exception {
                        ChannelPipeline pipeline = nsc.pipeline();
                        // 如果直接发送字符串，就需要添加这个处理器，如果发送的事ByteBuf，则不用
                        // pipeline.addLast(new StringEncoder());
                        pipeline.addLast("auto_handler", new ChannelInboundHandlerAdapter() {
                            // 连接建立后会自动调用，用于发送数据
                            @Override
                            public void channelActive(ChannelHandlerContext ctx) throws Exception {
                                ByteBuf msg = ctx.alloc().buffer();
                                String str = "你好，我是客户端";
                                log.debug("发送消息：{}", str);
                                msg.writeBytes(str.getBytes());
                                nsc.writeAndFlush(msg);
                            }
                            // 用于读取服务端的回应
                            @Override
                            public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
                                ByteBuf buf = (ByteBuf) msg;
                                log.debug("来自服务单的回应：{}", buf.toString(StandardCharsets.UTF_8));
                            }
                        });
                    }
                })
                .connect(new InetSocketAddress(8888));

        // 同步方式来进行输入
//        Channel channel = channelFuture.sync().channel();
//        // 输入线程
//        new Thread(()-> {
//            log.debug("输入线程");
//
//            Scanner scanner = new Scanner(System.in);
//            while(true) {
//                String msg = scanner.nextLine();
//                if("q".equals(msg)) {
//                    channel.close();
//                    break;
//                }
//                ByteBuf buffer = ByteBufAllocator.DEFAULT.buffer();
//                buffer.writeBytes(msg.getBytes(StandardCharsets.UTF_8));
//                channel.writeAndFlush(buffer);
//            }
//        }, "InputThread").start();

        // 异步回调方式进行开辟输入线程
        channelFuture.addListener((ChannelFutureListener) future -> {
            // 这个 channel 就是和服务端连接的 channel
            NioSocketChannel channel = (NioSocketChannel) future.channel();
            new Thread(()-> {
                log.debug("输入线程...");
                Scanner scanner = new Scanner(System.in);
                while (true) {
                    String msg = scanner.nextLine();
                    if ("q".equals(msg)) {
                        channel.close();
                        break;
                    }
                    log.debug("发送消息：{}", msg);
                    ByteBuf buffer = ByteBufAllocator.DEFAULT.buffer();
                    buffer.writeBytes(msg.getBytes(StandardCharsets.UTF_8));
                    // 传出的类型是 ByteBuf，而不是 String 或 byte
                    channel.writeAndFlush(buffer);
                }
            }, "input_thread").start();
        });
    }
}
