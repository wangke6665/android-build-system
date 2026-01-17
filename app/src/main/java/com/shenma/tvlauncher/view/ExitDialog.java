package com.shenma.tvlauncher.view;

import android.app.Activity;
import android.app.ActivityManager;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.Window;
import android.view.KeyEvent;
import android.view.WindowManager;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.shenma.tvlauncher.R;

/**
 * 退出的Dialog
 *
 * @author drowtram
 */
public class ExitDialog extends Dialog implements View.OnClickListener {
    private Context context;
    private Boolean isNet;
    private TextView tv_exit_msg_titile;
    private TextView tv_exit_msg;
    private TextView tv_exit_confirm;
    private TextView tv_exit_cancle;
    private LinearLayout lv_exit_ok;
    private LinearLayout lv_exit_cancle;

    public ExitDialog(Context context) {
        super(context, R.style.DialogStyle);
        this.context = context;
        View v = LayoutInflater.from(context).inflate(R.layout.tv_exit_dialog_layout, null);
        tv_exit_msg_titile = (TextView) v.findViewById(R.id.tv_exit_msg_titile);
        tv_exit_msg = (TextView) v.findViewById(R.id.tv_exit_msg);
        tv_exit_confirm = (TextView) v.findViewById(R.id.tv_exit_confirm);
        tv_exit_cancle = (TextView) v.findViewById(R.id.tv_exit_cancle);
        lv_exit_ok = (LinearLayout) v.findViewById(R.id.lv_exit_ok);
        lv_exit_cancle = (LinearLayout) v.findViewById(R.id.lv_exit_cancle);
        setContentView(v);
        lv_exit_ok.setOnClickListener(this);
        lv_exit_cancle.setOnClickListener(this);
        
        // 设置按钮可获取焦点，支持TV遥控器
        lv_exit_ok.setFocusable(true);
        lv_exit_cancle.setFocusable(true);
        
        // 设置按键监听，支持遥控器确认键
        lv_exit_ok.setOnKeyListener(new View.OnKeyListener() {
            @Override
            public boolean onKey(View v, int keyCode, KeyEvent event) {
                if (event.getAction() == KeyEvent.ACTION_DOWN && 
                    (keyCode == KeyEvent.KEYCODE_DPAD_CENTER || keyCode == KeyEvent.KEYCODE_ENTER)) {
                    onClick(v);
                    return true;
                }
                return false;
            }
        });
        
        lv_exit_cancle.setOnKeyListener(new View.OnKeyListener() {
            @Override
            public boolean onKey(View v, int keyCode, KeyEvent event) {
                if (event.getAction() == KeyEvent.ACTION_DOWN && 
                    (keyCode == KeyEvent.KEYCODE_DPAD_CENTER || keyCode == KeyEvent.KEYCODE_ENTER)) {
                    onClick(v);
                    return true;
                }
                return false;
            }
        });
        
        setScreenBrightness();
    }
    
//    @Override  
//    protected void onCreate(Bundle savedInstanceState) {  
//        super.onCreate(savedInstanceState);  
//        setContentView(R.layout.tv_exit_dialog_layout);  
//        setScreenBrightness();  
//    }


    public void setMessage(String message) {
        tv_exit_msg.setText(message);
    }

    public void setConfirm(String confirm) {
        tv_exit_confirm.setText(confirm);
    }

    public void setCancle(String cancle) {
        tv_exit_cancle.setText(cancle);
    }

    public void setTitle(String title) {
        tv_exit_msg_titile.setText(title);
    }

    @Override
    public void show() {
        Window window = this.getWindow();
        window.setWindowAnimations(R.style.DialogAnim);
        this.setCanceledOnTouchOutside(true);
        super.show();
        // 显示后让返回按钮获取焦点
        lv_exit_cancle.post(new Runnable() {
            @Override
            public void run() {
                lv_exit_cancle.requestFocus();
            }
        });
    }

    public void setIsNet(Boolean isNet) {
        this.isNet = isNet;
    }

    /**
     * 此处设置亮度值。dimAmount代表黑暗数量，也就是昏暗的多少，设置为0则代表完全明亮。
     * 范围是0.0到1.0
     */
    private void setScreenBrightness() {
        Window window = getWindow();
        WindowManager.LayoutParams lp = window.getAttributes();
        lp.dimAmount = 0.5f;
        window.setAttributes(lp);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.lv_exit_cancle:
                dismiss();
                if (isNet) {
//                    Intent intent = new Intent(context, HomeActivity.class);
//                    context.startActivity(intent);
                    ((Activity) context).overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
                    ((Activity)context).finish();
                }
                break;
            case R.id.lv_exit_ok:
                //如果设置网络，跳转到设置
                dismiss();
                if (isNet) {
//                    Intent intent = new Intent("android.settings.WIRELESS_SETTINGS");//跳到飞行界面
//                    Intent intent = new Intent("android.settings.WIFI_SETTINGS");//跳到wifi界面
                    Intent intent = new Intent("android.settings.SETTINGS");//跳到设置界面
                    context.startActivity(intent);
                    ((Activity) context).overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
                } else {
//				ActivityManager manager = (ActivityManager)context.getSystemService(context.ACTIVITY_SERVICE);       
//				manager.forceStopPackage("com.shenma.tvlauncher.tvlive");
//				manager.forceStopPackage("com.shenma.tvlauncher.tv");
//                    System.exit(0);
                    ((Activity)context).finish();
                }
                break;
            default:
                break;
        }
    }

    private ActivityManager getSystemService(String activityService) {
        // TODO Auto-generated method stub
        return null;
    }

}  