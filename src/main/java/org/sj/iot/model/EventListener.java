package org.sj.iot.model;

import org.sj.iot.model.Event.Type;
import org.sj.iot.util.JsonUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Consumer;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * 事件监听器
 *
 * @author shijian
 * @email shijianws@163.com
 * @date 2018-01-01
 */
public final class EventListener {
    private static final Logger LOGGER = LoggerFactory.getLogger(EventListener.class);

    private Map<String, Consumer<Event>> cacheCallback = new ConcurrentHashMap<>();
    private Map<String, Set<Type>> cacheType = new ConcurrentHashMap<>();

    private static String processMatch(String val) {
        return val.replaceAll("\\.", "\\\\.").replaceAll("\\*+", "\\.*");
    }

    /**
     * 添加监听器
     */
    public final void addListener(Consumer<Event> callback, String path, Type... types) {
        if (path == null || path.isEmpty()) {
            throw new NullPointerException("监听路径不能为空!");
        }
        Objects.requireNonNull(callback, "监听回调函数不能为空!");
        path = processMatch(path);
        cacheCallback.put(path, callback);
        if (types != null && types.length > 0) {
            Set<Type> typeSet = Stream.of(types).parallel().filter(Objects::nonNull).collect(Collectors.toSet());
            if (!typeSet.isEmpty()) {
                cacheType.put(path, typeSet);
            }
        }
    }

    public final void onData(String path, String data) {
        if (data == null || data.isEmpty()) {
            LOGGER.debug("{}: 无消息数据, 事件被跳过!");
            return;
        }
        if (cacheCallback.isEmpty()) {
            LOGGER.debug("{}: 未被监听, 事件被跳过!", path);
            return;
        }
        // 找出满足条件路径
        List<String> paths = cacheCallback.entrySet().parallelStream()
                .filter(entry -> path.matches(entry.getKey()))
                .map(Entry::getKey).collect(Collectors.toList());
        if (paths.isEmpty()) {
            LOGGER.debug("{}: 未被监听, 事件被跳过!", path);
            return;
        }
        Event event = JsonUtil.toObject(data, Event.class);
        if (event == null) {
            LOGGER.debug("{}: 消息数据解析失败, 事件被跳过!", path);
            return;
        }
        Type type = event.getType();
        if (!cacheType.isEmpty()) {
            // 移除不包含监听状态路径
            cacheType.entrySet().parallelStream()
                    .filter(entry -> paths.contains(entry.getKey()))
                    .forEach(entry -> {
                        Set<Type> types = entry.getValue();
                        if (!types.contains(type)) {
                            paths.remove(entry.getKey());
                            LOGGER.debug("{}: 当前事件{},期望事件{},事件被跳过!", path, type, types);
                            return;
                        }
                    });
        }
        if (paths.isEmpty()) {
            // 没有符合监听状态的路径
            return;
        }
        event.setPath(path);
        cacheCallback.entrySet().parallelStream()
                .filter(entry -> paths.contains(entry.getKey())) // 找出回调函数
                .forEach(entry -> {
                    try {
                        entry.getValue().accept(event);
                    } catch (Exception e) {
                        LOGGER.error("{}: 当前事件{}, 出现异常: {}", path, type, e.getMessage());
                    }
                });
    }
}
