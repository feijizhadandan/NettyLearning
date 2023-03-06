package cn.ChatRoom.protocol.codec;

import cn.ChatRoom.entity.Message;
import cn.ChatRoom.entity.TextMessage;
import com.alibaba.fastjson.JSON;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufUtil;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.ByteToMessageCodec;
import lombok.extern.slf4j.Slf4j;

import java.nio.charset.StandardCharsets;
import java.util.List;

/**
 *  MessageCodec 自定义消息体的编解码器
 *  泛型是自定义的消息类型
 */

@Slf4j
public class MessageCodec extends ByteToMessageCodec<Message> {
    /**
     * 编码器
     * @param channelHandlerContext channel处理器的上下文变量
     * @param message 需要被编码的对象
     * @param byteBuf 编码的字节流结果
     */
    @Override
    protected void encode(ChannelHandlerContext channelHandlerContext, Message message, ByteBuf byteBuf) throws Exception {
        // 魔数：6 字节
        byteBuf.writeBytes("zNetty".getBytes(StandardCharsets.UTF_8));
        // 版本号：1 字节
        byteBuf.writeByte(1);
        // 序列化方式：1 字节
        byteBuf.writeByte(1);
        // 指令类型：4 字节
        byteBuf.writeInt(message.getMessageType());
        // 请求序号：4 字节
        byteBuf.writeInt(message.getSequenceId());

        // Message 对象序列化后转换为字节数组
        String toJSONString = JSON.toJSONString(message);
        byte[] jsonBytes = toJSONString.getBytes();

        // 正文长度：4 字节
        byteBuf.writeInt(jsonBytes.length);
        // 正文
        byteBuf.writeBytes(jsonBytes);
    }

    /**
     * 解码器
     * @param channelHandlerContext channel处理器的上下文变量
     * @param byteBuf 需要解码的字节流
     * @param list 解码出来的结果
     */
    @Override
    protected void decode(ChannelHandlerContext channelHandlerContext, ByteBuf byteBuf, List<Object> list) throws Exception {
        String magicCode = new String(ByteBufUtil.getBytes(byteBuf.readBytes(6)));
        byte version = byteBuf.readByte();
        byte serializerType = byteBuf.readByte();
        int messageType = byteBuf.readInt();
        int sequenceId = byteBuf.readInt();
        int msgLength = byteBuf.readInt();

        // 根据正文长度读取消息的字节数组
        byte[] msgBytes = new byte[msgLength];
        byteBuf.readBytes(msgBytes, 0, msgLength);
        // 将 Message 的字节数组转换为 Json 字符串
        String msgJson = new String(msgBytes);
        // Json 反序列化
        Message message = JSON.parseObject(msgJson, TextMessage.class);

        log.debug("{} {} {} {} {} {}", magicCode, version, serializerType, messageType, sequenceId, msgLength);
        // 将内容放入容器传到下一个处理器
        list.add(message);
    }
}
