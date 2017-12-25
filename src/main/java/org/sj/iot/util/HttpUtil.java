package org.sj.iot.util;

import com.google.common.base.Strings;
import okhttp3.*;
import okhttp3.Request.Builder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.net.ssl.*;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URLEncoder;
import java.security.SecureRandom;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;

/**
 * HTTP工具类
 *
 * @author shijian
 * @email shijianws@163.com
 */
public class HttpUtil {
    private static final Logger LOGGER = LoggerFactory.getLogger(HttpUtil.class);

    static {
        // 信任证书管理
        X509TrustManager x509TrustManager = new X509TrustManager() {
            @Override
            public void checkClientTrusted(X509Certificate[] chain, String authType) throws CertificateException {
                // 信任任何客户端证书，不检查
            }

            @Override
            public void checkServerTrusted(X509Certificate[] chain, String authType) throws CertificateException {
                // 信任任何服务器证书，不检查
            }

            @Override
            public X509Certificate[] getAcceptedIssuers() {
                return new X509Certificate[0];
            }
        };
        SSLContext sc = null;
        try {
            sc = SSLContext.getInstance("SSL");
            sc.init(null, new TrustManager[]{x509TrustManager}, new SecureRandom());
        } catch (Exception e) {
            LOGGER.error(String.format("初始化SSL失败: %s", e.getMessage()), e);
        }
        OkHttpClient.Builder builder = new OkHttpClient.Builder();
        if (sc != null) {
            // 信任任何证书
            builder.sslSocketFactory(sc.getSocketFactory(), x509TrustManager).hostnameVerifier(new HostnameVerifier() {
                @Override
                public boolean verify(String hostname, SSLSession session) {
                    // 任何校验都通过
                    return true;
                }
            });
        }
        builder.connectTimeout(1, TimeUnit.HOURS) // 连接超时
                .writeTimeout(1, TimeUnit.HOURS) // 写超时
                .readTimeout(1, TimeUnit.HOURS); // 读超时

        okHttpClient = builder.build();

        noRedirectsClient = builder
                .followRedirects(false) // 禁止http重定向
                .followSslRedirects(false) // 禁止https重定向
                .cookieJar(new LocalCookieJar()) // 携带Cookie策略
                .build();
    }

    static class LocalCookieJar implements CookieJar {
        private static final Map<String, List<Cookie>> cache = new ConcurrentHashMap<>();

        @Override
        public void saveFromResponse(HttpUrl url, List<Cookie> cookies) {
            String path = url.uri().toString();
            int index = path.indexOf('?');
            if (index != -1) {
                path = path.substring(0, index);
            }
            path = path.substring(0, path.lastIndexOf('/')); // 截取当前路径的上一级绝对路径地址路径
            cache.put(path, cookies);
        }

        private static final List<Cookie> DEFAULT_LIST = new ArrayList<>();

        @Override
        public List<Cookie> loadForRequest(HttpUrl url) {
            String path = url.uri().toString();
            for (Entry<String, List<Cookie>> en : cache.entrySet()) {
                if (path.startsWith(en.getKey())) {
                    // 当前路径的父路径存在Cookie信息
                    List<Cookie> value = en.getValue();
                    if (value == null) {
                        value = DEFAULT_LIST; // value不能为null
                    }
                    return value;
                }
            }
            return DEFAULT_LIST;
        }
    }

    private static final OkHttpClient okHttpClient; // http客户端
    private static final OkHttpClient noRedirectsClient; // 关闭重定向http客户端

    public enum Method {
        GET, POST, PUT, PATCH, DELETE
    }

    public static class HttpResponse {
        private int status;
        private Map<String, String> headers;
        private boolean readBody = false;
        private InputStream bodyInputStream;

        private HttpResponse(int status, Map<String, String> headers, InputStream bodyInputStream) {
            this.status = status;
            this.headers = headers;
            this.bodyInputStream = bodyInputStream;
        }

        /**
         * 获取HTTP响应状态码
         */
        public int getStatus() {
            return status;
        }

        /**
         * 获取所有响应头
         */
        public Map<String, String> getHeaders() {
            if (headers != null && !headers.isEmpty()) {
                return new HashMap<>(headers);
            }
            return headers;
        }

        /**
         * 获取指定响应头
         */
        public String getHeader(String name) {
            if (headers != null && !headers.isEmpty() && name != null) {
                return headers.get(name.toLowerCase());
            }
            return null;
        }

        /**
         * 获取响应体字符数据
         */
        public String getBody() {
            byte[] bs = getBodyAsByteArray();
            if (bs == null || bs.length == 0) {
                return "";
            }
            return new String(bs);
        }

        /**
         * 获取响应体流
         */
        public InputStream getBodyAsStream() {
            return bodyInputStream;
        }

        /**
         * 获取响应体字节数据
         */
        public byte[] getBodyAsByteArray() {
            if (readBody) {
                throw new IllegalStateException("响应体数据已被读取!");
            }
            readBody = true;
            try (ByteArrayOutputStream os = new ByteArrayOutputStream()) {
                byte[] buf = new byte[1024];
                for (int len; (len = bodyInputStream.read(buf)) != -1; ) {
                    os.write(buf, 0, len);
                }
                return os.toByteArray();
            } catch (Exception e) {
                LOGGER.error(e.getMessage());
                throw new RuntimeException(e.getMessage(), e);
            }
        }
    }

    /**
     * 响应处理函数
     *
     * @param response
     * @param callback
     */
    private static void processResponse(Response response, Consumer<HttpResponse> callback) {
        if (response == null || callback == null) {
            return;
        }
        try {
            int status = response.code(); // HTTP请求状态码
            Map<String, String> headers = new HashMap<>();
            for (Entry<String, List<String>> en : response.headers().toMultimap().entrySet()) {
                headers.put(en.getKey(), en.getValue().get(0));
            }
            InputStream result = response.body().byteStream();
            callback.accept(new HttpResponse(status, headers, result));
        } catch (Exception e) {
            LOGGER.error("处理响应消息体失败: {} \n{}", e.getMessage(), response.request());
            if (e instanceof RuntimeException) {
                throw e;
            }
            throw new RuntimeException("处理响应消息体失败!", e);
        }
    }

    /**
     * GET方式请求获取数据
     *
     * @param url
     * @param callback 如果回调为null则只会触发请求
     */
    public static void get(String url, Consumer<HttpResponse> callback) {
        response(false, url, Method.GET, null, null, callback);
    }

    /**
     * GET方式请求获取数据
     *
     * @param url
     * @param headers  请求头
     * @param callback 如果回调为null则只会触发请求
     */
    public static void get(String url, Map<String, ? extends Object> headers, Consumer<HttpResponse> callback) {
        response(false, url, Method.GET, headers, null, callback);
    }

    /**
     * GET方式请求获取数据
     *
     * @param url
     * @param headers    请求头
     * @param parameters 请求头参数
     * @param callback   如果回调为null则只会触发请求
     */
    public static void get(String url, Map<String, ? extends Object> headers, Map<String, ? extends Object> parameters, Consumer<HttpResponse> callback) {
        response(false, url, Method.GET, headers, parameters, callback);
    }

    /**
     * POST方式请求获取数据
     *
     * @param url
     * @param callback 如果回调为null则只会触发请求
     */
    public static void post(String url, Consumer<HttpResponse> callback) {
        response(false, url, Method.POST, null, null, callback);
    }

    /**
     * POST方式请求获取数据
     *
     * @param url
     * @param headers    请求头
     * @param parameters 请求体参数
     * @param callback   如果回调为null则只会触发请求
     */
    public static void post(String url, Map<String, ? extends Object> headers, Map<String, ? extends Object> parameters, Consumer<HttpResponse> callback) {
        response(false, url, Method.POST, headers, parameters, callback);
    }

    /**
     * POST方式请求获取数据
     *
     * @param url
     * @param headers  请求头
     * @param obj      请求体参数、上传文件、输入流
     * @param callback 如果回调为null则只会触发请求
     */
    public static void post(String url, Map<String, ? extends Object> headers, Object obj, Consumer<HttpResponse> callback) {
        response(false, url, Method.POST, headers, obj, callback);
    }

    public static void post(String url, Map<String, ? extends Object> headers, Consumer<HttpResponse> callback) {
        response(false, url, Method.POST, headers, null, callback);
    }

    private static Map<String, ? extends Object> addJsonContentType(Map<String, ? extends Object> headers) {
        @SuppressWarnings("unchecked")
        Map<String, Object> map = (Map<String, Object>) headers;
        if (map == null) {
            map = new HashMap<String, Object>();
        }
        map.put("Content-Type", "application/json");
        return map;
    }

    /**
     * POST方式请求获取数据
     *
     * @param url
     * @param headers  请求头
     * @param jsonBody JSON格式请求体
     * @param callback 如果回调为null则只会触发请求
     */
    public static void post(String url, Map<String, ? extends Object> headers, List<? extends Object> jsonBody, Consumer<HttpResponse> callback) {
        response(false, url, Method.POST, addJsonContentType(headers), jsonBody, callback);
    }

    /**
     * POST方式请求获取数据
     *
     * @param url
     * @param headers  请求头
     * @param jsonBody JSON格式请求体
     * @param callback 如果回调为null则只会触发请求
     */
    public static void post(String url, Map<String, ? extends Object> headers, String jsonBody, Consumer<HttpResponse> callback) {
        response(false, url, Method.POST, addJsonContentType(headers), jsonBody, callback);
    }

    /**
     * PUT方式请求获取数据
     *
     * @param url
     * @param headers    请求头
     * @param parameters 请求体参数
     * @param callback   如果回调为null则只会触发请求
     */
    public static void put(String url, Map<String, ? extends Object> headers, Map<String, ? extends Object> parameters, Consumer<HttpResponse> callback) {
        response(false, url, Method.PUT, headers, parameters, callback);
    }

    /**
     * PUT方式请求获取数据
     *
     * @param url
     * @param headers  请求头
     * @param jsonBody JSON数据
     * @param callback 如果回调为null则只会触发请求
     */
    public static void put(String url, Map<String, ? extends Object> headers, String jsonBody, Consumer<HttpResponse> callback) {
        response(false, url, Method.PUT, addJsonContentType(headers), jsonBody, callback);
    }

    /**
     * PATCH方式请求获取数据
     *
     * @param url
     * @param headers    请求头
     * @param parameters 请求体参数
     * @param callback   如果回调为null则只会触发请求
     */
    public static void patch(String url, Map<String, ? extends Object> headers, Map<String, ? extends Object> parameters, Consumer<HttpResponse> callback) {
        response(false, url, Method.PATCH, headers, parameters, callback);
    }

    /**
     * PATCH方式请求获取数据
     *
     * @param url
     * @param headers  请求头
     * @param json     JSON对象请求体
     * @param callback 如果回调为null则只会触发请求
     */
    public static void patch(String url, Map<String, ? extends Object> headers, Object json, Consumer<HttpResponse> callback) {
        response(false, url, Method.PATCH, addJsonContentType(headers), json, callback);
    }

    /**
     * PATCH方式请求获取数据
     *
     * @param url
     * @param headers  请求头
     * @param jsonBody JSON格式请求体
     * @param callback 如果回调为null则只会触发请求
     */
    public static void patch(String url, Map<String, ? extends Object> headers, String jsonBody, Consumer<HttpResponse> callback) {
        response(false, url, Method.PATCH, addJsonContentType(headers), jsonBody, callback);
    }

    /**
     * DELETE方式请求获取数据
     *
     * @param url
     * @param callback 如果回调为null则只会触发请求
     */
    public static void delete(String url, Consumer<HttpResponse> callback) {
        response(false, url, Method.DELETE, null, null, callback);
    }

    /**
     * DELETE方式请求获取数据
     *
     * @param url
     * @param headers  请求头
     * @param callback 如果回调为null则只会触发请求
     */
    public static void delete(String url, Map<String, ? extends Object> headers, Map<String, ? extends Object> parameters, Consumer<HttpResponse> callback) {
        response(false, url, Method.DELETE, headers, parameters, callback);
    }

    /**
     * 请求URL，并获取响应
     *
     * @param async    是否异步请求
     * @param url
     * @param method   请求方式，如果为null则使用GET
     * @param headers  请求头
     * @param obj      请求参数，可能为Map、List(JSON)、String(JSON)
     * @param callback 回调函数，status:响应状体码，InputStream:响应消息体流，如果回调函数为null则触发异步请求
     */
    public static void response(boolean async, String url, Method method, Map<String, ? extends Object> headers, Object obj, Consumer<HttpResponse> callback) {
        response(okHttpClient, async, url, method, headers, obj, callback);
    }

    /**
     * 发送不自动重定向请求
     */
    public static void noRedirectsResponse(String url, Method method, Map<String, ? extends Object> headers, Object obj, Consumer<HttpResponse> callback) {
        response(noRedirectsClient, false, url, method, headers, obj, callback);
    }

    @SuppressWarnings("unchecked")
    private static void response(OkHttpClient client, boolean async, String url, Method method, Map<String, ? extends Object> headers, Object obj, Consumer<HttpResponse> callback) {
        if (Strings.isNullOrEmpty(url)) {
            LOGGER.error("请求URL不能为空!");
            throw new IllegalArgumentException("请求URL不能为空!");
        }
        Builder builder = new Builder(); // 请求构建器
        String[] contentType = new String[1];
        if (headers != null && !headers.isEmpty()) {
            // 添加请求头
            for (Entry<String, ? extends Object> en : headers.entrySet()) {
                String name = en.getKey();
                Object value = en.getValue();
                if ("Content-Type".equalsIgnoreCase(name)) {
                    contentType[0] = value.toString();
                }
                builder.addHeader(name, value.toString());
            }
        }
        if (method == null) {
            method = Method.GET;
        }
        if (Method.GET == method) {
            builder.get();
            Map<String, ? extends Object> parameters = (Map<String, ? extends Object>) obj;
            if (parameters != null && !parameters.isEmpty()) {
                // GET方式添加请求参数
                StringBuilder buf = new StringBuilder();
                for (Entry<String, ? extends Object> en : parameters.entrySet()) {
                    String name = en.getKey();
                    Object value = en.getValue();
                    if (!Strings.isNullOrEmpty(name)) {
                        buf.append('&').append(name).append('=');
                        try {
                            value = URLEncoder.encode(value.toString(), Constants.UTF8_CHARSET.name());
                        } catch (Exception e) {
                            LOGGER.error(e.getMessage(), e);
                        }
                        buf.append(value);
                    }
                }
                if (url.indexOf('?') == -1) {
                    // 不包含?
                    // url = url + '?' + buf.substring(1);
                    url = buf.deleteCharAt(0).insert(0, '?').insert(0, url).toString(); // 删除第一个字符&插入?插入url
                } else {
                    // url = url + buf.toString();
                    url = buf.insert(0, url).toString();
                }
            }
        } else {
            // 非GET方式添加请求参数进入请求体
            RequestBody requestBody = null;
            String value = contentType[0];
            if (value != null && value.toLowerCase().indexOf("json") != -1) {
                // Content-Type为JSON类型
                String jsonParameters = null;
                if (obj == null) {
                    jsonParameters = "{}"; // 空JSON
                } else if (obj instanceof String) {
                    jsonParameters = (String) obj; // 已经转换好的JSON字符串
                } else {
                    jsonParameters = JsonUtil.toJsonString(obj); // 将对象转换成JSON字符串
                }
                requestBody = RequestBody.create(MediaType.parse(value), jsonParameters); // 创建请求体对象
            } else {
                // 非JSON请求体的传统表单请求
                if (obj instanceof Map) {
                    Map<String, ? extends Object> parameters = (Map<String, ? extends Object>) obj; // 请求参数
                    if (parameters != null && !parameters.isEmpty()) {
                        boolean multipart = false;
                        for (Entry<String, ? extends Object> en : parameters.entrySet()) {
                            Object val = en.getValue();
                            if (val instanceof File) {
                                multipart = true;
                                break;
                            }
                        }
                        if (multipart) {
                            // 上传文件
                            MultipartBody.Builder bodyBuilder = new MultipartBody.Builder().setType(MultipartBody.FORM);
                            for (Entry<String, ? extends Object> en : parameters.entrySet()) {
                                String name = en.getKey();
                                Object val = en.getValue();
                                if (val instanceof File) {
                                    File file = (File) val;
                                    bodyBuilder.addFormDataPart(name, file.getName(), RequestBody.create(null, file));
                                    continue;
                                }
                                bodyBuilder.addFormDataPart(name, val.toString());
                            }
                            requestBody = bodyBuilder.build();
                        } else {
                            FormBody.Builder bodyBuilder = new FormBody.Builder(); // 表单构建器
                            for (Entry<String, ? extends Object> en : parameters.entrySet()) {
                                bodyBuilder.addEncoded(en.getKey(), en.getValue().toString());
                            }
                            requestBody = bodyBuilder.build();
                        }
                    }
                } else if (obj instanceof File) {
                    // application/octet-stream
                    requestBody = RequestBody.create(null, (File) obj);
                } else if (obj instanceof InputStream) {
                    byte[] bs = Tools.inputStream2ByteArray((InputStream) obj);
                    requestBody = RequestBody.create(null, bs);
                }
                if (requestBody == null) {
                    requestBody = new FormBody.Builder().build();
                }
            }
            builder.method(method.toString(), requestBody); // 设置请求方式和请求体
        }
        LOGGER.debug("\n\t请求URL: {} -> {}\n\t请求头: {}\n\t请求参数: ", method, url, headers, obj);
        Request request = builder.url(url).build(); // 构建请求
        Call call = client.newCall(request); // 创建调用对象
        try {
            if (async || callback == null) {
                // 异步调用
                call.enqueue(new Callback() {
                    @Override
                    public void onResponse(Call call, Response response) throws IOException {
                        // 服务端响应
                        if (callback != null) {
                            // 存在回调函数，执行回调函数
                            processResponse(response, callback);
                        }
                    }

                    @Override
                    public void onFailure(Call call, IOException e) {
                        LOGGER.error(e.getMessage());
                        // 发生错误
                        throw new RuntimeException(e.getMessage(), e);
                    }
                });
                return;
            }
            // 同步调用
            Response response = call.execute();
            if (callback != null) {
                processResponse(response, callback);
            }
        } catch (Exception e) {
            LOGGER.error(e.getMessage());
            if (e instanceof RuntimeException) {
                throw (RuntimeException) e;
            }
            throw new RuntimeException(e.getMessage(), e);
        }
    }
}
