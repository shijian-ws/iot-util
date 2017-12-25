package org.sj.iot.model;

import com.google.common.base.Strings;
import org.sj.iot.util.Constants;

import java.net.URLDecoder;
import java.net.URLEncoder;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 资源定位符
 *
 * @author shijian
 * @email shijianws@163.com
 * @date 2016-10-10
 */
public final class URL {
    private final String protocol; // 使用协议，默认使用tcp
    private final String ip; // 服务主机
    private final Integer port; // 服务端口
    private final String path; // 服务路径
    private final Map<String, String> parameters = new LinkedHashMap<>(); // 参数

    private static final Pattern pattern = Pattern.compile("^([a-zA-Z]+)://(\\w+(\\.\\w+)+)(:(\\d+))?(/[^?]*)?(\\?[^=]+=[^&]+(&[^=]+=[^&]+)*)?");

    public static void main(String[] args) {
        URL url = new URL("tcp://127.0.0.1:80/sa/d/sa/ds/a////?v=flak");
        url.addParameter("url", "fdjksakf&fjklad=");
        System.out.println(url);
    }

    public static URL process(byte[] spec) {
        return new URL(new String(spec, Constants.UTF8_CHARSET));
    }

    public URL(String spec) {
        if (Strings.isNullOrEmpty(spec)) {
            throw new RuntimeException("资源定位符描述格式错误: protocol://ip:port/path?key=value...");
        }
        /*// tcp://127.0.0.1:80/sa/d/sa/ds/a////?id=1&a=a&&&&&a=a11&&&&&&&&&&&&&&&
        Matcher matcher = Pattern.compile("^([a-zA-Z]+)://(\\d{1,3}\\.\\d{1,3}\\.\\d{1,3}\\.\\d{1,3}):(\\d+)(/[^?]*)?(\\?[^=]+=[^&]+(&[^=]+=[^&]+)*)?").matcher(spec);
		if (!matcher.find()) {
			throw new RuntimeException("资源定位符描述格式错误: protocol://ip:port/path?key=value...");
		}
		this.protocol = matcher.group(1);
		this.ip = matcher.group(2);
		this.port = Integer.parseInt(matcher.group(3));
		this.path = processPath(matcher.group(4));
		String args = matcher.group(5);*/
        Matcher matcher = pattern.matcher(spec);
        if (!matcher.find()) {
            throw new RuntimeException("资源定位符描述格式错误: protocol://ip|dns(:port)?/path?key=value...");
        }
        this.protocol = matcher.group(1);
        this.ip = matcher.group(2);
        String port = matcher.group(5);
        if (port != null) {
            this.port = Integer.parseInt(port);
        } else {
            this.port = null;
        }
        this.path = processPath(matcher.group(6));
        String args = matcher.group(7);
        if (args != null) {
            args = args.substring(1).replaceAll("&+", "&").replaceAll("&+$", "");
            String[] kvs = args.split("&");
            for (String kv : kvs) {
                int index = kv.indexOf('=');
                String key = kv.substring(0, index);
                String value = kv.substring(index + 1);
                try {
                    value = URLDecoder.decode(value, Constants.UTF8_CHARSET.name()); // 对value对URL解码
                } catch (Exception e) {
                }
                parameters.put(key, value);
            }
        }
    }

    public URL(String ip, int port) {
        this("tcp", ip, port, null);
    }

    public URL(String ip, int port, String path) {
        this("tcp", ip, port, path);
    }

    private static String processPath(String path) {
        StringBuilder buf = new StringBuilder();
        if (path == null || "".equals(path = path.trim())) {
            buf.append('/');
        } else {
            path = path.replace('\\', '/').replaceAll("/+$", "");
            if (!path.startsWith("/")) {
                buf.append('/');
            }
            buf.append(path);
        }
        return buf.toString();
    }

    public URL(String protocol, String ip, int port, String path) {
        this.protocol = protocol;
        this.ip = ip;
        this.port = port;
        this.path = processPath(path);
    }

    public String getProtocol() {
        return protocol;
    }

    public String getIp() {
        return ip;
    }

    public int getPort() {
        return port;
    }

    public String getPath() {
        return path;
    }

    /**
     * 获取参数
     */
    public String getParameter(String name) {
        return parameters.get(name);
    }

    /**
     * 获取一组参数名称集合
     */
    public Collection<String> getParameterNames() {
        return parameters.keySet();
    }

    /**
     * 获取克隆的参数映射集
     */
    public Map<String, String> getParameterMap() {
        return new HashMap<>(parameters);
    }

    /**
     * 将参数映射集放入指定Map中
     */
    public void getParameterMap(Map<String, String> newMap) {
        if (newMap != null) {
            newMap.putAll(parameters);
        }
    }

    /**
     * 添加参数
     */
    public boolean addParameter(String name, String value) {
        if (!Strings.isNullOrEmpty(name)) {
            if (!Strings.isNullOrEmpty(value)) {
                parameters.put(name, value);
                return true;
            }
        }
        return false;
    }

    /**
     * 添加参数
     */
    public boolean addParameters(Map<String, String> parameters) {
        if (parameters != null && !parameters.isEmpty()) {
            parameters.putAll(parameters);
            return true;
        }
        return false;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((ip == null) ? 0 : ip.hashCode());
        result = prime * result + ((path == null) ? 0 : path.hashCode());
        result = prime * result + port;
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        URL other = (URL) obj;
        if (ip == null) {
            if (other.ip != null) {
                return false;
            }
        } else if (!ip.equals(other.ip)) {
            return false;
        }
        if (path == null) {
            if (other.path != null) {
                return false;
            }
        } else if (!path.equals(other.path)) {
            return false;
        }
        if (!port.equals(other.port)) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        StringBuilder buf = new StringBuilder(protocol).append("://").append(ip);
        if (port != null) {
            buf.append(':').append(port);
        }
        buf.append(path);
        if (!parameters.isEmpty()) {
            buf.append('?');
            for (Entry<String, String> en : parameters.entrySet()) {
                String value = en.getValue();
                try {
                    value = URLEncoder.encode(value, Constants.UTF8_CHARSET.name()); // 对value对URL编码
                } catch (Exception e) {
                }
                buf.append(en.getKey()).append('=').append(value).append('&');
            }
            buf.deleteCharAt(buf.length() - 1);
        }
        return buf.toString();
    }

    public byte[] toByte() {
        return toString().getBytes(Constants.UTF8_CHARSET);
    }
}
