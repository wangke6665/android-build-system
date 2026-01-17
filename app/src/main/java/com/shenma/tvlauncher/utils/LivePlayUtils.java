package com.shenma.tvlauncher.utils;

import java.util.ArrayList;

public class LivePlayUtils {
    public LivePlayUtils() {
    }

    public static ArrayList<String> getData(int type) {
        ArrayList<String> list = new ArrayList<>();
        if (type == 0) {
//            list.add("选集列表");
            list.add("视频解码");
            list.add("画面比例");
            list.add("偏好设置");
            list.add("播放内核");
            list.add("清理数据");
        } else if (type == 1) {
            list.add("软解码");
            list.add("硬解码");
        } else if (type == 2) {
            list.add("原始比例");
            list.add("4:3 缩放");
            list.add("16:9缩放");
            list.add("全屏拉伸");
            list.add("等比缩放");
            list.add("全屏裁剪");
        } else if (type == 3) {
            list.add("上下键切换选集");
            list.add("上下键调节音量");
        } else if (type == 4) {
            list.add("自动");
            list.add("系统");
            list.add("IJK");
            list.add("EXO");
            list.add("阿里");
        }else if (type == 5) {
            list.add("清理所有记忆");
            list.add("清理频道记忆");
            list.add("清理资源记忆");
        }
        return list;
    }
}

