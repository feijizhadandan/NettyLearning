package cn.Netty_2.Template;

import io.netty.bootstrap.Bootstrap;
import io.netty.buffer.ByteBuf;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.codec.string.StringDecoder;

import java.net.InetSocketAddress;

public class Client {
    public static void main(String[] args) {

        // (1) 获取客户端启动器
        Bootstrap bootstrap = new Bootstrap();
        // (2) 选择客户端的 channel 类型
        bootstrap.channel(NioSocketChannel.class);
        // (3) 设置客户端的 EventLoopGroup，用来处理该客户端的channel本身
        NioEventLoopGroup workerEventLoopGroup = new NioEventLoopGroup();
        bootstrap.group(workerEventLoopGroup);
        // (4) 初始化处理 channel 的处理器
        bootstrap.handler(new ChannelInitializer<NioSocketChannel>() {
            @Override
            protected void initChannel(NioSocketChannel nioSocketChannel) throws Exception {
                // (5) 获取 socketChannel 的处理器管道
                ChannelPipeline pipeline = nioSocketChannel.pipeline();
                // (6) 添加处理器
                pipeline.addLast("defaultLoopGroup", new ChannelInboundHandlerAdapter() {
                    // (6.1) 连接建立成功后就会触发该处理器的 channelActive 方法
                    @Override
                    public void channelActive(ChannelHandlerContext ctx) throws Exception {
                        // (6.2) 发送数据的格式为 字节数组，或用 ByteBuf 进行封装，不能直接发送字符串。
                        // 也可以添加一个 StringEncoder 处理器，会帮你自动编码为字节数组。
                        for (int i = 0; i < 3; i++) {
                            ByteBuf buffer = ctx.alloc().buffer(20);
                            buffer.writeBytes("连接建立成功(1) —— ".getBytes());
                            ctx.writeAndFlush(buffer);
                        }
                    }
                });
            }
        });
        // (7) 绑定客户端的发送端口
        bootstrap.connect(new InetSocketAddress(9999));

    }
}
