package org.sj.iot.util;

import com.google.common.base.Splitter;
import com.google.common.base.Strings;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.lang.management.ManagementFactory;
import java.lang.management.RuntimeMXBean;
import java.lang.reflect.Array;
import java.math.BigInteger;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.nio.ByteBuffer;
import java.text.SimpleDateFormat;
import java.time.*;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.LongAdder;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Stream;

/**
 * 通用工具
 *
 * @author shijian
 * @email shijianws@163.com
 * @date 2016-04-05
 */
public final class Tools {
    private static final Logger LOGGER = LoggerFactory.getLogger(Tools.class);

    private Tools() {
        throw new RuntimeException("Tools类为工具类不允许实例化!");
    }

    private static String hostName; // 当前主机名称
    private static String hostIp; // 当前主机IP
    private static String macHex; // 当前主机网卡MAC地址
    private static long macLong; // 当前主机网卡MAC地址的long类型数值
    private static long pid; // 当前JVM进程ID

    static {
        try {
            DatagramSocket socket = new DatagramSocket();
            socket.connect(InetAddress.getByName("114.114.114.114"), 53);
            InetAddress addr = socket.getLocalAddress(); // 外网通信的ip 不一定是公网ip
            hostIp = addr.getHostAddress();
            socket.close();
            NetworkInterface networkInterface = NetworkInterface.getByInetAddress(addr);
            if (networkInterface != null) {
                byte[] mac = networkInterface.getHardwareAddress();
                macHex = toHex(mac).toUpperCase();
                macLong = macToLong(mac);
            }
        } catch (Exception e) {
            if (LOGGER.isErrorEnabled()) {
                LOGGER.error(Tools.formatString("连接阿里DNS服务器失败: {}", e.getMessage()), e);
            }
        }
        try {
            RuntimeMXBean runtimeMXBean = ManagementFactory.getRuntimeMXBean();
            String name = runtimeMXBean.getName(); // pid@hostname
            int index = name.lastIndexOf('@');
            if (index > -1) {
                pid = Long.parseLong(name.substring(0, index));
                hostName = name.substring(index + 1);
            }
        } catch (Exception e) {
            if (LOGGER.isErrorEnabled()) {
                LOGGER.error(Tools.formatString("解析JVM进程ID与主机名称失败: {}", e.getMessage()), e);
            }
        }
    }

    /**
     * 获取本机主机名
     */
    public static String getLocalHostName() {
        return hostName;
    }

    /**
     * 获取本机主机名
     */
    public static String getLocalHostIp() {
        return hostIp;
    }

    /**
     * 获取本机MAC
     */
    public static String getLocalHostMac() {
        return macHex;
    }

    /**
     * 获取JVM进程hex(ID)
     */
    public static String getJvmPid() {
        return toHex(pid, 8);
    }

    /**
     * 获取当前线程hex(ID)
     */
    public static String getThreadId() {
        return toHex(Thread.currentThread().getId(), 8);
    }

    /**
     * 获取当前服务ID
     */
    public static String getServiceIdBase64() {
        String serverId = String.format("%s\n%s\n%s", macHex, hostIp, getJvmPid());
        return Base64Util.encode(serverId);
    }

    /**
     * 根据前缀(3位)，JVM当前时间戳(3位)，机器码(3位)，PID(2位)，计数器(3位)生成一个16进制UUID
     */
    public static String createUUID() {
        return createUUID(getCurrentTimeMillis());
    }

    /**
     * 根据前缀(3位)，时间戳(3位)，机器码(3位)，PID(2位)，计数器(3位)生成一个16进制UUID
     */
    public static String createUUID(long timestramp) {
        return createUUID(timestramp, 16);
    }

    /**
     * 根据前缀(3位)，时间戳(3位)，机器码(3位)，PID(2位)，计数器(3位)生成一个UUID
     */
    public static String createUUID(long timestramp, int radix) {
        return createUUID(timestramp, radix, false);
    }

    /**
     * 获取随机数对象
     */
    public static Random getRandom() {
        return ThreadLocalRandom.current();
    }

    /**
     * 获取一个随机整数
     */
    public static int getRandomInt() {
        return getRandom().nextInt();
    }

    /**
     * 获取一个指定范围内的随机整数
     *
     * @param end 范围 不包含
     * @return
     */
    public static int getRandomInt(int end) {
        return getRandom().nextInt(end);
    }

    private static final LongAdder counter = new LongAdder(); // 累加计数器

    static {
        counter.add(getRandomInt()); // 初始化一个随机值
    }

    /**
     * 获取一个数值的右移指定位后得字节值
     */
    private static byte getRightByte(long value, int bit) {
        if (value == 0) {
            return 0;
        }
        return (byte) (value >> bit);
    }

    private static int getPrefixCode() {
        StackTraceElement[] stackTraces = Thread.currentThread().getStackTrace();
        // 跳过getStackTrace(),getPrefixCode()方法
        Predicate<? super Object> filter = Tools.class::equals;
        StackTraceElement stackTrace = Stream.of(Thread.currentThread().getStackTrace()).skip(2).filter(filter.negate()).findFirst().get();
        StringBuilder buf = new StringBuilder();
        buf.append(stackTrace.getFileName()).append(stackTrace.getMethodName());
        return buf.toString().hashCode();
    }

    /**
     * 根据前缀(3位)，时间戳(3位)，机器码(3位)，PID(2位)，计数器(3位)生成一个UUID
     *
     * @param timestramp 时间戳
     * @param radix      uuid字符进制范围，默认16进制
     * @param random     是否随机前缀
     * @return
     */
    public static String createUUID(long timestramp, int radix, boolean random) {
        byte[] bytes = new byte[14];
        int code = getRandomInt();
        // 前缀
        if (!random) {
            code = getPrefixCode();
        }
        bytes[0] = getRightByte(code, 16);
        bytes[2] = (byte) code;
        // 时间戳
        bytes[3] = getRightByte(timestramp, 24);
        bytes[4] = getRightByte(timestramp, 16);
        bytes[5] = (byte) timestramp;
        // 机器码
        bytes[6] = getRightByte(macLong, 16);
        bytes[7] = getRightByte(macLong, 8);
        bytes[8] = (byte) macLong;
        // PID
        bytes[9] = getRightByte(pid, 8);
        bytes[10] = (byte) pid;
        // 计数器
        counter.increment(); // 自增操作
        long count = counter.longValue(); // 获取值
        bytes[11] = getRightByte(count, 16);
        bytes[12] = getRightByte(count, 8);
        bytes[13] = (byte) count;
        if (radix < 16 || radix > 36) {
            // 16进制
            return toHex(bytes);
        }
        // 指定进制
        return new BigInteger(1, bytes).toString(radix);
    }

    /**
     * 判断是否为Linux系统
     */
    public static boolean isLinux() {
        return System.getProperty("os.name").toLowerCase().indexOf("linux") != -1;
    }

    /**
     * 获取世界协调时时间戳，无时区
     */
    public static long getCurrentTimeMillis() {
        return Clock.systemUTC().millis();
    }

    /**
     * 获取当前日期的格式化字符串，按照"yyyy-MM-dd"格式化
     */
    public static String formatDate() {
        return formatDateTime("yyyy-MM-dd", getCurrentTimeMillis());
    }

    /**
     * 将指定日期时间按照"yyyy-MM-dd"格式化为字符串
     *
     * @param time 时间戳
     * @return 如果time为null则返回null
     */
    public static String formatDate(Long time) {
        return formatDateTime("yyyy-MM-dd", time);
    }

    /**
     * 将当前日期时间按照"yyyy-MM-dd HH:mm:ss"格式转换为字符串
     */
    public static String formatDateTime() {
        return formatDateTime("yyyy-MM-dd HH:mm:ss", getCurrentTimeMillis());
    }

    /**
     * 将指定日期时间按照"yyyy-MM-dd HH:mm:ss"格式转换为字符串
     */
    public static String formatDateTime(Long time) {
        return formatDateTime("yyyy-MM-dd HH:mm:ss", time);
    }

    /**
     * 将当前时间按照自定义格式转换为字符串
     */
    public static String formatDateTime(String pattern) {
        return formatDateTime(pattern, getCurrentTimeMillis());
    }

    private static Map<String, DateTimeFormatter> FORMAT_CACHE = new ConcurrentHashMap<>();

    private static DateTimeFormatter getFormatter(String pattern) {
        DateTimeFormatter formatter = FORMAT_CACHE.get(pattern);
        if (formatter == null) {
            formatter = DateTimeFormatter.ofPattern(pattern); // 设置格式化样式
            FORMAT_CACHE.put(pattern, formatter);
        }
        return formatter;
    }

    /**
     * 将指定日期时间按照自定义格式转换为字符串
     */
    public static String formatDateTime(String pattern, Long time) {
        if (time == null) {
            return null;
        }
        return getFormatter(pattern).format(Instant.ofEpochMilli(time).atZone(ZoneOffset.UTC));
    }

    /**
     * 将指定日期时间字符串按照自定义格式转换为时间戳
     */
    // TODO 未使用新JDK API
    public static Long parseDateTime(String pattern, String time) {
        if (time == null) {
            return null;
        }
        try {
            return new SimpleDateFormat(pattern).parse(time).getTime();
        } catch (Exception e) {
            if (LOGGER.isErrorEnabled()) {
                LOGGER.error(e.getMessage());
            }
        }
        return null;
    }

    /**
     * 将指定时间戳转换为LocalDateTime对象
     */
    public static LocalDateTime getDate(long time) {
        return LocalDateTime.ofInstant(Instant.ofEpochMilli(time), ZoneId.systemDefault());
    }

    /**
     * 判断指定nanoTime与当前nanoTime相差的时间，单位: 毫秒，只能比较System.nanoTime()的时间
     */
    public static long diffNanoTime(long time) {
        return TimeUnit.NANOSECONDS.toMillis(Math.abs(System.nanoTime() - time));
    }

    /**
     * 判断指定时间和当前时间之间相差的时间
     *
     * @param start 开始时间戳
     * @return 单位秒
     */
    public static long diffSecondTime(long start) {
        return diffSecondTime(getCurrentTimeMillis(), start);
    }

    /**
     * 判断指定时间戳之间相差的时间
     *
     * @param end   结束时间戳
     * @param start 开始时间戳
     * @return 单位秒
     */
    public static long diffSecondTime(long end, long start) {
        return TimeUnit.MILLISECONDS.toSeconds(diffMilliTime(end, start));
    }

    /**
     * 判断指定时间戳之间相差的时间, 单位毫秒
     *
     * @param start 开始时间戳
     * @return
     */
    public static long diffMilliTime(long start) {
        return diffMilliTime(getCurrentTimeMillis());
    }

    /**
     * 判断指定时间戳之间相差的时间, 单位毫秒
     *
     * @param end   结束时间戳
     * @param start 开始时间戳
     * @return
     */
    public static long diffMilliTime(long end, long start) {
        return Math.abs(end - start);
    }

    /**
     * 将字符串转换成Hex
     */
    public static String toHex(String value) {
        if (!Strings.isNullOrEmpty(value)) {
            byte[] bytes;
            try {
                bytes = value.getBytes(Constants.UTF8_CHARSET);
            } catch (Exception e) {
                if (LOGGER.isErrorEnabled()) {
                    LOGGER.error(formatString("字符串转换为{}字节数组失败: {}", Constants.UTF8_CHARSET_NAME, e.getMessage()), e);
                }
                throw new IllegalArgumentException(e.getMessage(), e);
            }
            return toHex(bytes);
        }
        return value;
    }

    /**
     * 将long以指定个字节转换成Hex
     */
    public static String toHex(long value) {
        return toHex(value, 8);
    }

    /**
     * 将long以指定个字节转换成Hex
     */
    public static String toHex(long value, int len) {
        byte[] bs = new byte[len];
        for (; --len >= 0; ) {
            bs[len] = (byte) (value & 0xFF);
            value = value >>> 8;
        }
        return toHex(bs);
    }

    /**
     * 将字节转换成小写Hex
     */
    public static String toHex(byte[] bs) {
        if (bs == null || bs.length == 0) {
            throw new RuntimeException("转换为HEX的字节数组不能为空!");
        }
        StringBuilder buf = new StringBuilder();
        for (byte b : bs) {
            buf.append(Integer.toHexString(b >>> 4 & 0xF)).append(Integer.toHexString(b & 0xF));
        }
        return buf.toString().toLowerCase();
    }

    /**
     * 将HEX格式数据转换为字符串
     */
    public static String hexToString(String hexValue) {
        if (Strings.isNullOrEmpty(hexValue)) {
            throw new RuntimeException("HEX格式数据不能为空!");
        }
        int length = hexValue.length() >> 1; // 除2
        if ((hexValue.length() & 1) != 0) {
            throw new RuntimeException("HEX格式数据长度不正确!");
        }
        byte[] bs = new byte[length];
        for (int i = 0; i < length; i++) {
            int pos = i * 2;
            int hight = Character.digit(hexValue.charAt(pos), 16);
            int low = Character.digit(hexValue.charAt(pos + 1), 16);
            bs[i] = (byte) ((hight << 4 | low) & 0xFF);
        }
        try {
            return new String(bs, "UTF-8");
        } catch (Exception e) {
            if (LOGGER.isErrorEnabled()) {
                LOGGER.error(Tools.formatString("字符串转换为{}字节数组失败: {}", Constants.UTF8_CHARSET_NAME, e.getMessage()), e);
            }
            throw new IllegalArgumentException(e.getMessage(), e);
        }
    }

    /**
     * 判断一个数指定位的值是否为1
     *
     * @param value
     * @param bitIndex 范围: 1-32，如果不在这个范围则抛出异常 @exception
     */
    public static boolean getBitValue(long value, int bitIndex) {
        if (bitIndex < 0 || bitIndex > 63) {
            throw new IllegalArgumentException("位索引必须在0-63之间");
        }
        return (value >> bitIndex & 1) == 1; // 将一个数右移指定位获取第一位的值
    }

    /**
     * 给一个数的指定位设置值
     *
     * @param value
     * @param bitIndex
     * @param flag     true=1, false=0
     * @return 返回设置完之后的数值
     */
    public static long setBitValue(long value, int bitIndex, boolean flag) {
        if (bitIndex < 0 || bitIndex > 63) {
            throw new IllegalArgumentException("位索引必须在0-63之间");
        }
        int temp = 1 << bitIndex;
        return flag ? (value | temp) : (value & ~temp);
    }

    /**
     * 格式化字符串
     *
     * @param format 模板信息，{}占位符
     * @param args   变量值
     * @return
     */
    public static String formatString(String format, Object... args) {
        if (Strings.isNullOrEmpty(format)) {
            // 模板为空
            if (args != null && args.length > 0) {
                StringBuilder buf = new StringBuilder();
                for (Object arg : args) {
                    if (arg != null) {
                        buf.append(String.valueOf(arg)).append(' ');
                    }
                }
                return buf.toString();
            }
            return format;
        }
        // 模板不为空
        StringBuilder buf = new StringBuilder(format);
        if (args != null && args.length > 0) {
            for (int pos = 0, index = 0, length = args.length; (pos = buf.indexOf("{}", pos)) != -1 && index < length; index++) {
                buf.replace(pos, pos + 2, String.valueOf(args[index]));
            }
        }
        return buf.toString();
    }

    /**
     * 处理字符串，将字符串trim().toLowerCase()，如果字符串为空或者处理之后为空则返回null
     */
    public static String filter(String value) {
        if (value != null) {
            value = value.trim().toLowerCase();
            if (!value.isEmpty()) {
                return value;
            }
        }
        return null;
    }

    /**
     * 获取实例,如果对象为null则返回default
     * @param value
     * @param _default
     * @return
     */
    public static <T> T getOrDefault(T value, T _default) {
        if (value == null) {
            return _default;
        }
        return value;
    }

    /**
     * 将流中的数据写入本地临时文件并将文件交给处理函数进行操作，如果处理函数为null则不做任何操作，最终会将临时文件删除，注意如果是用多线程处理临时文件则可能会找不到文件
     *
     * @param is   流
     * @param func 处理函数
     */
    public static <T> T inputStream2File(InputStream is, Function<File, T> func) {
        File tempFile = null;
        try {
            tempFile = File.createTempFile("is2file_", null); // 创建临时文件
            try (FileOutputStream os = new FileOutputStream(tempFile)) {
                byte[] buf = new byte[1024];
                for (int len; (len = is.read(buf)) != -1; ) {
                    os.write(buf, 0, len);
                }
                if (func != null) {
                    return func.apply(tempFile);
                }
                return null;
            } catch (Exception e) {
                if (LOGGER.isErrorEnabled()) {
                    LOGGER.error(Tools.formatString("处理流中数据失败: {}", e.getMessage()), e);
                }
                throw new IllegalStateException("处理流中数据失败!", e);
            }
        } catch (Exception e) {
            if (LOGGER.isErrorEnabled()) {
                LOGGER.error(e.getMessage());
            }
            throw new RuntimeException(e.getMessage(), e);
        } finally {
            if (tempFile != null) {
                tempFile.deleteOnExit(); // 移除临时文件
            }
        }
    }

    /**
     * 读取字节流中数据并返回
     */
    public static byte[] inputStream2ByteArray(InputStream is) {
        if (is == null) {
            return null;
        }
        byte[] buf = new byte[1024];
        try (ByteArrayOutputStream os = new ByteArrayOutputStream()) {
            for (int len; (len = is.read(buf)) != -1; ) {
                os.write(buf, 0, len);
            }
            return os.toByteArray();
        } catch (Exception e) {
            if (LOGGER.isErrorEnabled()) {
                LOGGER.error(e.getMessage());
            }
            throw new RuntimeException(e.getMessage(), e);
        }
    }

    /**
     * 读取字节流中数据并转换成字符串返回
     */
    public static String inputStream2String(InputStream is) {
        byte[] bs = inputStream2ByteArray(is);
        if (bs != null) {
            return new String(bs);
        }
        return null;
    }

    /**
     * 根据文件名称生成2层目录
     */
    public static String getPath(String fileName) {
        return getPath(fileName, 2);
    }

    /**
     * 根据文件名称生成多层目录，最少1层，最多8层，如果文件名称为null或空串则返回/
     */
    public static String getPath(String fileName, int level) {
        if (Strings.isNullOrEmpty(fileName)) {
            return "/";
        }
        if (level > 8) {
            level = 8;
        } else if (level < 1) {
            level = 1;
        }
        StringBuilder buf = new StringBuilder();
        int hashCode = fileName.hashCode();
        for (int i = 0; i < level; i++) {
            buf.append("/").append(Integer.toHexString((hashCode >>> (i * 4)) & 0xF).toUpperCase());
        }
        return buf.toString();
    }

    /**
     * 逗号分割器对象
     */
    public static final Splitter COMMA_SPLITTER = Splitter.on(',').trimResults().omitEmptyStrings();

    /**
     * 句号分割器对象
     */
    public static final Splitter POINT_SPLITTER = Splitter.on('.').trimResults().omitEmptyStrings();

    /**
     * 换行分割器对象
     */
    public static final Splitter NEWLINE_SPLITTER = Splitter.on('\n').trimResults().omitEmptyStrings();

    /**
     * 将MAC地址转换为long
     */
    public static long macToLong(String mac) {
        if (Strings.isNullOrEmpty(mac) || mac.length() != 12) {
            throw new IllegalArgumentException("非法MAC地址!");
        }
        int length = 6;
        byte[] bs = new byte[length];
        for (int i = 0; i < length; i++) {
            int pos = i * 2; // 只需6位
            int hight = Character.digit(mac.charAt(pos), 16);
            int low = Character.digit(mac.charAt(pos + 1), 16);
            bs[i] = (byte) ((hight << 4 | low) & 0xFF);
        }
        return macToLong(bs);
    }

    /**
     * 将MAC地址转换为long
     */
    public static long macToLong(byte[] mac) {
        if (mac == null || mac.length != 6) {
            throw new IllegalArgumentException("非法MAC地址!");
        }
        byte[] bs = new byte[8];
        for (int i = 0; i < 6; i++) {
            bs[i + 2] = mac[i];
        }
        ByteBuffer buf = ByteBuffer.allocate(8);
        buf.put(bs);
        buf.flip();
        return buf.getLong();
    }

    /**
     * 网关设备绑定校验密钥
     */
    private static final char[] key = {0x15, 0x16, 0x17, 0x18, 0x19, 0x20, 0x81, 0x82, 0x83, 0x84, 0x85, 0x86, 0x87,
            0x88, 0x88, 0x10, 0x11, 0x12, 0x13, 0x14, 0x21, 0x22, 0x23, 0x24, 0x25, 0x26, 0x27, 0x28, 0x29, 0x30, 0x31,
            0x32, 0x37, 0x38, 0x39, 0x3A, 0x3B, 0x3D, 0x3E, 0x3F, 0xE1, 0xE2, 0xE2, 0xEA, 0xEB, 0xEE, 0xEF, 0xF4, 0xF5,
            0xF6, 0xF7, 0xF8, 0xF9, 0xFA, 0xFB, 0xFD, 0xFE, 0xFF, 0x6A, 0x7A, 0x8C, 0x9B, 0xAF, 0xAA};

    /**
     * 绑定签名校验
     *
     * @param userId 用户主键
     * @param mac    设备MAC地址
     * @param check  App端发送绑定校验码
     * @return 通过校验返回true，未通过返回false
     */
    public static boolean validateBindSign(String userId, String mac, String check) {
        if (Strings.isNullOrEmpty(userId) || Strings.isNullOrEmpty(mac) || Strings.isNullOrEmpty(check)) {
            return false;
        }
        char[] data = (mac + userId).toCharArray(); // MAC地址与用户ID拼接的字符数组
        int len = data.length; // 数据长度
        char[] buf = new char[len]; // 存储签名缓冲
        for (int i = 0; i < len; i++) {
            buf[i] = (char) (data[i] ^ key[i % 64]); // 数据字符^密钥字符，当数据长度超出密钥时重新从0索引覆盖
        }
        StringBuilder hexBuf = new StringBuilder(); // 16进制签名缓冲
        for (char c : buf) {
            hexBuf.append(Integer.toHexString((c >> 4) & 0xF)).append(Integer.toHexString(c & 0xF));
        }
        return hexBuf.toString().toLowerCase().equals(check.toLowerCase());
    }

    /**
     * 将一个集合转换为一个数组
     *
     * @param type 数组元素类型
     * @param coll 集合
     * @return
     */
    @SuppressWarnings("unchecked")
    public static <T> T[] toArray(Class<T> type, Collection<? extends T> coll) {
        if (type == null) {
            throw new IllegalArgumentException("数组类型不能为空!");
        }
        if (coll == null || coll.isEmpty()) {
            throw new IllegalArgumentException("集合不能为空!");
        }
        int size = coll.size();
        T[] array = (T[]) Array.newInstance(type, size);
        return coll.toArray(array);
    }
}
