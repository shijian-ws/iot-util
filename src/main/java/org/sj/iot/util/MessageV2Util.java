package org.sj.iot.util;

import org.sj.iot.model.DataBody;
import org.sj.iot.model.Message;
import org.sj.iot.model.MessageV2;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.LongAdder;

/**
 * 消息工具类
 *
 * @author shijian
 * @email shijianws@163.com
 * @date 2017-09-12
 */
public class MessageV2Util {
    private static final Logger LOGGER = LoggerFactory.getLogger(MessageV2Util.class);

    private static final Map<String, LongAdder> messageId = new ConcurrentHashMap<>();

    /**
     * 清除消息ID生成器
     */
    public static void clearMessageId(String mac) {
        messageId.remove(String.format("%s%s", Constants.MAC_MESSAGE_ID, mac));
    }

    /**
     * 根据MAC生成消息ID
     */
    public static byte createMessageId(String mac) {
        String key = (Constants.MAC_MESSAGE_ID + mac).intern();
        LongAdder counter = messageId.get(key);
        if (counter == null) {
            synchronized (messageId) {
                if (counter == null) {
                    counter = new LongAdder(); // 0
                    messageId.put(key, counter);
                }
            }
        }
        counter.increment(); // +1
        byte messageId = counter.byteValue();
        if (messageId == Byte.MAX_VALUE) {
            counter.reset();
        }
        return messageId;
    }

    /**
     * 根据判断请求魔数是否正确
     */
    public static boolean checkMagic(int magic) {
        return Constants.V2.MAGIC_NUMBER == magic;
    }

    /**
     * 获取业务数据长度
     */
    public static int getDataLength(int packet) {
        return packet - (Constants.V2.VERSION_LEN + Constants.V2.MAC_LEN + Constants.V2.MESSAGE_ID_LEN + Constants.V2.STATUS_LEN + Constants.V2.CHECK_SUM_LEN);
    }

    /**
     * 获取从version到status逐个进行异或后的校验码
     */
    public static byte getCheckSum(int magic, int packetLength, byte version, long mac, byte messageId, byte[] data, byte status) {
        int checkSum = version ^ messageId ^ status;
        // 异或魔数头
        for (int i = Constants.V2.MAGIC_NUMBER_LEN - 1; i >= 0; i--) {
            checkSum = checkSum ^ (byte) (magic >>> (i * 8) & 0xFF);
        }
        // 异或包体长度
        for (int i = Constants.V2.PACKET_LEN - 1; i >= 0; i--) {
            checkSum = checkSum ^ (byte) (packetLength >>> (i * 8) & 0xFF);
        }
        // 异或MAC地址长度
        for (int i = Constants.V2.MAC_LEN - 1; i >= 0; i--) {
            checkSum = checkSum ^ (byte) (mac >>> (i * 8) & 0xFF);
        }
        for (byte b : data) {
            checkSum = checkSum ^ b;
        }
        return (byte) checkSum;
    }

    /**
     * 将消息对象转换成字节数组
     */
    public static byte[] messageToByte(Message message) {
        return ZipUtil.compress(JsonUtil.toJsonByte(message));
    }

    /**
     * 将字节数组转换成对象
     */
    public static MessageV2 byteToMessage(byte[] bs) {
        return JsonUtil.toObject(ZipUtil.decompress(bs), MessageV2.class);
    }

    public static Map<String, Object> get(Message message) {
        return get(message, Map.class);
    }

    public static <T> T get(Message message, Class<T> returnType) {
        byte[] data = message.getData();
        if (data != null && data.length > 0) {
            byte[] bs = ZipUtil.decompress(data);
            return JsonUtil.toObject(bs, returnType);
        }
        return null;
    }

    /**
     * 生成消息对象
     */
    public static Message createMessage(String macHex, byte messageId) {
        long mac = Tools.macToLong(macHex);
        return createMessage(mac, messageId, (byte[]) null);
    }

    /**
     * 生成消息对象
     */
    public static Message createMessage(String macHex, byte messageId, Object json) {
        long mac = Tools.macToLong(macHex);
        return createMessage(mac, messageId, JsonUtil.toJsonString(json));
    }

    /**
     * 生成消息对象
     */
    public static Message createMessage(String macHex, byte messageId, String data) {
        long mac = Tools.macToLong(macHex);
        return createMessage(mac, messageId, data);
    }

    /**
     * 生成消息对象
     */
    public static Message createMessage(String macHex, byte messageId, byte[] data) {
        long mac = Tools.macToLong(macHex);
        return createMessage(mac, messageId, data);
    }

    /**
     * 生成消息对象
     */
    public static Message createMessage(long mac, byte messageId, String data) {
        byte[] bs = data == null ? null : data.getBytes(Constants.UTF8_CHARSET);
        return createMessage(mac, messageId, bs);
    }

    /**
     * 生成消息对象
     */
    public static Message createMessage(long mac, byte messageId, byte[] data) {
        int magic = Constants.V2.MAGIC_NUMBER;
        byte version = Constants.V2.VERSION;
        byte[] bs = null;
        if (data != null) {
            bs = ZipUtil.compress(data); // zip压缩
        } else {
            bs = new byte[0];
        }
        byte status = Constants.V2.STATUS;
        int packetLength = Constants.V2.VERSION_LEN + Constants.V2.MAC_LEN + Constants.V2.MESSAGE_ID_LEN + bs.length + Constants.V2.STATUS_LEN + Constants.V2.CHECK_SUM_LEN;
        byte checkSum = getCheckSum(magic, packetLength, version, mac, messageId, bs, status);
        return new MessageV2(magic, packetLength, version, mac, messageId, bs, status, checkSum);
    }

    /**
     * 通用响应
     */
    public static Message responseOK(Message request) {
        if (request == null) {
            throw new IllegalArgumentException("未找到请求消息，无法根据请求消息生成响应消息对象!");
        }
        String mac = request.getMacHex();
        byte messageId = request.getMessageId();
        String type = request.getDataBody().getType(); // 获取请求类型
        return createMessage(mac, messageId, new DataBody(type, Tools.getCurrentTimeMillis() / 1000, "ok"));
    }
}