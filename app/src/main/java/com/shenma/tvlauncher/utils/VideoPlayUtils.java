package com.shenma.tvlauncher.utils;

import java.util.ArrayList;

public class VideoPlayUtils {
    public VideoPlayUtils() {
    }

    public static ArrayList<String> getData(int type) {
        ArrayList<String> list = new ArrayList<>();
        if (type == 0) {
            list.add("选集列表");
            list.add("倍速播放");
            list.add("视频解码");
            list.add("画面比例");
            list.add("偏好设置");
            list.add("跳过片头");
            list.add("跳过片尾");
            list.add("播放内核");
            list.add("弹幕设置");
        } else if (type == 1) {
            list.add("0.50倍速");
            list.add("0.75倍速");
            list.add("1.00倍速");
            list.add("1.25倍速");
            list.add("1.50倍速");
            list.add("2.00倍速");
//            if (VideoPlayerActivity.nhposition != 0 && VideoPlayerActivity.nhposition != 2){
//                list.add("3.00倍速");
//                list.add("4.00倍速");
//                list.add("5.00倍速");
//            }
//            list.add("3.00倍速");
//            list.add("4.00倍速");
//            list.add("5.00倍速");
        } else if (type == 2) {
            list.add("软解码");
            list.add("硬解码");
        } else if (type == 3) {
            list.add("原始比例");
            list.add("4:3 缩放");
            list.add("16:9缩放");
            list.add("全屏拉伸");
            list.add("等比缩放");
            list.add("全屏裁剪");
        } else if (type == 4) {
            list.add("上下键切换选集");
            list.add("上下键调节音量");
        }else if (type == 5) {//跳过片头
            list.add("0秒");
            list.add("10秒");
            list.add("15秒");
            list.add("20秒");
            list.add("30秒");
            list.add("60秒");
            list.add("90秒");
            list.add("120秒");
            list.add("150秒");
            list.add("180秒");
            list.add("240秒");
            list.add("300秒");
        }else if (type == 6) {//跳过片尾
            list.add("0秒");
            list.add("10秒");
            list.add("15秒");
            list.add("20秒");
            list.add("30秒");
            list.add("60秒");
            list.add("90秒");
            list.add("120秒");
            list.add("150秒");
            list.add("180秒");
            list.add("240秒");
            list.add("300秒");
        }else if (type == 7) {//内核
            list.add("自动");
            list.add("系统");
            list.add("IJK");
            list.add("EXO");
            list.add("阿里");
        }else if (type == 8) {//弹幕设置主菜单
            list.add("弹幕开关");
            list.add("弹幕大小");
            list.add("弹幕速度");
            list.add("弹幕行数");
            list.add("弹幕透明");
        }else if (type == 9) {//弹幕开关
            list.add("关闭弹幕");
            list.add("开启弹幕");
        }else if (type == 10) {//弹幕大小
            list.add("极小");
            list.add("较小");
            list.add("标准");
            list.add("较大");
            list.add("极大");
        }else if (type == 11) {//弹幕速度
            list.add("极慢");
            list.add("较慢");
            list.add("标准");
            list.add("较快");
            list.add("极快");
        }else if (type == 12) {//弹幕行数
            list.add("3行");
            list.add("5行");
            list.add("8行");
            list.add("10行");
            list.add("不限");
        }else if (type == 13) {//弹幕透明
            list.add("100%");
            list.add("80%");
            list.add("60%");
            list.add("40%");
            list.add("20%");
        }
        return list;
    }
}

