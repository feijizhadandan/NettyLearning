package cn.JavaSocketHomework;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;
import java.nio.charset.StandardCharsets;
import java.util.Scanner;

public class Client {

    final static int MAX_THREADS = 1;

    public static void main(String[] args) throws IOException, InterruptedException {

        while(true){

            int curCount = ClientUser.getThreadCount();

            if(curCount < MAX_THREADS)
                new Thread(new ClientUser()).run();
            else
                break;
        }

    }

    static class ClientUser implements Runnable{

        static int threadCount = 0;

        public ClientUser() {
            threadCount ++;
        }

        @Override
        public void run() {
            final int MAX_CAPACITY = 500;
            SocketChannel sc = null;
            try {
                sc = SocketChannel.open();
                sc.connect(new InetSocketAddress(8888));
            } catch (IOException e) {
                throw new RuntimeException(e);
            }

            while (true) {
                Scanner scanner = new Scanner(System.in);
                System.out.print("请输入需要发送的消息：");
                String input = scanner.nextLine();
                if ("exit".equals(input))
                    break;

                int byteSize = input.getBytes().length;

                ByteBuffer buffer = ByteBuffer.allocate(byteSize + 1);
                buffer.put(StandardCharsets.UTF_8.encode(input));
                buffer.flip();
                try {
                    sc.write(buffer);
                    ByteBuffer receive = ByteBuffer.allocate(MAX_CAPACITY);
                    // read()方法没收到回应会阻塞
                    sc.read(receive);
                    receive.flip();
                    System.out.println("服务端回应：" + StandardCharsets.UTF_8.decode(receive));
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }

            }

            try {
                sc.close();
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }

        public static int getThreadCount() {
            return threadCount;
        }
    }

}
