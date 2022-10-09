package cn.FutureAndPromise;

import io.netty.channel.EventLoop;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.util.concurrent.DefaultPromise;
import lombok.extern.slf4j.Slf4j;

import java.util.concurrent.ExecutionException;

@Slf4j
public class NettyPromise {
    public static void main(String[] args) throws ExecutionException, InterruptedException {

        // 1、获得 EventLoop 对象
        NioEventLoopGroup eventLoopGroup = new NioEventLoopGroup();
        EventLoop eventLoop = eventLoopGroup.next();

        // 2、主动创建 promise 对象
        DefaultPromise<Integer> promise = new DefaultPromise<>(eventLoop);

        new Thread(()-> {
            // 3、任意线程执行计算，并将结果放入 promise 中
            try {
                log.debug("计算线程...");
                // int i = 1 / 0;
                Thread.sleep(1000);
                promise.setSuccess(800);
            } catch (Exception e) {
                promise.setFailure(e);
            }
        }).start();

        // 4、尝试直接获取结果
        log.debug(String.valueOf(promise.getNow()));
        // 5、等待并获取 promise 中的结果
        log.debug("等待结果");
        log.debug("结果是：{}", promise.get());

    }
}
