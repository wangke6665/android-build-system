package com.forcetech.android;

import com.shenma.tvlauncher.utils.Rc4;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public class BaseUrl {
    public static String BASEURL = "YUhSMGNEb3ZMelF6TGpFME15NHhPVE11TWpJMk9qazVPUzkwZGk1cWMyOXU=";//两次Base64加密  加密网址https://www.bejson.com/enc/base64/

    public static String getdata(String rest, String appname, String packageName) {
        String KEY = encode(appname + packageName);
        return Rc4.decry_RC4(rest, KEY);
    }

    public static String encode(String pwd) {
        try {
            MessageDigest digest = MessageDigest.getInstance("MD5");
            byte[] bytes = digest.digest(pwd.getBytes());
            StringBuffer sb = new StringBuffer();
            for (int i = 0; i < bytes.length; i++) {
                String s = Integer.toHexString(0xff & bytes[i]);

                if (s.length() == 1) {
                    sb.append("0" + s);
                } else {
                    sb.append(s);
                }
            }
            return sb.toString();
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
            throw new RuntimeException("buhuifasheng");
        }
    }
}
