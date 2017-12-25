package org.sj.iot.util;

import org.sj.iot.model.Processable;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 任务处理工具类
 *
 * @author shijian
 * @email shijianws@163.com
 * @date 2017-09-18
 */
public class ProcessUtil {

    /**
     * 服务端任务处理器对象集，key=MAC + '\n' + messageId, value=Processable
     */
    private static final Map<String, Processable<?>> processColl = new ConcurrentHashMap<>();

    private static String getKey(String mac, byte messageId) {
        if (mac != null) {
            return mac + '\n' + messageId;
        }
        return null;
    }

    /**
     * 设置远程设备的任务处理对象
     */
    public static <T> boolean set(String mac, byte messageId, Processable<T> process) {
        String key = getKey(mac, messageId);
        if (key != null) {
            processColl.put(key, process);
            return true;
        }
        return false;
    }

    /**
     * 获取任务处理对象
     */
    public static <T> Processable<T> get(String mac, byte messageId) {
        String key = getKey(mac, messageId);
        if (key != null) {
            return (Processable<T>) processColl.get(key);
        }
        return null;
    }

    /**
     * 获取并移除任务处理对象
     */
    public static <T> Processable<T> remove(String mac, byte messageId) {
        String key = getKey(mac, messageId);
        if (key != null) {
            return (Processable<T>) processColl.remove(key);
        }
        return null;
    }
}
