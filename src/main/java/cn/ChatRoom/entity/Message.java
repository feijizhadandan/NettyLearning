package cn.ChatRoom.entity;

import lombok.Data;

/**
 * Message 消息基类
 */
@Data
public class Message {

    /**
     * 请求序号。为了双工通讯，提供异步能力。
     */
    protected int sequenceId;

    /**
     * 指令类型。指明该数据包是用于什么业务。
     */
    protected int messageType;

}
