package org.sj.iot.util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Base64;
import java.util.Base64.Decoder;
import java.util.Base64.Encoder;

/**
 * Base64工具类
 *
 * @author shijian
 * @email shijianws@163.com
 * @date 2016-04-05
 */
public class Base64Util {
    private static final Logger LOGGER = LoggerFactory.getLogger(Base64Util.class);

    /**
     * 对字节数组进行Base64加密，出现任何错误都将返回null
     */
    public static byte[] encode(byte[] bs) {
        try {
            return encode(bs, Base64.getEncoder());
        } catch (Exception e) {
            LOGGER.error(e.getMessage(), e);
            return null;
        }
    }

    /**
     * 对字节数组进行Base64加密，出现任何错误都将返回null
     */
    public static String encodeAsString(byte[] bs) {
        try {
            return encodeAsString(bs, Base64.getEncoder());
        } catch (Exception e) {
            LOGGER.error(e.getMessage(), e);
            return null;
        }
    }

    /**
     * 对字符串进行Base64加密，出现任何错误都将返回null
     */
    public static String encode(String value) {
        try {
            return encode(value, Base64.getEncoder());
        } catch (Exception e) {
            LOGGER.error(e.getMessage(), e);
            return null;
        }
    }

    /**
     * 对字符串进行Base64加密，出现任何错误都将返回null
     */
    public static byte[] encodeAsByteArray(String value) {
        try {
            return encode(value.getBytes(Constants.UTF8_CHARSET), Base64.getEncoder());
        } catch (Exception e) {
            LOGGER.error(e.getMessage(), e);
            return null;
        }
    }

    /**
     * 对字节数组进行Url格式的Base64加密，出现任何错误都将返回null
     */
    public static String urlEncode(byte[] bs) {
        try {
            return new String(encode(bs, Base64.getUrlEncoder()));
        } catch (Exception e) {
            LOGGER.error(e.getMessage(), e);
            return null;
        }
    }

    /**
     * 对字符串进行Url格式的Base64加密，出现任何错误都将返回null
     */
    public static String urlEncode(String value) {
        try {
            return encode(value, Base64.getUrlEncoder());
        } catch (Exception e) {
            LOGGER.error(e.getMessage(), e);
            return null;
        }
    }

    /**
     * 对Base64格式字符串进行解密，出现任何错误都将返回null
     */
    public static String decode(String value) {
        try {
            return decodeAsString(value.getBytes(Constants.UTF8_CHARSET), Base64.getDecoder());
        } catch (Exception e) {
            LOGGER.error(e.getMessage(), e);
            return null;
        }
    }

    /**
     * 对Base64格式字符串进行解密，出现任何错误都将返回null
     */
    public static byte[] decodeAsByteArray(String value) {
        try {
            return decode(value.getBytes(Constants.UTF8_CHARSET), Base64.getDecoder());
        } catch (Exception e) {
            LOGGER.error(e.getMessage(), e);
            return null;
        }
    }

    /**
     * 对Base64格式字节数组进行解密，出现任何错误都将返回null
     */
    public static byte[] decode(byte[] bs) {
        try {
            return decode(bs, Base64.getDecoder());
        } catch (Exception e) {
            LOGGER.error(e.getMessage(), e);
            return null;
        }
    }

    /**
     * 对Base64格式字节数组进行解密，出现任何错误都将返回null
     */
    public static String decodeAsString(byte[] bs) {
        try {
            return decodeAsString(bs, Base64.getDecoder());
        } catch (Exception e) {
            LOGGER.error(e.getMessage(), e);
            return null;
        }
    }

    /**
     * 对Base64格式字符串进行解密，出现任何错误都将返回null
     */
    public static String urlDecode(String value) {
        try {
            return decode(value, Base64.getUrlDecoder());
        } catch (Exception e) {
            LOGGER.error(e.getMessage(), e);
            return null;
        }
    }

    /**
     * 对Base64格式字符串进行解密，出现任何错误都将返回null
     */
    public static byte[] urlDecodeAsByteArray(String value) {
        try {
            return decodeByByteArray(value.getBytes(Constants.UTF8_CHARSET), Base64.getUrlDecoder());
        } catch (Exception e) {
            LOGGER.error(e.getMessage(), e);
            return null;
        }
    }

    private static String encode(String value, Encoder encoder) {
        return encodeAsString(value.getBytes(Constants.UTF8_CHARSET), encoder);
    }

    private static String encodeAsString(byte[] bs, Encoder encoder) {
        byte[] encode = encoder.encode(bs);
        return new String(encode);
    }

    private static byte[] encode(byte[] bs, Encoder encoder) {
        return encoder.encode(bs);
    }

    private static String decode(String value, Decoder decoder) {
        return decodeAsString(value.getBytes(Constants.UTF8_CHARSET), decoder);
    }

    private static String decodeAsString(byte[] bs, Decoder decoder) {
        byte[] decode = decoder.decode(bs);
        return new String(decode);
    }

    private static byte[] decode(byte[] bs, Decoder decoder) {
        return decoder.decode(bs);
    }

    private static byte[] decodeByByteArray(byte[] bs, Decoder decoder) {
        return decoder.decode(bs);
    }
}
