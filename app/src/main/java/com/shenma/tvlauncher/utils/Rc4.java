package com.shenma.tvlauncher.utils;

import android.support.v4.view.MotionEventCompat;
import android.util.Base64;

import javax.crypto.Cipher;
import javax.crypto.spec.SecretKeySpec;

/**
 * @author joychang
 * @Description RC4加解密
 */

public class Rc4 {
    private static final String TAG = "Rc4";

    public static String decry_RC4(byte[] data, String key) {
        if (data == null || key == null) {
            return null;
        }
        return asString(RC4Base(data, key));
    }

    /**
     * Rc4解密
     *
     * @param data
     * @param key
     * @return
     */
    public static String decry_RC4(String data, String key) {
        if (data == null || key == null) {
            return null;
        }
        return new String(RC4Base(HexString2Bytes(data), key));
    }

    public static byte[] encry_RC4_byte(String data, String key) {
        if (data == null || key == null) {
            return null;
        }
        return RC4Base(data.getBytes(), key);
    }

    /**
     * Rc4加密
     *
     * @param data
     * @param key
     * @return
     */
    public static String encry_RC4_string(String data, String key) {
        if (data == null || key == null) {
            return null;
        }
        return toHexString(asString(encry_RC4_byte(data, key)));
    }

    private static String asString(byte[] buf) {
        StringBuffer strbuf = new StringBuffer(buf.length);
        for (byte b : buf) {
            strbuf.append((char) b);
        }
        return strbuf.toString();
    }

    private static byte[] initKey(String aKey) {
        int i;
        byte[] b_key = aKey.getBytes();
        byte[] state = new byte[256];
        for (i = 0; i < 256; i++) {
            state[i] = (byte) i;
        }
        int index1 = 0;
        int index2 = 0;
        if (b_key == null || b_key.length == 0) {
            return null;
        }
        for (i = 0; i < 256; i++) {
            index2 = (((b_key[index1] & MotionEventCompat.ACTION_MASK) + (state[i] & MotionEventCompat.ACTION_MASK)) + index2) & MotionEventCompat.ACTION_MASK;
            byte tmp = state[i];
            state[i] = state[index2];
            state[index2] = tmp;
            index1 = (index1 + 1) % b_key.length;
        }
        return state;
    }

    private static String toHexString(String s) {
        String str = "";
        for (int i = 0; i < s.length(); i++) {
            String s4 = Integer.toHexString(s.charAt(i) & MotionEventCompat.ACTION_MASK);
            if (s4.length() == 1) {
                s4 = new StringBuilder(String.valueOf('0')).append(s4).toString();
            }
            str = new StringBuilder(String.valueOf(str)).append(s4).toString();
        }
        return str;
    }

    private static byte[] HexString2Bytes(String src) {
        int size = src.length();
        byte[] ret = new byte[(size / 2)];
        byte[] tmp = src.getBytes();
        for (int i = 0; i < size / 2; i++) {
            ret[i] = uniteBytes(tmp[i * 2], tmp[(i * 2) + 1]);
        }
        return ret;
    }

    private static byte uniteBytes(byte src0, byte src1) {
        char _b0 = (char) Byte.decode("0x" + new String(new byte[]{src0})).byteValue();
        _b0 = (char) (_b0 << 4);
        char _b1 = (char) Byte.decode("0x" + new String(new byte[]{src1})).byteValue();
        byte ret = (byte) (_b0 ^ _b1);
        return ret;
    }

    private static byte[] RC4Base(byte[] input, String mKkey) {
        int x = 0;
        int y = 0;
        byte[] key = initKey(mKkey);
        byte[] result = new byte[input.length];
        for (int i = 0; i < input.length; i++) {
            x = (x + 1) & MotionEventCompat.ACTION_MASK;
            y = ((key[x] & MotionEventCompat.ACTION_MASK) + y) & MotionEventCompat.ACTION_MASK;
            byte tmp = key[x];
            key[x] = key[y];
            key[y] = tmp;
            result[i] = (byte) (input[i] ^ key[((key[x] & MotionEventCompat.ACTION_MASK) + (key[y] & MotionEventCompat.ACTION_MASK)) & MotionEventCompat.ACTION_MASK]);
        }

        return result;
    }


    /**
     * Base64Rc4解密
     *
     * @param encryptedBase64
     * @param key
     * @return
     */
    public static String decryptBase64(String encryptedBase64, String key) {
        try {
            byte[] encryptedData = Base64.decode(encryptedBase64, Base64.DEFAULT);
            byte[] decryptedData = rc4Decrypt(encryptedData, key.getBytes());
            return new String(decryptedData);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    private static byte[] rc4Decrypt(byte[] data, byte[] key) throws Exception {
        SecretKeySpec secretKeySpec = new SecretKeySpec(key, "RC4");
        Cipher cipher = Cipher.getInstance("RC4");
        cipher.init(Cipher.DECRYPT_MODE, secretKeySpec);
        return cipher.doFinal(data);
    }


}
