package org.sj.iot.util;

import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonParser.Feature;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.PropertyNamingStrategy;
import com.fasterxml.jackson.databind.SerializationFeature;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.StringReader;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

/**
 * JSON工具包
 *
 * @author shijian
 * @email shijianws@163.com
 * @date 2016-04-05
 */
public class JsonUtil {
    private static final Logger LOGGER = LoggerFactory.getLogger(JsonUtil.class);

    private static final PropertyNamingStrategy NAMING_STRATEGY = PropertyNamingStrategy.SNAKE_CASE;

    public static final ObjectMapper objectMapper;

    static {
        JsonFactory factory = new JsonFactory();
        objectMapper = new ObjectMapper(factory);
        objectMapper.setPropertyNamingStrategy(NAMING_STRATEGY); // 蛇形命名法
        objectMapper.configure(Feature.ALLOW_UNQUOTED_FIELD_NAMES, true); // 允许不规范的JSON字符串, 例如:key未使用双引号
        objectMapper.configure(Feature.ALLOW_SINGLE_QUOTES, true); // 允许使用单引号
        objectMapper.configure(Feature.ALLOW_UNQUOTED_CONTROL_CHARS, true); // 允许出现特殊字符和转义字符，小于32的ASCII
        objectMapper.disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES); // 忽略不存在的属性
        objectMapper.configure(SerializationFeature.FAIL_ON_EMPTY_BEANS, false); // 忽略懒加载属性
        objectMapper.setSerializationInclusion(Include.NON_EMPTY); // 不将空属性写入JSON
    }

    /**
     * 将字符串或字符串字节数组或实体转换为JsonMap
     */
    public static <T> Map<String, T> toJsonMap(Object json) {
        return toJsonMap(objectMapper, json);
    }

    /**
     * 将字符串或字符串字节数组或实体转换为JsonMap
     */
    @SuppressWarnings("unchecked")
    public static <T> Map<String, T> toJsonMap(ObjectMapper objectMapper, Object json) {
        try {
            if (json instanceof byte[]) {
                return objectMapper.readValue((byte[]) json, Map.class);
            } else if (json instanceof String) {
                return objectMapper.readValue((String) json, Map.class);
            } else if (json instanceof Map) {
                return new HashMap<>((Map<String, ? extends T>) json);
            }
            // 可能为实体
            try {
                String jsonString = objectMapper.writeValueAsString(json);
                return toJsonMap(jsonString);
            } catch (Exception e) {
                LOGGER.error(String.format("实体转换为JSON字符串失败: %s", e.getMessage()), e);
            }
        } catch (Exception e) {
            LOGGER.error(String.format("JSON转换为Map失败: %s", e.getMessage()), e);
        }
        return null;
    }

    /**
     * 将JSON格式字符串转换为一个对象，如果clazz为数组Class则返回数组
     */
    public static <T> T toObject(String json, Class<T> clazz) {
        return toObject(objectMapper, json, clazz);
    }

    /**
     * 将JSON格式字符串转换为一个对象，如果clazz为数组Class则返回数组
     */
    public static <T> T toObject(ObjectMapper objectMapper, String json, Class<T> clazz) {
        try {
            return objectMapper.readValue(json, clazz);
        } catch (Exception e) {
            LOGGER.error(String.format("JSON转换为 %s 失败: %s", clazz, e.getMessage()), e);
        }
        return null;
    }

    /**
     * 将JSON格式字符串转换为一个对象，如果clazz为数组Class则返回数组
     */
    public static <T> T toObject(byte[] json, Class<T> clazz) {
        return toObject(objectMapper, json, clazz);
    }

    /**
     * 将JSON格式字符串转换为一个对象，如果clazz为数组Class则返回数组
     */
    public static <T> T toObject(ObjectMapper objectMapper, byte[] json, Class<T> clazz) {
        try {
            return objectMapper.readValue(json, clazz);
        } catch (Exception e) {
            LOGGER.error(String.format("JSON转换为 %s 失败: %s", clazz, e.getMessage()), e);
        }
        return null;
    }

    /**
     * 将一个实体转换成JSON格式的字符串
     */
    public static String toJsonString(Object json) {
        return toJsonString(objectMapper, json);
    }

    /**
     * 将一个实体转换成JSON格式的字符串
     */
    public static String toJsonString(ObjectMapper objectMapper, Object json) {
        if (json != null) {
            try {
                return objectMapper.writeValueAsString(json);
            } catch (Exception e) {
                LOGGER.error(String.format("JSON转换为字符串失败: %s", e.getMessage()), e);
            }
        }
        return null;
    }

    /**
     * 将一个实体转换成JSON格式的字节数组
     */
    public static byte[] toJsonByte(Object json) {
        return toJsonByte(objectMapper, json);
    }

    /**
     * 将一个实体转换成JSON格式的字节数组
     */
    public static byte[] toJsonByte(ObjectMapper objectMapper, Object json) {
        if (json != null) {
            try {
                return objectMapper.writeValueAsBytes(json);
            } catch (Exception e) {
                LOGGER.error(String.format("JSON转换为字符串失败: %s", e.getMessage()), e);
            }
        }
        return null;
    }

    /**
     * 格式化JSON
     */
    public static String formatJson(Object json) {
        return formatJson(json, -1);
    }

    /**
     * 格式化JSON
     *
     * @param json
     * @param valueLength 截取字符串类型值的指定字符
     * @return
     */
    public static String formatJson(Object json, int valueLength) {
        return formatJson(json, valueLength, null);
    }

    /**
     * 格式化JSON
     *
     * @param json
     * @param valueLength
     * @param prefix      追加前缀
     * @return
     */
    public static String formatJson(Object json, int valueLength, String prefix) {
        if (json != null) {
            Map<String, Object> jsonMap = toJsonMap(json);
            if (jsonMap != null) {
                if (valueLength > 0) {
                    for (Entry<String, Object> en : jsonMap.entrySet()) {
                        Object value = en.getValue();
                        if (value instanceof String) {
                            String val = (String) value;
                            if (val.length() > valueLength) {
                                jsonMap.put(en.getKey(), val.substring(0, valueLength) + "...");
                            }
                        }
                    }
                }
                try {
                    String jsonString = objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(jsonMap);
                    if (prefix != null) {
                        try {
                            BufferedReader reader = new BufferedReader(new StringReader(jsonString));
                            StringBuilder buf = new StringBuilder();
                            String lineSeparator = System.lineSeparator();
                            for (String line; (line = reader.readLine()) != null; ) {
                                buf.append(prefix).append(line).append(lineSeparator);
                            }
                            reader.close();
                            jsonString = buf.toString();
                        } catch (Exception e) {
                        }
                    }
                    return jsonString;
                } catch (Exception e) {
                    LOGGER.error("格式化JSON对象失败: {}", e.getMessage());
                }
            }
        }
        return null;
    }

    /**
     * 将JavaBean属性名用蛇形命名法转换
     */
    public static String toJsonPropertyName(String javaPropertyName) {
        return NAMING_STRATEGY.nameForField(null, null, javaPropertyName);
    }
}
