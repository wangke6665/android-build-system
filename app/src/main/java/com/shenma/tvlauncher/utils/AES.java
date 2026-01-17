package com.shenma.tvlauncher.utils;

import android.os.Build;
import android.util.Base64;
import android.util.Log;

import javax.crypto.Cipher;
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.security.MessageDigest;

public class AES {
    private static final String TAG = "Aes";
    private static final String CBC_PKCS5_PADDING = "AES/CBC/PKCS5Padding";
    private static final String GCM_NO_PADDING = "AES/GCM/NoPadding";
    private static final String AES = "AES";
    private static final int GCM_TAG_LENGTH = 16; // GCM认证标签长度（128位）
    private static final int GCM_NONCE_LENGTH = 12; // GCM nonce长度（96位）
    /**
     * AES 加密
     *
     * @param strKey            加密密钥
     * @param strClearText      待加密内容
     * @param mstrIvParameter   密钥偏移量
     * @return 返回Base64转码后的加密数据
     */
    public static String encrypt_Aes(String strKey, String strClearText, String mstrIvParameter){

        try {
            byte[] raw = strKey.getBytes();
            // 创建AES密钥
            SecretKeySpec skeySpec = new SecretKeySpec(raw, AES);
            // 创建密码器
            Cipher cipher = Cipher.getInstance(CBC_PKCS5_PADDING);
            // 创建偏移量
            IvParameterSpec iv = new IvParameterSpec(mstrIvParameter.getBytes());
            // 初始化加密器
            cipher.init(Cipher.ENCRYPT_MODE, skeySpec, iv);
            // 执行加密操作
            byte[] cipherText = cipher.doFinal(strClearText.getBytes());
            //Log.d(TAG, "encrypt result(not BASE64): " + cipherText.toString());
            String strBase64Content = Base64.encodeToString(cipherText, Base64.DEFAULT); // encode it by BASE64 again
            //Log.d(TAG, "encrypt result(BASE64): " + strBase64Content);
            strBase64Content = strBase64Content.replaceAll(System.getProperty("line.separator"), "");

            return strBase64Content;
        } catch (Exception e) {
            e.printStackTrace();
        }

        return null;
    }

    /**
     * AES 解密
     *
     * @param strKey            解密密钥
     * @param strCipherText      待解密内容
     * @param mstrIvParameter   偏移量
     * @return 返回Base64转码后的加密数据
     */
    public static String decrypt_Aes(String strKey, String strCipherText, String mstrIvParameter) throws Exception {

        try {
            byte[] raw = strKey.getBytes("ASCII");
            // 创建AES秘钥
            SecretKeySpec skeySpec = new SecretKeySpec(raw, AES);
            // 创建密码器
            Cipher cipher = Cipher.getInstance(CBC_PKCS5_PADDING);
            // 创建偏移量
            IvParameterSpec iv = new IvParameterSpec(mstrIvParameter.getBytes());
            // 初始化解密器
            cipher.init(Cipher.DECRYPT_MODE, skeySpec, iv);
            // 执行解密操作
            byte[] cipherText = Base64.decode(strCipherText, Base64.DEFAULT); // decode by BASE64 first
            //Log.d(TAG, "BASE64 decode result(): " + cipherText.toString());
            byte[] clearText = cipher.doFinal(cipherText);
            String strClearText = new String(clearText);
            //Log.d(TAG, "decrypt result: " + strClearText);

            return strClearText;
        } catch (Exception e) {
            e.printStackTrace();
        }

        return null;
    }

    /**
     * AES-256-GCM 解密（新加密方式，更安全）
     * 输入格式：Base64(nonce + ciphertext + tag)
     * Android 4.4 兼容：直接使用 CBC 模式（Android 4.4 不支持 GCM）
     * 
     * @param strKey            解密密钥（32字节）
     * @param strCipherText     Base64编码的密文（格式：nonce + ciphertext + tag 或 IV + ciphertext）
     * @return 解密后的明文（字节数组）
     */
    public static byte[] decryptAES256GCMToBytes(String strKey, String strCipherText) throws Exception {
        try {
            // Base64解码
            byte[] ciphertextBytes = Base64.decode(strCipherText, Base64.DEFAULT);
            if (ciphertextBytes == null || ciphertextBytes.length < 16) {
                throw new IllegalArgumentException("密文长度不足（至少需要16字节IV）");
            }
            
            // 准备密钥（确保32字节）
            // 注意：密钥来自EncryptionConfig.getAESKey()，使用ISO-8859-1编码（1:1字节映射）
            byte[] keyBytes = prepareKey(strKey.getBytes("ISO-8859-1"), 32);
            SecretKeySpec skeySpec = new SecretKeySpec(keyBytes, AES);
            
            // Android 4.4 及以下版本直接使用 CBC 模式（完全兼容）
            if (Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP) {
                Log.d(TAG, "Android 4.4 检测到，使用 CBC 模式解密");
                
                // CBC 模式：前16字节是IV，剩余是密文
                byte[] iv = new byte[16];
                System.arraycopy(ciphertextBytes, 0, iv, 0, 16);
                
                byte[] ciphertextOnly = new byte[ciphertextBytes.length - 16];
                if (ciphertextBytes.length > 16) {
                    System.arraycopy(ciphertextBytes, 16, ciphertextOnly, 0, ciphertextOnly.length);
                } else {
                    throw new IllegalArgumentException("密文数据不足");
                }
                
                Cipher cipher = Cipher.getInstance(CBC_PKCS5_PADDING);
                IvParameterSpec ivSpec = new IvParameterSpec(iv);
                cipher.init(Cipher.DECRYPT_MODE, skeySpec, ivSpec);
                byte[] plaintext = cipher.doFinal(ciphertextOnly);
                
                Log.d(TAG, "CBC 解密成功，明文长度: " + plaintext.length);
                return plaintext;
            } else {
                // Android 5.0+ 尝试使用 GCM 模式
                try {
            // 提取nonce（前12字节）
            byte[] nonce = new byte[GCM_NONCE_LENGTH];
            System.arraycopy(ciphertextBytes, 0, nonce, 0, GCM_NONCE_LENGTH);
            
            // 提取密文和tag（剩余部分）
            byte[] ciphertextAndTag = new byte[ciphertextBytes.length - GCM_NONCE_LENGTH];
            System.arraycopy(ciphertextBytes, GCM_NONCE_LENGTH, ciphertextAndTag, 0, ciphertextAndTag.length);
            
            // 创建GCM参数
            GCMParameterSpec gcmSpec = new GCMParameterSpec(GCM_TAG_LENGTH * 8, nonce);
            
            // 创建密码器
            Cipher cipher = Cipher.getInstance(GCM_NO_PADDING);
            cipher.init(Cipher.DECRYPT_MODE, skeySpec, gcmSpec);
            
            // 执行解密（GCM会自动验证认证标签）
            byte[] plaintext = cipher.doFinal(ciphertextAndTag);
            
                    Log.d(TAG, "GCM 解密成功，明文长度: " + plaintext.length);
                    return plaintext;
                } catch (Exception gcmException) {
                    // GCM 失败，回退到 CBC 模式
                    Log.w(TAG, "GCM 解密失败，回退到 CBC 模式: " + gcmException.getMessage());
                    
                    // CBC 模式：前16字节是IV，剩余是密文
                    byte[] iv = new byte[16];
                    System.arraycopy(ciphertextBytes, 0, iv, 0, Math.min(16, ciphertextBytes.length));
                    
                    byte[] ciphertextOnly = new byte[ciphertextBytes.length - 16];
                    if (ciphertextBytes.length > 16) {
                        System.arraycopy(ciphertextBytes, 16, ciphertextOnly, 0, ciphertextOnly.length);
                    } else {
                        throw new IllegalArgumentException("密文数据不足");
                    }
                    
                    Cipher cipher = Cipher.getInstance(CBC_PKCS5_PADDING);
                    IvParameterSpec ivSpec = new IvParameterSpec(iv);
                    cipher.init(Cipher.DECRYPT_MODE, skeySpec, ivSpec);
                    byte[] plaintext = cipher.doFinal(ciphertextOnly);
                    
                    Log.d(TAG, "CBC 回退解密成功，明文长度: " + plaintext.length);
            return plaintext;
                }
            }
        } catch (Exception e) {
            Log.e(TAG, "AES解密失败: " + e.getMessage());
            Log.e(TAG, "Android 版本: " + Build.VERSION.SDK_INT);
            e.printStackTrace();
            throw e;
        }
    }
    
    /**
     * AES-256-GCM 解密（新加密方式，更安全）
     * 输入格式：Base64(nonce + ciphertext + tag)
     * 
     * @param strKey            解密密钥（32字节）
     * @param strCipherText     Base64编码的密文（格式：nonce + ciphertext + tag）
     * @return 解密后的明文（UTF-8字符串）
     */
    public static String decryptAES256GCM(String strKey, String strCipherText) throws Exception {
        byte[] plaintext = decryptAES256GCMToBytes(strKey, strCipherText);
        return new String(plaintext, "UTF-8");
    }
    
    /**
     * 准备密钥，确保长度为指定字节数
     * 如果密钥长度不够，使用MD5哈希扩展到指定长度
     */
    private static byte[] prepareKey(byte[] key, int requiredLength) throws Exception {
        if (key.length == requiredLength) {
            return key;
        }
        
        byte[] result = new byte[requiredLength];
        if (key.length < requiredLength) {
            // 复制原始密钥
            System.arraycopy(key, 0, result, 0, key.length);
            
            // 使用MD5哈希扩展到所需长度
            MessageDigest md = MessageDigest.getInstance("MD5");
            int offset = key.length;
            while (offset < requiredLength) {
                byte[] hash = md.digest(key);
                int copyLength = Math.min(hash.length, requiredLength - offset);
                System.arraycopy(hash, 0, result, offset, copyLength);
                offset += copyLength;
                // 更新哈希输入（使用之前的哈希结果）
                md.reset();
                md.update(hash);
            }
        } else {
            // 如果密钥太长，截取前requiredLength字节
            System.arraycopy(key, 0, result, 0, requiredLength);
        }
        
        return result;
    }

}

