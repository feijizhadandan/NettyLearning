package cn.Echo;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.buffer.ByteBuf;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import lombok.extern.slf4j.Slf4j;

import java.nio.charset.StandardCharsets;

/**
 *  回声双向通信的服务端
 *  Note：
 *      1、一般双方传送的数据都是 ByteBuf，而之前都是直接传送 String 类型的，因此发送之前需要添加一个 StringEncoder 处理器
 *         而这里不用，因此需要在发送数据时，将 String 类型转换成 ByteBuf 类型再进行 writeAndFlush
 *      2、客户端使用到了一个新的处理函数 channelActive()，作用是在连接建立后会自动调用，类似于对 channelFuture.addListener
 *      3、客户端的处理器不仅使用了 channelActive()，还有一个 channelRead() 用来读取数据。
 *          （1）无论是客户端还是服务端，都可以添加各种处理器来实现对 channel 中数据的读写。
 *          （2）而我们又可以添加多个处理器，来对数据进行分步读写
 *      4、ChannelHandlerContext ctx：用于和 pipeline、其他 handler 交互，比如通知一下个 handler（fireChannelRead方法）
 *      5、服务端在处理器的 channelRead 中不仅完成了消息的接收，还顺带完成了消息的回应
 *         客户端的处理器设置有 channelRead，因此可以完成消息的接收。
 *         从而实现双向通讯
 *      6、Java Socket 是全双工通讯的（不只是 nio 或 netty）
 *         也就是读写是相互独立，不会阻塞的。这点主要体现在多线程上，两个不同的线程上分别进行读、写，是不会阻塞的，因此是全双工通讯
 *         因此 阻塞IO 和 全双工 没有必然关联，阻塞 IO 也可以是全双工的，因为阻塞的可能只是读线程，而只要另开一个写线程就可以实现同时运作。
 *         因此使用 Java Socket 是可以实现高效双向通讯的：写完后，不必等待回应，而是可以继续发送，不一定要一问一答。
 */
@Slf4j
public class EchoServer {
    public static void main(String[] args) {

        // NioServerSocketChannel：服务端用来接收连接的 Channel
        // NioSocketChannel：客户端和服务端用来传送数据的 Channel
        ChannelFuture channelFuture = new ServerBootstrap()
                .group(new NioEventLoopGroup(), new NioEventLoopGroup(2))
                .channel(NioServerSocketChannel.class)
                .childHandler(new ChannelInitializer<NioSocketChannel>() {
                    @Override
                    // 参数 nsc 就是和客户端连接的 NioSocketChannel
                    protected void initChannel(NioSocketChannel nsc) throws Exception {
                        ChannelPipeline pipeline = nsc.pipeline();
                        pipeline.addLast("read_handler", new ChannelInboundHandlerAdapter() {
                            @Override
                            // TODO ChannelHandlerContext ctx 是什么东西
                            public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
                                ByteBuf buf = (ByteBuf) msg;
                                log.debug("来自客户端的信息：{}", buf.toString(StandardCharsets.UTF_8));
                                // 建议使用 ctx.alloc() 创建 ByteBuf
                                ByteBuf response = ctx.alloc().buffer();
                                response.writeBytes("服务端已收到您的消息".getBytes(StandardCharsets.UTF_8));
                                nsc.writeAndFlush(response);
                                ctx.fireChannelRead(msg);
                            }
                        });
                    }
                })
                .bind(8888);

    }
}
