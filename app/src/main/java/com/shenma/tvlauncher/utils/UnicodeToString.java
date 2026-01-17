package com.shenma.tvlauncher.utils;

public class UnicodeToString {
    public static String decode(String unicodeStr) {
        if (unicodeStr == null) {
            return null;
        }
        StringBuffer retBuf = new StringBuffer();
        int maxLoop = unicodeStr.length();
        int i = 0;
        while (i < maxLoop) {
            if (unicodeStr.charAt(i) != '\\') {
                retBuf.append(unicodeStr.charAt(i));
            } else if (i >= maxLoop - 5 || !(unicodeStr.charAt(i + 1) == 'u' || unicodeStr.charAt(i + 1) == 'U')) {
                retBuf.append(unicodeStr.charAt(i));
            } else {
                try {
                    retBuf.append((char) Integer.parseInt(unicodeStr.substring(i + 2, i + 6), 16));
                    i += 5;
                } catch (NumberFormatException e) {
                    retBuf.append(unicodeStr.charAt(i));
                }
            }
            i++;
        }
        return retBuf.toString();
    }
}
