package cn.Netty_2.Sticky_Half;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.codec.http.*;
import io.netty.handler.codec.string.StringDecoder;

import static io.netty.handler.codec.http.HttpHeaderNames.CONTENT_LENGTH;

public class HttpLTC {
    public static void main(String[] args) {
        ServerBootstrap serverBootstrap = new ServerBootstrap();
        serverBootstrap.channel(NioServerSocketChannel.class);
        NioEventLoopGroup bossEventLoopGroup = new NioEventLoopGroup();
        NioEventLoopGroup workerEventLoopGroup = new NioEventLoopGroup();
        serverBootstrap.group(bossEventLoopGroup, workerEventLoopGroup);
        serverBootstrap.childHandler(new ChannelInitializer<NioSocketChannel>() {
            @Override
            protected void initChannel(NioSocketChannel socketChannel) throws Exception {
                ChannelPipeline pipeline = socketChannel.pipeline();
                pipeline.addLast(new ChannelInboundHandlerAdapter() {
                    @Override
                    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
                        System.out.println(msg.getClass());
                        System.out.println(msg);
                        ctx.fireChannelRead(msg);
                    }
                });
                // Http 编码/解码器
                pipeline.addLast(new HttpServerCodec());
                // Http 请求头筛选器
                pipeline.addLast("http_head", new SimpleChannelInboundHandler<HttpRequest>() {
                    @Override
                    protected void channelRead0(ChannelHandlerContext ctx, HttpRequest httpRequest) throws Exception {
                        System.out.println(httpRequest.uri());
                        // 两个筛选器是并列关系，不需要重复放行。
                        // ctx.fireChannelRead(httpRequest);
                    }
                });
                pipeline.addLast("http_body", new SimpleChannelInboundHandler<HttpContent>() {
                    @Override
                    protected void channelRead0(ChannelHandlerContext ctx, HttpContent httpContent) throws Exception {
                        System.out.println(httpContent.content());
                        ctx.fireChannelRead(httpContent);
                    }
                });
                pipeline.addLast("http_response", new ChannelInboundHandlerAdapter() {
                    @Override
                    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
                        System.out.println("响应处理器");
                        // 设置响应
                        DefaultFullHttpResponse response = new DefaultFullHttpResponse(HttpVersion.HTTP_1_1, HttpResponseStatus.OK);
                        String content = "<h1>this is response</h1>";
                        response.content().writeBytes(content.getBytes());

                        // 同时需要告诉浏览器响应体的长度，否则会浏览器会一直等待更多内容，会一直转圈
                        response.headers().setInt(CONTENT_LENGTH, content.length());

                        // 返回相应，会经过 HttpServerCodec 编码器
                        ctx.writeAndFlush(response);
                    }
                });
            }
        });
        serverBootstrap.bind(9999);
    }
}
