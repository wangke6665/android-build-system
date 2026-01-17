/*混淆版*/
package com.shenma.tvlauncher.utils;

import kod.Loader;

public class IO {
    static {
        Loader.registerNativesForClass(0);
        native_special_clinit3();
    }

    public static native String a(String str, String str2);

    private static native byte[] b(byte[] bArr, byte[] bArr2) throws Exception;

    private static native void native_special_clinit3();
}

/*原版*/
//package com.shenma.tvlauncher.utils;
//
//import android.util.Base64;
//import javax.crypto.Cipher;
//import javax.crypto.spec.SecretKeySpec;
//
///**
// * @author joychang
// * @Description RC4解密
// */
//
//public class IO {
////    /**
////     * Base64Rc4解密
////     *
////     * @param encryptedBase64
////     * @param key
////     * @return
////     */
////    public static String decryptBase64(String encryptedBase64, String key) {
////        try {
////            byte[] encryptedData = Base64.decode(encryptedBase64, Base64.DEFAULT);
////            byte[] decryptedData = rc4Decrypt(encryptedData, key.getBytes());
////            return new String(decryptedData);
////        } catch (Exception e) {
////            e.printStackTrace();
////        }
////        return null;
////    }
////
////    private static byte[] rc4Decrypt(byte[] data, byte[] key) throws Exception {
////        SecretKeySpec secretKeySpec = new SecretKeySpec(key, "RC4");
////        Cipher cipher = Cipher.getInstance("RC4");
////        cipher.init(Cipher.DECRYPT_MODE, secretKeySpec);
////        return cipher.doFinal(data);
////    }
//
//    /**
//     * Base64Rc4解密
//     *
//     * @param a 偏移字符串
//     * @param b 解密密钥
//     * @return
//     */
//    public static String a(String a, String b) {
//        try {
//            byte[] c = android.util.Base64.decode(a, android.util.Base64.DEFAULT);
//            byte[] d = b(c, Md5Encoder.encode(Constant.e + Md5Encoder.encode(Constant.e)).getBytes());
//            return new String(d);
//        } catch (Exception e) {
//            e.printStackTrace();
//        }
//        return null;
//    }
//
//    private static byte[] b(byte[] a, byte[] b) throws Exception {
//        javax.crypto.spec.SecretKeySpec c = new javax.crypto.spec.SecretKeySpec(b, "RC4");
//        javax.crypto.Cipher d = javax.crypto.Cipher.getInstance("RC4");
//        d.init(javax.crypto.Cipher.DECRYPT_MODE, c);
//        return d.doFinal(a);
//    }
//
//}
