package cn.Pipeline;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.buffer.ByteBuf;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;

import java.nio.charset.Charset;

@Slf4j
public class TestPipelineServer {
    public static void main(String[] args) {

        new ServerBootstrap()
                .group(new NioEventLoopGroup())
                .channel(NioServerSocketChannel.class)
                .childHandler(new ChannelInitializer<NioSocketChannel>() {
                    @Override
                    protected void initChannel(NioSocketChannel nsc) throws Exception {
                        // 通过 channel 得到 pipeline
                        ChannelPipeline pipeline = nsc.pipeline();

                        // 添加入站处理器
                        pipeline.addLast("h1", new ChannelInboundHandlerAdapter() {
                            @Override
                            public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
                                log.debug("入站处理器(1)...");
                                ByteBuf buffer = (ByteBuf) msg;
                                String name = buffer.toString(Charset.defaultCharset());
                                // super.channelRead 和 ctx.FireChannel 是一样的
                                super.channelRead(ctx, name);
                            }
                        });
                        pipeline.addLast("h2", new ChannelInboundHandlerAdapter() {
                            @Override
                            public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
                                log.debug("入站处理器(2)...");
                                Student student = new Student(msg.toString());
                                super.channelRead(ctx, student);
                            }
                        });
                        pipeline.addLast("h3", new ChannelInboundHandlerAdapter() {
                            @Override
                            public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
                                log.debug("入站处理器(3)...获得结果: {}", msg);
                                // 用来将数据传给下一个入站处理器，但是这里后续没有入站处理器了，因此可以不用往下传
                                // super.channelRead(ctx, msg);

                                // 触发后续的出站处理器
                                nsc.writeAndFlush(ctx.alloc().buffer().writeBytes("服务端发送数据".getBytes()));
                            }
                        });

                        // 添加出站处理器
                        pipeline.addLast("h4", new ChannelOutboundHandlerAdapter() {
                            @Override
                            public void write(ChannelHandlerContext ctx, Object msg, ChannelPromise promise) throws Exception {
                                log.debug("h4");
                                super.write(ctx, msg, promise);
                            }
                        });
                        pipeline.addLast("h5", new ChannelOutboundHandlerAdapter() {
                            @Override
                            public void write(ChannelHandlerContext ctx, Object msg, ChannelPromise promise) throws Exception {
                                log.debug("h5");
                                super.write(ctx, msg, promise);
                            }
                        });

                    }
                })
                .bind(8888);

    }

    @Data
    static class Student {
        private String name;

        public Student(String name) {
            this.name = name;
        }
    }
}
