package cn.FutureAndPromise;

import lombok.extern.slf4j.Slf4j;

import java.util.concurrent.*;

@Slf4j
public class JDKFuture {
    public static void main(String[] args) throws ExecutionException, InterruptedException {
        // 1、使用线程池
        ExecutorService servicePool = Executors.newFixedThreadPool(2);
        // 2、提交任务
        Future<Integer> future = servicePool.submit(() -> {
            try {
                log.debug("执行计算线程");
                Thread.sleep(1000);
                return 50;
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        });
        // 3、主线程通过 future 获取结果
        log.debug("主线程等待结果");
        log.debug("结果是 {}", future.get());
    }
}
