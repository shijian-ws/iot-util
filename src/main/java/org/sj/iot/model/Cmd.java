package org.sj.iot.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.google.common.base.Strings;
import org.sj.iot.util.*;

import java.util.HashMap;
import java.util.Map;

/**
 * 命令模型
 *
 * @author shijian
 * @email shijianws@163.com
 * @date 2017-09-06
 */
public class Cmd {
    public static final String GATEWAY_INFO = "gateway_info";

    private String uuid; // 命令ID
    private String source; // 触发命令源定位描述
    private String sender; // 发送人
    private String userId; // 命令所属用户
    private String receiver; // 接收人
    private String device; // 目标设备标识, 单一命令: 设备MAC, 广播命令: FFFFFFFFFFFF
    private DataBody dataBody; // 数据体
    private int expires; // 有效时间, 单位: 秒，<1:永久
    private Integer timeout; // 命令超时时间, 网络或命令本身原因可能导致TCP通道等待的时间, 默认为expires, 不能小于expires，单位: 秒
    private Long creationTime; // 消息创建时间
    private Long sendTime; // 单一命令消息发送时间
    private Long ackTime; // 单一命令消息应答时间
    private Map<String, Long> acks = new HashMap<>(); // 广播的设备应答时间
    private Integer expect; // 期望应答数量
    private Boolean forecExpired; // 是否强制失效

    public String getUuid() {
        return uuid;
    }

    public void setUuid(String uuid) {
        this.uuid = uuid;
    }

    public String getSource() {
        return source;
    }

    public void setSource(String source) {
        this.source = source;
    }

    public String getSender() {
        return sender;
    }

    public void setSender(String sender) {
        this.sender = sender;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getReceiver() {
        return receiver;
    }

    public void setReceiver(String receiver) {
        this.receiver = receiver;
    }

    public String getDevice() {
        return device;
    }

    public void setDevice(String device) {
        this.device = device;
    }

    public DataBody getDataBody() {
        return dataBody;
    }

    public void setDataBody(DataBody dataBody) {
        this.dataBody = dataBody;
    }

    @JsonIgnore
    public String getDataBodyString() {
        DataBody dataBody = this.dataBody;
        if (dataBody != null) {
            return JsonUtil.toJsonString(dataBody);
        }
        return null;
    }

    public int getExpires() {
        return expires;
    }

    public void setExpires(int expires) {
        this.expires = expires;
    }

    public Integer getTimeout() {
        return timeout;
    }

    public void setTimeout(Integer timeout) {
        this.timeout = timeout;
    }

    public Long getCreationTime() {
        return creationTime;
    }

    public void setCreationTime(Long creationTime) {
        this.creationTime = creationTime;
    }

    /**
     * 是否已过期
     */
    public boolean isExpired() {
        if (forecExpired != null && forecExpired == true) {
            // 被强制失效
            return true;
        }
        if (creationTime == null) {
            // 不存在创建时间
            return true;
        }
        if (expires > 0 && Tools.diffSecondTime(creationTime) > expires) {
            // 超时
            return true;
        }
        return false;
    }

    public Long getSendTime() {
        return sendTime;
    }

    public void setSendTime(Long sendTime) {
        this.sendTime = sendTime;
    }

    public Long getAckTime() {
        return ackTime;
    }

    public void setAckTime(Long ackTime) {
        this.ackTime = ackTime;
    }

    public Map<String, Long> getAcks() {
        return new HashMap<>(acks);
    }

    public void setAcks(Map<String, Long> acks) {
        this.acks = acks;
    }

    public Integer getExpect() {
        return expect;
    }

    public void setExpect(Integer expect) {
        this.expect = expect;
    }

    public Boolean getForecExpired() {
        return forecExpired;
    }

    public void setForecExpired(Boolean forecExpired) {
        this.forecExpired = forecExpired;
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("Cmd{");
        sb.append("uuid='").append(uuid).append('\'');
        sb.append(", source='").append(source).append('\'');
        sb.append(", sender='").append(sender).append('\'');
        sb.append(", userId='").append(userId).append('\'');
        sb.append(", receiver='").append(receiver).append('\'');
        sb.append(", device='").append(device).append('\'');
        sb.append(", dataBody=").append(dataBody);
        sb.append(", expires=").append(expires);
        sb.append(", timeout=").append(timeout);
        sb.append(", creationTime=").append(creationTime);
        sb.append(", sendTime=").append(sendTime);
        sb.append(", ackTime=").append(ackTime);
        sb.append(", acks=").append(acks);
        sb.append(", expect=").append(expect);
        sb.append(", forecExpired=").append(forecExpired);
        sb.append('}');
        return sb.toString();
    }

    private Runnable callback;

    /**
     * 设置回调函数
     */
    public void setCallback(Runnable callback) {
        this.callback = callback;
    }

    /**
     * 回调方法
     */
    public void callback(DataBody result) {
        if (dataBody != null && result != null) {
            AckInfo ack = result.getAck();
            dataBody.setAck(ack);
        }
        callback.run();
    }

    /**
     * 生成远程控制命令
     */
    public static Cmd createRemoteControl(String userId, String uuid, String receiver, String device, Map<String, Object> cmdArgs) {
        if (cmdArgs == null || cmdArgs.isEmpty()) {
            throw new IllegalArgumentException("缺少远程控制参数!");
        }
        String data = (String) cmdArgs.get("data");
        CmdInfo cmdInfo = new CmdInfo(); // 命令信息
        if (!Strings.isNullOrEmpty(data)) {
            // 加密后命令
            Integer unzipDataLength = (Integer) cmdArgs.get("unzipDataLength");
            Integer dataLength = (Integer) cmdArgs.get("dataLength");
            String dataid = (String) cmdArgs.get("dataid");
            cmdInfo.unzipDataLength = unzipDataLength; // 原始数据长度
            cmdInfo.dataLength = dataLength; // 编码后数据长度
            cmdInfo.data = data; // 设置加密命令
            cmdInfo.dataid = dataid; // 命令数据包唯一标识
            cmdInfo.checkData(); // 检查加密命令
        } else {
            // 未加密命令
            String dataid = (String) cmdArgs.remove("dataid"); // 移除并获取
            if (dataid == null) {
                dataid = String.valueOf(Tools.getCurrentTimeMillis());
            }
            cmdInfo.cmdMap = new HashMap<>(cmdArgs);
            byte[] cmdData = JsonUtil.toJsonByte(cmdArgs); // 命令参数
            byte[] zipData = ZipUtil.compress(cmdData); // Zip压缩
            String encode = Base64Util.encodeAsString(xor(zipData)); // Zip->XOR->Base64
            cmdInfo.unzipDataLength = cmdData.length; // 原始数据长度
            cmdInfo.dataLength = encode.length(); // 编码后数据长度
            cmdInfo.data = encode; // 设置加密命令
            cmdInfo.dataid = dataid;
        }
        Cmd instance = new Cmd();
        instance.userId = userId;
        instance.uuid = uuid;
        instance.sender = Tools.getServiceIdBase64(); // 发送人
        instance.receiver = receiver; // 接收人
        instance.device = device; // 目标设备
        DataBody dataBody = new DataBody();
        dataBody.setType(DataBody.REMOTE_CONTROL);
        dataBody.setCmd(cmdInfo);  // 设置发送命令
        instance.dataBody = dataBody;
        instance.creationTime = Tools.getCurrentTimeMillis();
        return instance;
    }

    /**
     * 根据请求命令创建响应对象
     *
     * @param cmdinfo      请求的命令
     * @param result       结果状态码
     * @param errormsg     结果状态描述信息
     * @param responseData 响应数据
     * @return
     */
    public static AckInfo createAckInfo(Cmd.CmdInfo cmdinfo, int result, String errormsg, String responseData) {
        AckInfo instance = new AckInfo();
        instance.result = result;
        instance.errormsg = errormsg;
        instance.dataid = cmdinfo.getDataid();
        instance.cmd = cmdinfo.getCmdName();
        if (responseData != null && !responseData.isEmpty()) {
            byte[] cmdData = responseData.getBytes();
            byte[] zipData = ZipUtil.compress(cmdData); // Zip压缩
            String encode = Base64Util.encodeAsString(xor(zipData)); // Zip->XOR->Base64
            instance.unzipDataLength = cmdData.length; // 原始数据长度
            instance.dataLength = encode.length(); // 编码后数据长度
            instance.data = encode; // 设置加密命令
        }
        return instance;
    }

    /**
     * 命令数据加密 密钥
     */
    private static char[] key = {0xFF, 0x3F, 0x48, 0x89, 0x90, 0x3, 0x51, 0x52, 0x5, 0xF7, 0xCB, 0x54, 0x15, 0x4D, 0xFC, 0x17, 0x36, 0xDF, 0x2C, 0xCB, 0xA4, 0xA0, 0x48, 0xCC, 0xFB, 0x56, 0x5A, 0x94, 0x2, 0x72, 0xD3, 0xFF};

    private static byte[] xor(byte[] data) {
        int length = data.length;
        byte[] newData = new byte[length];
        for (int i = 0, y = length; i < y; i++) {
            newData[i] = (byte) (data[i] ^ key[i % 32]); // 异或
        }
        return newData;
    }

    /**
     * 解码
     */
    public static String decrypt(String encode) {
        if (Strings.isNullOrEmpty(encode)) {
            return null;
        }
        byte[] bs = encode.getBytes(Constants.UTF8_CHARSET);
        byte[] decode = Base64Util.decode(bs); // Base64
        byte[] xor = xor(decode); // xor
        byte[] data = ZipUtil.decompress(xor); // unzip
        return new String(data);
    }

    private static abstract class AbstractInfo {
        protected String token; // App与网关通信的凭证，云平台为null
        protected String dataid; // 本次命令数据包的唯一标识，谁发起的命令由谁生成
        protected String data; // 命令加密压缩后的数据包
        protected Integer dataLength; // 命令加密压缩后的数据包长度
        protected Integer unzipDataLength; // 命令未加密压缩的数据包长度

        public String getToken() {
            return token;
        }

        public void setToken(String token) {
            this.token = token;
        }

        public String getDataid() {
            return dataid;
        }

        public void setDataid(String dataid) {
            this.dataid = dataid;
        }

        public String getData() {
            return data;
        }

        public void setData(String data) {
            this.data = data;
        }

        public Integer getDataLength() {
            return dataLength;
        }

        public void setDataLength(Integer dataLength) {
            this.dataLength = dataLength;
        }

        public Integer getUnzipDataLength() {
            return unzipDataLength;
        }

        public void setUnzipDataLength(Integer unzipDataLength) {
            this.unzipDataLength = unzipDataLength;
        }

        /**
         * 获取解码后命令
         */
        public String decryptData() {
            return Cmd.decrypt(data);
        }

        /**
         * 检查数据包，如果不合法则跑出异常
         */
        public void checkData() {
            if (data == null || data.isEmpty()) {
                return;
            }
            int length = data.length();
            if (length == this.dataLength) {
                // 数据长度合法
                try {
                    byte[] bs = data.getBytes(Constants.UTF8_CHARSET);
                    byte[] decode = Base64Util.decode(bs); // Base64
                    byte[] xor = xor(decode); // xor
                    byte[] data = ZipUtil.decompress(xor); // unzip
                    if (data.length == this.unzipDataLength) {
                        return;
                    }
                } catch (Exception e) {
                    throw new IllegalArgumentException(String.format("解密远程控制透传数据包失败: %s", e.getMessage()));
                }
            }
            throw new IllegalArgumentException("远程控制透传数据包校验失败!");
        }

        @Override
        public String toString() {
            StringBuilder builder = new StringBuilder();
            builder.append("dataid=");
            builder.append(dataid);
            builder.append(", data=");
            builder.append(Cmd.decrypt(data));
            return builder.toString();
        }
    }

    /**
     * 命令信息
     */
    public static final class CmdInfo extends AbstractInfo {
        private Map<String, Object> cmdMap; // 原始未加密压缩命令参数

        @JsonIgnore
        public String getCmdName() {
            if (cmdMap == null) {
                synchronized (this) {
                    String data = decryptData();
                    cmdMap = JsonUtil.toJsonMap(data);
                }
            }
            return (String) cmdMap.get("cmd");
        }

        @JsonIgnore
        public Map<String, Object> getCmdMap() {
            if (cmdMap == null) {
                synchronized (this) {
                    String data = decryptData();
                    cmdMap = JsonUtil.toJsonMap(data);
                }
            }
            return new HashMap<>(cmdMap);
        }

        @Override
        public String toString() {
            StringBuilder builder = new StringBuilder();
            builder.append("CmdInfo [");
            builder.append(super.toString());
            builder.append("]");
            return builder.toString();
        }
    }

    /**
     * 应答信息
     */
    public static final class AckInfo extends AbstractInfo {
        private Integer result;
        private String errormsg;
        private String cmd;

        public Integer getResult() {
            return result;
        }

        public void setResult(Integer result) {
            this.result = result;
        }

        public String getErrormsg() {
            return errormsg;
        }

        public void setErrormsg(String errormsg) {
            this.errormsg = errormsg;
        }

        public String getCmd() {
            return cmd;
        }

        public void setCmd(String cmd) {
            this.cmd = cmd;
        }

        @Override
        public String toString() {
            StringBuilder builder = new StringBuilder();
            builder.append("AckInfo [result=");
            builder.append(result);
            builder.append(", errormsg=");
            builder.append(errormsg);
            builder.append(", cmd=");
            builder.append(cmd);
            builder.append(", ");
            builder.append(super.toString());
            builder.append("]");
            return builder.toString();
        }
    }
}
