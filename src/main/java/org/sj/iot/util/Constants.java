package org.sj.iot.util;

import java.nio.charset.Charset;

/**
 * 常量类
 *
 * @author shijian
 * @email shijianws@163.com
 * @date 2017-09-06
 */
public class Constants {
    /**
     * V2 版本常亮
     */
    public static final class V2 {
        /**
         * 魔数头字节长度
         */
        public static final int MAGIC_NUMBER_LEN = 4;
        /**
         * 数据包字节长度,从version到checksum
         */
        public static final int PACKET_LEN = 4;
        /**
         * 版本字节长度
         */
        public static final int VERSION_LEN = 1;
        /**
         * MAC地址字节长度
         */
        public static final int MAC_LEN = 6;
        /**
         * 消息ID字节长度
         */
        public static final int MESSAGE_ID_LEN = 1;
        /**
         * 状态字节长度
         */
        public static final int STATUS_LEN = 1;
        /**
         * 校验码字节长度
         */
        public static final int CHECK_SUM_LEN = 1;

        /**
         * 魔数头
         */
        public static final int MAGIC_NUMBER = 0x88 << (8 * 3) | 0x88 << (8 * 2) | 0x88 << 8 | 0x88;
        /**
         * 数据包版本号
         */
        public static final byte VERSION = 0x02;
        /**
         * 数据包状态值
         */
        public static final byte STATUS = 0x00;
    }

    /**
     * TCP服务器根路径
     */
    private static final String TCP_SERVER_ROOT_PATH = "/tcpserver";
    /**
     * 服务提供商标识路径
     */
    public static final String TCP_SERVER_PROVIDER_PATH = TCP_SERVER_ROOT_PATH + "/provider"; // /tcpserver/provider/Base64(serverId)
    /**
     * TCP服务端下网关设备标识路径
     */
    public static final String DEVICE_GATEWAY_PATH = TCP_SERVER_ROOT_PATH + "/gateway/entity"; // /tcpserver/gateway/MAC data=Base64(serverId)
    /**
     * 网关设备固件信息标识路径
     */
    public static final String GATEWAY_FIRMWARE_PATH = TCP_SERVER_ROOT_PATH + "/gateway/firmware"; // /tcpserver/gateway/firmware/version data=Base64(firmware_info)
    /**
     * 云平台根路径
     */
    private static final String CLOUD_SERVER_PATH = "/cloudserver";
    /**
     * 服务提供商标识路径
     */
    private static final String CLOUD_SERVER_PROVIDER_PATH = CLOUD_SERVER_PATH + "/provider";
    /**
     * 消息队列根路径
     */
    private static final String MQ_PATH = "/mq";
    /**
     * 单播标识
     */
    public static final String UNICAST_ID = "000000000000";
    /**
     * 广播标识
     */
    public static final String BROADCAST_ID = "FFFFFFFFFFFF";

    /**
     * TCP服务端消息队列
     */
    private static final String TCP_SERVER_MQ_PATH = MQ_PATH + "/tcpserver"; // /mq/tcpserver
    /**
     * 单一消息
     */
    public static final String TCP_SERVER_UNICAST_MQ_PATH = TCP_SERVER_MQ_PATH + '/' + UNICAST_ID; // /mq/tcpserver/000000000000/uuid
    /**
     * 广播消息
     */
    public static final String TCP_SERVER_BROADCAST_MQ_PATH = TCP_SERVER_MQ_PATH + '/' + BROADCAST_ID; // /mq/tcpserver/FFFFFFFFFFFF/uuid

    /**
     * 云平台消息队列
     */
    private static final String CLOUD_SERVER_MQ_PATH = MQ_PATH + "/cloudserver"; // /mq/cloudserver
    /**
     * 单一消息
     */
    private static final String CLOUD_SERVER_UNICAST_MQ_PATH = CLOUD_SERVER_MQ_PATH + '/' + UNICAST_ID; // /mq/cloudserver/000000000000/uuid
    /**
     * 广播消息
     */
    public static final String CLOUD_SERVER_BROADCAST_MQ_PATH = CLOUD_SERVER_MQ_PATH + '/' + BROADCAST_ID; // /mq/cloudserver/FFFFFFFFFFFF/uuid

    /**
     * 生成消息ID键的前缀，格式: "message_id_" + MAC
     */
    public static final String MAC_MESSAGE_ID = "message_id_";

    /**
     * UTF-8字符集
     */
    public static final Charset UTF8_CHARSET = Charset.forName("UTF-8"); // 字符集

    /**
     * 字符集名称
     */
    public static final String UTF8_CHARSET_NAME = UTF8_CHARSET.name();
}
