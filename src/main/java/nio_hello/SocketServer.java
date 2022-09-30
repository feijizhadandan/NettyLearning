package nio_hello;

import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.*;
import java.nio.charset.StandardCharsets;
import java.util.Iterator;

/**
 *  基础NIO服务端，使用单一 selector，能够处理强制断开连接和正常断开连接
 */
@Slf4j
public class SocketServer {
    public static void main(String[] args) throws IOException {
        
        // 1、创建selector，可以管理多个channel
        Selector selector = Selector.open();

        ServerSocketChannel ssc = ServerSocketChannel.open();
        ssc.bind(new InetSocketAddress(8888));
        ssc.configureBlocking(false);
        
        // 2、将 channel 注册到 selector 上
        // 可以通过SelectionKey 知道是哪个channel的事件
        SelectionKey sscKey = ssc.register(selector, 0, null);
        // 该 selectorKey 只关注 accept 事件
        sscKey.interestOps(SelectionKey.OP_ACCEPT);
        
        while(true) {
            // 3、select 方法，没有事件发生，则线程阻塞；有事件，线程才运行
            // select() 在事件未处理时【可能是检查缓存队列来看是否处理完毕】，不会阻塞，事件发生后要么处理，要么取消 key.cancel()
            selector.select();
            // 4、处理事件，获取 selector 中发生了事件的 key（是set集合，使用迭代器遍历，因为要进行删除）
            // 因为是所有发生的事件，因此可能是 accept/read，因此在循环中需要区分事件类型
            Iterator<SelectionKey> iterator = selector.selectedKeys().iterator();
            while(iterator.hasNext()) {
                SelectionKey key = iterator.next();
                System.out.println(key);
                // 在处理完事件后，要从selectedKeys中删除，否则下次循环会出现没有发生事件的key
                iterator.remove();
                // 5、区分事件类型
                if(key.isAcceptable()) {
                    // 通过key，获取对应的channel（selector -- key -- channel）
                    ServerSocketChannel channel = (ServerSocketChannel) key.channel();
                    // 6、接受连接，获取socket
                    SocketChannel sc = channel.accept();
                    sc.configureBlocking(false);
                    
                    sc.write(StandardCharsets.UTF_8.encode("nihaohao"));
                    
                    // 7、将socketChannel 注册到 selector，并指定关注事件
                    SelectionKey scKey = sc.register(selector, 0, null);
                    scKey.interestOps(SelectionKey.OP_READ);
                }
                else if(key.isReadable()) {
                    try {
                        ByteBuffer buffer = ByteBuffer.allocate(10);
                        SocketChannel channel = (SocketChannel) key.channel();
                        int read = channel.read(buffer);    // 如果正常断开，read() 返回 -1
                        if(read == -1) {
                            key.cancel();
                        }
                        else {
                            buffer.flip();
                            System.out.println(StandardCharsets.UTF_8.decode(buffer).toString());
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                        key.cancel();
                    }
                }
            }
        }
        
    }
}
