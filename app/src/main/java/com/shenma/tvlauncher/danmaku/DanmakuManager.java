package com.shenma.tvlauncher.danmaku;

import android.content.Context;
import android.graphics.Color;
import android.os.Build;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.View;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import master.flame.danmaku.controller.DrawHandler;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Response;
import com.shenma.tvlauncher.utils.OkHttpUtil;
import master.flame.danmaku.danmaku.model.BaseDanmaku;
import master.flame.danmaku.danmaku.model.DanmakuTimer;
import master.flame.danmaku.danmaku.model.IDanmakus;
import master.flame.danmaku.danmaku.model.IDisplayer;
import master.flame.danmaku.danmaku.model.android.DanmakuContext;
import master.flame.danmaku.danmaku.model.android.Danmakus;
import master.flame.danmaku.danmaku.parser.BaseDanmakuParser;
import master.flame.danmaku.ui.widget.DanmakuView;

import com.shenma.tvlauncher.tvlive.network.ThreadPoolManager;

/**
 * 弹幕管理器
 */
public class DanmakuManager {
    private static final String TAG = "DanmakuManager";
    
    private Context context;
    private DanmakuView danmakuView;
    private DanmakuContext danmakuContext;
    private DanmakuConfig config;
    private Handler mainHandler;
    private boolean isPrepared = false;
    private boolean isShowing = true;
    private List<DanmakuItem> danmakuItems = new ArrayList<>(); // 存储所有弹幕数据
    private List<DanmakuItem> allDanmakuItems = new ArrayList<>(); // 存储所有弹幕数据（完整列表）
    private long currentPlayPosition = 0; // 当前播放位置，用于弹幕加载完成后正确跳转和动态添加弹幕
    private boolean isDataLoaded = false; // 弹幕数据是否已加载完成
    private Handler timerHandler = new Handler(Looper.getMainLooper()); // 用于动态添加弹幕的定时器
    private Runnable addDanmakuRunnable = null; // 动态添加弹幕的任务
    
    // 弹幕数据项
    private static class DanmakuItem {
        long time;
        int type;
        int color;
        String content;
        
        DanmakuItem(long time, int type, int color, String content) {
            this.time = time;
            this.type = type;
            this.color = color;
            this.content = content;
        }
    }
    
    public DanmakuManager(Context context) {
        this.context = context;
        this.mainHandler = new Handler(Looper.getMainLooper());
        this.config = new DanmakuConfig();
    }
    
    /**
     * 初始化弹幕视图
     */
    public void init(DanmakuView danmakuView, DanmakuConfig config) {
        this.danmakuView = danmakuView;
        this.config = config;
        
        if (danmakuView == null || config == null) {
            Log.e(TAG, "DanmakuView or config is null");
            return;
        }
        
        // 弹幕视图只用于显示，不参与焦点和按键事件
        // 触摸事件由 VideoDetailsActivity 设置转发
        danmakuView.setClickable(false);
        danmakuView.setFocusable(false);
        danmakuView.setFocusableInTouchMode(false);
        danmakuView.setOnKeyListener(null);
        
        // 性能优化：Android 5.0+ 使用硬件加速，低版本使用软件渲染
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP) {
            danmakuView.setLayerType(View.LAYER_TYPE_SOFTWARE, null);
            Log.d(TAG, "Android 5.0以下设备，使用软件渲染模式");
        } else {
            // Android 5.0+ 使用硬件加速，性能更好
            danmakuView.setLayerType(View.LAYER_TYPE_HARDWARE, null);
            Log.d(TAG, "使用硬件加速渲染模式");
        }
        
        // 启用弹幕绘制缓存，提升性能
        danmakuView.enableDanmakuDrawingCache(true);
        
        // 创建弹幕上下文
        danmakuContext = DanmakuContext.create();
        
        // 配置弹幕样式
        setupDanmakuStyle();
        
        // 设置回调
        danmakuView.setCallback(new DrawHandler.Callback() {
            @Override
            public void prepared() {
                isPrepared = true;
                Log.d(TAG, "Danmaku prepared");
                // 弹幕准备完成后再次确保禁用焦点（防止内部重置）
                DanmakuManager.this.danmakuView.setFocusable(false);
                DanmakuManager.this.danmakuView.setFocusableInTouchMode(false);
            }

            @Override
            public void updateTimer(DanmakuTimer timer) {
            }

            @Override
            public void danmakuShown(BaseDanmaku danmaku) {
            }

            @Override
            public void drawingFinished() {
            }
        });
        
        // 准备弹幕
        danmakuView.prepare(new BaseDanmakuParser() {
            @Override
            protected IDanmakus parse() {
                return new Danmakus();
            }
        }, danmakuContext);
        
        // 设置默认显示状态
        isShowing = config.isDefaultShow();
    }
    
    /**
     * 配置弹幕样式（TV性能优化版）
     */
    private void setupDanmakuStyle() {
        if (danmakuContext == null) return;
        
        // TV盒子性能较弱，严格限制同屏弹幕数量
        int maxLines = Math.max(2, config.getMaxCount() / 25);
        maxLines = Math.min(maxLines, 4); // 最多4行，大幅减少渲染压力
        
        // 设置最大显示行数
        HashMap<Integer, Integer> maxLinesPair = new HashMap<>();
        maxLinesPair.put(BaseDanmaku.TYPE_SCROLL_RL, maxLines);
        maxLinesPair.put(BaseDanmaku.TYPE_FIX_TOP, 1);    // 顶部弹幕最多1行
        maxLinesPair.put(BaseDanmaku.TYPE_FIX_BOTTOM, 1); // 底部弹幕最多1行
        
        // 设置禁止重叠，减少弹幕堆叠
        HashMap<Integer, Boolean> overlappingEnablePair = new HashMap<>();
        overlappingEnablePair.put(BaseDanmaku.TYPE_SCROLL_RL, true);
        overlappingEnablePair.put(BaseDanmaku.TYPE_FIX_TOP, true);
        overlappingEnablePair.put(BaseDanmaku.TYPE_FIX_BOTTOM, true);
        
        danmakuContext
                .setDanmakuStyle(IDisplayer.DANMAKU_STYLE_NONE, 0) // 无描边，大幅减少渲染开销
                .setDuplicateMergingEnabled(true) // 开启重复弹幕合并
                .setScrollSpeedFactor(1.6f * (2f - config.getSpeedFactor())) // 加快速度，减少同屏弹幕停留时间
                .setScaleTextSize(config.getFontSize() / 25f) // 字体缩放
                .setMaximumLines(maxLinesPair)
                .preventOverlapping(overlappingEnablePair)
                .setMaximumVisibleSizeInScreen(40) // 限制同屏最大弹幕数为40条
                .setDanmakuTransparency(config.getOpacityFloat()); // 透明度
        
        Log.d(TAG, "弹幕样式配置(TV优化): maxLines=" + maxLines + ", opacity=" + config.getOpacityFloat());
    }
    
    /**
     * 加载弹幕数据
     * 修改：现在接收完整的后端dmku URL（通过/api路由），不再自己构建外部API URL
     * 优化：使用OkHttpUtil替代HttpURLConnection，统一网络请求库
     */
    public void loadDanmaku(final String apiUrl) {
        if (!config.isEnabled() || danmakuView == null) {
            Log.d(TAG, "Danmaku disabled or view is null");
            return;
        }
        
        if (apiUrl == null || apiUrl.isEmpty()) {
            Log.e(TAG, "弹幕API URL为空");
            return;
        }
        
        Log.d(TAG, "Loading danmaku from: " + apiUrl);
        
        // 优化：使用OkHttpUtil统一网络请求，替代HttpURLConnection
        OkHttpUtil.okhttpget(apiUrl, new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                Log.e(TAG, "Load danmaku error: " + e.getMessage());
            }
            
            @Override
            public void onResponse(Call call, Response response) throws IOException {
                if (response.isSuccessful()) {
                    final String jsonStr = response.body().string();
                    // 在主线程解析数据
                    mainHandler.post(new Runnable() {
                        @Override
                        public void run() {
                            parseDanmakuData(jsonStr);
                        }
                    });
                } else {
                    Log.e(TAG, "Load danmaku failed, code: " + response.code());
                }
            }
        });
    }
    
    /**
     * 解析弹幕数据
     */
    private void parseDanmakuData(String jsonStr) {
        try {
            Log.d(TAG, "弹幕API响应长度: " + jsonStr.length());
            // 打印响应内容的前500个字符用于调试（避免日志过长）
            String preview = jsonStr.length() > 500 ? jsonStr.substring(0, 500) + "..." : jsonStr;
            Log.d(TAG, "弹幕API响应内容: " + preview);
            
            JSONObject json = new JSONObject(jsonStr);
            
            // 检查errorCode或code
            int errorCode = json.optInt("errorCode", json.optInt("code", -999));
            boolean success = json.optBoolean("success", false);
            
            // Code 23 是弹幕的正常返回，0 和 200 也是正常状态码
            boolean isNormalCode = (errorCode == 0 || errorCode == 200 || errorCode == 23);
            
            // 获取弹幕数组 - 优先检查 danmuku 字段（新格式）
            JSONArray dataArray = json.optJSONArray("danmuku");
            if (dataArray == null) {
                dataArray = json.optJSONArray("comments");
            }
            if (dataArray == null) {
                dataArray = json.optJSONArray("data");
            }
            if (dataArray == null) {
                dataArray = json.optJSONArray("danmaku");
            }
            if (dataArray == null) {
                dataArray = json.optJSONArray("list");
            }
            
            // 如果找到了数据数组，继续解析
            if (dataArray != null && dataArray.length() > 0) {
                Log.d(TAG, "找到弹幕数据数组，长度: " + dataArray.length() + "，code: " + errorCode);
                // 继续解析数据
            } else {
                // 没有找到数据数组
                if (!isNormalCode && !success) {
                    Log.e(TAG, "Danmaku API error, code: " + errorCode + ", 且未找到弹幕数据");
                } else {
                    Log.d(TAG, "No danmaku data found, code: " + errorCode);
                }
                return;
            }
            
            // 加载所有弹幕数据，不限制总数（max_count用于控制同屏显示数量）
            int totalCount = dataArray.length();
            Log.d(TAG, "Loaded " + totalCount + " danmaku items");
            
            // 清空旧数据
            danmakuItems.clear();
            allDanmakuItems.clear();
            
            for (int i = 0; i < totalCount; i++) {
                try {
                    Object itemObj = dataArray.get(i);
                    
                    // 检查是否是数组的数组格式（新格式：danmuku字段）
                    if (itemObj instanceof JSONArray) {
                        // 数组的数组格式：[时间/类型, 位置, 颜色, ?, 内容]
                        JSONArray itemArray = (JSONArray) itemObj;
                        if (itemArray.length() < 5) continue;
                        
                        // 解析时间/类型（第一个元素可能是数字或字符串）
                        long time = 0;
                        int type = 1;
                        try {
                            Object timeObj = itemArray.get(0);
                            if (timeObj instanceof Number) {
                                time = ((Number) timeObj).longValue() * 1000; // 转换为毫秒
                            } else {
                                time = Long.parseLong(timeObj.toString()) * 1000;
                            }
                        } catch (Exception e) {}
                        
                        // 解析位置（第二个元素：right/top/bottom）
                        String position = itemArray.optString(1, "right");
                        if ("top".equals(position)) {
                            type = 5; // 顶部弹幕
                        } else if ("bottom".equals(position)) {
                            type = 4; // 底部弹幕
                        } else {
                            type = 1; // 滚动弹幕
                        }
                        
                        // 解析颜色（第三个元素：颜色字符串，如 #fff）
                        String colorStr = itemArray.optString(2, "#ffffff");
                        int color = parseColorString(colorStr);
                        
                        // 解析内容（第五个元素）
                        String content = itemArray.optString(4, "");
                        if (content.isEmpty()) continue;
                        
                        // 存储弹幕数据到完整列表
                        allDanmakuItems.add(new DanmakuItem(time, type, color, content));
                    } else if (itemObj instanceof JSONObject) {
                        // 对象格式（旧格式）
                        JSONObject item = (JSONObject) itemObj;
                        
                        // 解析 p 字段: "时间,类型,颜色,[其他]"
                        String pStr = item.optString("p", "");
                        String content = item.optString("m", item.optString("content", item.optString("text", "")));
                        
                        if (content.isEmpty()) continue;
                        
                        long time = 0;
                        int type = 1;
                        int color = 16777215; // 白色
                        
                        if (!pStr.isEmpty()) {
                            String[] pParts = pStr.split(",");
                            if (pParts.length >= 1) {
                                try {
                                    // 时间是秒，需要转换为毫秒
                                    time = (long) (Double.parseDouble(pParts[0]) * 1000);
                                } catch (Exception e) {}
                            }
                            if (pParts.length >= 2) {
                                try {
                                    type = Integer.parseInt(pParts[1]);
                                } catch (Exception e) {}
                            }
                            if (pParts.length >= 3) {
                                try {
                                    color = Integer.parseInt(pParts[2]);
                                } catch (Exception e) {}
                            }
                        } else {
                            // 尝试直接读取字段
                            time = item.optLong("time", item.optLong("t", 0));
                            type = item.optInt("type", item.optInt("mode", 1));
                            color = item.optInt("color", item.optInt("c", 16777215));
                        }
                        
                        // 存储弹幕数据到完整列表
                        allDanmakuItems.add(new DanmakuItem(time, type, color, content));
                    }
                } catch (Exception e) {
                    Log.e(TAG, "解析弹幕项失败: " + e.getMessage());
                }
            }
            
            Log.d(TAG, "弹幕解析完成，共 " + allDanmakuItems.size() + " 条");
            
            // 在主线程重新初始化弹幕视图
            mainHandler.post(new Runnable() {
                @Override
                public void run() {
                    reloadDanmakuWithData();
                }
            });
            
        } catch (Exception e) {
            Log.e(TAG, "Parse danmaku error: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    private static final int BATCH_SIZE = 100; // 每批添加100条弹幕，减少调度开销
    private int currentBatchIndex = 0;
    
    /**
     * 使用已解析的弹幕数据添加到弹幕视图（分批异步添加，不阻塞UI）
     * 优化：只添加当前播放位置附近的弹幕，其他弹幕在播放过程中动态添加，避免所有弹幕同时出现
     */
    private void reloadDanmakuWithData() {
        if (danmakuView == null || danmakuContext == null || !isPrepared || allDanmakuItems.isEmpty()) {
            Log.w(TAG, "Cannot reload danmaku: view=" + (danmakuView != null) + ", prepared=" + isPrepared + ", items=" + allDanmakuItems.size());
            return;
        }
        
        isDataLoaded = true;
        
        // 先跳转到当前播放位置，确保弹幕时间轴正确
        if (currentPlayPosition > 0 && danmakuView != null && isPrepared) {
            danmakuView.seekTo(currentPlayPosition);
            Log.d(TAG, "弹幕加载前先跳转到播放位置: " + currentPlayPosition);
        }
        
        // 筛选出需要立即添加的弹幕（只添加当前播放位置前后30秒内的弹幕）
        danmakuItems.clear();
        long timeWindow = 30000; // 30秒时间窗口
        long startTime = Math.max(0, currentPlayPosition - timeWindow);
        long endTime = currentPlayPosition + timeWindow;
        
        for (DanmakuItem item : allDanmakuItems) {
            // 只添加时间窗口内的弹幕
            if (item.time >= startTime && item.time <= endTime) {
                danmakuItems.add(item);
            }
        }
        
        // 如果当前播放位置为0或很小，只加载前30秒的弹幕
        if (currentPlayPosition < 1000) {
            danmakuItems.clear();
            for (DanmakuItem item : allDanmakuItems) {
                if (item.time <= timeWindow) {
                    danmakuItems.add(item);
                }
            }
        }
        
        Log.d(TAG, "开始分批添加弹幕: 总弹幕数=" + allDanmakuItems.size() + ", 当前时间窗口内弹幕数=" + danmakuItems.size() + ", 当前播放位置: " + currentPlayPosition);
        
        currentBatchIndex = 0;
        
        // 分批添加弹幕，避免阻塞UI线程
        addDanmakuBatch();
        
        // 启动动态添加弹幕的定时器，在播放过程中逐步添加其他弹幕
        startDynamicDanmakuAdder();
    }
    
    /**
     * 启动动态添加弹幕的定时器
     * 每隔一段时间检查并添加即将播放的弹幕
     */
    private void startDynamicDanmakuAdder() {
        // 取消之前的任务
        if (addDanmakuRunnable != null) {
            timerHandler.removeCallbacks(addDanmakuRunnable);
        }
        
        addDanmakuRunnable = new Runnable() {
            @Override
            public void run() {
                if (danmakuView == null || !isPrepared || !isDataLoaded) {
                    return;
                }
                
                // 使用当前播放位置（通过VideoDetailsActivity更新）
                long currentTime = currentPlayPosition;
                
                // 查找需要添加的弹幕（当前时间之后60秒内的弹幕，且尚未添加）
                // 扩大时间窗口，确保有足够的弹幕可以添加
                long endTime = currentTime + 60000; // 扩大到60秒
                int addedCount = 0;
                
                for (DanmakuItem item : allDanmakuItems) {
                    // 检查是否在时间窗口内且尚未添加
                    // 添加条件：弹幕时间在当前时间之后，且在60秒窗口内
                    if (item.time > currentTime && item.time <= endTime) {
                        // 检查是否已经在danmakuItems中（避免重复添加）
                        boolean alreadyAdded = false;
                        for (DanmakuItem addedItem : danmakuItems) {
                            if (addedItem.time == item.time && 
                                addedItem.content.equals(item.content) &&
                                addedItem.type == item.type) {
                                alreadyAdded = true;
                                break;
                            }
                        }
                        
                        if (!alreadyAdded) {
                            danmakuItems.add(item);
                            addDanmaku(item.time, item.type, item.color, item.content);
                            addedCount++;
                        }
                    }
                }
                
                if (addedCount > 0) {
                    Log.d(TAG, "动态添加了 " + addedCount + " 条弹幕, 当前时间: " + currentTime + "ms, 窗口: " + currentTime + "-" + endTime);
                }
                
                // 继续定时检查（每3秒检查一次，缩短间隔确保及时添加）
                timerHandler.postDelayed(this, 3000);
            }
        };
        
        // 延迟2秒后开始第一次检查，然后每5秒检查一次
        timerHandler.postDelayed(addDanmakuRunnable, 2000);
    }
    
    /**
     * 分批添加弹幕（每批BATCH_SIZE条，通过Handler分散到多帧执行）
     */
    private void addDanmakuBatch() {
        if (danmakuView == null || !isPrepared) return;
        
        int start = currentBatchIndex * BATCH_SIZE;
        int end = Math.min(start + BATCH_SIZE, danmakuItems.size());
        
        // 添加当前批次
        for (int i = start; i < end; i++) {
            DanmakuItem item = danmakuItems.get(i);
            addDanmaku(item.time, item.type, item.color, item.content);
        }
        
        currentBatchIndex++;
        
        // 如果还有更多弹幕，延迟添加下一批（让出UI线程）
        if (end < danmakuItems.size()) {
            mainHandler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    addDanmakuBatch();
                }
            }, 50); // 50ms间隔，减少CPU占用
        } else {
            // 所有弹幕添加完成
            Log.d(TAG, "弹幕分批添加完成");
            onDanmakuLoadComplete();
        }
    }
    
    /**
     * 弹幕加载完成后的处理
     */
    private void onDanmakuLoadComplete() {
        // 如果有记忆的播放位置，跳转到该位置
        if (currentPlayPosition > 0) {
            Log.d(TAG, "弹幕加载完成，跳转到记忆位置: " + currentPlayPosition);
            mainHandler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    if (danmakuView != null && isPrepared) {
                        danmakuView.seekTo(currentPlayPosition);
                        readdDanmakuAfterSeek(currentPlayPosition);
                        if (isShowing) {
                            danmakuView.show();
                        }
                    }
                }
            }, 100);
        }
    }
    
    /**
     * 添加单条弹幕
     */
    private void addDanmaku(long time, int type, int color, String content) {
        if (danmakuView == null || danmakuContext == null || !isPrepared) return;
        
        int danmakuType;
        switch (type) {
            case 4:
                danmakuType = BaseDanmaku.TYPE_FIX_BOTTOM;
                break;
            case 5:
                danmakuType = BaseDanmaku.TYPE_FIX_TOP;
                break;
            default:
                danmakuType = BaseDanmaku.TYPE_SCROLL_RL;
                break;
        }
        
        BaseDanmaku danmaku = danmakuContext.mDanmakuFactory.createDanmaku(danmakuType);
        if (danmaku == null) return;
        
        danmaku.text = content;
        danmaku.padding = 5;
        danmaku.priority = 0;
        danmaku.isLive = false;
        danmaku.setTime(time);
        danmaku.textSize = config.getFontSize() * (danmakuView.getContext().getResources().getDisplayMetrics().density - 0.6f);
        danmaku.textColor = color;
        danmaku.textShadowColor = color <= Color.BLACK ? Color.WHITE : Color.BLACK;
        
        danmakuView.addDanmaku(danmaku);
    }
    
    /**
     * 解析颜色字符串（如 #fff 或 #ffffff）为颜色值
     */
    private int parseColorString(String colorStr) {
        if (colorStr == null || colorStr.isEmpty()) {
            return 16777215; // 白色
        }
        
        try {
            // 移除 # 号
            if (colorStr.startsWith("#")) {
                colorStr = colorStr.substring(1);
            }
            
            // 如果是3位颜色（如 fff），扩展为6位（ffffff）
            if (colorStr.length() == 3) {
                char[] chars = colorStr.toCharArray();
                colorStr = "" + chars[0] + chars[0] + chars[1] + chars[1] + chars[2] + chars[2];
            }
            
            // 解析为整数颜色值
            return Color.parseColor("#" + colorStr);
        } catch (Exception e) {
            Log.e(TAG, "解析颜色失败: " + colorStr + ", " + e.getMessage());
            return 16777215; // 默认白色
        }
    }
    
    /**
     * 开始播放弹幕
     */
    public void start() {
        Log.d(TAG, "start: isPrepared=" + isPrepared + ", defaultShow=" + (config != null ? config.isDefaultShow() : "null"));
        if (danmakuView != null && isPrepared) {
            danmakuView.start();
            // 根据配置决定是否默认显示
            if (config != null && config.isDefaultShow()) {
                isShowing = true;
                danmakuView.show();
            } else if (isShowing) {
                danmakuView.show();
            } else {
                danmakuView.hide();
            }
            Log.d(TAG, "弹幕已启动, isShowing=" + isShowing);
        }
    }
    
    /**
     * 暂停弹幕
     */
    public void pause() {
        if (danmakuView != null && isPrepared) {
            danmakuView.pause();
        }
    }
    
    /**
     * 恢复弹幕（平滑恢复，避免抖动）
     */
    public void resume() {
        Log.d(TAG, "resume: isPrepared=" + isPrepared + ", isShowing=" + isShowing);
        if (danmakuView != null && isPrepared) {
            // 确保显示状态正确后再恢复，避免抖动
            if (isShowing) {
                danmakuView.show();
            }
            // 延迟一帧再恢复，让显示状态先稳定
            mainHandler.post(new Runnable() {
                @Override
                public void run() {
                    if (danmakuView != null && isPrepared) {
                        danmakuView.resume();
                    }
                }
            });
        }
    }
    
    private long pendingSeekPosition = -1;
    private Runnable seekRunnable = null;
    private static final long SEEK_DELAY = 500; // 500ms防抖延迟
    private boolean isSeeking = false; // 是否正在seek操作中
    
    /**
     * 更新当前播放位置（用于动态添加弹幕）
     */
    public void updatePlayPosition(long position) {
        currentPlayPosition = position;
    }
    
    /**
     * 跳转弹幕（防抖处理，避免频繁seek）
     */
    public void seekTo(long position) {
        Log.d(TAG, "seekTo request: " + position + ", isDataLoaded=" + isDataLoaded);
        
        // 记录当前播放位置，用于弹幕数据加载完成后跳转和动态添加弹幕
        currentPlayPosition = position;
        
        // 如果弹幕数据还没加载完成，只记录位置，等加载完成后再跳转
        if (!isDataLoaded) {
            Log.d(TAG, "弹幕数据未加载完成，记录位置等待跳转: " + position);
            return;
        }
        
        if (danmakuView == null || !isPrepared) return;
        
        // 开始seek时隐藏弹幕并清屏
        if (!isSeeking) {
            isSeeking = true;
            danmakuView.hide();
            danmakuView.clearDanmakusOnScreen();
        }
        
        // 记录最新的seek位置
        pendingSeekPosition = position;
        
        // 取消之前的延迟任务
        if (seekRunnable != null) {
            mainHandler.removeCallbacks(seekRunnable);
        }
        
        // 创建新的延迟任务
        seekRunnable = new Runnable() {
            @Override
            public void run() {
                if (danmakuView != null && isPrepared && pendingSeekPosition >= 0) {
                    Log.d(TAG, "seekTo execute: " + pendingSeekPosition);
                    danmakuView.seekTo(pendingSeekPosition);
                    
                    // seek后重新添加该时间点之后的弹幕
                    readdDanmakuAfterSeek(pendingSeekPosition);
                    
                    // seek完成，恢复弹幕显示
                    isSeeking = false;
                    if (isShowing) {
                        danmakuView.show();
                    }
                    pendingSeekPosition = -1;
                }
            }
        };
        
        // 延迟执行
        mainHandler.postDelayed(seekRunnable, SEEK_DELAY);
    }
    
    private List<DanmakuItem> seekBatchItems = new ArrayList<>();
    private int seekBatchIndex = 0;
    
    /**
     * seek后重新添加弹幕（分批异步，不阻塞UI）
     */
    private void readdDanmakuAfterSeek(long position) {
        if (danmakuItems.isEmpty() || danmakuView == null || !isPrepared) return;
        
        // 筛选出需要添加的弹幕
        seekBatchItems.clear();
        for (DanmakuItem item : danmakuItems) {
            if (item.time >= position) {
                seekBatchItems.add(item);
            }
        }
        
        if (seekBatchItems.isEmpty()) return;
        
        Log.d(TAG, "seek后需要添加 " + seekBatchItems.size() + " 条弹幕");
        seekBatchIndex = 0;
        addSeekBatch();
    }
    
    /**
     * 分批添加seek后的弹幕
     */
    private void addSeekBatch() {
        if (danmakuView == null || !isPrepared || seekBatchItems.isEmpty()) return;
        
        int start = seekBatchIndex * BATCH_SIZE;
        int end = Math.min(start + BATCH_SIZE, seekBatchItems.size());
        
        for (int i = start; i < end; i++) {
            DanmakuItem item = seekBatchItems.get(i);
            addDanmaku(item.time, item.type, item.color, item.content);
        }
        
        seekBatchIndex++;
        
        if (end < seekBatchItems.size()) {
            mainHandler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    addSeekBatch();
                }
            }, 50); // 50ms间隔，减少CPU占用
        } else {
            Log.d(TAG, "seek后弹幕添加完成");
            seekBatchItems.clear();
        }
    }
    
    /**
     * 显示弹幕
     */
    public void show() {
        isShowing = true;
        if (danmakuView != null) {
            danmakuView.show();
        }
    }
    
    /**
     * 隐藏弹幕
     */
    public void hide() {
        isShowing = false;
        if (danmakuView != null) {
            danmakuView.hide();
        }
    }
    
    /**
     * 切换显示状态
     */
    public void toggle() {
        if (isShowing) {
            hide();
        } else {
            show();
        }
    }
    
    /**
     * 是否正在显示
     */
    public boolean isShowing() {
        return isShowing;
    }
    
    /**
     * 释放资源
     */
    public void release() {
        isPrepared = false;
        
        // 停止动态添加弹幕的定时器
        if (addDanmakuRunnable != null) {
            timerHandler.removeCallbacks(addDanmakuRunnable);
            addDanmakuRunnable = null;
        }
        
        if (danmakuView != null) {
            danmakuView.release();
            danmakuView = null;
        }
    }
    
    /**
     * 清空弹幕
     */
    public void clear() {
        if (danmakuView != null) {
            danmakuView.clearDanmakusOnScreen();
        }
    }
    
    /**
     * 重置弹幕状态（切换视频时调用）
     */
    public void reset() {
        currentPlayPosition = 0;
        isDataLoaded = false;
        danmakuItems.clear();
        allDanmakuItems.clear();
        
        // 停止动态添加弹幕的定时器
        if (addDanmakuRunnable != null) {
            timerHandler.removeCallbacks(addDanmakuRunnable);
            addDanmakuRunnable = null;
        }
        
        if (danmakuView != null) {
            danmakuView.clearDanmakusOnScreen();
        }
        Log.d(TAG, "弹幕状态已重置");
    }
    
    /**
     * 设置弹幕文字大小
     * @param scale 缩放比例，1.0为标准大小
     */
    public void setTextSize(float scale) {
        if (danmakuContext != null) {
            danmakuContext.setScaleTextSize(scale);
            Log.d(TAG, "设置弹幕大小: " + scale);
        }
    }
    
    /**
     * 设置弹幕速度
     * @param speed 速度因子，1.0为标准速度，值越大越慢
     */
    public void setSpeed(float speed) {
        if (danmakuContext != null) {
            // 速度因子：值越大弹幕越慢，所以需要反转
            float factor = 1.2f * (2f - speed);
            danmakuContext.setScrollSpeedFactor(factor);
            Log.d(TAG, "设置弹幕速度: " + speed + ", factor=" + factor);
        }
    }
    
    /**
     * 设置弹幕最大行数
     * @param maxLines 最大行数，-1表示不限制
     */
    public void setMaxLines(int maxLines) {
        if (danmakuContext != null) {
            HashMap<Integer, Integer> maxLinesPair = new HashMap<>();
            if (maxLines > 0) {
                maxLinesPair.put(BaseDanmaku.TYPE_SCROLL_RL, maxLines);
            } else {
                maxLinesPair.put(BaseDanmaku.TYPE_SCROLL_RL, Integer.MAX_VALUE);
            }
            danmakuContext.setMaximumLines(maxLinesPair);
            Log.d(TAG, "设置弹幕行数: " + maxLines);
        }
    }
    
    /**
     * 设置弹幕透明度
     * @param alpha 透明度，1.0为完全不透明，0.0为完全透明
     */
    public void setAlpha(float alpha) {
        if (danmakuContext != null) {
            danmakuContext.setDanmakuTransparency(alpha);
            Log.d(TAG, "设置弹幕透明度: " + alpha);
        }
    }
}
