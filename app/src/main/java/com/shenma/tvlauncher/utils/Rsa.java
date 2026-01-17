package com.shenma.tvlauncher.utils;

import android.util.Base64;

import java.io.ByteArrayOutputStream;
import java.security.KeyFactory;
import java.security.PublicKey;
import java.security.spec.X509EncodedKeySpec;

import javax.crypto.Cipher;

public class Rsa {
    private static final String TAG = "Rsa";


    /**
     * 公钥解密
     *
     * @param dataString
     * @param publicKeyString
     * @return
     */
//    public static String decrypt_Rsa(String dataString, String publicKeyString) throws Exception {
//        byte[] publicKeyBytes = Base64.decode(publicKeyString, Base64.DEFAULT);
//        X509EncodedKeySpec keySpec = new X509EncodedKeySpec(publicKeyBytes);
//        KeyFactory keyFactory = KeyFactory.getInstance("RSA");
//        PublicKey publicKey = keyFactory.generatePublic(keySpec);
//        Cipher cipher = Cipher.getInstance("RSA/ECB/PKCS1Padding");
//        cipher.init(Cipher.DECRYPT_MODE, publicKey);
//        byte[] encryptedBytes = Base64.decode(dataString, Base64.DEFAULT);
//        byte[] decryptedBytes = cipher.doFinal(encryptedBytes);
//        return new String(decryptedBytes);
//    }

    /**
     * 公钥解密
     *
     * @param dataString
     * @param publicKeyString
     * @return
     */
    public static String decrypt_Rsa(String dataString, String publicKeyString) throws Exception {
        byte[] publicKeyBytes = Base64.decode(publicKeyString, Base64.DEFAULT);
        X509EncodedKeySpec keySpec = new X509EncodedKeySpec(publicKeyBytes);
        KeyFactory keyFactory = KeyFactory.getInstance("RSA");
        PublicKey publicKey = keyFactory.generatePublic(keySpec);
        Cipher cipher = Cipher.getInstance("RSA/ECB/PKCS1Padding");
        cipher.init(Cipher.DECRYPT_MODE, publicKey);

        byte[] encryptedBytes = Base64.decode(dataString, Base64.DEFAULT);
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        int inputLength = encryptedBytes.length;
        int offset = 0;
        int maxDecryptBlockSize = 128;

        while (inputLength - offset > 0) {
            byte[] cache;
            if (inputLength - offset > maxDecryptBlockSize) {
                cache = cipher.doFinal(encryptedBytes, offset, maxDecryptBlockSize);
            } else {
                cache = cipher.doFinal(encryptedBytes, offset, inputLength - offset);
            }
            outputStream.write(cache, 0, cache.length);
            offset += maxDecryptBlockSize;
        }

        byte[] decryptedBytes = outputStream.toByteArray();
        outputStream.close();
        return new String(decryptedBytes);
    }


    /**
     * 公钥加密
     *
     * @param dataString
     * @param publicKeyString
     * @return
     */
    public static String encrypt_Rsa(String dataString, String publicKeyString) throws Exception {
        byte[] publicKeyBytes = Base64.decode(publicKeyString, Base64.DEFAULT);
        X509EncodedKeySpec keySpec = new X509EncodedKeySpec(publicKeyBytes);
        KeyFactory keyFactory = KeyFactory.getInstance("RSA");
        PublicKey publicKey = keyFactory.generatePublic(keySpec);
        Cipher cipher = Cipher.getInstance("RSA/ECB/PKCS1Padding");
        cipher.init(Cipher.ENCRYPT_MODE, publicKey);

        byte[] dataBytes = dataString.getBytes();
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        int inputLength = dataBytes.length;
        int offset = 0;
        int maxEncryptBlockSize = 117;

        while (inputLength - offset > 0) {
            byte[] cache;
            if (inputLength - offset > maxEncryptBlockSize) {
                cache = cipher.doFinal(dataBytes, offset, maxEncryptBlockSize);
            } else {
                cache = cipher.doFinal(dataBytes, offset, inputLength - offset);
            }
            outputStream.write(cache, 0, cache.length);
            offset += maxEncryptBlockSize;
        }

        byte[] encryptedBytes = outputStream.toByteArray();
        outputStream.close();
        return Base64.encodeToString(encryptedBytes, Base64.DEFAULT);
    }

}
