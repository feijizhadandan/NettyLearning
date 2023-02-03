package cn.Netty_1.bytebuf;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufAllocator;
import io.netty.buffer.ByteBufUtil;

public class TestByteBuf {
    public static void main(String[] args) {


        ByteBuf buffer = ByteBufAllocator.DEFAULT.buffer(5);

        // 1、写入整数（整数占4个字节，因此打印了四个元素）
        buffer.writeInt(5);
        System.out.println(buffer);
        byte[] bytes = ByteBufUtil.getBytes(buffer);
        for (byte aByte : bytes) {
            System.out.println(aByte);
        }

        // 2、自动扩容
        buffer.writeInt(8);
        System.out.println(buffer);
        bytes = ByteBufUtil.getBytes(buffer);
        for (byte aByte : bytes) {
            System.out.println(aByte);
        }
    }
}
