package cn.ChatRoom;

import cn.ChatRoom.codec.MessageCodecSharable;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.codec.LengthFieldBasedFrameDecoder;

public class Server {
    public static void main(String[] args) {

        ServerBootstrap serverBootstrap = new ServerBootstrap();
        serverBootstrap.channel(NioServerSocketChannel.class);
        NioEventLoopGroup bossEventLoopGroup = new NioEventLoopGroup();
        NioEventLoopGroup workerEventLoopGroup = new NioEventLoopGroup();
        serverBootstrap.group(bossEventLoopGroup, workerEventLoopGroup);

        MessageCodecSharable messageCodecSharable = new MessageCodecSharable();

        serverBootstrap.childHandler(new ChannelInitializer<NioSocketChannel>() {
            @Override
            protected void initChannel(NioSocketChannel socketChannel) throws Exception {
                ChannelPipeline pipeline = socketChannel.pipeline();
                // 防止半包问题的发生，需要用帧解码器确认数据包长度，保证传给下一个处理器的 byteBuf 是一个完整的数据包
                pipeline.addLast("LTC", new LengthFieldBasedFrameDecoder(1024, 16, 4, 0, 0));
                pipeline.addLast("messageCodec", messageCodecSharable);
                pipeline.addLast("defaultLoopGroup", new ChannelInboundHandlerAdapter() {
                    @Override
                    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
                        System.out.println(msg);
                    }
                });
            }
        });
        serverBootstrap.bind(9999);

    }
}
