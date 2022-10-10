package cn.bytebuf;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufAllocator;
import io.netty.buffer.ByteBufUtil;

import java.util.Arrays;

public class TestSlice {
    public static void main(String[] args) {

        ByteBuf buf = ByteBufAllocator.DEFAULT.buffer(10);
        buf.writeBytes(new byte[]{'a', 'b', 'c', 'd', 'e', 'f', 'g', 'h'});
        System.out.println(buf);

        // 切片过程中，没有发生数据复制
        ByteBuf buf1 = buf.slice(0, 3);
        ByteBuf buf2 = buf.slice(1, 3);
        System.out.println(Arrays.toString(ByteBufUtil.getBytes(buf1)));
        System.out.println(Arrays.toString(ByteBufUtil.getBytes(buf2)));
        buf1.setByte(0, 'z');
        buf.setByte(3, 'y');
        System.out.println(Arrays.toString(ByteBufUtil.getBytes(buf)));
        System.out.println(Arrays.toString(ByteBufUtil.getBytes(buf2)));

        // buf1的废弃部分 不会影响 buf2的读写指针
        byte b = buf1.readByte();
        System.out.println(Arrays.toString(ByteBufUtil.getBytes(buf1)));
        System.out.println(Arrays.toString(ByteBufUtil.getBytes(buf2)));

        // 切片后不能在切片后追加元素, 有容量限制
        // buf1.writeByte(97);

    }
}
