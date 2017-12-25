package org.sj.iot.util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.TrustManagerFactory;
import java.io.InputStream;
import java.security.KeyStore;
import java.security.cert.Certificate;
import java.security.cert.CertificateFactory;

/**
 * Ssl工具类
 *
 * @author shijian
 * @email shijianws@163.com
 * @date 2017-09-13
 */
public class SslUtil {
    private static final Logger LOGGER = LoggerFactory.getLogger(SslUtil.class);

    /**
     * 加载秘钥库
     */
    public static KeyManagerFactory createKeyManagerFactory(String keystore, String password) {
        try {
            char[] pwd = password.toCharArray();
            KeyStore keyStore = KeyStore.getInstance("JKS");
            keyStore.load(Tools.class.getClassLoader().getResourceAsStream(keystore), pwd);
            KeyManagerFactory keyFactory = KeyManagerFactory.getInstance("SunX509");
            keyFactory.init(keyStore, pwd);
            return keyFactory;
        } catch (Exception e) {
            if (LOGGER.isErrorEnabled()) {
                LOGGER.error("加载秘钥库失败: {}", e.getMessage());
            }
            throw new RuntimeException("加载秘钥库失败!", e);
        }
    }

    /**
     * 加载秘钥与证书
     */
    public static KeyManagerFactory createKeyManagerFactory(InputStream privateKey, InputStream keyCertChain) {
        try {
            CertificateFactory cf = CertificateFactory.getInstance("X.509");
            Certificate[] certs = cf.generateCertificates(keyCertChain).stream()/*.map(X509Certificate.class::cast)*/.toArray(Certificate[]::new);
            KeyStore keyStore = KeyStore.getInstance("PKCS12");
            keyStore.setKeyEntry(null, Tools.inputStream2ByteArray(privateKey), certs);
        } catch (Exception e) {
            if (LOGGER.isErrorEnabled()) {
                LOGGER.error("加载秘钥、证书失败: {}", e.getMessage());
            }
            throw new RuntimeException(e);
        }
        return null;

    }

    /**
     * 加载秘钥库
     */
    public static TrustManagerFactory createTrustManagerFactory(String keystore, String password) {
        try {
            KeyStore trustStore = KeyStore.getInstance("JKS");
            trustStore.load(Tools.class.getClassLoader().getResourceAsStream(keystore), password.toCharArray());
            TrustManagerFactory trustFactory = TrustManagerFactory.getInstance("SunX509");
            trustFactory.init(trustStore);
            return trustFactory;
        } catch (Exception e) {
            if (LOGGER.isErrorEnabled()) {
                LOGGER.error("加载授信证书库失败: {}", e.getMessage());
            }
            throw new RuntimeException("加载授信证书库失败!", e);
        }
    }
}
