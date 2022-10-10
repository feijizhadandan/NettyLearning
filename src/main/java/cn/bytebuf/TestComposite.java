package cn.bytebuf;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufAllocator;
import io.netty.buffer.ByteBufUtil;
import io.netty.buffer.CompositeByteBuf;

import java.util.Arrays;

public class TestComposite {
    public static void main(String[] args) {

        ByteBuf buf1 = ByteBufAllocator.DEFAULT.buffer();
        buf1.writeBytes(new byte[]{'1', '2', '3', '4'});
        System.out.println(buf1);

        ByteBuf buf2 = ByteBufAllocator.DEFAULT.buffer();
        buf2.writeBytes(new byte[]{1, 2, 3, 4});
        System.out.println(buf2);

        // 零拷贝，合成多个 ByteBuf
        CompositeByteBuf buffer = ByteBufAllocator.DEFAULT.compositeBuffer();
        // 第一个参数如果不加，则写指针不会移动
        buffer.addComponents(true, buf1, buf2);
        System.out.println(buffer);
        System.out.println(Arrays.toString(ByteBufUtil.getBytes(buffer)));

    }
}
