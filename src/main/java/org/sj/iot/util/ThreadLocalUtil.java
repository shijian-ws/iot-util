package org.sj.iot.util;

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

/**
 * 线程缓存工具类
 *
 * @author shijian
 * @email shijianws@163.com
 * @date 2017-04-27
 */
public class ThreadLocalUtil {
    private static final ThreadLocal<Map<Object, Object>> CACHE = new ThreadLocal<>(); // 线程缓存
    private static final Object KEY = new Object(); // 存储单一对象默认的KEY

    /**
     * 向当前线程缓存存储数据
     */
    @SuppressWarnings("unchecked")
    public static <T> T set(Object key, Object value) {
        Map<Object, Object> map = CACHE.get(); // 获取线程存储的映射
        if (map == null) {
            map = new HashMap<>();
            CACHE.set(map); // 未存储映射，创建一个存入
        }
        return (T) map.put(key, value);
    }

    /**
     * 向当前线程缓存存储数据
     */
    public static <T> T set(Object value) {
        return set(KEY, value); // 存储一个对象，键使用默认
    }

    /**
     * 获取当前线程缓存存储的数据
     */
    @SuppressWarnings("unchecked")
    public static <T> T get(Object key) {
        Map<Object, Object> map = CACHE.get();
        if (map != null && !map.isEmpty()) {
            Object val = map.get(key);
            if (val == null) {
                if (key instanceof Class) {
                    for (Entry<Object, Object> entry : map.entrySet()) {
                        Object k = entry.getKey();
                        if (k instanceof Class) {
                            if (((Class) key).isAssignableFrom((Class<?>) k)) {
                                val = entry.getValue();
                                break;
                            }
                        }
                    }
                }
            }
            return (T) val; // 获取线程缓存中映射存储的值
        }
        return null;
    }

    /**
     * 获取当前线程缓存存储的数据
     */
    public static <T> T get() {
        return get(KEY); // 获取默认键对应的值
    }

    /**
     * 移除存储数据
     */
    @SuppressWarnings("unchecked")
    public static void removeAll() {
        Map<Object, Object> map = CACHE.get();
        if (map != null) {
            CACHE.remove(); // 移除线程缓存Map对象
            map.clear(); // 清空Map中对象
        }
    }

    /**
     * 移除存储数据
     */
    @SuppressWarnings("unchecked")
    public static <T> T remove() {
        Map<Object, Object> map = CACHE.get();
        if (map != null) {
            return (T) map.remove(KEY);
        }
        return null;
    }

    /**
     * 移除存储数据
     */
    @SuppressWarnings("unchecked")
    public static <T> T remove(Object key) {
        Map<Object, Object> map = CACHE.get();
        if (map != null) {
            return (T) map.remove(key);
        }
        return null;
    }

    private ThreadLocalUtil() {
    }
}
