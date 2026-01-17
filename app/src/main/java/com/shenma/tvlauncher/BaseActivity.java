package com.shenma.tvlauncher;


import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.media.AudioManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.app.FragmentActivity;
import android.util.DisplayMetrics;
import android.view.DisplayCutout;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.Window;
import android.view.WindowInsets;
import android.view.WindowManager;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.shenma.tvlauncher.utils.Constant;
import com.shenma.tvlauncher.utils.HttpStringHandler;
import com.shenma.tvlauncher.utils.Logger;
import com.shenma.tvlauncher.utils.Rc4;
import com.shenma.tvlauncher.utils.SharePreferenceDataUtil;
import com.shenma.tvlauncher.utils.Utils;
import com.shenma.tvlauncher.view.ExitDialog;
import com.shenma.tvlauncher.view.ExitFullDialog;
import com.shenma.tvlauncher.view.HomeDialog;
import com.shenma.tvlauncher.vod.SearchActivity;

import net.sunniwell.android.httpserver.HttpServer;

import org.apache.http.HttpEntity;
import org.apache.http.HttpEntityEnclosingRequest;
import org.apache.http.HttpException;
import org.apache.http.HttpRequest;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.MethodNotSupportedException;
import org.apache.http.entity.ContentProducer;
import org.apache.http.entity.EntityTemplate;
import org.apache.http.entity.InputStreamEntity;
import org.apache.http.protocol.HttpContext;
import org.apache.http.protocol.HttpRequestHandler;
import org.apache.http.protocol.HttpRequestHandlerRegistry;
import org.apache.http.util.EntityUtils;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.URLDecoder;
import java.util.Locale;

import android.support.v4.content.LocalBroadcastManager;

/**
 * @author joychang
 * @Description 基类
 */
public abstract class BaseActivity extends FragmentActivity {
    private static final String TAG = "BaseActivity";
    /*原版*/
    /*protected static Context context;*/

    protected Context context;
    protected SharedPreferences sp;
    protected SharedPreferences SP;
    protected Animation breathingAnimation;
    protected int mWidth;
    protected int mHeight;
    protected String from;
    protected String devicetype;
    protected String version;
    protected String params;
    private DisplayCutout displayCutout = null;
    /**
     * @brief 退出对话框。
     */
    protected ExitFullDialog exitfullDialog = null;
    protected ExitDialog exitDialog = null;
    /**
     * @brief 联网对话框。
     */
    protected ExitDialog netDialog = null;
    protected double screenSize;
    protected Toast mToast = null;
    protected AudioManager mAudioManager = null;

    private Handler mediaHandler = new Handler() {
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case 1:
                    Utils.showToast(BaseActivity.this, R.string.Turn_soff_sVPN, R.drawable.toast_err);
                    mediaHandler.sendEmptyMessageDelayed(3,1000);
                    return;
                case 2:
                    Utils.showToast(BaseActivity.this, R.string.Turn_soff_sXP, R.drawable.toast_err);
                    mediaHandler.sendEmptyMessageDelayed(3, 1000);
                    return;
                case 3:
                    System.exit(0);//闪退APP
                    return;
                default:
                    return;
            }
        }
    };
    private HttpServer server;
    protected int Port = 9978;
    private BroadcastReceiver BroadcastReceiver;
    private int  Search;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        // getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
        // WindowManager.LayoutParams.FLAG_FULLSCREEN);// 设置全屏
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE); // 设置横屏
        context = BaseActivity.this;
        sp = getSharedPreferences("shenma", MODE_PRIVATE);
        SP = getSharedPreferences("initData", MODE_PRIVATE);
        breathingAnimation = AnimationUtils.loadAnimation(context,
                R.anim.breathing);
        WindowManager manager = (WindowManager) context
                .getSystemService(Context.WINDOW_SERVICE);
        DisplayMetrics dm = new DisplayMetrics();
        manager.getDefaultDisplay().getMetrics(dm);
        mWidth = dm.widthPixels;
        mHeight = dm.heightPixels;
        //Logger.d(TAG, "mWidth=" + mWidth + "..mHeight=" + mHeight);
        double diagonalPixels = Math.sqrt(Math.pow(dm.widthPixels, 2)
                + Math.pow(dm.heightPixels, 2));
        screenSize = diagonalPixels / (160 * dm.density);
//        from = Utils.getFormInfo(BaseActivity.class, 0);
//        devicetype = Utils.getFormInfo(BaseActivity.class, 3);
        version = Utils.getVersion(this);
        try {
            params = Utils.encode("version=" + version + "&from=" + from + "&devicetype=" + devicetype, "utf-8");
        } catch (UnsupportedEncodingException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

        if (SharePreferenceDataUtil.getSharedIntData(this, Constant.vu, 0) == 1){
            /*防抓包*/
            if (Utils.isVpnConnected() || Utils.isWifiProxy(context)) {
                mediaHandler.sendEmptyMessage(1);
            }
        }

        if (SharePreferenceDataUtil.getSharedIntData(this, "Xp_check", 0) == 1){
            /*防XP环境*/
            if (Utils.checkXpFormMap() || Utils.isHookByStack()) {
                mediaHandler.sendEmptyMessage(2);
            }
        }

        /*验证服务是否存在*/
        PackageManager pm = getPackageManager();
        Intent serviceIntent1 = new Intent(this, com.shenma.tvlauncher.view.MyServices.class);
        Intent serviceIntent2 = new Intent(this, com.shenma.tvlauncher.view.MyService.class);
        Intent serviceIntent3 = new Intent(this, com.shenma.tvlauncher.view.JSONService.class);

        boolean isService1Exist = pm.resolveService(serviceIntent1, 0) != null;
        boolean isService2Exist = pm.resolveService(serviceIntent2, 0) != null;
        boolean isService3Exist = pm.resolveService(serviceIntent3, 0) != null;

        if (!isService1Exist || !isService2Exist || !isService3Exist) {
            //闪退app
            throw new RuntimeException("One of the services is missing");
        }

        BroadcastReceiver = new AutoStartReceiver();
        IntentFilter filter = new IntentFilter();
        // 添加你想要监听的Intent的action，例如："com.example.MY_ACTION"
        filter.addAction("android.content.movie.search.Action");
        registerReceiver(BroadcastReceiver, filter);
        Search = SharePreferenceDataUtil.getSharedIntData(this, "search", 0);
    }


    /*刘海屏附加*/
    public final int AttachedToWindow() {
        int identifier;
        Context applicationContext = getApplicationContext();
        if (displayCutout  == null || (identifier = applicationContext.getResources().getIdentifier("status_bar_height", "dimen", "android")) <= 0) {
            return 0;
        }
        return applicationContext.getResources().getDimensionPixelSize(identifier);
    }

    /*刘海屏*/
    @Override
    public void onAttachedToWindow() {
        super.onAttachedToWindow();
        if (Build.VERSION.SDK_INT >= 28) {
            try {
                WindowInsets rootWindowInsets = getWindow().getDecorView().getRootWindowInsets();
                if (rootWindowInsets != null) {
                    displayCutout  = rootWindowInsets.getDisplayCutout();
                }
            } catch (Exception unused) {
            }
            if (displayCutout  != null) {
                WindowManager.LayoutParams attributes = getWindow().getAttributes();
//                attributes.layoutInDisplayCutoutMode = 1;
                attributes.layoutInDisplayCutoutMode = WindowManager.LayoutParams.LAYOUT_IN_DISPLAY_CUTOUT_MODE_SHORT_EDGES;
                getWindow().setAttributes(attributes);
//                getWindow().getDecorView().setSystemUiVisibility(1280);
                getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN | View.SYSTEM_UI_FLAG_LAYOUT_STABLE);
            }
        }
    }
    /**
     * 初始化
     */
    protected abstract void initView();

    /**
     * 加载布局文件
     */
    protected abstract void loadViewLayout();

    /**
     * 初始化控件
     */
    protected abstract void findViewById();

    /**
     * 设置监听器
     */
    protected abstract void setListener();

    @Override
    protected void onStart() {
        super.onStart();
        //Logger.i(TAG, "BaseActivity... onStart");
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        //Logger.i(TAG, "BaseActivity... onDestroy");
        if (BroadcastReceiver != null) {
            unregisterReceiver(BroadcastReceiver);
            BroadcastReceiver = null; // 设置为null以便垃圾回收
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        //Logger.i(TAG, "BaseActivity... onPause");
    }

    @Override
    protected void onResume() {
        super.onResume();
        //Logger.i(TAG, "BaseActivity... onResume");
    }

    @Override
    protected void onStop() {
        super.onStop();
        //Logger.i(TAG, "BaseActivity... onStop");

    }

    protected void openActivity(Class<?> pClass) {
        openActivity(pClass, null);
    }

    protected void openActivity(Class<?> pClass, Bundle pBundle) {
        Intent intent = new Intent(this, pClass);
        if (pBundle != null) {
            intent.putExtras(pBundle);
        }
        startActivity(intent);
        overridePendingTransition(android.R.anim.fade_in,
                android.R.anim.fade_out);
    }

    protected void openActivity(String pAction) {
        openActivity(pAction, null);
    }

    protected void openActivity(String pAction, Bundle pBundle) {
        Intent intent = new Intent(pAction);
        if (pBundle != null) {
            intent.putExtras(pBundle);
        }
        startActivity(intent);
        overridePendingTransition(android.R.anim.fade_in,
                android.R.anim.fade_out);
    }


    /**
     * 联网Dialog
     *
     * @param context
     */
    protected void showNetDialog(Context context) {
        if (netDialog == null) {
            ExitDialog exitDialog = new ExitDialog(context);
            netDialog = exitDialog;
            // netDialog.setMsgLineVisible();
        }
        netDialog.setIsNet(true);
        netDialog.setTitle(R.string.Network_not_connected);
        netDialog.setMessage("当前网络未连接，现在设置网络？");
        netDialog.setConfirm("好，现在设置");
        netDialog.setCancle("算了，现在不管");
        netDialog.setCancelable(true);
        netDialog.setCanceledOnTouchOutside(false);
        netDialog.show();
    }

    /**
     * 退出Dialog
     */
    protected void showExitDialog() {
        if (exitfullDialog == null) {
            ExitFullDialog.Builder builder = new ExitFullDialog.Builder(context);
            builder.setNeutralButton(R.string.exitdialog_back, new OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    dialog.dismiss();
                }
            });
            builder.setPositiveButton(R.string.exitdialog_out, new OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    BaseActivity.this.finish();
                    dialog.dismiss();
                }
            });
            exitfullDialog = builder.create();
            // netDialog.setMsgLineVisible();
            //Logger.v("joychang", "exitDialog == null");
        }
        exitfullDialog.setCancelable(true);
        exitfullDialog.setCanceledOnTouchOutside(false);
        exitfullDialog.show();
    }

    /**
     * 退出Dialog
     *
     * @param context
     * @param title
     */
    protected void showExitDialog(String title, Context context) {
        String Message = Rc4.decry_RC4(SP.getString("Exit_Message", null), Constant.d);
        HomeDialog.Builder builder = new HomeDialog.Builder(context);
        builder.setTitle(title);
        builder.setMessage(Message);
        builder.setPositiveButton(R.string.exitdialog_out, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                if (server != null){
                    server.stop();
                }
                BaseActivity.this.finish();
                dialog.dismiss();
            }
        });
        builder.setNeutralButton(R.string.exitdialog_back, new DialogInterface.OnClickListener() {

            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });
        builder.creates().shows();
    }




    /**
     * 显示音量的吐司
     *
     * @param max
     * @param current
     */
    private void showVolumeToast(int resId, int max, int current) {
        View view;
        mAudioManager.setStreamVolume(AudioManager.STREAM_MUSIC, current, 0);
        if (mToast == null) {
            mToast = new Toast(this);
            view = LayoutInflater.from(this).inflate(R.layout.mv_media_volume_controler,
                    null);
            ImageView center_image = (ImageView) view.findViewById(R.id.center_image);
            //TextView textView = (TextView) view.findViewById(R.id.center_info);
            ProgressBar center_progress = (ProgressBar) view
                    .findViewById(R.id.center_progress);
            center_progress.setMax(max);
            center_progress.setProgress(current);
            center_image.setImageResource(resId);
            mToast.setView(view);
        } else {
            view = mToast.getView();
            //TextView textView = (TextView) view.findViewById(R.id.center_info);
            ImageView center_image = (ImageView) view.findViewById(R.id.center_image);
            ProgressBar center_progress = (ProgressBar) view
                    .findViewById(R.id.center_progress);
            center_progress.setMax(max);
            center_progress.setProgress(current);
            center_image.setImageResource(resId);
        }
        mToast.setGravity(Gravity.FILL_HORIZONTAL, 0, 0);
        mToast.setDuration(Toast.LENGTH_SHORT);
        mToast.show();
    }

    /**
     * 应用崩溃toast
     */
    protected void handleFatalError() {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                Toast.makeText(BaseActivity.this, "发生了一点意外，程序终止!",
                        Toast.LENGTH_SHORT).show();
                finish();
            }
        });
    }

    /**
     * 内存空间不足
     */
    protected void handleOutmemoryError() {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                Toast.makeText(BaseActivity.this, "内存空间不足!", Toast.LENGTH_SHORT)
                        .show();
                finish();
            }
        });
    }

    /**
     * Activity关闭和启动动画
     */
    public void finish() {
        super.finish();
        overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
    }

    /**
     * 开启语音/远程搜索服务
     */
    public void start(int startPort) {
        if (server != null) {
            server.stop();
        }
        final int MAX_PORT = 65535; // TCP/IP端口号的最大值是65535
        int port = startPort;

        while (port <= MAX_PORT) {
            if (Utils.isPortAvailable(port)) {
                this.Port = port;
                try {
                    server = new HttpServer(this.Port);
                    break;
                } catch (Exception e) {
                    // 如果在创建服务器时发生异常，则记录异常并继续尝试下一个端口
                    e.printStackTrace();
                }
            }
            // 如果端口被占用，则尝试下一个端口
            port++;
        }

        // 检查是否成功找到可用端口并启动服务器
        if (server == null) {
            // 没有找到可用端口，抛出异常或采取其他措施
            throw new RuntimeException("No available ports found within the range.");
        }else{
            HttpRequestHandlerRegistry registry = server.getHttpRequestHandlerRegistry();
            registry.register("/action", new HttpRequestHandler() {
                @Override
                public void handle(HttpRequest request, HttpResponse response, HttpContext context) throws HttpException, IOException {
                    String method = request.getRequestLine().getMethod().toUpperCase(Locale.ENGLISH);
                    if (!method.equals("GET") && !method.equals("HEAD") && !method.equals("POST")) {
                        throw new MethodNotSupportedException(method + " method not supported");
                    }
                    if (method.equals("POST")) {
                        HttpEntity entity = ((HttpEntityEnclosingRequest) request).getEntity();
                        if (entity != null) {
                            // 从实体中获取参数
                            String postParams = EntityUtils.toString(entity);
                            String word = null;
                            String action = null;
                            String[] params = postParams.split("&");
                            for (String param : params) {
                                String[] keyValue = param.split("=");
                                if (keyValue.length == 2) {
                                    if (keyValue[0].equals("word")) {
                                        word = keyValue[1];
                                    } else if (keyValue[0].equals("do")) {
                                        action = keyValue[1];
                                    }
                                }
                            }
                            // 处理参数
                            if (word != null && action.equals("search")) {
                                SearchIntent(word);
                                overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
                                response.setStatusCode(HttpStatus.SC_OK);
                                String target = request.getRequestLine().getUri();
                                final String path = URLDecoder.decode(target, "UTF-8");
                                EntityTemplate body = new EntityTemplate(new ContentProducer() {
                                    public void writeTo(final OutputStream outstream) throws IOException {
                                        OutputStreamWriter writer = new OutputStreamWriter(outstream, "UTF-8");
                                        writer.write("<html><body><h1>");
                                        writer.write("Search");
                                        writer.write(URLDecoder.decode(path, "UTF-8"));
                                        writer.write("SearchSuccess");
                                        writer.write("</h1></body></html>");
                                        writer.flush();
                                    }
                                });
                                body.setContentType("text/html; charset=UTF-8");
                                response.setEntity(body);
                            }
                        }
                    }
                }
            });
            registry.register("*", new HttpStringHandler(context));
            //启动服务
            server.start();
        }
        // 保存端口号到持久化存储
        sp.edit().putInt("port", this.Port).commit();
    }
    
    public void startA(int port) {
        //停止已启用的服务
        if (server != null){
            server.stop();
        }

        try {
            //检查端口是否被占用
            boolean portAvailable = Utils.isPortAvailable(port);
            if (portAvailable) {
                Port = port;
                server = new HttpServer(Port);
            } else {
                Port = port + 1;
                server = new HttpServer(Port);
            }

            HttpRequestHandlerRegistry registry = server.getHttpRequestHandlerRegistry();
            registry.register("/action", new HttpRequestHandler() {
                @Override
                public void handle(HttpRequest request, HttpResponse response, HttpContext context) throws HttpException, IOException {
                    String method = request.getRequestLine().getMethod().toUpperCase(Locale.ENGLISH);
                    if (!method.equals("GET") && !method.equals("HEAD") && !method.equals("POST")) {
                        throw new MethodNotSupportedException(method + " method not supported");
                    }
                    if (method.equals("POST")) {
                        HttpEntity entity = ((HttpEntityEnclosingRequest) request).getEntity();
                        if (entity != null) {
                            // 从实体中获取参数
                            String postParams = EntityUtils.toString(entity);
                            String word = null;
                            String action = null;
                            String[] params = postParams.split("&");
                            for (String param : params) {
                                String[] keyValue = param.split("=");
                                if (keyValue.length == 2) {
                                    if (keyValue[0].equals("word")) {
                                        word = keyValue[1];
                                    } else if (keyValue[0].equals("do")) {
                                        action = keyValue[1];
                                    }
                                }
                            }
                            // 处理参数
                            if (word != null && action.equals("search")) {
                                SearchIntent(word);
                                overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
                                response.setStatusCode(HttpStatus.SC_OK);
                                String target = request.getRequestLine().getUri();
                                final String path = URLDecoder.decode(target, "UTF-8");
                                EntityTemplate body = new EntityTemplate(new ContentProducer() {
                                    public void writeTo(final OutputStream outstream) throws IOException {
                                        OutputStreamWriter writer = new OutputStreamWriter(outstream, "UTF-8");
                                        writer.write("<html><body><h1>");
                                        writer.write("Search");
                                        writer.write(URLDecoder.decode(path, "UTF-8"));
                                        writer.write("SearchSuccess");
                                        writer.write("</h1></body></html>");
                                        writer.flush();
                                    }
                                });
                                body.setContentType("text/html; charset=UTF-8");
                                response.setEntity(body);
                            }
                        }
                    }
                }
            });
            registry.register("*", new HttpStringHandler(context));
            //启动服务
            server.start();
            //把启动的端口保存起来
            sp.edit().putInt("port", Port).commit();

        } catch (Exception e) {

        }
    }

    /*收听广播*/
    public class AutoStartReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            /*夏杰语音*/
            if (Search == 1){
                String title = intent.getExtras().getString("title");
                if ("android.content.movie.search.Action".equals(intent.getAction()) && intent.getExtras() != null) {
                    if (!title.equals("")){
                        SearchIntent(title);
                        overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
                    }
                }
            }else{
                Utils.showToast(context, getString(R.string.search_Not_yet_activated), R.drawable.toast_err);
            }
        }
    }

    /*搜索意图*/
    public void SearchIntent(String title){
        //广播通知暂停/停止正在播放的视频
        //Intent localIntent = new Intent("com.example.MY_ACTION_PAUSE_VIDEO");
        Intent localIntent = new Intent("com.example.MY_ACTION_FINISH_LIVE");
        LocalBroadcastManager.getInstance(BaseActivity.this).sendBroadcast(localIntent);
        /*传递搜索信息*/
        Intent intent = new Intent(BaseActivity.this, SearchActivity.class);
        intent.putExtra("TYPE", "ALL");
        intent.putExtra("NAME", Utils.sanitizeUri(title));
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP); // Intent.FLAG_ACTIVITY_SINGLE_TOP或 Intent.FLAG_ACTIVITY_CLEAR_TOP
        startActivity(intent);
    }

}
