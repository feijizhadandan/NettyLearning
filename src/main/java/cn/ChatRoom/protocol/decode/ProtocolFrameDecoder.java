package cn.ChatRoom.protocol.decode;

import io.netty.handler.codec.LengthFieldBasedFrameDecoder;

/**
 *  因为使用帧解码器的参数一般不会变，所以单独封装一个类，就不会将参数暴露在外部代码中。
 */
public class ProtocolFrameDecoder extends LengthFieldBasedFrameDecoder {

    public ProtocolFrameDecoder() {
        super(1024, 16, 4, 0, 0);
    }

    public ProtocolFrameDecoder(int maxFrameLength, int lengthFieldOffset, int lengthFieldLength, int lengthAdjustment, int initialBytesToStrip) {
        super(maxFrameLength, lengthFieldOffset, lengthFieldLength, lengthAdjustment, initialBytesToStrip);
    }
}
