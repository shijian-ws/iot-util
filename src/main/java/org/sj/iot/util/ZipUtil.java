package org.sj.iot.util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.util.Map;
import java.util.Map.Entry;
import java.util.zip.*;

/**
 * Zip工具
 *
 * @author shijian
 * @email shijianws@163.com
 * @date 2016-07-01
 */
public class ZipUtil {
    private static final Logger LOGGER = LoggerFactory.getLogger(ZipUtil.class);

    /**
     * 解压zip压缩数据
     */
    public static byte[] decompress(byte[] data) {
        if (data != null && data.length > 0) {
            Inflater decompress = new Inflater();
            try (ByteArrayOutputStream os = new ByteArrayOutputStream()) {
                decompress.setInput(data); // 需要解压的数据
                byte[] buf = new byte[1024];
                do {
                    // 未读取完成则循环写入内存
                    int len = decompress.inflate(buf);
                    if (len == 0) {
                        // TODO 解压数据不完整
                        break;
                    }
                    os.write(buf, 0, len);
                } while (!decompress.finished());
                return os.toByteArray();
            } catch (Exception e) {
                if (LOGGER.isErrorEnabled()) {
                    LOGGER.error("解压zip数据失败：{}", e.getMessage());
                }
                throw new RuntimeException("解压zip数据失败!", e);
            } finally {
                try {
                    decompress.end();
                } catch (Exception e) {
                    throw new RuntimeException(e.getMessage(), e);
                }
            }
        }
        return data;
    }

    /**
     * zip压缩数据
     */
    public static byte[] compress(byte[] data) {
        if (data != null && data.length > 0) {
            Deflater compress = new Deflater();
            try (ByteArrayOutputStream os = new ByteArrayOutputStream()) {
                compress.setInput(data); // 需要压缩的数据
                compress.finish(); // 压缩到缓冲区结尾
                byte[] buf = new byte[1024];
                do {
                    // 未读取完成则循环写入内存
                    int len = compress.deflate(buf);
                    os.write(buf, 0, len);
                } while (!compress.finished());
                return os.toByteArray();
            } catch (Exception e) {
                if (LOGGER.isErrorEnabled()) {
                    LOGGER.error("压缩zip数据失败：{}", e.getMessage());
                }
                throw new RuntimeException("压缩数据失败!", e);
            } finally {
                try {
                    compress.end();
                } catch (Exception e) {
                    throw new RuntimeException(e.getMessage(), e);
                }
            }
        }
        return data;
    }

    /**
     * 创建zip并添加条目
     */
    public static byte[] generateZip(Map<String, byte[]> entries) {
        return addZipEntry(null, entries);
    }

    /**
     * 给zip压缩包添加条目
     *
     * @param zip
     * @param entries
     * @return
     */
    public static byte[] addZipEntry(byte[] zip, Map<String, byte[]> entries) {
        try (ByteArrayOutputStream os = new ByteArrayOutputStream()) {
            if (zip != null && zip.length > 0) {
                os.write(zip);
            }
            ZipOutputStream zos = new ZipOutputStream(os);
            if (entries != null && !entries.isEmpty()) {
                for (Entry<String, byte[]> en : entries.entrySet()) {
                    String name = en.getKey();
                    byte[] data = en.getValue();
                    try {
                        ZipEntry entry = new ZipEntry(name);
                        zos.putNextEntry(entry);
                        zos.write(data); // 写入数据
                        zos.closeEntry(); // 关闭当前条目，指向下一个条目
                    } catch (Exception e) {
                        throw new RuntimeException(e.getMessage(), e);
                    }
                }
            }
            return os.toByteArray();
        } catch (Exception e) {
            if (LOGGER.isErrorEnabled()) {
                LOGGER.error("压缩zip包失败: {}", e.getMessage());
            }
            throw new RuntimeException("压缩zip包失败!", e);
        }
    }

    public static String getZipContent(byte[] zipData, String entryName) {
        if (entryName != null && !"".equals(entryName = entryName.trim())) {
            try (ZipInputStream zip = new ZipInputStream(new ByteArrayInputStream(zipData))) {
                for (ZipEntry en; (en = zip.getNextEntry()) != null; ) {
                    if (entryName.equalsIgnoreCase(en.getName())) {
                        if (!en.isDirectory()) {
                            byte[] buffer = new byte[2048];
                            try (ByteArrayOutputStream os = new ByteArrayOutputStream()) {
                                for (int len; (len = zip.read(buffer)) != -1; ) {
                                    os.write(buffer, 0, len);
                                }
                                return new String(os.toByteArray());
                            } catch (Exception e) {
                                if (LOGGER.isErrorEnabled()) {
                                    LOGGER.error("读取ZIP的条目[{}]的内容失败: {}", entryName, e.getMessage());
                                }
                                return null;
                            }
                        }
                    }
                    zip.closeEntry();
                }
            } catch (Exception e) {
                if (LOGGER.isErrorEnabled()) {
                    LOGGER.error("提取zip包条目数据失败: {}", e.getMessage());
                }
            }
        }
        return null;
    }
}
