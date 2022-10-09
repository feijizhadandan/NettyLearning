package cn.nio_hello;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;
import java.nio.charset.StandardCharsets;


public class SocketClient {
    public static void main(String[] args) throws IOException {

        SocketChannel sc = SocketChannel.open();
        sc.connect(new InetSocketAddress("localhost", 8888));

        ByteBuffer buffer = ByteBuffer.allocate(200);
        sc.read(buffer);
        buffer.flip();
        System.out.println(buffer);
        System.out.println(StandardCharsets.UTF_8.decode(buffer));
        
        sc.close();
        System.out.println("waiting");
        
    }
}