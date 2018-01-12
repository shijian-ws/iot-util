package org.sj.iot.model;

import org.sj.iot.util.Constants;

import java.util.Arrays;
import java.util.Objects;

/**
 * 事件模型
 *
 * @author shijian
 * @email shijianws@163.com
 * @date 2018-01-01
 */
public class Event {
    private Type type; // 事件类型
    private String path; // 触发路径
    private byte[] data; // 携带数据

    public Event() {
    }

    public Event(Type type, byte[] data) {
        this(type, null, data);
    }

    public Event(Type type, String path, byte[] data) {
        this.type = type;
        this.path = path;
        if (data != null && data.length > 0) {
            this.data = data;
        }
    }

    public Event(Type type, String data) {
        this(type, null, data);
    }

    public Event(Type type, String path, String data) {
        this.type = type;
        this.path = path;
        if (data != null && !data.isEmpty()) {
            this.data = data.getBytes(Constants.UTF8_CHARSET);
        }
    }

    public Type getType() {
        return type;
    }

    public void setType(Type type) {
        this.type = type;
    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public byte[] getData() {
        return data;
    }

    public void setData(byte[] data) {
        this.data = data;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof Event)) {
            return false;
        }
        Event event = (Event) o;
        return getType() == event.getType() && Objects.equals(getPath(), event.getPath());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getType(), getPath());
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("Event{");
        sb.append("type=").append(type);
        sb.append(", path='").append(path).append('\'');
        sb.append(", data=").append(Arrays.toString(data));
        sb.append('}');
        return sb.toString();
    }

    public enum Type {
        /**
         * 添加
         */
        ADDED,
        /**
         * 更新
         */
        UPDATED,
        /**
         * 移除
         */
        REMOVED,
    }
}
