package cn.FutureAndPromise;


import io.netty.channel.EventLoop;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.util.concurrent.Future;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class NettyFuture {
    public static void main(String[] args) throws InterruptedException {

        // netty 中的线程池（EventLoop）和 Future 配套使用
        NioEventLoopGroup eventLoopGroup = new NioEventLoopGroup();

        EventLoop eventLoop = eventLoopGroup.next();

        Future<Integer> future = eventLoop.submit(() -> {
            try {
                log.debug("执行计算线程");
                Thread.sleep(2000);
                return 80;
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        });

        // future 的回调方法，会在 call() 方法完成后回调，并与 call() 在同一个执行线程中进行回调
        future.addListener((future1) -> {
            log.debug("异步接收结果：{}", future1.getNow());
            // 在任务结束后优雅的关闭 EventLoop 中的线程
            eventLoopGroup.shutdownGracefully();
        });

    }
}
