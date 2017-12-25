package org.sj.iot.util;

import com.google.common.base.Preconditions;
import com.google.common.hash.HashFunction;
import com.google.common.hash.Hashing;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Objects;

/**
 * 消息摘要工具类
 * 
 * @author shijian
 * @email shijianws@163.com
 * @date 2016-04-05
 */
@SuppressWarnings("deprecation")
public class MessageDigestUtil {
    private static final Logger LOGGER = LoggerFactory.getLogger(MessageDigestUtil.class);

    /**
     * MD5加密摘要对象
     */
    private static HashFunction md5;
    /**
     * SHA1加密摘要对象
     */
    private static HashFunction sha1;
    /**
     * SHA512加密摘要对象
     */
    private static HashFunction sha512;
    /**
     * CRC32加密摘要对象
     */
    private static HashFunction crc32;

    static {
        try {
            md5 = Hashing.md5();
            sha1 = Hashing.sha1();
            sha512 = Hashing.sha512();
            crc32 = Hashing.crc32();
        } catch (Exception e) {
            if (LOGGER.isErrorEnabled()) {
                LOGGER.error("初始化摘要信息工具类失败: {}", e.getMessage());
            }
            throw new RuntimeException("初始化摘要信息工具类失败!", e);
        }
    }

    /**
     * 摘要加密
     * 
     * @param func 摘要对象
     * @param value 加密字符串
     * @return
     */
    private static final String md(HashFunction func, String value) {
        Objects.requireNonNull(value, "加密字符串不能为空!");
        try {
            return md(func, value.getBytes(Constants.UTF8_CHARSET));
        } catch (Exception e) {
            if (LOGGER.isErrorEnabled()) {
                LOGGER.error(e.getMessage());
            }
            throw new RuntimeException(e.getMessage(), e);
        }
    }

    /**
     * 摘要加密
     */
    private static final String md(HashFunction func, byte[] data) {
        Objects.requireNonNull(data, "加密数据不能为空!");
        return func.newHasher().putBytes(data).hash().toString().toLowerCase();
    }

    /**
     * 将一个字符串进行MD5加密，并返回小写的加密32位字符串
     */
    public static final String md5(String value) {
        return md5(value, 32);
    }

    /**
     * 将一个字符串进行MD5加密，并返回小写的加密32位字符串
     */
    public static final String md5(byte[] data) {
        return md5(data, 32);
    }

    /**
     * 将一个字符串进行MD5加密，并返回小写的加密16位或32位字符串
     */
    public static final String md5(String value, int len) {
        String message = md(md5, value);
        if (len == 16) {
            return message.substring(8, 24);
        }
        return message;
    }

    /**
     * 将一个字符串进行MD5加密，并返回小写的加密16位或32位字符串
     */
    public static final String md5(byte[] data, int len) {
        String message = md(md5, data);
        if (len == 16) {
            return message.substring(8, 24);
        }
        return message;
    }

    /**
     * 将一个字符数组进行SHA1加密，并返回小写的加密40位字符串
     */
    public static final String sha1(String value) {
        return md(sha1, value);
    }

    /**
     * 将一个字符数组进行SHA1加密，并返回小写的加密40位字符串
     */
    public static final String sha1(byte[] data) {
        return md(sha1, data);
    }

    /**
     * 将一个字符数组进行SHA512加密，并返回小写的加密128位字符串
     */
    public static final String sha512(String value) {
        return md(sha512, value);
    }

    /**
     * 将一个字符数组进行SHA512加密，并返回小写的加密128位字符串
     */
    public static final String sha512(byte[] data) {
        return md(sha512, data);
    }

    /**
     * 将一个字符数组进行CRC32加密，并返回小写的加密8位字符串
     */
    public static final String crc32(String value) {
        return md(crc32, value);
    }

    /**
     * 将一个字符数组进行CRC32加密，并返回小写的加密8位字符串
     */
    public static final String crc32(byte[] data) {
        return md(crc32, data);
    }

    /**
     * 将明文密码进行MD5加密、加盐，返回小写的加密32位字符串
     */
    public static final String processPassword(String password, String salt) {
        return saltPassword(md(md5, password), salt);
    }

    /**
     * 将MD5加密的密码进行加盐，返回小写的加密32位字符串
     */
    public static final String saltPassword(String password, String salt) {
        Objects.requireNonNull(password, "密码不能为空!");
        Objects.requireNonNull(salt, "加盐值不能为空!");
        Preconditions.checkArgument(password.length() == 32, "密码必须为32位md5串!");
        try {
            int hashCode = salt.hashCode();
            StringBuilder buf = new StringBuilder();
            for (int x = 0, y = 32; x < y; x++) {
                // 加盐
                buf.append(password.charAt(x));
                buf.append(Integer.toHexString(hashCode >>> x & 0xF));
            }
            return md(md5, buf.toString());
        } catch (Exception e) {
            if (LOGGER.isErrorEnabled()) {
                LOGGER.error("加盐失败: {}", e.getMessage());
            }
            throw new RuntimeException(e.getMessage(), e);
        }
    }
}
