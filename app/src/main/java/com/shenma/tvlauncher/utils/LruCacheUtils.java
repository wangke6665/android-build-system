package com.shenma.tvlauncher.utils;

import android.app.ActivityManager;
import android.content.Context;
import android.graphics.Bitmap;
import android.util.LruCache;

/**
 * Image Cache Utils
 * 优化：修复线程安全问题，改进缓存大小计算
 *
 * @author drowtram
 */
public class LruCacheUtils {

    private static volatile LruCacheUtils mCacheUtils;
    private LruCache<String, Bitmap> mMemoryCache;
    private int MAXMEMONRY = (int) (Runtime.getRuntime().maxMemory() / 1024);

    private LruCacheUtils() {
        if (mMemoryCache == null) {
            // 优化：使用更合理的缓存大小计算方式
            int cacheSize = calculateCacheSize();
            mMemoryCache = new LruCache<String, Bitmap>(cacheSize) {
                @Override
                protected int sizeOf(String key, Bitmap bitmap) {
                    // 优化：更准确的Bitmap大小计算
                    if (bitmap == null) return 0;
                    return bitmap.getRowBytes() * bitmap.getHeight() / 1024;
                }

                @Override
                protected void entryRemoved(boolean evicted, String key, Bitmap oldValue, Bitmap newValue) {
                    Logger.v("zhouchuan", "hard cache is full , push to soft cache " + key);
                    // 优化：如果被移除的Bitmap不再被使用，可以回收
                    if (evicted && oldValue != null && !oldValue.isRecycled()) {
                        // 注意：不要在这里recycle，因为可能还在使用
                    }
                }
            };
        }
    }

    /**
     * 优化：修复线程安全问题，使用双重检查锁定
     */
    public static LruCacheUtils getInstance() {
        if (mCacheUtils == null) {
            synchronized (LruCacheUtils.class) {
                if (mCacheUtils == null) {
                    mCacheUtils = new LruCacheUtils();
                }
            }
        }
        return mCacheUtils;
    }

    /**
     * 优化：根据设备内存动态计算缓存大小
     */
    private int calculateCacheSize() {
        // 使用可用内存的1/8作为缓存大小（KB）
        return MAXMEMONRY / 8;
    }

    /**
     * 清除缓存
     */
    public void clearCache() {
        if (mMemoryCache != null) {
            if (mMemoryCache.size() > 0) {
                Logger.d("zhouchuan", "mMemoryCache.size() " + mMemoryCache.size());
                mMemoryCache.evictAll();
                Logger.d("zhouchuan", "mMemoryCache.size()" + mMemoryCache.size());
            }
            mMemoryCache = null;
        }
    }

    /**
     * 添加图片到缓存
     *
     * @param key    位图key标示符
     * @param bitmap 位图对象
     */
    public synchronized void addBitmapToMemoryCache(String key, Bitmap bitmap) {
        if (mMemoryCache.get(key) == null) {
            if (key != null && bitmap != null)
                mMemoryCache.put(key, bitmap);
        } else
            Logger.w("zhouchuan", "the res is aready exits");
    }

    /**
     * 从缓存中取得图片
     * 优化：修复逻辑错误，先检查key再获取
     *
     * @param key
     * @return
     */
    public synchronized Bitmap getBitmapFromMemCache(String key) {
        if (key != null && mMemoryCache != null) {
            return mMemoryCache.get(key);
        }
        return null;
    }

    /**
     * 移除缓存
     *
     * @param key
     */
    public synchronized void removeImageCache(String key) {
        if (key != null) {
            if (mMemoryCache != null) {
                Bitmap bm = mMemoryCache.remove(key);
                if (bm != null)
                    bm.recycle();
            }
        }
    }

    /**
     * 清空缓存
     */
    public void clearAllImageCache() {
        if (mMemoryCache != null && mMemoryCache.size() > 0) {
            Logger.d("zhouchuan", "before mMemoryCache.size() " + mMemoryCache.size() + "KB");
            mMemoryCache.evictAll();
            Logger.d("zhouchuan", "after mMemoryCache.size()" + mMemoryCache.size() + "KB");
        }
    }
}
