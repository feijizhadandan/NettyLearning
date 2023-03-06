package cn.ChatRoom;

import cn.ChatRoom.protocol.codec.MessageCodecSharable;
import cn.ChatRoom.common.MessageType;
import cn.ChatRoom.entity.TextMessage;
import io.netty.bootstrap.Bootstrap;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioSocketChannel;

import java.net.InetSocketAddress;

public class Client {
    public static void main(String[] args) {

        Bootstrap bootstrap = new Bootstrap();
        bootstrap.channel(NioSocketChannel.class);
        NioEventLoopGroup workerEventLoopGroup = new NioEventLoopGroup();
        bootstrap.group(workerEventLoopGroup);

        MessageCodecSharable messageCodecSharable = new MessageCodecSharable();

        bootstrap.handler(new ChannelInitializer<NioSocketChannel>() {
            @Override
            protected void initChannel(NioSocketChannel nioSocketChannel) throws Exception {
                ChannelPipeline pipeline = nioSocketChannel.pipeline();
                pipeline.addLast(messageCodecSharable);
                pipeline.addLast("defaultLoopGroup", new ChannelInboundHandlerAdapter() {
                    @Override
                    public void channelActive(ChannelHandlerContext ctx) throws Exception {
                        TextMessage message = new TextMessage();
                        message.setMessageType(MessageType.TEXT_MESSAGE);
                        message.setSequenceId(0);
                        message.setText("连接成功");
                        ctx.writeAndFlush(message);
                    }
                });
            }
        });
        bootstrap.connect(new InetSocketAddress(9999));

    }
}
