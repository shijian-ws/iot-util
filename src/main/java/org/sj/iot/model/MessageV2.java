package org.sj.iot.model;

import org.sj.iot.util.MessageV2Util;
import org.sj.iot.util.Tools;

import java.util.Objects;

/**
 * 网关TCP通信协议栈消息报文模型
 *
 * @author shijian
 * @email shijianws@163.com
 * @date 2017-09-13
 */
public class MessageV2 implements Message {
    private int magic; // 魔数, 4个字节
    private int packetLength; // 包长度, 4字节, 从version到status的长度
    private byte version; // 版本，1字节
    private long mac; // mac地址, 3字节
    private byte messageId; // 消息ID, 1个字节，上行指令,服务端原值返回，下行指令,远程设备原值返回
    private byte[] data; // 数据体，变长
    private DataBody dataBody; // 云端数据体
    private byte status; // 状态，1个字节
    private byte checkSum; // 校验码，1个字节，从version到MessageId的总字节的逐个异或值

    private String macHex; // mac的字符串表现形式

    public MessageV2() {
    }

    public MessageV2(int magic, int packetLength, byte version, long mac, byte messageId, byte[] data, byte status, byte checkSum) {
        this.magic = magic;
        this.packetLength = packetLength;
        this.version = version;
        this.setMac(mac);
        this.messageId = messageId;
        this.data = data;
        this.status = status;
        this.checkSum = checkSum;
    }

    @Override
    public int getMagic() {
        return magic;
    }

    @Override
    public void setMagic(int magic) {
        this.magic = magic;
    }

    @Override
    public int getPacketLength() {
        return packetLength;
    }

    @Override
    public void setPacketLength(int packetLength) {
        this.packetLength = packetLength;
    }

    @Override
    public byte getVersion() {
        return version;
    }

    @Override
    public void setVersion(byte version) {
        this.version = version;
    }

    @Override
    public long getMac() {
        return mac;
    }

    @Override
    public void setMac(long mac) {
        this.mac = mac;
        this.macHex = Tools.toHex(mac, 6).toUpperCase();
    }

    @Override
    public String getMacHex() {
        return macHex;
    }

    @Override
    public byte getMessageId() {
        return messageId;
    }

    @Override
    public void setMessageId(byte messageId) {
        this.messageId = messageId;
    }

    @Override
    public byte[] getData() {
        return data;
    }

    @Override
    public DataBody getDataBody() {
        if (dataBody == null && data != null && data.length > 0) {
            dataBody = MessageV2Util.get(this, DataBody.class);
        }
        return dataBody;
    }

    @Override
    public void setData(byte[] data) {
        this.data = data;
    }

    @Override
    public byte getStatus() {
        return status;
    }

    @Override
    public void setStatus(byte status) {
        this.status = status;
    }

    @Override
    public byte getCheckSum() {
        return checkSum;
    }

    @Override
    public void setCheckSum(byte checkSum) {
        this.checkSum = checkSum;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof MessageV2)) {
            return false;
        }
        MessageV2 messageV2 = (MessageV2) o;
        return mac == messageV2.mac && messageId == messageV2.messageId;
    }

    @Override
    public int hashCode() {
        return Objects.hash(mac, messageId);
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("MessageV2{");
        sb.append("magic=").append(magic);
        sb.append(", packetLength=").append(packetLength);
        sb.append(", version=").append(version);
        sb.append(", mac=").append(macHex);
        sb.append(", messageId=").append(messageId);
        sb.append(", data=").append("[...]");
        sb.append(", status=").append(status);
        sb.append(", checkSum=").append(checkSum);
        sb.append('}');
        return sb.toString();
    }
}
