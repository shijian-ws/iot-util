package org.sj.iot.util;

import com.google.common.base.Strings;
import org.bouncycastle.asn1.pkcs.PrivateKeyInfo;
import org.bouncycastle.asn1.x500.X500Name;
import org.bouncycastle.asn1.x500.X500NameBuilder;
import org.bouncycastle.asn1.x500.style.BCStyle;
import org.bouncycastle.asn1.x509.SubjectPublicKeyInfo;
import org.bouncycastle.cert.X509CertificateHolder;
import org.bouncycastle.cert.jcajce.JcaX509CertificateConverter;
import org.bouncycastle.cert.jcajce.JcaX509v3CertificateBuilder;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.bouncycastle.openssl.PEMEncryptedKeyPair;
import org.bouncycastle.openssl.PEMKeyPair;
import org.bouncycastle.openssl.PEMParser;
import org.bouncycastle.openssl.PKCS8Generator;
import org.bouncycastle.openssl.jcajce.*;
import org.bouncycastle.operator.ContentSigner;
import org.bouncycastle.operator.OutputEncryptor;
import org.bouncycastle.operator.jcajce.JcaContentSignerBuilder;
import org.bouncycastle.pkcs.PKCS10CertificationRequest;
import org.bouncycastle.pkcs.PKCS8EncryptedPrivateKeyInfo;
import org.bouncycastle.pkcs.jcajce.JcaPKCS10CertificationRequestBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.math.BigInteger;
import java.security.*;
import java.security.cert.Certificate;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.security.interfaces.RSAPrivateKey;
import java.util.*;

/**
 * CA工具类
 *
 * @author shijian
 * @email shijianws@163.com
 * @date 2016-07-15
 */
public class CaUtil {
    private static final Logger LOGGER = LoggerFactory.getLogger(CaUtil.class);

    private static final String ALGORITHM_NAME = "RSA"; // 算法名
    private static final String ALGORITHM_SIGNATURE = "SHA256WITHRSA"; // 算法签名
    public static final Provider PROVIDER = new BouncyCastleProvider(); // 密钥服务提供商BouncyCastle，可以使用Security.addProvider(PROVIDER);添加到默认，也可以再每个构造器中指定

    private static KeyPairGenerator generator;
    private static final JcaPEMKeyConverter keyConverter = new JcaPEMKeyConverter().setProvider(PROVIDER);
    private static final JcaX509CertificateConverter certConverter = new JcaX509CertificateConverter().setProvider(PROVIDER);

    static {
        try {
            generator = KeyPairGenerator.getInstance(ALGORITHM_NAME, PROVIDER);
            generator.initialize(2048);
        } catch (Exception e) {
            LOGGER.error(String.format("密钥工具类初始化失败: %s", e.getMessage()), e);
            throw new RuntimeException("密钥工具类初始化失败!", e);
        }
    }

    private CaUtil() {
    }

    /**
     * 生成未加密的RSA私钥
     */
    public static String generateRSAPrivateKey() {
        return generateRSAPrivateKey(null);
    }

    /**
     * 生成加密的RSA私钥
     */
    public static String generateRSAPrivateKey(String password) {
        try {
            KeyPair keyPair = generator.generateKeyPair();
            RSAPrivateKey privateKey = (RSAPrivateKey) keyPair.getPrivate();
            return convert(privateKey, password); // 加密私钥
        } catch (Exception e) {
            LOGGER.error(String.format("生成RSA私钥失败: %s", e.getMessage()), e);
            throw new RuntimeException("生成RSA私钥失败!", e);
        }
    }

    /**
     * 生成RSA公钥
     */
    public static String generateRSAPublicKey(String privateKey, String password) {
        try {
            KeyPair keyPair = parseKeyPair(privateKey, password);
            return convert(keyPair.getPublic(), null);
        } catch (Exception e) {
            LOGGER.error(String.format("生成RSA公钥失败: %s", e.getMessage()), e);
            throw new RuntimeException("生成RSA公钥失败!", e);
        }
    }

    /**
     * 解析非加密的私钥
     */
    public static PrivateKey parsePrivateKey(String privateKey) {
        return parsePrivateKey(privateKey, null);
    }

    /**
     * 解析私钥
     */
    public static PrivateKey parsePrivateKey(String privateKey, String password) {
        try {
            Object obj = parseKey(new StringReader(privateKey));
            Objects.requireNonNull(obj, "没有加载到私钥信息!");
            if (obj instanceof PEMKeyPair) {
                // 解析成功
                PEMKeyPair keyPair = (PEMKeyPair) obj;
                return keyConverter.getPrivateKey(keyPair.getPrivateKeyInfo());
            } else if (obj instanceof PKCS8EncryptedPrivateKeyInfo) {
                // 解析到了带有密码的私钥
                if (Strings.isNullOrEmpty(password)) {
                    LOGGER.error("解析到加密的私钥，需要密码!");
                    throw new RuntimeException("解析到加密的私钥，需要密码!");
                }
                PKCS8EncryptedPrivateKeyInfo encryptInfo = (PKCS8EncryptedPrivateKeyInfo) obj;
                try {
                    // 使用密码解密，注意密码解析器的类型，需使用OpenSSL生成的加密密钥
                    PrivateKeyInfo keyInfo = encryptInfo
                            .decryptPrivateKeyInfo(new JceOpenSSLPKCS8DecryptorProviderBuilder()
                                    .setProvider(PROVIDER).build(password.toCharArray()));
                    return keyConverter.getPrivateKey(keyInfo);
                } catch (Exception e) {
                    LOGGER.error(String.format("解密私钥时发生错误: %s", e.getMessage()), e);
                    throw new RuntimeException("解密私钥时发生错误", e);
                }
            } else if (obj instanceof PEMEncryptedKeyPair) {
                // 解析到了带有密码的私钥
                if (Strings.isNullOrEmpty(password)) {
                    LOGGER.error("解析到加密的私钥，需要密码!");
                    throw new RuntimeException("解析到加密的私钥，需要密码!");
                }
                PEMEncryptedKeyPair encryptKeyPair = (PEMEncryptedKeyPair) obj;
                try {
                    // 使用密码解密，注意密码解析器的类型，使用JcePEMEncryptorBuilder生成的加密密钥
                    PEMKeyPair keyPair = encryptKeyPair.decryptKeyPair(new JcePEMDecryptorProviderBuilder().setProvider(PROVIDER).build(password.toCharArray()));
                    return keyConverter.getPrivateKey(keyPair.getPrivateKeyInfo());
                } catch (Exception e) {
                    LOGGER.error(String.format("解密私钥时发生错误: %s", e.getMessage()), e);
                    throw new RuntimeException("解密私钥时发生错误!", e);
                }
            }
            LOGGER.error("未知私钥类型: {}", obj.getClass());
            throw new RuntimeException("未知私钥类型: " + obj.getClass());
        } catch (Exception e) {
            throw new RuntimeException(e.getMessage(), e);
        }
    }

    /**
     * 解析密钥对
     */
    public static KeyPair parseKeyPair(String privateKey, String password) {
        try {
            Object obj = parseKey(new StringReader(privateKey));
            Objects.requireNonNull(obj, "没有加载到私钥信息!");
            if (obj instanceof PEMKeyPair) {
                // 解析成功
                PEMKeyPair keyPair = (PEMKeyPair) obj;
                return keyConverter.getKeyPair(keyPair);
            } else if (obj instanceof PKCS8EncryptedPrivateKeyInfo) {
                // 解析到了带有密码的私钥
                if (Strings.isNullOrEmpty(password)) {
                    LOGGER.error("解析到加密的私钥，需要密码!");
                    throw new RuntimeException("解析到加密的私钥，需要密码!");
                }
                PKCS8EncryptedPrivateKeyInfo encryptInfo = (PKCS8EncryptedPrivateKeyInfo) obj;
                PrivateKeyInfo keyInfo = null;
                try {
                    // 使用密码解密，注意密码解析器的类型，需使用OpenSSL生成的加密密钥
                    keyInfo = encryptInfo.decryptPrivateKeyInfo(new JceOpenSSLPKCS8DecryptorProviderBuilder()
                            .setProvider(PROVIDER).build(password.toCharArray()));
                } catch (Exception e) {
                    LOGGER.error(String.format("解密私钥时发生错误: %s", e.getMessage()), e);
                    throw new RuntimeException("解密私钥时发生错误!", e);
                }
                // 针对已解密的密钥进行重新加载
                Object key = parseKey(new StringReader(convert(keyConverter.getPrivateKey(keyInfo), null)));
                if (key instanceof PEMKeyPair) {
                    // 解析成功
                    PEMKeyPair keyPair = (PEMKeyPair) key;
                    return keyConverter.getKeyPair(keyPair);
                }
                LOGGER.error("解析错误，生成公钥的私钥是未知私钥类型: %s", obj.getClass());
                throw new RuntimeException("解析错误，生成公钥的私钥是未知私钥类型: " + obj.getClass());
            }
            LOGGER.error("未知私钥类型: " + obj.getClass());
            throw new RuntimeException("未知私钥类型: " + obj.getClass());
        } catch (Exception e) {
            throw new RuntimeException(e.getMessage(), e);
        }
    }

    /**
     * 解析公钥
     */
    public static PublicKey parsePublicKey(String publicKey) {
        try {
            Object obj = parseKey(new StringReader(publicKey));
            Objects.requireNonNull(obj, "没有加载到公钥信息!");
            if (obj instanceof SubjectPublicKeyInfo) {
                // 解析成功
                SubjectPublicKeyInfo info = (SubjectPublicKeyInfo) obj;
                return keyConverter.getPublicKey(info);
            }
            LOGGER.error("未知公钥类型: " + obj.getClass());
            throw new RuntimeException("未知公钥类型: " + obj.getClass());
        } catch (Exception e) {
            throw new RuntimeException(e.getMessage(), e);
        }
    }

    /**
     * 解析证书
     */
    public static X509Certificate parseCertificate(String crt) {
        try {
            Object obj = parseKey(new StringReader(crt));
            Objects.requireNonNull(obj, "没有加载到证书信息!");
            if (obj instanceof X509CertificateHolder) {
                X509CertificateHolder holder = (X509CertificateHolder) obj;
                return certConverter.getCertificate(holder);
            }
            LOGGER.error("未知证书类型: " + obj.getClass());
            throw new RuntimeException("未知证书类型: " + obj.getClass());
        } catch (Exception e) {
            throw new RuntimeException(e.getMessage(), e);
        }
    }

    /**
     * 解析请求文件
     */
    public static PKCS10CertificationRequest parseCertificateRequest(String csr) {
        try {
            Object obj = parseKey(new StringReader(csr));
            Objects.requireNonNull(obj, "没有加载到请求文件信息!");
            if (obj instanceof PKCS10CertificationRequest) {
                return (PKCS10CertificationRequest) obj;
            }
            LOGGER.error("未知请求文件类型: " + obj.getClass());
            throw new RuntimeException("未知请求文件类型: " + obj.getClass());
        } catch (Exception e) {
            throw new RuntimeException(e.getMessage(), e);
        }
    }

    /**
     * 将密钥信息转换为字符串，可对私钥进行加密
     */
    public static String convert(Object key, String password) {
        Objects.requireNonNull(key, "密钥对象不能为空!");
        try (StringWriter buf = new StringWriter()) {
            try (JcaPEMWriter writer = new JcaPEMWriter(buf)) {
                Object obj = key;
                if (!Strings.isNullOrEmpty(password) && key instanceof PrivateKey) {
                    // 需要使用JceOpenSSLPKCS8DecryptorProviderBuilder解密
                    OutputEncryptor encryptor = new JceOpenSSLPKCS8EncryptorBuilder(PKCS8Generator.PBE_SHA1_3DES)
                            .setProvider(PROVIDER).setPasssword(password.toCharArray()).build();
                    obj = new JcaPKCS8Generator((PrivateKey) key, encryptor).generate();
                    // 需要JcePEMDecryptorProviderBuilder解密
                    // obj = new JcePEMEncryptorBuilder("DES-EDE3-CBC").setProvider(PROVIDER).build(password.toCharArray());
                }
                writer.writeObject(obj);
            } catch (Exception e) {
                LOGGER.error(String.format("转换密钥失败: %s", e.getMessage()), e);
                throw new RuntimeException("转换密钥失败!", e);
            }
            return buf.toString();
        } catch (Exception e) {
            throw new RuntimeException(e.getMessage(), e);
        }
    }

    private static KeyStore keystore;

    /**
     * 从密钥库中读取密钥对
     *
     * @param is       密钥库输入流
     * @param alias    私钥别名
     * @param password 密钥库密码
     * @return
     */
    public static KeyPair parseKeystore(InputStream is, String alias, String password) {
        if (keystore == null) {
            synchronized (String.format("%s.parseKeystore", KeyStore.class.getName())) {
                if (keystore == null) {
                    try {
                        keystore = KeyStore.getInstance("JKS");
                    } catch (KeyStoreException e) {
                        LOGGER.error(String.format("初始化密钥库失败: %s", e.getMessage()), e);
                        return null;
                    }
                }
            }
        }
        if (keystore == null) {
            return null;
        }
        char[] pwd = password == null ? null : password.toCharArray();
        try {
            keystore.load(is, pwd);
            Key key = keystore.getKey(alias, pwd);
            if (key instanceof PrivateKey) {
                Certificate cert = keystore.getCertificate(alias);
                PublicKey publicKey = cert.getPublicKey();
                return new KeyPair(publicKey, (PrivateKey) key);
            }
            return null;
        } catch (NoSuchAlgorithmException e) {
            LOGGER.error(String.format("未找到加密算法: %s", e.getMessage()), e);
            throw new RuntimeException(e.getMessage(), e);
        } catch (UnrecoverableKeyException e) {
            LOGGER.error(String.format("无法读取密钥库中私钥: %s", e.getMessage()), e);
            throw new RuntimeException(e.getMessage(), e);
        } catch (IOException | CertificateException | KeyStoreException e) {
            LOGGER.error(String.format("读取密钥库失败: %s", e.getMessage()), e);
            throw new RuntimeException(e.getMessage(), e);
        }
    }

    /**
     * 解析字符流中的密钥信息
     */
    private static Object parseKey(Reader reader) {
        try (PEMParser parser = new PEMParser(reader)) {
            return parser.readObject();
        } catch (Exception e) {
            LOGGER.error(String.format("解析密钥失败: %s", e.getMessage()), e);
            throw new RuntimeException("解析密钥失败!", e);
        }
    }

    /**
     * 生成的danme信息
     */
    private static X500Name getX500Name(String cn) {
        // C=CN, S=BJ, L=BJ, O=DiaoDiao, OU=YanFa, CN={0}
        return new X500NameBuilder()
                .addRDN(BCStyle.C, "CN")
                .addRDN(BCStyle.ST, "BJ")
                .addRDN(BCStyle.O, "DiaoDiao")
                .addRDN(BCStyle.OU, "YanFa")
                .addRDN(BCStyle.CN, cn)
                .build();
    }

    /**
     * 生成请求文件内容
     */
    public static String generateCertificateRequest(String cn, String key, String password) {
        try {
            KeyPair keyPair = parseKeyPair(key, password);
            JcaPKCS10CertificationRequestBuilder builder = new JcaPKCS10CertificationRequestBuilder(getX500Name(cn), keyPair.getPublic());
            ContentSigner signer = new JcaContentSignerBuilder(ALGORITHM_SIGNATURE).setProvider(PROVIDER).build(keyPair.getPrivate());
            PKCS10CertificationRequest request = builder.build(signer);
            return convert(request, null);
        } catch (Exception e) {
            LOGGER.error(String.format("生成请求文件失败: %s", e.getMessage()), e);
            throw new RuntimeException("生成请求文件失败!", e);
        }
    }

    /**
     * 生成私钥和自签证书，返回zip包字节数据
     */
    public static byte[] selfSignedCertificate(String cn) {
        try {
            KeyPair keyPair = generator.generateKeyPair();
            PrivateKey privateKey = keyPair.getPrivate();
            PublicKey publicKey = keyPair.getPublic();
            X500Name issuer = getX500Name(cn != null && !"".equals(cn = cn.trim()) ? "ROOT" : cn);
            X509Certificate cert = signCertificate(privateKey, issuer, null, publicKey, issuer, null);
            String privateKeyContent = convert(privateKey, null);
            String certContent = convert(cert, null);
            Map<String, byte[]> entries = new LinkedHashMap<>();
            entries.put("root.key", privateKeyContent.getBytes(Constants.UTF8_CHARSET));
            entries.put("root.crt", certContent.getBytes(Constants.UTF8_CHARSET));
            return ZipUtil.generateZip(entries);
        } catch (Exception e) {
            throw new RuntimeException(e.getMessage(), e);
        }
    }

    /**
     * 证书认证
     *
     * @param rootKey  CA私钥
     * @param rootCrt  CA证书
     * @param csr      请求文件
     * @param notAfter 证书过期时间
     * @return
     */
    public static byte[] signCertificate(String rootKey, String rootCrt, String csr, Date notAfter) {
        try {
            return signCertificate(parsePrivateKey(rootKey), parseCertificate(rootCrt), parseCertificateRequest(csr), notAfter);
        } catch (Exception e) {
            LOGGER.error(String.format("签发证书失败: %s", e.getMessage()), e);
            throw new RuntimeException("签发证书失败!", e);
        }
    }

    private static byte[] signCertificate(PrivateKey rootKey, X509Certificate rootCert, PKCS10CertificationRequest req, Date notAfter) {
        try {
            X500Name issuer = X500Name.getInstance(rootCert.getIssuerX500Principal().getEncoded());
            X500Name subject = req.getSubject();
            PublicKey publicKey = keyConverter.getPublicKey(req.getSubjectPublicKeyInfo()); // 请求文件中的公钥
            X509Certificate cert = signCertificate(rootKey, issuer, null, publicKey, subject, notAfter);
            Map<String, byte[]> entries = new LinkedHashMap<>();
            entries.put("root.crt", convert(rootCert, null).getBytes(Constants.UTF8_CHARSET));
            entries.put("user.crt", convert(cert, null).getBytes(Constants.UTF8_CHARSET));
            return ZipUtil.generateZip(entries);
        } catch (Exception e) {
            LOGGER.error(String.format("签发证书失败: %s", e.getMessage()), e);
            throw new RuntimeException("签发证书失败!", e);
        }
    }

    /**
     * 证书认证
     *
     * @param caKey     CA私钥
     * @param issuer    CA描述
     * @param serial    证书序列号
     * @param publicKey 证书公钥
     * @param subject   使用者描述
     * @param notAfter  过期时间，默认1年
     * @return
     */
    private static X509Certificate signCertificate(PrivateKey caKey, X500Name issuer, BigInteger serial, PublicKey publicKey, X500Name subject, Date notAfter) {
        try {
            if (caKey == null) {
                LOGGER.error("签发证书时，没有找到CA私钥!");
                throw new RuntimeException("签发证书时，没有找到CA私钥!");
            }
            if (publicKey == null) {
                LOGGER.error("签发证书时，没有找到证书公钥!");
                throw new RuntimeException("签发证书时，没有找到证书公钥!");
            }
            Date notBefore = new Date(); // 证书生效时间
            if (issuer == null) {
                issuer = getX500Name("CA"); // 颁发者信息
            }
            if (serial == null) {
                serial = BigInteger.valueOf(Tools.getCurrentTimeMillis());
            }
            if (subject == null) {
                LOGGER.error("证书使用者信息不能为空!");
                throw new RuntimeException("证书使用者信息不能为空!");
            }
            if (notAfter == null || notAfter.getTime() <= notBefore.getTime()) {
                Calendar calendar = Calendar.getInstance();
                calendar.add(Calendar.MONTH, 12);
                notAfter = calendar.getTime(); // 证书过期时间
            }
            ContentSigner signer = new JcaContentSignerBuilder(ALGORITHM_SIGNATURE).setProvider(PROVIDER).build(caKey);
            X509CertificateHolder holder = new JcaX509v3CertificateBuilder(issuer, serial, notBefore, notAfter, subject, publicKey).build(signer);
            X509Certificate cert = certConverter.getCertificate(holder);
            cert.verify(publicKey);
            return cert;
        } catch (Exception e) {
            LOGGER.error(String.format("签发证书失败: %s", e.getMessage()), e);
            throw new RuntimeException("签发证书失败!", e);
        }
    }
}
