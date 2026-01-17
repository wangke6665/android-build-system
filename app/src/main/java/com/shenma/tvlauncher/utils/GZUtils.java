package com.shenma.tvlauncher.utils;

import java.io.ByteArrayOutputStream;
import java.util.zip.Inflater;


public class GZUtils {
    public static byte[] A(byte[] H) {
        byte[] T = null;
        ByteArrayOutputStream C = new ByteArrayOutputStream();
        // 使用nowrap=false（默认值），因为PHP的gzcompress和Go的zlib.NewWriter都使用zlib格式（有zlib头和校验和）
        Inflater R = new Inflater(false);
        try {
            R.setInput(H);
            final byte[] M = new byte[256];
            while (!R.finished()) {
                int count = R.inflate(M);
                C.write(M, 0, count);
            }
            T = C.toByteArray();
            C.close();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            R.end();
        }
        return T;
    }

//    public static byte[] gzuncompress(byte[] data) {
//        byte[] unCompressed = null;
//        ByteArrayOutputStream bos = new ByteArrayOutputStream();
//        Inflater deCompressor = new Inflater();
//        try {
//            deCompressor.setInput(data);
//            final byte[] buf = new byte[256];
//            while (!deCompressor.finished()) {
//                int count = deCompressor.inflate(buf);
//                bos.write(buf, 0, count);
//            }
//            unCompressed = bos.toByteArray();
//            bos.close();
//        } catch (Exception e) {
//            e.printStackTrace();
//        } finally {
//            deCompressor.end();
//        }
//        return unCompressed;
//    }


}