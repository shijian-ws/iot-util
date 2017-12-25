package org.sj.iot.model;

/**
 * 消息报文模型
 *
 * @author shijian
 * @email shijianws@163.com
 * @date 2017-09-13
 */
public interface Message {
    int getMagic();

    void setMagic(int magic);

    int getPacketLength();

    void setPacketLength(int packetLength);

    byte getVersion();

    void setVersion(byte version);

    long getMac();

    void setMac(long mac);

    String getMacHex();

    byte getMessageId();

    void setMessageId(byte messageId);

    byte[] getData();

    void setData(byte[] data);

    DataBody getDataBody();

    byte getStatus();

    void setStatus(byte status);

    byte getCheckSum();

    void setCheckSum(byte checkSum);
}
