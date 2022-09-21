package NIO_multi;

import lombok.extern.slf4j.Slf4j;

import java.io.FileInputStream;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.*;
import java.nio.charset.StandardCharsets;
import java.util.Iterator;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.atomic.AtomicInteger;


/**
 * 手写NIO，分工 Boss 和 Worker，并设定多个 Workers，进行轮流处理
 */
@Slf4j
public class BossServer {
    public static void main(String[] args) throws IOException {
        Thread.currentThread().setName("boss");

        ServerSocketChannel ssc = ServerSocketChannel.open();
        ssc.configureBlocking(false);
        Selector bossSelector = Selector.open();
        SelectionKey bossKey = ssc.register(bossSelector, SelectionKey.OP_ACCEPT, null);
        ssc.bind(new InetSocketAddress(8888));

        // 创建 workers 数组，并逐一初始化
        ReadWorker[] workers = new ReadWorker[5];
        for (int i = 0; i < 5; i++) {
            workers[i] = new ReadWorker("worker-" + i);
            workers[i].initWorker();
        }
        
        // 计数器
        AtomicInteger index = new AtomicInteger();
        
        while(true) {
            bossSelector.select();
            Iterator<SelectionKey> iterator = bossSelector.selectedKeys().iterator();
            while(iterator.hasNext()) {
                SelectionKey key = iterator.next();
                iterator.remove();
                if(key.isAcceptable()) {
                    SocketChannel sc = ssc.accept();
                    log.debug("connecting..." + sc.getRemoteAddress());
                    sc.configureBlocking(false);
                    // 将 sc 注册到 worker 的 selector 上，并监听读事件
                    workers[index.getAndIncrement() % workers.length].newRegister(sc);
                    log.debug("after register..." + sc.getRemoteAddress());
                }
            }
        }

    }
    
    static class ReadWorker implements Runnable{
        
        private Thread thread;
        private Selector selector;
        private String name;
        private boolean isInit = false;
        // 任务队列：将 register 操作放到 worker 线程中执行
        private ConcurrentLinkedQueue<Runnable> taskQueue = new ConcurrentLinkedQueue<>();
        
        public ReadWorker(String name) {
            this.name = name;
        }

        /**
         * 利用自身对象创建线程并启动线程
         */
        public void initWorker() throws IOException {
            if(!isInit) {
                thread = new Thread(this, name);
                selector = Selector.open();
                thread.start();
                isInit = true;
            }
        }

        /**
         * 有新的连接请求，需要为selector添加监听任务
         */
        public void newRegister(SocketChannel sc) {
            taskQueue.add(()-> {
                try {
                    sc.register(selector, SelectionKey.OP_READ, null);
                } catch (ClosedChannelException e) {
                    e.printStackTrace();
                }
            });
            // worker 中的 selector 仍在阻塞状态，因此需要主动唤醒，在他在 worker 线程中执行 register 任务
            selector.wakeup();
        }

        @Override
        public void run() {
            while(true) {
                try {
                    selector.select();
                    
                    Runnable task = taskQueue.poll();
                    if(task != null) {
                        task.run();
                    }
                    
                    Iterator<SelectionKey> iterator = selector.selectedKeys().iterator();
                    while(iterator.hasNext()) {
                        SelectionKey key = iterator.next();
                        iterator.remove();
                        if(key.isReadable()) {
                            try {
                                ByteBuffer buffer = ByteBuffer.allocate(16);
                                SocketChannel sc = (SocketChannel) key.channel();
                                int read = sc.read(buffer);
                                // 正常断开处理
                                if(read == -1) {
                                    key.cancel();
                                }
                                log.debug("reading...." + sc.getRemoteAddress());
                                buffer.flip();
                                System.out.println(StandardCharsets.UTF_8.decode(buffer));
                            } catch (IOException e) {
                                e.printStackTrace();
                                // 强制断开处理
                                key.cancel();
                            }
                        }
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }
}
