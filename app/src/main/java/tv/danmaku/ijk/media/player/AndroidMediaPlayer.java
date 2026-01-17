package tv.danmaku.ijk.media.player;
import android.annotation.TargetApi;
import android.content.Context;
import android.media.AudioManager;
import android.media.MediaDataSource;
import android.media.MediaPlayer;
import android.media.PlaybackParams;
import android.media.TimedText;
import android.net.Uri;
import android.os.Build;
import android.support.annotation.RequiresApi;
import android.text.TextUtils;
import android.view.Surface;
import android.view.SurfaceHolder;

import com.shenma.tvlauncher.utils.Constant;
import com.shenma.tvlauncher.utils.SharePreferenceDataUtil;

import java.io.FileDescriptor;
import java.io.IOException;
import java.lang.ref.WeakReference;
import java.lang.reflect.Method;
import java.util.Map;

import tv.danmaku.ijk.media.player.misc.AndroidTrackInfo;
import tv.danmaku.ijk.media.player.misc.IMediaDataSource;
import tv.danmaku.ijk.media.player.misc.ITrackInfo;
import tv.danmaku.ijk.media.player.pragma.DebugLog;
public class AndroidMediaPlayer extends AbstractMediaPlayer {
    private static MediaInfo sMediaInfo;
    private final MediaPlayer mInternalMediaPlayer;
    private final AndroidMediaPlayerListenerHolder mInternalListenerAdapter;
    private final Object mInitLock = new Object();
    private String mDataSource;
    private MediaDataSource mMediaDataSource;
    private boolean mIsReleased;
    private float Speed = 1.0f;
    private Context mAppContext;

    public AndroidMediaPlayer(Context context) {
        mAppContext = context.getApplicationContext();
        synchronized (mInitLock) {
            mInternalMediaPlayer = new MediaPlayer();
        }
        mInternalMediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
        mInternalListenerAdapter = new AndroidMediaPlayerListenerHolder(this);
        attachInternalListeners();
    }

    public MediaPlayer getInternalMediaPlayer() {
        return mInternalMediaPlayer;
    }


    public void setSpeed(float speed) {
        Speed = speed;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            PlaybackParams params = mInternalMediaPlayer.getPlaybackParams();
            params.setSpeed(speed);
            mInternalMediaPlayer.setPlaybackParams(params);
        } else {
            // 在Android 4.0及以下版本中，使用setSpeed方法设置播放速度
            try {
                Class<?>[] paramTypes = new Class[]{float.class};
                Method setSpeedMethod = MediaPlayer.class.getMethod("setSpeed", paramTypes);
                setSpeedMethod.invoke(mInternalMediaPlayer, speed);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    public float getSpeed() {
        return Speed;
    }

    @Override
    public void seekTo(long msec) throws IllegalStateException {
        mInternalMediaPlayer.seekTo((int) msec);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            PlaybackParams params = mInternalMediaPlayer.getPlaybackParams();
            params.setSpeed(Speed);
            mInternalMediaPlayer.setPlaybackParams(params);
        } else {
            // 在Android 4.0及以下版本中，使用setSpeed方法设置播放速度
            try {
                Class<?>[] paramTypes = new Class[]{float.class};
                Method setSpeedMethod = MediaPlayer.class.getMethod("setSpeed", paramTypes);
                setSpeedMethod.invoke(mInternalMediaPlayer, Speed);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public void start() throws IllegalStateException {
        mInternalMediaPlayer.start();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            PlaybackParams params = mInternalMediaPlayer.getPlaybackParams();
            params.setSpeed(Speed);
            mInternalMediaPlayer.setPlaybackParams(params);
        } else {
            // 在Android 4.0及以下版本中，使用setSpeed方法设置播放速度
            try {
                Class<?>[] paramTypes = new Class[]{float.class};
                Method setSpeedMethod = MediaPlayer.class.getMethod("setSpeed", paramTypes);
                setSpeedMethod.invoke(mInternalMediaPlayer, Speed);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }








    @Override
    public void setDisplay(SurfaceHolder sh) {
        synchronized (mInitLock) {
            if (!mIsReleased) {
                mInternalMediaPlayer.setDisplay(sh);
            }
        }
    }

    @TargetApi(Build.VERSION_CODES.ICE_CREAM_SANDWICH)
    @Override
    public void setSurface(Surface surface) {
        mInternalMediaPlayer.setSurface(surface);
    }

    @Override
    public void setDataSource(Context context, Uri uri)
            throws IOException, IllegalArgumentException, SecurityException, IllegalStateException {
        mInternalMediaPlayer.setDataSource(context, uri);
    }

    @TargetApi(Build.VERSION_CODES.ICE_CREAM_SANDWICH)
    @Override
    public void setDataSource(Context context, Uri uri, Map<String, String> headers)
            throws IOException, IllegalArgumentException, SecurityException, IllegalStateException {
        mInternalMediaPlayer.setDataSource(context, uri, headers);
    }

    @Override
    public String getDataSource() {
        return mDataSource;
    }

    @Override
    public void setDataSource(FileDescriptor fd)
            throws IOException, IllegalArgumentException, IllegalStateException {
        mInternalMediaPlayer.setDataSource(fd);
    }

    @Override
    public void setDataSource(String path) throws IOException,
            IllegalArgumentException, SecurityException, IllegalStateException {
        mDataSource = path;

        Uri uri = Uri.parse(path);
        String scheme = uri.getScheme();
        if (!TextUtils.isEmpty(scheme) && scheme.equalsIgnoreCase("file")) {
            mInternalMediaPlayer.setDataSource(uri.getPath());
        } else {
            mInternalMediaPlayer.setDataSource(path);
        }
    }

    @TargetApi(Build.VERSION_CODES.M)
    @Override
    public void setDataSource(IMediaDataSource mediaDataSource) {
        releaseMediaDataSource();

        mMediaDataSource = new MediaDataSourceProxy(mediaDataSource);
        mInternalMediaPlayer.setDataSource(mMediaDataSource);
    }

    private void releaseMediaDataSource() {
        if (mMediaDataSource != null) {
            try {
                mMediaDataSource.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
            mMediaDataSource = null;
        }
    }

    /*切换软件解码会闪退*/
//    @Override
//    public void prepareAsync() throws IllegalStateException {
////        mInternalMediaPlayer.reset(); // 添加重置操作
//        mInternalMediaPlayer.prepareAsync();
//    }

    @Override
    public void prepareAsync() throws IllegalStateException {
        try {
            mInternalMediaPlayer.prepareAsync();
        } catch (IllegalStateException e) {
            // 处理异常，例如打印错误日志或者进行其他操作
            e.printStackTrace();
        }
    }


//    @Override
//    public void start() throws IllegalStateException {
//        mInternalMediaPlayer.start();
//    }

    @Override
    public void stop() throws IllegalStateException {
        mInternalMediaPlayer.stop();
    }

    @Override
    public void pause() throws IllegalStateException {
        mInternalMediaPlayer.pause();
    }

    @Override
    public void setScreenOnWhilePlaying(boolean screenOn) {
        mInternalMediaPlayer.setScreenOnWhilePlaying(screenOn);
    }

    @Override
    public ITrackInfo[] getTrackInfo() {
        return AndroidTrackInfo.fromMediaPlayer(mInternalMediaPlayer);
    }

    @Override
    public int getVideoWidth() {
        return mInternalMediaPlayer.getVideoWidth();
    }

    @Override
    public int getVideoHeight() {
        return mInternalMediaPlayer.getVideoHeight();
    }

    @Override
    public int getVideoSarNum() {
        return 1;
    }

    @Override
    public int getVideoSarDen() {
        return 1;
    }

    @Override
    public boolean isPlaying() {
        try {
            return mInternalMediaPlayer.isPlaying();
        } catch (IllegalStateException e) {
            DebugLog.printStackTrace(e);
            return false;
        }
    }

//    @Override
//    public void seekTo(long msec) throws IllegalStateException {
//        mInternalMediaPlayer.seekTo((int) msec);
//    }

    @Override
    public long getCurrentPosition() {
        try {
            return mInternalMediaPlayer.getCurrentPosition();
        } catch (IllegalStateException e) {
            DebugLog.printStackTrace(e);
            return 0;
        }
    }

    @Override
    public long getDuration() {
        try {
            return mInternalMediaPlayer.getDuration();
        } catch (IllegalStateException e) {
            DebugLog.printStackTrace(e);
            return 0;
        }
    }

    @Override
    public void release() {
        mIsReleased = true;
        mInternalMediaPlayer.release();
        releaseMediaDataSource();
        resetListeners();
        attachInternalListeners();
    }

    @Override
    public void reset() {
        try {
            mInternalMediaPlayer.reset();
        } catch (IllegalStateException e) {
            DebugLog.printStackTrace(e);
        }
        releaseMediaDataSource();
        resetListeners();
        attachInternalListeners();
    }

    @Override
    public boolean isLooping() {
        return mInternalMediaPlayer.isLooping();
    }

    @Override
    public void setLooping(boolean looping) {
        mInternalMediaPlayer.setLooping(looping);
    }

    @Override
    public void setVolume(float leftVolume, float rightVolume) {
        mInternalMediaPlayer.setVolume(leftVolume, rightVolume);
    }

    @Override
    public int getAudioSessionId() {
        return mInternalMediaPlayer.getAudioSessionId();
    }

    @Override
    public MediaInfo getMediaInfo() {
        if (sMediaInfo == null) {
            MediaInfo module = new MediaInfo();

            module.mVideoDecoder = "android";
            module.mVideoDecoderImpl = "HW";

            module.mAudioDecoder = "android";
            module.mAudioDecoderImpl = "HW";

            sMediaInfo = module;
        }

        return sMediaInfo;
    }

    @Override
    public void setLogEnabled(boolean enable) {
    }

    @Override
    public boolean isPlayable() {
        return true;
    }

    /*--------------------
     * misc
     */
    @Override
    public void setWakeMode(Context context, int mode) {
        mInternalMediaPlayer.setWakeMode(context, mode);
    }

    @Override
    public void setAudioStreamType(int streamtype) {
        mInternalMediaPlayer.setAudioStreamType(streamtype);
    }

    @Override
    public void setKeepInBackground(boolean keepInBackground) {
    }

    /*--------------------
     * Listeners adapter
     */
    private void attachInternalListeners() {
        mInternalMediaPlayer.setOnPreparedListener(mInternalListenerAdapter);
        mInternalMediaPlayer
                .setOnBufferingUpdateListener(mInternalListenerAdapter);
        mInternalMediaPlayer.setOnCompletionListener(mInternalListenerAdapter);
        mInternalMediaPlayer
                .setOnSeekCompleteListener(mInternalListenerAdapter);
        mInternalMediaPlayer
                .setOnVideoSizeChangedListener(mInternalListenerAdapter);
        mInternalMediaPlayer.setOnErrorListener(mInternalListenerAdapter);
        mInternalMediaPlayer.setOnInfoListener(mInternalListenerAdapter);
        mInternalMediaPlayer.setOnTimedTextListener(mInternalListenerAdapter);
    }

    @TargetApi(Build.VERSION_CODES.M)
    private static class MediaDataSourceProxy extends MediaDataSource {
        private final IMediaDataSource mMediaDataSource;

        public MediaDataSourceProxy(IMediaDataSource mediaDataSource) {
            mMediaDataSource = mediaDataSource;
        }

        @Override
        public int readAt(long position, byte[] buffer, int offset, int size) throws IOException {
            return mMediaDataSource.readAt(position, buffer, offset, size);
        }

        @Override
        public long getSize() throws IOException {
            return mMediaDataSource.getSize();
        }

        @Override
        public void close() throws IOException {
            mMediaDataSource.close();
        }
    }

    private class AndroidMediaPlayerListenerHolder implements
            MediaPlayer.OnPreparedListener, MediaPlayer.OnCompletionListener,
            MediaPlayer.OnBufferingUpdateListener,
            MediaPlayer.OnSeekCompleteListener,
            MediaPlayer.OnVideoSizeChangedListener,
            MediaPlayer.OnErrorListener, MediaPlayer.OnInfoListener,
            MediaPlayer.OnTimedTextListener {
        public final WeakReference<AndroidMediaPlayer> mWeakMediaPlayer;

        public AndroidMediaPlayerListenerHolder(AndroidMediaPlayer mp) {
            mWeakMediaPlayer = new WeakReference<AndroidMediaPlayer>(mp);
        }

        @Override
        public boolean onInfo(MediaPlayer mp, int what, int extra) {
            AndroidMediaPlayer self = mWeakMediaPlayer.get();
            return self != null && notifyOnInfo(what, extra);

        }

        @Override
        public boolean onError(MediaPlayer mp, int what, int extra) {
            AndroidMediaPlayer self = mWeakMediaPlayer.get();
            return self != null && notifyOnError(what, extra);

        }

        @Override
        public void onVideoSizeChanged(MediaPlayer mp, int width, int height) {
            AndroidMediaPlayer self = mWeakMediaPlayer.get();
            if (self == null)
                return;

            notifyOnVideoSizeChanged(width, height, 1, 1);
        }

        @Override
        public void onSeekComplete(MediaPlayer mp) {
            AndroidMediaPlayer self = mWeakMediaPlayer.get();
            if (self == null)
                return;

            notifyOnSeekComplete();
        }

        @Override
        public void onBufferingUpdate(MediaPlayer mp, int percent) {
            AndroidMediaPlayer self = mWeakMediaPlayer.get();
            if (self == null)
                return;
            if (SharePreferenceDataUtil.getSharedIntData(mAppContext, Constant.mt, 0) == 1){
                notifyOnBufferingUpdate(percent);
            }
//            notifyOnBufferingUpdate(percent);
        }

        @Override
        public void onCompletion(MediaPlayer mp) {
            AndroidMediaPlayer self = mWeakMediaPlayer.get();
            if (self == null)
                return;

            notifyOnCompletion();
        }

        @Override
        public void onPrepared(MediaPlayer mp) {
            AndroidMediaPlayer self = mWeakMediaPlayer.get();
            if (self == null)
                return;

            notifyOnPrepared();
        }

        @Override
        public void onTimedText(MediaPlayer mp, TimedText text) {
            AndroidMediaPlayer self = mWeakMediaPlayer.get();
            if (self == null)
                return;

            IjkTimedText ijkText = null;

            if (text != null) {
                ijkText = new IjkTimedText(text.getBounds(), text.getText());
            }

            notifyOnTimedText(ijkText);
        }
    }
}
