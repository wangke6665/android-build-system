package com.shenma.tvlauncher.utils;

import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.util.Log;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.StringReader;
import java.io.IOException;
import java.security.MessageDigest;
import java.util.Properties;

import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;

/**
 * 加密配置工具类
 * 优先从 assets/config.properties 读取，如果不存在则从 files/config.properties 读取
 * 配置文件只存储密钥种子，实际密钥通过KDF结合设备特征生成
 */
public class EncryptionConfig {
    private static final String TAG = "EncryptionConfig";
    private static final String CONFIG_FILE = "config.properties";
    
    // 前端默认AES密钥种子（用于默认路径，会结合设备指纹派生真正密钥）
    // 如需修改，直接改这个常量即可
    private static final String DEFAULT_AES_KEY_SEED = "G25zqaNtpQW1rJ96ZKIPLHSvGHb1M0kRCG0=";
    
    private static String rc4KeySeed = null;  // RC4密钥种子（不是完整密钥）
    private static String aesKeySeed = null;   // AES密钥种子（不是完整密钥）
    private static String encryptionMethod = null;
    private static Context appContext = null;
    private static boolean initialized = false;
    
    /**
     * 检查是否已初始化
     */
    public static boolean isInitialized() {
        return initialized && appContext != null;
    }
    
    // PBKDF2参数
    private static final int PBKDF2_ITERATIONS = 10000;  // 迭代次数
    private static final int KEY_LENGTH = 256;  // 密钥长度（位）
    
    /**
     * 初始化配置
     * 优先从 assets/config.properties 读取，如果不存在则从 files/config.properties 读取
     * 注意：配置文件只存储密钥种子，实际密钥通过KDF结合设备特征生成
     * @param context Android上下文
     */
    public static void init(Context context) {
        if (initialized && appContext == context) {
            return;
        }
        
        appContext = context.getApplicationContext();
        boolean configLoaded = false;

        // 1. 优先尝试从 assets/config.properties 读取
        try {
            InputStream assetsIs = appContext.getAssets().open(CONFIG_FILE);
            try {
                Properties prop = new Properties();
                prop.load(assetsIs);
                applyProperties(prop);
                configLoaded = true;
                Log.d(TAG, "从 assets/config.properties 读取配置成功");
            } finally {
                try { assetsIs.close(); } catch (Exception ignore) {}
            }
        } catch (Exception e) {
            Log.d(TAG, "assets/config.properties 不存在或读取失败，尝试读取 files/config.properties: " + e.getMessage());
        }

        // 2. 如果 assets 中没有，尝试从 files/config.properties 读取
        if (!configLoaded) {
            File localFile = new File(appContext.getFilesDir(), CONFIG_FILE);
            if (localFile.exists() && localFile.isFile()) {
                Log.d(TAG, "检测到本地覆盖配置文件，尝试读取: " + localFile.getAbsolutePath());
                try {
                    Properties prop = new Properties();
                    InputStream is = new FileInputStream(localFile);
                    try {
                        prop.load(is);
                    } finally {
                        try { is.close(); } catch (Exception ignore) {}
                    }
                    applyProperties(prop);
                    configLoaded = true;
                    Log.d(TAG, "从 files/config.properties 读取配置成功");
                } catch (Exception e) {
                    Log.w(TAG, "files/config.properties 读取失败: " + e.getMessage());
                }
            }
        }

        // 3. 如果两个文件都不存在，使用默认配置
        if (!configLoaded) {
            Log.w(TAG, "未找到配置文件，使用默认配置");
            applyProperties(null); // 使用默认值
        }
        
        initialized = true;
    }

    /**
     * 从指定的配置内容字符串更新配置（支持从外部获取的config.properties文本）
     * 同时将内容写入本地文件，后续启动可直接使用本地覆盖配置
     */
    public static void updateFromConfigContent(String content) {
        if (content == null) return;
        try {
            // 1. 解析配置内容
            Properties prop = new Properties();
            prop.load(new StringReader(content));
            applyProperties(prop);

            // 2. 写入到本地文件，作为覆盖配置
            if (appContext != null) {
                File outFile = new File(appContext.getFilesDir(), CONFIG_FILE);
                FileOutputStream fos = null;
                try {
                    fos = new FileOutputStream(outFile, false);
                    fos.write(content.getBytes("UTF-8"));
                    fos.flush();
                    Log.d(TAG, "已将config.properties覆盖写入本地: " + outFile.getAbsolutePath());
                } catch (IOException ioe) {
                    Log.e(TAG, "写入本地config.properties失败: " + ioe.getMessage());
                } finally {
                    if (fos != null) {
                        try { fos.close(); } catch (Exception ignore) {}
                    }
                }
            }
        } catch (Exception e) {
            Log.e(TAG, "updateFromConfigContent 解析配置失败: " + e.getMessage());
            e.printStackTrace();
        }
    }


    /**
     * 将Properties中的配置应用到当前内存配置
     */
    private static void applyProperties(Properties prop) {
        if (prop == null) {
            initialized = true;
            return;
        }

        try {
            // 读取加密方式配置（默认使用aes256gcm）
            String method = prop.getProperty("encryption_method", "aes256gcm");
            if (method != null) {
                encryptionMethod = method.trim();
            } else {
                encryptionMethod = "aes256gcm";
            }
            Log.d(TAG, "加密方式: " + encryptionMethod);
        } catch (Exception e) {
            encryptionMethod = "aes256gcm";
            Log.w(TAG, "读取encryption_method失败，使用默认: aes256gcm");
        }
            
        try {
            // 读取RC4密钥种子（legacy模式使用）
            String configRc4Seed = prop.getProperty("rc4_key_seed", "");
            if (configRc4Seed != null && !configRc4Seed.trim().isEmpty()) {
                rc4KeySeed = configRc4Seed.trim();
                Log.d(TAG, "从配置文件读取RC4密钥种子，长度: " + rc4KeySeed.length());
            } else {
                Log.d(TAG, "配置文件中未找到rc4_key_seed，将使用默认种子");
            }
        } catch (Exception e) {
            Log.w(TAG, "读取rc4_key_seed失败: " + e.getMessage());
            }
            
        try {
            // 读取AES密钥种子（aes256gcm模式使用）
            String configAesSeed = prop.getProperty("aes_key_seed", "");
            if (configAesSeed != null && !configAesSeed.trim().isEmpty()) {
                aesKeySeed = configAesSeed.trim();
                Log.d(TAG, "从配置文件读取AES密钥种子，长度: " + aesKeySeed.length());
            } else {
                Log.d(TAG, "配置文件中未找到aes_key_seed，将使用默认种子");
            }
        } catch (Exception e) {
            Log.w(TAG, "读取aes_key_seed失败: " + e.getMessage());
        }

        initialized = true;
    }
    
    /**
     * 获取设备特征字符串（用于密钥派生）
     * 结合包名和签名信息，确保即使配置文件泄露，没有设备特征也无法生成正确密钥
     */
    /**
     * 获取设备指纹（公开方法，用于在请求中传递）
     * @return 设备指纹的MD5哈希值
     */
    public static String getDeviceFingerprintForRequest() {
        if (appContext == null) {
            Log.w(TAG, "appContext为空，无法获取设备指纹");
            return "";
        }
        return getDeviceFingerprint(appContext);
    }
    
    private static String getDeviceFingerprint(Context context) {
        try {
            // 简化：直接使用"包的MD5+包名"
            String packageName = context.getPackageName();
            
            // 获取包的签名MD5
            String signatureMD5 = "";
            try {
                PackageInfo packageInfo = context.getPackageManager()
                    .getPackageInfo(packageName, PackageManager.GET_SIGNATURES);
                if (packageInfo.signatures != null && packageInfo.signatures.length > 0) {
                    // 使用签名的完整内容计算MD5
                    byte[] signatureBytes = packageInfo.signatures[0].toByteArray();
                    signatureMD5 = md5(new String(signatureBytes));
                }
            } catch (Exception e) {
                Log.w(TAG, "获取签名信息失败: " + e.getMessage());
            }
            
            // 如果签名MD5为空，使用包名的MD5作为fallback
            if (signatureMD5 == null || signatureMD5.isEmpty()) {
                signatureMD5 = md5(packageName);
                Log.w(TAG, "签名MD5为空，使用包名MD5作为fallback");
            }
            
            // 返回：包的MD5 + 包名
            String fingerprint = signatureMD5 + packageName;
            Log.d(TAG, "设备指纹计算完成，长度: " + fingerprint.length() + ", 包名: " + packageName);
            return fingerprint;
        } catch (Exception e) {
            Log.e(TAG, "获取设备特征失败: " + e.getMessage(), e);
            // 如果获取失败，使用包名MD5+包名作为fallback
            try {
                String packageName = context.getPackageName();
                return md5(packageName) + packageName;
            } catch (Exception e2) {
                return "fallback_fingerprint";
            }
        }
    }
    
    /**
     * 使用PBKDF2从种子和设备特征派生密钥
     */
    private static String deriveKey(String seed, String salt, int keyLengthBytes) {
        try {
            // 如果没有种子，使用默认种子
            if (seed == null || seed.isEmpty()) {
                seed = "DEFAULT_SEED_2024";
            }
            
            // 使用PBKDF2派生密钥
            // Android 4.4 不支持 PBKDF2WithHmacSHA256（需要 API 21+），需要使用兼容算法
            String algorithm = "PBKDF2WithHmacSHA256";
            if (android.os.Build.VERSION.SDK_INT < android.os.Build.VERSION_CODES.LOLLIPOP) {
                // Android 4.4 及以下版本使用 SHA1（兼容）
                algorithm = "PBKDF2WithHmacSHA1";
                Log.d(TAG, "Android 4.4 检测到，使用 PBKDF2WithHmacSHA1 替代 SHA256");
            }
            
            PBEKeySpec spec = new PBEKeySpec(seed.toCharArray(), salt.getBytes("UTF-8"), 
                PBKDF2_ITERATIONS, keyLengthBytes * 8);
            SecretKeyFactory factory = SecretKeyFactory.getInstance(algorithm);
            byte[] keyBytes = factory.generateSecret(spec).getEncoded();
            
            // 转换为十六进制字符串（用于RC4）或直接使用字节（用于AES）
            return bytesToHex(keyBytes);
        } catch (Exception e) {
            Log.e(TAG, "密钥派生失败: " + e.getMessage());
            e.printStackTrace();
            // Fallback：使用简单的MD5哈希
            try {
                MessageDigest md = MessageDigest.getInstance("MD5");
                md.update((seed + salt).getBytes("UTF-8"));
                byte[] hash = md.digest();
                // 扩展到所需长度
                byte[] keyBytes = new byte[keyLengthBytes];
                for (int i = 0; i < keyLengthBytes; i++) {
                    keyBytes[i] = hash[i % hash.length];
                }
                return bytesToHex(keyBytes);
            } catch (Exception e2) {
                Log.e(TAG, "Fallback密钥生成也失败: " + e2.getMessage());
                return seed + salt; // 最后的fallback
            }
        }
    }
    
    /**
     * 字节数组转十六进制字符串
     */
    private static String bytesToHex(byte[] bytes) {
        StringBuilder sb = new StringBuilder();
        for (byte b : bytes) {
            sb.append(String.format("%02x", b));
        }
        return sb.toString();
    }
    
    /**
     * 十六进制字符串转字节数组（用于AES密钥）
     */
    private static byte[] hexToBytes(String hex) {
        int len = hex.length();
        byte[] data = new byte[len / 2];
        for (int i = 0; i < len; i += 2) {
            data[i / 2] = (byte) ((Character.digit(hex.charAt(i), 16) << 4)
                + Character.digit(hex.charAt(i+1), 16));
        }
        return data;
    }
    
    /**
     * 获取加密方式
     * @return 加密方式（"aes256gcm" 或 "legacy"）
     */
    public static String getEncryptionMethod() {
        if (encryptionMethod != null && !encryptionMethod.isEmpty()) {
            return encryptionMethod;
        }
        return "aes256gcm"; // 默认使用新方式
    }
    
    /**
     * 获取RC4密钥（legacy模式使用）
     * 简化版本，适配 Android 4.4：直接使用配置的种子或默认密钥
     * @return RC4密钥
     */
    public static String getRC4Key() {
        // 简化：直接使用配置的种子或默认密钥
        String seed = (rc4KeySeed != null && !rc4KeySeed.isEmpty()) ? rc4KeySeed : "DEFAULT_RC4_SEED";
        
        try {
            // 使用简单的MD5哈希生成密钥（Android 4.4 兼容）
            MessageDigest md = MessageDigest.getInstance("MD5");
            md.update(seed.getBytes("UTF-8"));
            byte[] hash = md.digest();
            
            // 转换为十六进制字符串
            String derivedKey = bytesToHex(hash);
            
            if (derivedKey == null || derivedKey.isEmpty()) {
                Log.w(TAG, "密钥生成失败，使用默认RC4密钥");
                return Constant.rq + Constant.dp;
            }
            
            Log.d(TAG, "使用生成的RC4密钥，长度: " + derivedKey.length());
            return derivedKey;
        } catch (Exception e) {
            Log.e(TAG, "RC4密钥生成失败: " + e.getMessage());
            e.printStackTrace();
            // Fallback到默认密钥
            return Constant.rq + Constant.dp;
        }
    }
    
    /**
     * 获取AES密钥（aes256gcm模式使用）
     * 简化版本，适配 Android 4.4：使用 md5($md5.md5($md5)) 计算并扩展到32字节
     * 不再使用设备指纹，简化加密流程
     * @return AES密钥（字符串，32字节）
     */
    public static String getAESKey() {
        // 使用 md5($md5.md5($md5)) 计算并扩展到32字节
        // 优先使用后端返回的种子，如果为空则使用前端默认种子
        String md5Key = (aesKeySeed != null && !aesKeySeed.isEmpty()) ? aesKeySeed : DEFAULT_AES_KEY_SEED;
        
        try {
            // 简化算法，适配 Android 4.4：不再使用设备指纹
            // 使用 md5($md5.md5($md5)) 计算
            // 步骤1: md5(md5Key)
            String firstMD5 = md5(md5Key);
            // 步骤2: md5(md5Key + firstMD5)
            String combined = md5Key + firstMD5;
            // 步骤3: md5(combined)
            String finalMD5 = md5(combined);
            
            // 将MD5十六进制字符串转换为字节数组
            byte[] keyBytes = hexToBytes(finalMD5);
            
            // 扩展到32字节（重复或填充）
            if (keyBytes.length < 32) {
                byte[] extended = new byte[32];
                int pos = 0;
                while (pos < 32) {
                    int copyLen = Math.min(keyBytes.length, 32 - pos);
                    System.arraycopy(keyBytes, 0, extended, pos, copyLen);
                    pos += copyLen;
                }
                keyBytes = extended;
            } else {
                // 截取前32字节
                byte[] truncated = new byte[32];
                System.arraycopy(keyBytes, 0, truncated, 0, 32);
                keyBytes = truncated;
            }
            
            // 转换为字符串（使用ISO-8859-1编码，确保1:1映射）
            try {
                String key = new String(keyBytes, "ISO-8859-1");
                // 调试：输出密钥的十六进制表示（前32字节）
                StringBuilder keyHex = new StringBuilder();
                for (int i = 0; i < Math.min(32, keyBytes.length); i++) {
                    keyHex.append(String.format("%02x", keyBytes[i] & 0xff));
                }
                Log.d(TAG, "使用计算的AES密钥（长度: " + key.length() + "，前32字节hex: " + keyHex.toString() + "）");
                return key;
            } catch (Exception e) {
                return new String(keyBytes);
            }
        } catch (Exception e) {
            Log.e(TAG, "计算AES密钥失败: " + e.getMessage());
            // Fallback：使用RC4密钥扩展到32字节
            String defaultRc4Key = Constant.rq + Constant.dp;
            StringBuilder sb = new StringBuilder(defaultRc4Key);
            while (sb.length() < 32) {
                sb.append(defaultRc4Key);
            }
            return sb.substring(0, 32);
        }
    }
    
    /**
     * MD5哈希计算（返回十六进制字符串，小写）
     */
    private static String md5(String input) {
        try {
            MessageDigest md = MessageDigest.getInstance("MD5");
            byte[] digest = md.digest(input.getBytes("UTF-8"));
            StringBuilder sb = new StringBuilder();
            for (byte b : digest) {
                sb.append(String.format("%02x", b & 0xff));
            }
            return sb.toString();
        } catch (Exception e) {
            Log.e(TAG, "MD5计算失败: " + e.getMessage());
            return "";
        }
    }
    
    /**
     * 重置配置（用于测试或重新加载）
     */
    public static void reset() {
        initialized = false;
        rc4KeySeed = null;
        aesKeySeed = null;
        encryptionMethod = null;
        appContext = null;
    }
}

