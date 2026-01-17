/*原版*/
package com.shenma.tvlauncher.utils;

/**
 * @author joychang
 * @Description 字符串偏移
 */

public class Kodi {
    /**
     * 字符串偏移
     *
     * @param str 偏移字符串
     * @return
     */

    public static String a(String str) {
        StringBuilder result = new StringBuilder();
        for (int i = 0; i < str.length(); i++) {
            char c = str.charAt(i);
            if (c >= 'a' && c <= 'm') {
                c += 13;
            } else if (c >= 'n' && c <= 'z') {
                c -= 13;
            } else if (c >= 'A' && c <= 'M') {
                c += 13;
            } else if (c >= 'N' && c <= 'Z') {
                c -= 13;
            }
            result.append(c);
        }
        return result.toString();
    }

    public static String b(String str) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < str.length(); i++) {
            char c = str.charAt(i);
            if (c >= 'a' && c <= 'z') {
                c = (char) (c + 13);
                if (c > 'z') {
                    c = (char) (c - 26);
                }
            } else if (c >= 'A' && c <= 'Z') {
                c = (char) (c + 13);
                if (c > 'Z') {
                    c = (char) (c - 26);
                }
            }
            sb.append(c);
        }
        return sb.toString();
    }

    public static String c(String str) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < str.length(); i++) {
            char c = str.charAt(i);
            if (c >= 'a' && c <= 'z') {
                c = (char) (c + 7);
                if (c > 'z') {
                    c = (char) (c - 26);
                }
            } else if (c >= 'A' && c <= 'Z') {
                c = (char) (c + 7);
                if (c > 'Z') {
                    c = (char) (c - 26);
                }
            }
            sb.append(c);
        }
        return sb.toString();
    }
}
