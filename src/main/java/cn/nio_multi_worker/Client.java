package cn.nio_multi_worker;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;
import java.nio.charset.StandardCharsets;

public class Client {
    public static void main(String[] args) throws IOException, InterruptedException {

        SocketChannel sc = SocketChannel.open();
        sc.connect(new InetSocketAddress(8888));

        ByteBuffer buffer = ByteBuffer.allocate(20);
        buffer.put(StandardCharsets.UTF_8.encode("nihao"));
        buffer.flip();
        sc.write(buffer);
        sc.close();


    }
}
