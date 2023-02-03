package cn.Netty_2.Template;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.codec.string.StringDecoder;

public class Server {
    public static void main(String[] args) {

        // (1) 先获取服务端启动器
        ServerBootstrap serverBootstrap = new ServerBootstrap();
        // (2) 选择服务器的 channel 类型
        serverBootstrap.channel(NioServerSocketChannel.class);
        // (3) 设置服务器的 EventLoopGroup，可以分别设置Boss线程和Worker线程。
        NioEventLoopGroup bossEventLoopGroup = new NioEventLoopGroup();
        NioEventLoopGroup workerEventLoopGroup = new NioEventLoopGroup();
        serverBootstrap.group(bossEventLoopGroup, workerEventLoopGroup);
        // (4) 初始化处理 channel 的处理器
        serverBootstrap.childHandler(new ChannelInitializer<NioSocketChannel>() {
            @Override
            protected void initChannel(NioSocketChannel socketChannel) throws Exception {
                // (5) 获取 socketChannel 的处理器管道
                ChannelPipeline pipeline = socketChannel.pipeline();
                // (6) 向管道中添加所需要的自定义处理器，并可以指定该处理器在哪个 EventLoopGroup 中运行

                // (6.1) 使用默认的 Worker 的 LoopGroup，来将 ByteBuf 中的信息转为字符串
                pipeline.addLast("defaultLoopGroup", new StringDecoder());
                // (6.2) 再用另外的 executeLoopGroup 将上一个处理器的处理结果（字符串）进行打印
                EventLoopGroup executeLoopGroup = new DefaultEventLoopGroup();
                pipeline.addLast(executeLoopGroup, "executeLoopGroup", new ChannelInboundHandlerAdapter() {
                    @Override
                    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
                        System.out.println((String) msg);
                    }
                });
            }
        });
        // (7) 绑定服务器的监听端口
        serverBootstrap.bind(9999);

    }
}
