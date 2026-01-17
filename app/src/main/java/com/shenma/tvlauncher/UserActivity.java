package com.shenma.tvlauncher;

import android.annotation.SuppressLint;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Handler;
import android.os.Message;
import android.text.TextUtils;
import android.util.Log;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnKeyListener;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemLongClickListener;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.LinearLayout.LayoutParams;
import android.widget.ListView;
import android.widget.PopupWindow;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.RadioGroup.OnCheckedChangeListener;
import android.widget.TextView;
import android.widget.Toast;
import com.android.volley.AuthFailureError;
import com.android.volley.NetworkError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.ServerError;
import com.android.volley.TimeoutError;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.HurlStack;
import com.shenma.tvlauncher.network.ExtHttpStack;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.shenma.tvlauncher.utils.AES;
import com.shenma.tvlauncher.utils.Constant;
import com.shenma.tvlauncher.utils.Rsa;
import com.shenma.tvlauncher.utils.SharePreferenceDataUtil;
import com.shenma.tvlauncher.view.HomeDialog;
import com.umeng.analytics.MobclickAgent;
import com.shenma.tvlauncher.adapter.UserTypeAdapter;
import com.shenma.tvlauncher.dao.bean.AppInfo;
import com.shenma.tvlauncher.db.DatabaseOperator;
import com.shenma.tvlauncher.utils.GetTimeStamp;
import com.shenma.tvlauncher.utils.Logger;
import com.shenma.tvlauncher.utils.Md5Encoder;
import com.shenma.tvlauncher.utils.Rc4;
import com.shenma.tvlauncher.utils.Utils;
import com.shenma.tvlauncher.view.WiFiDialog;
import com.shenma.tvlauncher.view.WiFiDialog.Builder;
import com.shenma.tvlauncher.vod.VideoDetailsActivity;
import com.shenma.tvlauncher.vod.dao.VodDao;
import com.shenma.tvlauncher.vod.db.Album;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;



/**
 * @author joychang
 * @Description 个人中心
 */
public class UserActivity extends BaseActivity {
    private final String TAG = "UserActivity";
    protected ImageLoader imageLoader = ImageLoader.getInstance();
    private RadioGroup rg_member;
    private RadioButton rb_user;
    private RadioButton rb_user_alert;
    private RadioButton rb_user_history;
    private RadioButton rb_user_app;
    private RadioButton rb_user_collect;
    private TextView tv_no_data, tv_filter_content;
    private LinearLayout user_type_details;
    private GridView gv_user_type_details_grid;
    private List<Album> Albumls = null;
    private UserTypeAdapter userTypeAdapter = null;
    private VodDao dao;
    private int mPosition = -1;
    private PopupWindow menupopupWindow;
    private ListView menulist;
    private MyAdapter mAdapter = null;
    private int USER_TYPE;
    private DatabaseOperator dbtools;
    private List<AppInfo> templovLst = new ArrayList<AppInfo>();
    private Boolean ISAPP = true, fromApp = false;
    private RequestQueue mQueue;
    private TextView tv_user_name;
    private Dialog mDialog;
    private String userName;
    private Handler mediaHandler = new Handler() {
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case 5://绑定邮箱
                    showUpdateDialog(UserActivity.this,type);
                    return;
                case 6:
                    login_notify();
                    return;
                case 7:
                    Bitmap empower = Utils.createQRCodeBitmap(empower_url, 200, 200,"UTF-8","H", "1", Color.BLACK, Color.WHITE);
                    users_empower.setImageBitmap(empower);
                    return;
                case 8:
                    Utils.showToast(UserActivity.this, Msg, R.drawable.toast_smile);
                    myCountDownTimer = new MyCountDownTimer(180000,1000);
                    myCountDownTimer.start();
                    return;
                case 9:
                    Utils.showToast(UserActivity.this, getString(R.string.fail) + Msg , R.drawable.toast_err);
                    return;
                case 10:
                    Utils.showToast(UserActivity.this, getString(R.string.fail) + "当前模式禁止退出账户" , R.drawable.toast_err);
                    return;
                default:
                    return;
            }
        }
    };
    private String data;
    private String sign;
    private String Msg;
    private Button send_code_bt;
    private MyCountDownTimer myCountDownTimer;
    private ImageView users_empower;
    private Dialog mDialogs;
    private String empower_url;
    private String t;
    private String notify;//扫码登录查询
    private String type;///绑定类型
    private EditText user_inv_et;
    private String logindata;

    /*创建时的回调函数*/
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_member);
        findViewById(R.id.member).setBackgroundResource(R.drawable.video_details_bg);
        dao = new VodDao(this);
        dbtools = new DatabaseOperator(this);
        initView();
        initData();
    }

    /*启动时*/
    @Override
    protected void onStart() {
        super.onStart();
        Logger.d(TAG, "onStart()...");
    }

    /*暂停时*/
    @Override
    protected void onPause() {
        super.onPause();
        Logger.d(TAG, "onPause()...");
        MobclickAgent.onPageEnd(TAG);
        MobclickAgent.onPause(this);
    }

    /*恢复时*/
    @Override
    protected void onResume() {
        super.onResume();
        Logger.d(TAG, "onResume()...");
        MobclickAgent.onPageStart(TAG);
        MobclickAgent.onResume(this);
    }

    /*初始化视图*/
    @Override
    protected void initView() {
        loadViewLayout();
        findViewById();
        setListener();
    }

    /**
     * 初始化数据
     */
    private void initData() {
        String uName = sp.getString("userName", null);
        if (!TextUtils.isEmpty(uName)) {
            tv_user_name.setText(uName);
            tv_no_data.setText( getString(R.string.user) + ":" + uName + getString(R.string.Logged_in));
        } else {
            tv_no_data.setText(R.string.not_logged_on);
            showUserDialog();
        }
        tv_no_data.setVisibility(View.VISIBLE);
    }

    /*调度密钥事件*/
    @SuppressLint("RestrictedApi")
    @Override
    public boolean dispatchKeyEvent(KeyEvent event) {
        int keyCode = event.getKeyCode();
        switch (keyCode) {
            case KeyEvent.KEYCODE_MENU:/*按下菜单键*/
                if (!ISAPP) {
                    showMenu();
                } else {
                    //openActivity(AppManageActivity.class);
                }
                break;
            case KeyEvent.KEYCODE_DPAD_CENTER://按下遥控器ok键刷新二维码
                //seek_empower();
                break;
            case KeyEvent.KEYCODE_ENTER://按下回车刷新二维码
                //seek_empower();
                break;
            case KeyEvent.KEYCODE_BACK:/*按下返回键*/
                hideMenu();
                break;
            default:
                break;
        }
        return super.dispatchKeyEvent(event);
    }

    /*加载视图布局*/
    @Override
    protected void loadViewLayout() {
        onCreateMenu();
    }

    /*按ID查找视图*/
    @Override
    protected void findViewById() {
        tv_user_name = (TextView) findViewById(R.id.tv_user_name);
        rg_member = (RadioGroup) findViewById(R.id.rg_member);
        rb_user = (RadioButton) findViewById(R.id.rb_user);
        rb_user_alert = (RadioButton) findViewById(R.id.rb_user_alert);
        rb_user_history = (RadioButton) findViewById(R.id.rb_user_history);
        rb_user_app = (RadioButton) findViewById(R.id.rb_user_app);
        rb_user_collect = (RadioButton) findViewById(R.id.rb_user_collect);
        gv_user_type_details_grid = (GridView) findViewById(R.id.user_type_details_grid);
        gv_user_type_details_grid.setSelector(new ColorDrawable(Color.TRANSPARENT));
        user_type_details = (LinearLayout) findViewById(R.id.user_type_details);
        tv_no_data = (TextView) findViewById(R.id.tv_no_data);
        tv_filter_content = (TextView) findViewById(R.id.filter_content);
        rb_user.setChecked(true);
    }

    /*设置侦听器*/
    @Override
    protected void setListener() {
        rg_member.setOnCheckedChangeListener(new OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup rg, int position) {
            }
        });
        for (int i = 0; i < rg_member.getChildCount(); i++) {
            rg_member.getChildAt(i).setOnClickListener(new OnClickListener() {

                @Override
                public void onClick(View v) {
                    fromApp = false;
                    String username = sp.getString("userName", null);
                    switch (v.getId()) {
                        case R.id.rb_user:/*登录和注销*/
                            ISAPP = true;
                            Albumls = null;
                            tv_no_data.setVisibility(View.VISIBLE);
                            tv_filter_content.setVisibility(View.GONE);
                            if (null != userTypeAdapter) {
                                userTypeAdapter.clearDatas();
                                userTypeAdapter.notifyDataSetChanged();
                            }
                            if (TextUtils.isEmpty(username)) {
                                tv_no_data.setText(R.string.not_logged_on);
                                //注销 TODO
                                showUserDialog();
                            } else {
                                tv_no_data.setText(getString(R.string.user) + ":" + username + getString(R.string.Logged_in));
                                //注销登录 TODO
                                showLogoutDialog();
                            }
                            break;
                        case R.id.rb_user_alert:/*我的追剧*/
                            if(TextUtils.isEmpty(username)) {
                                Utils.showToast(UserActivity.this, R.string.Please_log_in_to_your_account_first, R.drawable.toast_err);
                                showUserDialog();
                                break;
                            }
                            ISAPP = false;
                            USER_TYPE = Constant.TYPE_ZJ;
                            tv_no_data.setText(R.string.no_follow_up_drama);
                            Albumls = dao.queryAllAppsByType(0);
                            break;
                        case R.id.rb_user_collect:/*我的收藏*/
                            if(TextUtils.isEmpty(username)) {
                                Utils.showToast(UserActivity.this, R.string.Please_log_in_to_your_account_first, R.drawable.toast_err);
                                showUserDialog();
                                break;
                            }
                            ISAPP = false;
                            USER_TYPE = Constant.TYPE_SC;
                            tv_no_data.setText(R.string.No_favorites);
                            Albumls = dao.queryAllAppsByType(1);
                            break;
                        case R.id.rb_user_history:/*历史记录*/
                            if(TextUtils.isEmpty(username)) {
                                Utils.showToast(UserActivity.this, R.string.Please_log_in_to_your_account_first, R.drawable.toast_err);
                                showUserDialog();
                                break;
                            }
                            ISAPP = false;
                            USER_TYPE = Constant.TYPE_LS;
                            tv_no_data.setText(R.string.No_record);
                            Albumls = dao.queryAllAppsByType(2);
                            break;
                        case R.id.rb_user_app:/*授权中心*/
                            if(TextUtils.isEmpty(username)) {
                                Utils.showToast(UserActivity.this, R.string.Please_log_in_to_your_account_first, R.drawable.toast_err);
                                showUserDialog();
                            }else {
                                startActivity(new Intent(UserActivity.this, EmpowerActivity.class));
                                overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
                                break;
                            }
                    }
                    if (!ISAPP) {
                        if (null != Albumls && Albumls.size() > 0) {
                            //Collections.reverse(Albumls);//将影片内容倒序
                            tv_filter_content.setText(getString(R.string.common) + " " + Albumls.size() + " " + getString(R.string.Film));
                            tv_filter_content.setVisibility(View.VISIBLE);
                            tv_no_data.setVisibility(View.GONE);
                            userTypeAdapter = new UserTypeAdapter(UserActivity.this, Albumls, imageLoader, ISAPP);
                            if (gv_user_type_details_grid.getVisibility() != View.VISIBLE) {
                                gv_user_type_details_grid.setVisibility(View.VISIBLE);
                            }
                            gv_user_type_details_grid.setAdapter(userTypeAdapter);
                        } else {
                            tv_no_data.setVisibility(View.VISIBLE);
                            tv_filter_content.setText(R.string.films_in_total);
                            tv_filter_content.setVisibility(View.VISIBLE);
                            if (null != userTypeAdapter) {
                                userTypeAdapter.clearDatas();
                                userTypeAdapter.notifyDataSetChanged();
                            }
                        }
                    }
                }
            });
        }
        gv_user_type_details_grid.setOnItemSelectedListener(new OnItemSelectedListener() {


            @Override
            public void onItemSelected(AdapterView<?> pratenView, View v,
                                       int position, long arg3) {
                mPosition = position;
                //dao.deleteByWhere(Albumls.get(position).getAlbumId(), albumType, typeId)
            }

            @Override
            public void onNothingSelected(AdapterView<?> arg0) {
            }
        });

        gv_user_type_details_grid.setOnItemLongClickListener(new OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> arg0, View arg1,
                                           int position, long arg3) {
                if (!ISAPP) {
                    mPosition = position;
                    showMenu();
                }
                return true;
            }
        });
        gv_user_type_details_grid.setOnItemClickListener(new OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> pratenView, View v, int position,
                                    long arg3) {
                if (ISAPP) {
                    String packname = ((TextView) v.findViewById(R.id.packflag)).getText().toString();
                    String appname = ((TextView) v.findViewById(R.id.app_title)).getText().toString();
                    Intent intent = getPackageManager().getLaunchIntentForPackage(packname);
                    startActivity(intent);
                    ////Logger.d(TAG, "appname=" + appname);
                    Map<String, String> m_value = new HashMap<String, String>();
                    m_value.put("UserAppName", appname);
                    m_value.put("UserPackName", packname);
                    MobclickAgent.onEvent(UserActivity.this, "USER_APP_NAME", m_value);
                } else {
                    Intent intent = new Intent(UserActivity.this,
                            VideoDetailsActivity.class);
                    //intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                    intent.putExtra("vodtype", Albumls.get(position).getAlbumType());
                    intent.putExtra("vodstate", Albumls.get(position).getAlbumState());
                    intent.putExtra("nextlink", Albumls.get(position).getNextLink());
                    startActivity(intent);
                }
            }
        });

    }

    /* 初始化menu*/
    public void onCreateMenu() {
        View menuView = View.inflate(this, R.layout.mv_controler_menu, null);
        menulist = (ListView) menuView.findViewById(R.id.media_controler_menu);
        menupopupWindow = new PopupWindow(menuView, LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
        menupopupWindow.setOutsideTouchable(true);// 允许在外点击popu消失
        menupopupWindow.setTouchable(true);
        menupopupWindow.setFocusable(true);
        menulist.setOnKeyListener(new OnKeyListener() {
            @Override
            public boolean onKey(View v, int keyCode, KeyEvent event) {
                if (event.getAction() == KeyEvent.ACTION_UP) {
                    return false;
                }
                switch (keyCode) {
                    case KeyEvent.KEYCODE_BACK:
                        menupopupWindow.dismiss();
                        break;
                }
                return false;
            }
        });

        menulist.setOnItemClickListener(new OnItemClickListener() {

            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Album album = null;
                if (mPosition != -1) {
                    album = Albumls.get(mPosition);
                }
                switch (position) {
                    //删除
                    case 0:
                        if (null != album) {
                            dao.deleteByWhere(album.getAlbumId(), album.getAlbumType(), album.getTypeId());
                            Albumls = (ArrayList<Album>) dao.queryAllAppsByType(album.getTypeId());
                            userTypeAdapter.remove(mPosition);
                            userTypeAdapter.notifyDataSetChanged();
                            tv_filter_content.setText(getString(R.string.common) + " " + userTypeAdapter.getCount() + " " + getString(R.string.Film));
                            //user_type_details_sum.setText("共"+userTypeAdapter.vodDatas.size()+"部");
                        } else {
                            Utils.showToast(UserActivity.this, R.string.No_selection, R.drawable.toast_smile);
                        }
                        mPosition = -1;
                        hideMenu();
                        break;
                    //全部删除
                    case 1:
                        Albumls = (ArrayList<Album>) dao.queryAllAppsByType(USER_TYPE);
                        if (null != Albumls && Albumls.size() > 0) {
                            userTypeAdapter.clearDatas();
                            userTypeAdapter.notifyDataSetChanged();
                            tv_filter_content.setText(R.string.films_in_total);
                            //user_type_details_sum.setText("共0部");
                        } else {
                            Utils.showToast(UserActivity.this, R.string.No_deletion, R.drawable.toast_shut);
                        }
                        dao.deleteAllByWhere(USER_TYPE);
                        mPosition = -1;
                        hideMenu();
                        break;
                }
            }

        });
    }

    /*打开menu*/
    private void showMenu() {
        if (null != menupopupWindow) {
            mAdapter = new MyAdapter(this, Utils.getUserData(0));
            menulist.setAdapter(mAdapter);
            menupopupWindow.setAnimationStyle(R.style.AnimationMenu);
            menupopupWindow.showAtLocation(user_type_details, Gravity.TOP | Gravity.RIGHT, 0, 0);
            menupopupWindow.update(0, 0, getResources().getDimensionPixelSize(R.dimen.sm_350), mHeight);
        }
    }

    /*隐藏menu*/
    private void hideMenu() {
        if (menupopupWindow.isShowing()) {
            menupopupWindow.dismiss();
        }
    }

    /*显示用户登录注册的dialog*/
    private void showUserDialog() {
        final String User_url = Rc4.decry_RC4(SharePreferenceDataUtil.getSharedStringData(this, Constant.s, ""),Constant.d);
        Builder builder = new Builder(context);
        if (SharePreferenceDataUtil.getSharedIntData(this, "Login_control", 0) == 0){
            View mView = View.inflate(context, R.layout.user_form_bak, null);
            final EditText user_name_et = (EditText) mView.findViewById(R.id.user_name_et);
            final EditText user_pass_et = (EditText) mView.findViewById(R.id.user_pass_et);
            /*有邀请码*/
            if (SharePreferenceDataUtil.getSharedIntData(this, "login_type", 0) == 2){
                user_inv_et = (EditText) mView.findViewById(R.id.user_inv_et);//邀请码
                final LinearLayout user_inv = (LinearLayout) mView.findViewById(R.id.user_inv);//邀请码
                user_inv.setVisibility(View.VISIBLE);
            }

            builder.setContentView(mView);
            /*登录按钮*/
            builder.setPositiveButton(R.string.log_on, new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int which) {
                    Login(user_name_et, user_pass_et, User_url + "/api?app=" + Api.APPID + "&act=user_logon");
                }
            });
            /*注册按钮*/
            builder.setNeutralButton(R.string.register, new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int which) {

                    if (SharePreferenceDataUtil.getSharedIntData(UserActivity.this, "login_type", 0) == 2){
                        /*有邀请码*/
                        Regedit(user_name_et, user_pass_et,user_inv_et, User_url+"/api?app=" + Api.APPID + "&act=user_reg");
                    }else{
                        /*无邀请码*/
                        Regedit(user_name_et, user_pass_et,null, User_url + "/api?app=" + Api.APPID + "&act=user_reg");
                    }
                }
            });
        }else{
            View mView = View.inflate(context, R.layout.user_form, null);
            final EditText user_name_et = (EditText) mView.findViewById(R.id.user_name_et);
            final EditText user_pass_et = (EditText) mView.findViewById(R.id.user_pass_et);
            /*有邀请码*/
            if (SharePreferenceDataUtil.getSharedIntData(this, "login_type", 0) == 2){
                user_inv_et = (EditText) mView.findViewById(R.id.user_inv_et);//邀请码
                final LinearLayout user_inv = (LinearLayout) mView.findViewById(R.id.user_inv);//邀请码
                user_inv.setVisibility(View.VISIBLE);
            }
            builder.setContentView(mView);
            /*登录按钮*/
            builder.setPositiveButton(R.string.log_on, new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int which) {
                    Login(user_name_et, user_pass_et, User_url + "/api?app=" + Api.APPID + "&act=user_logon");
                }
            });
            /*注册按钮*/
            builder.setNeutralButton(R.string.register, new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int which) {
                    if (SharePreferenceDataUtil.getSharedIntData(UserActivity.this, "login_type", 0) == 2){
                        /*有邀请码*/
                        Regedit(user_name_et, user_pass_et,user_inv_et, User_url+"/api?app=" + Api.APPID + "&act=user_reg");
                    }else{
                        /*无邀请码*/
                        Regedit(user_name_et, user_pass_et,null, User_url + "/api?app=" + Api.APPID + "&act=user_reg");
                    }
                }
            });
            /*找回密码*/
            final Button user_password = (Button) mView.findViewById(R.id.user_password);//找回密码
            user_password.setOnClickListener(new OnClickListener() {//找回密码单击
                public void onClick(View arg0) {
                    mDialog.dismiss();
                    showUserDialogpwd();
                }
            });
            /*扫码登录*/
            final Button user_empower = (Button) mView.findViewById(R.id.user_empower);//扫码登录
            user_empower.setOnClickListener(new OnClickListener() {//扫码登录
                public void onClick(View arg0) {
                    mDialog.dismiss();
                    showUserDialogempower();
                }
            });

        }
        mDialog = builder.create();
        mDialog.show();
    }

    /*用户注销登录dialog*/
    private void showLogoutDialog() {
        WiFiDialog.Builder builder = new WiFiDialog.Builder(UserActivity.this);
        View mView = View.inflate(UserActivity.this, R.layout.logout_dialog, null);
//        int Exit = SharePreferenceDataUtil.getSharedIntData(this, "Exit", 0);
        int Exit = sp.getInt("Exit", 0);
        builder.setContentView(mView);
        builder.setPositiveButton(R.string.log_off, new DialogInterface.OnClickListener() {

            @Override
            public void onClick(DialogInterface dialog, int which) {
                //注销登录
                if (Exit == 0){
                    sp.edit().putString("userName", null).putString("passWord", null).putString("vip", null).putString("fen", null).putString("ckinfo", null).commit();
                    tv_no_data.setText(R.string.not_logged_on);
                    tv_user_name.setText(R.string.no_login);
                    dialog.dismiss();
                }else{
                    mediaHandler.sendEmptyMessage(10);
                    dialog.dismiss();
                }


            }
        });
        builder.setNeutralButton(R.string.popup_confirmation_dialog_Negative, new DialogInterface.OnClickListener() {

            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });
        mDialog = builder.create();
        mDialog.show();
    }

    /**
     * 请求登录服务器
     *
     * @param uNameET 户名输入框
     * @param uPassET 密码输入框
     * @param requestUrl 请求路径地址
     */

    private void Login(EditText uNameET, EditText uPassET, String requestUrl) {
        mQueue = Volley.newRequestQueue(this, new ExtHttpStack());
        final String RC4KEY = Rc4.decry_RC4(SharePreferenceDataUtil.getSharedStringData(this, Constant.kd, ""),Constant.d);
        final String RSAKEY = Rc4.decry_RC4(SharePreferenceDataUtil.getSharedStringData(this, Constant.tb, ""),Constant.d);
        final String AESKEY = Rc4.decry_RC4(SharePreferenceDataUtil.getSharedStringData(this, Constant.um, ""),Constant.d);
        final String AESIV = Rc4.decry_RC4(SharePreferenceDataUtil.getSharedStringData(this, Constant.im, ""),Constant.d);
        final String Appkey = Rc4.decry_RC4(SharePreferenceDataUtil.getSharedStringData(this, Constant.yk, ""),Constant.d);
        userName = uNameET.getText().toString().trim();
        final String passWord = uPassET.getText().toString().trim();
        String logindata = "account=" + userName + "&password=" + passWord + "&markcode=" + Utils.GetAndroidID(this) + "&t=" + GetTimeStamp.timeStamp();


        int miType = SharePreferenceDataUtil.getSharedIntData(this, Constant.ue, 1);
        if (miType == 1) {
            data = Rc4.encry_RC4_string(logindata, RC4KEY);
        } else if (miType == 2) {
            try {
                data = Rsa.encrypt_Rsa(logindata, RSAKEY);
            } catch (Exception e) {
                e.printStackTrace();
            }
        } else if (miType == 3) {
            data = AES.encrypt_Aes(AESKEY,logindata, AESIV);
        }

        sign = Md5Encoder.encode(new StringBuilder(String.valueOf(logindata)).append("&").append(Appkey).toString());
        /*验证是否输入账户密码信息*/
        if (TextUtils.isEmpty(userName)) {
            Utils.showToast(UserActivity.this, R.string.No_account_entered , R.drawable.toast_err);
        } else if (TextUtils.isEmpty(passWord)) {
            Utils.showToast(UserActivity.this, R.string.No_password_entered , R.drawable.toast_err);
        } else {
            Utils.loadingShow_tv(context, R.string.is_loading);
            StringRequest stringRequest = new StringRequest(Request.Method.POST, requestUrl,
                    new com.android.volley.Response.Listener<String>() {
                        public void onResponse(String response) {
                            LoginResponse(response,passWord);
                        }
                    }, new com.android.volley.Response.ErrorListener() {
                public void onErrorResponse(VolleyError error) {
                    Error(error);
                }
            }) {
                @Override
                protected Map<String, String> getParams() throws AuthFailureError {
                    Map<String, String> params = new HashMap<>();
                    params.put("data", data);
                    params.put("sign", sign);
                    return params;
                }

                @Override
                public Map<String, String> getHeaders() throws AuthFailureError {
                    Map<String, String> headers = new HashMap<>();
                    headers.put("Authorization", Rc4.decry_RC4(SharePreferenceDataUtil.getSharedStringData(UserActivity.this, "Authorization", ""),Constant.d));  /*设置其他请求头*/
                    return headers;
                }
            };
            mQueue.add(stringRequest);
        }
    }

    /*登录响应成功*/
    public void LoginResponse(String response,String passWord) {
        //Log.i(TAG, "LoginResponse: " + response);
        String RC4KEY = Rc4.decry_RC4(SharePreferenceDataUtil.getSharedStringData(this, Constant.kd, ""),Constant.d);
        String RSAKEY = Rc4.decry_RC4(SharePreferenceDataUtil.getSharedStringData(this, Constant.tb, ""),Constant.d);
        String AESKEY = Rc4.decry_RC4(SharePreferenceDataUtil.getSharedStringData(this, Constant.um, ""),Constant.d);
        String AESIV = Rc4.decry_RC4(SharePreferenceDataUtil.getSharedStringData(this, Constant.im, ""),Constant.d);
        Utils.loadingClose_Tv();
        try {
            JSONObject jSONObject = new JSONObject(response);
            int code = jSONObject.optInt("code");
            if (code == 200){
                int miType = SharePreferenceDataUtil.getSharedIntData(this, Constant.ue, 1);
                JSONObject msg = null;
                if (miType == 1) {
                    msg = new JSONObject(Rc4.decry_RC4(jSONObject.optString("msg"), RC4KEY));
                } else if (miType == 2) {
                    msg = new JSONObject(Rsa.decrypt_Rsa(jSONObject.optString("msg"), RSAKEY));
                } else if (miType == 3) {
                    msg = new JSONObject(AES.decrypt_Aes(AESKEY,jSONObject.optString("msg"), AESIV));
                }
                String token = msg.optString("token");
                JSONObject info = msg.getJSONObject("info");
                String id = info.getString("id");
                String pic = info.getString("pic");
                String name = info.getString("name");
                String vip = info.getString("vip");
                String fen = info.getString("fen");
                String email = info.getString("email");
                String user = info.getString("user");
                int phone = info.getInt("phone");
                int Exit = info.getInt("Exit");
                tv_user_name.setText(name);
                tv_no_data.setText(getString(R.string.user )+ ":" + user + getString(R.string.Logged_in));
                if (email.equals("")){
                    type = "邮箱";
                    mediaHandler.sendEmptyMessage(5);
                }
                if (phone == 0){
                    type = "手机";
                    mediaHandler.sendEmptyMessage(5);
                }
                sp.edit()
                        .putInt("Exit", Exit)/*是否允许退出*/
                        .putString("userName", user)/*帐号*/
                        .putString("passWord", passWord)/*密码*/
                        .putString("ckinfo", token)/*token*/
                        .putString("vip", vip)/*vip时间*/
                        .putString("fen", fen)/*积分数量*/
                        .commit();
                if (mDialog != null && mDialog.isShowing()) {
                    mDialog.dismiss();
                }
            }else{
                int miType = SharePreferenceDataUtil.getSharedIntData(this, Constant.ue, 1);
                if (miType == 1) {
                    Msg = URLDecoder.decode(Rc4.decry_RC4(jSONObject.optString("msg"),RC4KEY), "UTF-8");
                } else if (miType == 2) {
                    Msg = URLDecoder.decode(Rsa.decrypt_Rsa(jSONObject.optString("msg"),RSAKEY), "UTF-8");
                } else if (miType == 3) {
                    Msg = URLDecoder.decode(AES.decrypt_Aes(AESKEY,jSONObject.optString("msg"), AESIV), "UTF-8");
                }
                mediaHandler.sendEmptyMessage(9);
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 请求注册
     *
     * @param uNameET 用户名输入框
     * @param uPassET 密码输入框
     */
    private void Regedit(final EditText uNameET, final EditText uPassET,final EditText uInvET, String URL) {
        mQueue = Volley.newRequestQueue(this, new ExtHttpStack());
        final String RC4KEY = Rc4.decry_RC4(SharePreferenceDataUtil.getSharedStringData(this, Constant.kd, ""),Constant.d);
        final String RSAKEY = Rc4.decry_RC4(SharePreferenceDataUtil.getSharedStringData(this, Constant.tb, ""),Constant.d);
        final String AESKEY = Rc4.decry_RC4(SharePreferenceDataUtil.getSharedStringData(this, Constant.um, ""),Constant.d);
        final String AESIV = Rc4.decry_RC4(SharePreferenceDataUtil.getSharedStringData(this, Constant.im, ""),Constant.d);
        final String Appkey = Rc4.decry_RC4(SharePreferenceDataUtil.getSharedStringData(this, Constant.yk, ""),Constant.d);
        final String user = uNameET.getText().toString().trim();
        final String passWord = uPassET.getText().toString().trim();
        if (uInvET != null){
            final String inv = uInvET.getText().toString().trim();//邀请码
            logindata = "user=" + user + "&password=" + passWord + "&inv=" + inv + "&markcode=" + Utils.GetAndroidID(this) + "&t=" + GetTimeStamp.timeStamp();
            if (TextUtils.isEmpty(inv)) {
                Utils.showToast(UserActivity.this, R.string.No_invitation_code, R.drawable.toast_err);
                return;
            }
        }else{
            logindata = "user=" + user + "&password=" + passWord + "&markcode=" + Utils.GetAndroidID(this) + "&t=" + GetTimeStamp.timeStamp();
        }
        int miType = SharePreferenceDataUtil.getSharedIntData(this, Constant.ue, 1);
        if (miType == 1) {
            data = Rc4.encry_RC4_string(logindata, RC4KEY);
        } else if (miType == 2) {
            try {
                data = Rsa.encrypt_Rsa(logindata, RSAKEY);
            } catch (Exception e) {
                e.printStackTrace();
            }
        } else if (miType == 3) {
            data = AES.encrypt_Aes(AESKEY,logindata, AESIV);
        }

        sign = Md5Encoder.encode(new StringBuilder(String.valueOf(logindata)).append("&").append(Appkey).toString());

        if (TextUtils.isEmpty(user)) {
            Utils.showToast(UserActivity.this, R.string.No_account_entered , R.drawable.toast_err);
            return;
        }
        if (TextUtils.isEmpty(passWord)) {
            Utils.showToast(UserActivity.this, R.string.No_password_entered , R.drawable.toast_err);
            return;
        }else {
            Utils.loadingShow_tv(context, R.string.is_registing);
            StringRequest stringRequest = new StringRequest(Request.Method.POST, URL,
                    new com.android.volley.Response.Listener<String>() {
                        public void onResponse(String response) {
                            RegeditResponse(response,uNameET,uPassET);
                        }
                    }, new com.android.volley.Response.ErrorListener() {
                public void onErrorResponse(VolleyError error) {
                    Error(error);
                }
            }) {
                @Override
                protected Map<String, String> getParams() throws AuthFailureError {
                    Map<String, String> params = new HashMap<>();
                    params.put("data", data);
                    params.put("sign", sign);
                    return params;
                }

                @Override
                public Map<String, String> getHeaders() throws AuthFailureError {
                    Map<String, String> headers = new HashMap<>();
                    headers.put("Authorization", Rc4.decry_RC4(SharePreferenceDataUtil.getSharedStringData(UserActivity.this, "Authorization", ""),Constant.d));  /*设置其他请求头*/
                    return headers;
                }
            };
            mQueue.add(stringRequest);
        }
    }

    /*注册响应成功*/
    public void RegeditResponse(String response, EditText uNameET, EditText uPassET) {
        //Log.i(TAG, "RegeditResponse: " + response);
        final String User_url = Rc4.decry_RC4(SharePreferenceDataUtil.getSharedStringData(this, Constant.s, ""),Constant.d);
        String RC4KEY = Rc4.decry_RC4(SharePreferenceDataUtil.getSharedStringData(this, Constant.kd, ""),Constant.d);
        String RSAKEY = Rc4.decry_RC4(SharePreferenceDataUtil.getSharedStringData(this, Constant.tb, ""),Constant.d);
        String AESKEY = Rc4.decry_RC4(SharePreferenceDataUtil.getSharedStringData(this, Constant.um, ""),Constant.d);
        String AESIV = Rc4.decry_RC4(SharePreferenceDataUtil.getSharedStringData(this, Constant.im, ""),Constant.d);
        Utils.loadingClose_Tv();
        try {
            JSONObject jSONObject = new JSONObject(response);
            int code = jSONObject.optInt("code");
            if (code == 200){
                Login(uNameET, uPassET, User_url + "/api?app=" + Api.APPID + "&act=user_logon");
            }else{
                int miType = SharePreferenceDataUtil.getSharedIntData(this, Constant.ue, 1);
                if (miType == 1) {
                    Msg = URLDecoder.decode(Rc4.decry_RC4(jSONObject.optString("msg"),RC4KEY), "UTF-8") ;
                } else if (miType == 2) {
                    Msg = URLDecoder.decode(Rsa.decrypt_Rsa(jSONObject.optString("msg"),RSAKEY), "UTF-8") ;

                } else if (miType == 3) {
                    Msg = URLDecoder.decode(AES.decrypt_Aes(AESKEY,jSONObject.optString("msg"),AESIV),"UTF-8");
                }
                mediaHandler.sendEmptyMessage(9);
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /*验证找回密码*/
    private void showUserDialogpwd() {
        final String User_url = Rc4.decry_RC4(SharePreferenceDataUtil.getSharedStringData(this, Constant.s, ""),Constant.d);
        Builder builder = new Builder(context);
        View mView = View.inflate(context, R.layout.forget_form, null);
        final EditText user_phone_et = (EditText) mView.findViewById(R.id.user_phone_et);//邮箱号
        final EditText user_code_et = (EditText) mView.findViewById(R.id.user_code_et);//验证码
        send_code_bt = (Button) mView.findViewById(R.id.send_code_bt);//获取验证码
        final EditText user_new_pass_et = (EditText) mView.findViewById(R.id.user_new_pass_et);//新密码
        builder.setContentView(mView);
        builder.setPositiveButton(R.string.popup_confirmation_dialog_Positive, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                final String email = user_phone_et.getText().toString().trim();
                final String send_code = user_code_et.getText().toString().trim();
                if (TextUtils.isEmpty(email)) {
                    Utils.showToast(UserActivity.this, R.string.No_email_phone, R.drawable.toast_err);
                }else if (email.length() <= 5) {
                    Utils.showToast(UserActivity.this,  R.string.Incorrect_length, R.drawable.toast_err);
                }else if (!Utils.isEmail(email)) {
                    Utils.showToast(UserActivity.this, R.string.Incorrect_format, R.drawable.toast_err);
                }else if(TextUtils.isEmpty(send_code)) {
                    Utils.showToast(UserActivity.this, R.string.verification_code_empty, R.drawable.toast_err);
                }else{
                    /*请求新密码*/
                    seek_pass(user_phone_et,user_code_et,user_new_pass_et, User_url + "/api?app=" + Api.APPID + "&act=seek_pass");
                }
            }
        });
        builder.setNeutralButton(R.string.popup_confirmation_dialog_Negative, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });
        /*发送验证码*/
        send_code_bt.setOnClickListener(new OnClickListener() {
            public void onClick(View arg0) {
                final String email = user_phone_et.getText().toString().trim();
                if (TextUtils.isEmpty(email)) {
                    Utils.showToast(UserActivity.this, R.string.No_email_phone, R.drawable.toast_err);
                }else if (email.length() <= 5) {
                    Utils.showToast(UserActivity.this, R.string.Incorrect_length, R.drawable.toast_err);
                }else if (!Utils.isEmail(email)) {
                    Utils.showToast(UserActivity.this,  R.string.Incorrect_format, R.drawable.toast_err);
                }else{
                    send_code(user_phone_et, User_url + "/api?app=" + Api.APPID + "&act=afcrc","seek");
                }
            }
        });
        mDialog = builder.creates();
        mDialog.show();
    }

    /*发送验证码请求*/
    private void send_code(EditText uNameET, String requestUrl, final String type) {
        mQueue = Volley.newRequestQueue(this, new ExtHttpStack());
        final String RC4KEY = Rc4.decry_RC4(SharePreferenceDataUtil.getSharedStringData(this, Constant.kd, ""),Constant.d);
        final String RSAKEY = Rc4.decry_RC4(SharePreferenceDataUtil.getSharedStringData(this, Constant.tb, ""),Constant.d);
        final String AESKEY = Rc4.decry_RC4(SharePreferenceDataUtil.getSharedStringData(this, Constant.um, ""),Constant.d);
        final String AESIV = Rc4.decry_RC4(SharePreferenceDataUtil.getSharedStringData(this, Constant.im, ""),Constant.d);
        final String Appkey = Rc4.decry_RC4(SharePreferenceDataUtil.getSharedStringData(this, Constant.yk, ""),Constant.d);
        String email = uNameET.getText().toString().trim();
        String logindata = "email=" + email + "&type=" + type + "&t=" + GetTimeStamp.timeStamp();
        int miType = SharePreferenceDataUtil.getSharedIntData(this, Constant.ue, 1);
        if (miType == 1) {
            data = Rc4.encry_RC4_string(logindata, RC4KEY);
        } else if (miType == 2) {
            try {
                data = Rsa.encrypt_Rsa(logindata, RSAKEY);
            } catch (Exception e) {
                e.printStackTrace();
            }
        } else if (miType == 3) {
            data = AES.encrypt_Aes(AESKEY,logindata, AESIV);
        }

        sign = Md5Encoder.encode(new StringBuilder(String.valueOf(logindata)).append("&").append(Appkey).toString());
        Utils.loadingShow_tv(context, R.string.loading);
        StringRequest stringRequest = new StringRequest(Request.Method.POST, requestUrl,
                new com.android.volley.Response.Listener<String>() {
                    public void onResponse(String response) {
                        CodeResponse(response);
                    }
                }, new com.android.volley.Response.ErrorListener() {
            public void onErrorResponse(VolleyError error) {
                Error(error);
            }
        }) {
            @Override
            protected Map<String, String> getParams() throws AuthFailureError {
                Map<String, String> params = new HashMap<>();
                params.put("data", data);
                params.put("sign", sign);
                return params;
            }

            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                Map<String, String> headers = new HashMap<>();
                headers.put("Authorization", Rc4.decry_RC4(SharePreferenceDataUtil.getSharedStringData(UserActivity.this, "Authorization", ""),Constant.d));  /*设置其他请求头*/
                return headers;
            }
        };
        mQueue.add(stringRequest);
    }

    /*验证码响应*/
    public void CodeResponse(String response) {
        Utils.loadingClose_Tv();
        //Log.i(TAG, "CodeResponse: " + response);
        String RC4KEY = Rc4.decry_RC4(SharePreferenceDataUtil.getSharedStringData(this, Constant.kd, ""),Constant.d);
        String RSAKEY = Rc4.decry_RC4(SharePreferenceDataUtil.getSharedStringData(this, Constant.tb, ""),Constant.d);
        String AESKEY = Rc4.decry_RC4(SharePreferenceDataUtil.getSharedStringData(this, Constant.um, ""),Constant.d);
        String AESIV = Rc4.decry_RC4(SharePreferenceDataUtil.getSharedStringData(this, Constant.im, ""),Constant.d);
        try {
            JSONObject jSONObject = new JSONObject(response);
            int code = jSONObject.optInt("code");
            int miType = SharePreferenceDataUtil.getSharedIntData(this, Constant.ue, 1);
            String msg = null;
            if (miType == 1) {
                msg = Rc4.decry_RC4(jSONObject.optString("msg"), RC4KEY);
            } else if (miType == 2) {
                msg = Rsa.decrypt_Rsa(jSONObject.optString("msg"), RSAKEY);
            } else if (miType == 3) {
                msg = AES.decrypt_Aes(AESKEY,jSONObject.optString("msg"), AESIV);
            }
            if (code == 200){
                String keyWord = URLDecoder.decode(msg, "UTF-8");
                Msg = keyWord;
                mediaHandler.sendEmptyMessage(8);
            }else{
                String keyWord = URLDecoder.decode(msg, "UTF-8");
                Msg = keyWord;
                mediaHandler.sendEmptyMessage(9);
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /*修改密码请求*/
    private void seek_pass(final EditText NameET, EditText codeET , final EditText passET, String requestUrl) {
        mQueue = Volley.newRequestQueue(this, new ExtHttpStack());
        final String RC4KEY = Rc4.decry_RC4(SharePreferenceDataUtil.getSharedStringData(this, Constant.kd, ""),Constant.d);
        final String RSAKEY = Rc4.decry_RC4(SharePreferenceDataUtil.getSharedStringData(this, Constant.tb, ""),Constant.d);
        final String AESKEY = Rc4.decry_RC4(SharePreferenceDataUtil.getSharedStringData(this, Constant.um, ""),Constant.d);
        final String AESIV = Rc4.decry_RC4(SharePreferenceDataUtil.getSharedStringData(this, Constant.im, ""),Constant.d);
        final String Appkey = Rc4.decry_RC4(SharePreferenceDataUtil.getSharedStringData(this, Constant.yk, ""),Constant.d);
        String email = NameET.getText().toString().trim();
        String code = codeET.getText().toString().trim();
        String pass = passET.getText().toString().trim();
        String logindata = "email=" + email + "&crc=" + code + "&newpassword=" + pass +"&t=" + GetTimeStamp.timeStamp();
        int miType = SharePreferenceDataUtil.getSharedIntData(this, Constant.ue, 1);
        if (miType == 1) {
            data = Rc4.encry_RC4_string(logindata, RC4KEY);
        } else if (miType == 2) {
            try {
                data = Rsa.encrypt_Rsa(logindata, RSAKEY);
            } catch (Exception e) {
                e.printStackTrace();
            }
        } else if (miType == 3) {
            data = AES.encrypt_Aes(AESKEY,logindata, AESIV);
        }

        sign = Md5Encoder.encode(new StringBuilder(String.valueOf(logindata)).append("&").append(Appkey).toString());
        Utils.loadingShow_tv(context, R.string.loading);
        StringRequest stringRequest = new StringRequest(Request.Method.POST, requestUrl,
                new com.android.volley.Response.Listener<String>() {
                    public void onResponse(String response) {
                        PassResponse(response,NameET,passET);
                    }
                }, new com.android.volley.Response.ErrorListener() {
            public void onErrorResponse(VolleyError error) {
                Error(error);
            }
        }) {
            @Override
            protected Map<String, String> getParams() throws AuthFailureError {
                Map<String, String> params = new HashMap<>();
                params.put("data", data);
                params.put("sign", sign);
                return params;
            }

            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                Map<String, String> headers = new HashMap<>();
                headers.put("Authorization", Rc4.decry_RC4(SharePreferenceDataUtil.getSharedStringData(UserActivity.this, "Authorization", ""),Constant.d));  /*设置其他请求头*/
                return headers;
            }
        };
        mQueue.add(stringRequest);
    }

    /*新密码响应*/
    public void PassResponse(String response,EditText NameET,EditText passET) {
        Utils.loadingClose_Tv();
        //Log.i(TAG, "PassResponse: " + response);
        final String User_url = Rc4.decry_RC4(SharePreferenceDataUtil.getSharedStringData(this, Constant.s, ""),Constant.d);
        String RC4KEY = Rc4.decry_RC4(SharePreferenceDataUtil.getSharedStringData(this, Constant.kd, ""),Constant.d);
        String RSAKEY = Rc4.decry_RC4(SharePreferenceDataUtil.getSharedStringData(this, Constant.tb, ""),Constant.d);
        String AESKEY = Rc4.decry_RC4(SharePreferenceDataUtil.getSharedStringData(this, Constant.um, ""),Constant.d);
        String AESIV = Rc4.decry_RC4(SharePreferenceDataUtil.getSharedStringData(this, Constant.im, ""),Constant.d);
        try {
            JSONObject jSONObject = new JSONObject(response);
            int code = jSONObject.optInt("code");
            int miType = SharePreferenceDataUtil.getSharedIntData(this, Constant.ue, 1);
            String msg = null;
            if (miType == 1) {
                msg = Rc4.decry_RC4(jSONObject.optString("msg"), RC4KEY);
            } else if (miType == 2) {
                msg = Rsa.decrypt_Rsa(jSONObject.optString("msg"), RSAKEY);
            } else if (miType == 3) {
                msg = AES.decrypt_Aes(AESKEY,jSONObject.optString("msg"), AESIV);
            }
            if (code == 200){
                String keyWord = URLDecoder.decode(msg, "UTF-8");
                Msg = keyWord;
                mediaHandler.sendEmptyMessage(8);
                if (mDialog != null && mDialog.isShowing()) {
                    mDialog.dismiss();
                }
                /*请求登录*/
                Login(NameET, passET, User_url + "/api?app=" + Api.APPID + "&act=user_logon");
            }else{
                String keyWord = URLDecoder.decode(msg, "UTF-8");
                Msg = keyWord;
                mediaHandler.sendEmptyMessage(9);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /*扫码登录*/
    private void showUserDialogempower() {
        Builder builder = new Builder(context);
        View mView = View.inflate(context, R.layout.forget_empower, null);
        users_empower = (ImageView) mView.findViewById(R.id.users_empower);
        builder.setContentView(mView);
        seek_empower();
        users_empower.setOnClickListener(new OnClickListener() {
            /*二维码框被单击*/
            public void onClick(View arg0) {
                /*刷新登录二维码*/
                seek_empower();//获取登录二维码地址
            }
        });
        mDialogs = builder.creates();
        mDialogs.show();
    }

    /*获取扫码登录二维码*/
    private void seek_empower() {
        mQueue = Volley.newRequestQueue(this, new ExtHttpStack());
        String User_url = Rc4.decry_RC4(SharePreferenceDataUtil.getSharedStringData(this, Constant.s, ""),Constant.d);
        String RC4KEY = Rc4.decry_RC4(SharePreferenceDataUtil.getSharedStringData(this, Constant.kd, ""),Constant.d);
        String RSAKEY = Rc4.decry_RC4(SharePreferenceDataUtil.getSharedStringData(this, Constant.tb, ""),Constant.d);
        String AESKEY = Rc4.decry_RC4(SharePreferenceDataUtil.getSharedStringData(this, Constant.um, ""),Constant.d);
        String AESIV = Rc4.decry_RC4(SharePreferenceDataUtil.getSharedStringData(this, Constant.im, ""),Constant.d);
        String Appkey = Rc4.decry_RC4(SharePreferenceDataUtil.getSharedStringData(this, Constant.yk, ""),Constant.d);
        String logindata = "markcode=" + Utils.GetAndroidID(this) + "&t=" + GetTimeStamp.timeStamp();
        int miType = SharePreferenceDataUtil.getSharedIntData(this, Constant.ue, 1);
        if (miType == 1) {
            data = Rc4.encry_RC4_string(logindata, RC4KEY);
        } else if (miType == 2) {
            try {
                data = Rsa.encrypt_Rsa(logindata, RSAKEY);
            } catch (Exception e) {
                e.printStackTrace();
            }
        } else if (miType == 3) {
            data = AES.encrypt_Aes(AESKEY,logindata, AESIV);
        }

        sign = Md5Encoder.encode(new StringBuilder(String.valueOf(logindata)).append("&").append(Appkey).toString());
        //Utils.loadingShow_tv(context, R.string.loading);
        StringRequest stringRequest = new StringRequest(Request.Method.POST, User_url + "/api?app=" + Api.APPID + "&act=empower_logon",
                new com.android.volley.Response.Listener<String>() {
                    public void onResponse(String response) {
                        EmpowerResponse(response);
                    }
                }, new com.android.volley.Response.ErrorListener() {
            public void onErrorResponse(VolleyError error) {
                Error(error);
            }
        }) {
            @Override
            protected Map<String, String> getParams() throws AuthFailureError {
                Map<String, String> params = new HashMap<>();
                params.put("data", data);
                params.put("sign", sign);
                return params;
            }

            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                Map<String, String> headers = new HashMap<>();
                headers.put("Authorization", Rc4.decry_RC4(SharePreferenceDataUtil.getSharedStringData(UserActivity.this, "Authorization", ""),Constant.d));  /*设置其他请求头*/
                return headers;
            }
        };
        mQueue.add(stringRequest);
    }

    /*二维码响应*/
    public void EmpowerResponse(String response) {
        //Utils.loadingClose_Tv();
        //Log.i(TAG, "EmpowerResponse: " + response);
        String RC4KEY = Rc4.decry_RC4(SharePreferenceDataUtil.getSharedStringData(this, Constant.kd, ""),Constant.d);
        String RSAKEY = Rc4.decry_RC4(SharePreferenceDataUtil.getSharedStringData(this, Constant.tb, ""),Constant.d);
        String AESKEY = Rc4.decry_RC4(SharePreferenceDataUtil.getSharedStringData(this, Constant.um, ""),Constant.d);
        String AESIV = Rc4.decry_RC4(SharePreferenceDataUtil.getSharedStringData(this, Constant.im, ""),Constant.d);
        try {
            JSONObject jSONObject = new JSONObject(response);
            int code = jSONObject.optInt("code");
            int miType = SharePreferenceDataUtil.getSharedIntData(this, Constant.ue, 1);
            String msg = null;
            if (miType == 1) {
                msg = Rc4.decry_RC4(jSONObject.optString("msg"), RC4KEY);
            } else if (miType == 2) {
                msg = Rsa.decrypt_Rsa(jSONObject.optString("msg"), RSAKEY);
            } else if (miType == 3) {
                msg = AES.decrypt_Aes(AESKEY,jSONObject.optString("msg"), AESIV);
            }
            if (code == 200) {
                JSONObject jSON = new JSONObject(msg);
                JSONObject jSONB = new JSONObject(jSON.getString("info"));
                empower_url = jSONB.getString("login_url");
                t = jSONB.getString("t");
                notify = jSONB.getString("notify");
                mediaHandler.sendEmptyMessage(7);
                if (notify != "" && notify != null){
                    mediaHandler.sendEmptyMessageDelayed(6, 3000);//3秒后开始查询扫码状态
                }
            }else {
                Msg = URLDecoder.decode(msg, "UTF-8");
                mediaHandler.sendEmptyMessage(9);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /*扫码回调*/
    private void login_notify() {
        mQueue = Volley.newRequestQueue(this, new ExtHttpStack());
        String RC4KEY = Rc4.decry_RC4(SharePreferenceDataUtil.getSharedStringData(this, Constant.kd, ""),Constant.d);
        String RSAKEY = Rc4.decry_RC4(SharePreferenceDataUtil.getSharedStringData(this, Constant.tb, ""),Constant.d);
        String AESKEY = Rc4.decry_RC4(SharePreferenceDataUtil.getSharedStringData(this, Constant.um, ""),Constant.d);
        String AESIV = Rc4.decry_RC4(SharePreferenceDataUtil.getSharedStringData(this, Constant.im, ""),Constant.d);
        String Appkey = Rc4.decry_RC4(SharePreferenceDataUtil.getSharedStringData(this, Constant.yk, ""),Constant.d);
        String logindata = "oin=" + Utils.GetAndroidID(this) + "&time=" + t + "&t=" + GetTimeStamp.timeStamp();
        int miType = SharePreferenceDataUtil.getSharedIntData(this, Constant.ue, 1);
        if (miType == 1) {
            data = Rc4.encry_RC4_string(logindata, RC4KEY);
        } else if (miType == 2) {
            try {
                data = Rsa.encrypt_Rsa(logindata, RSAKEY);
            } catch (Exception e) {
                e.printStackTrace();
            }
        } else if (miType == 3) {
            data = AES.encrypt_Aes(AESKEY,logindata, AESIV);
        }

        sign = Md5Encoder.encode(new StringBuilder(String.valueOf(logindata)).append("&").append(Appkey).toString());
        StringRequest stringRequest = new StringRequest(Request.Method.POST, notify,
                new com.android.volley.Response.Listener<String>() {
                    public void onResponse(String response) {
                        NotifyResponse(response);
                    }
                }, new com.android.volley.Response.ErrorListener() {
            public void onErrorResponse(VolleyError error) {
                Error(error);
            }
        }) {
            @Override
            protected Map<String, String> getParams() throws AuthFailureError {
                Map<String, String> params = new HashMap<>();
                params.put("data", data);
                params.put("sign", sign);
                return params;
            }

            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                Map<String, String> headers = new HashMap<>();
                headers.put("Authorization", Rc4.decry_RC4(SharePreferenceDataUtil.getSharedStringData(UserActivity.this, "Authorization", ""),Constant.d));  /*设置其他请求头*/
                return headers;
            }
        };
        mQueue.add(stringRequest);
    }

    /*回调响应*/
    public void NotifyResponse(String response) {
        //Log.i(TAG, "EmpowerResponse: " + response);
        String User_url = Rc4.decry_RC4(SharePreferenceDataUtil.getSharedStringData(this, Constant.s, ""),Constant.d);
        String RC4KEY = Rc4.decry_RC4(SharePreferenceDataUtil.getSharedStringData(this, Constant.kd, ""),Constant.d);
        String RSAKEY = Rc4.decry_RC4(SharePreferenceDataUtil.getSharedStringData(this, Constant.tb, ""),Constant.d);
        String AESKEY = Rc4.decry_RC4(SharePreferenceDataUtil.getSharedStringData(this, Constant.um, ""),Constant.d);
        String AESIV = Rc4.decry_RC4(SharePreferenceDataUtil.getSharedStringData(this, Constant.im, ""),Constant.d);
        try {
            JSONObject jo = new JSONObject(response);
            int code = jo.optInt("code");
            int miType = SharePreferenceDataUtil.getSharedIntData(this, Constant.ue, 1);
            String msg = null;
            if (miType == 1) {
                // RC4加密，需要解密
                msg = Rc4.decry_RC4(jo.optString("msg"), RC4KEY);
            } else if (miType == 2) {
                // RSA加密，需要解密
                msg = Rsa.decrypt_Rsa(jo.optString("msg"), RSAKEY);
            } else if (miType == 3) {
                // AES加密，需要解密
                msg = AES.decrypt_Aes(AESKEY,jo.optString("msg"), AESIV);
            } else {
                // miType == 0，不加密，直接使用原始msg（已经是URL编码的）
                msg = jo.optString("msg");
            }
            if (code == 200) {
                // 成功响应，解析JSON
                JSONObject jot = new JSONObject(msg);
                String user = jot.getString("user");
                String pwd = jot.getString("pwd");
                if (mDialogs != null && mDialogs.isShowing()) {
                    // 修复：使用equals()方法比较字符串，而不是!=运算符
                    // 在Java中，!=比较的是引用，不是字符串内容
                    if (!"null".equals(user) && !"null".equals(pwd)){
                        mDialogs.dismiss();
                        Login(user,pwd, User_url + "/api?app=" + Api.APPID + "&act=user_logon");
                    }else{
                        mediaHandler.sendEmptyMessageDelayed(6, 3000);//3秒后开始查询扫码状态
                    }
                }
            }else {
                // 错误响应，处理错误消息
                if (msg != null && msg.length() > 0) {
                    try {
                        // URL解码（后端使用encode函数进行了URL编码）
                        Msg = URLDecoder.decode(msg, "UTF-8");
                    } catch (Exception e) {
                        // 如果解码失败，直接使用原始消息
                        Msg = msg;
                    }
                } else {
                    // 如果msg为null或空，使用默认错误消息
                    Msg = "请求失败，请重试";
                }
                mediaHandler.sendEmptyMessage(9);
                // 修复：code!=200时不应该关闭扫码对话框，让前端继续轮询
                // 只有在真正的错误时才关闭对话框
                // if (mDialogs != null && mDialogs.isShowing()) {
                //     mDialogs.dismiss();
                // }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    /*扫码成功请求登录*/
    private void Login(String user,final String pwd, String requestUrl) {
        mQueue = Volley.newRequestQueue(this, new ExtHttpStack());
        final String RC4KEY = Rc4.decry_RC4(SharePreferenceDataUtil.getSharedStringData(this, Constant.kd, ""),Constant.d);
        final String RSAKEY = Rc4.decry_RC4(SharePreferenceDataUtil.getSharedStringData(this, Constant.tb, ""),Constant.d);
        final String AESKEY = Rc4.decry_RC4(SharePreferenceDataUtil.getSharedStringData(this, Constant.um, ""),Constant.d);
        final String AESIV = Rc4.decry_RC4(SharePreferenceDataUtil.getSharedStringData(this, Constant.im, ""),Constant.d);
        final String Appkey = Rc4.decry_RC4(SharePreferenceDataUtil.getSharedStringData(this, Constant.yk, ""),Constant.d);
        userName = user;
        String logindata = "account=" + userName + "&password=" + pwd + "&markcode=" + Utils.GetAndroidID(this) + "&t=" + GetTimeStamp.timeStamp();
        int miType = SharePreferenceDataUtil.getSharedIntData(this, Constant.ue, 1);
        if (miType == 1) {
            data = Rc4.encry_RC4_string(logindata, RC4KEY);
        } else if (miType == 2) {
            try {
                data = Rsa.encrypt_Rsa(logindata, RSAKEY);
            } catch (Exception e) {
                e.printStackTrace();
            }
        } else if (miType == 3) {
            data = AES.encrypt_Aes(AESKEY,logindata, AESIV);
        }

        sign = Md5Encoder.encode(new StringBuilder(String.valueOf(logindata)).append("&").append(Appkey).toString());
        StringRequest stringRequest = new StringRequest(Request.Method.POST, requestUrl,
                new com.android.volley.Response.Listener<String>() {
                    public void onResponse(String response) {
                        empowerResponse(response,pwd);
                    }
                }, new com.android.volley.Response.ErrorListener() {
            public void onErrorResponse(VolleyError error) {
                Error(error);
            }
        }) {
            @Override
            protected Map<String, String> getParams() throws AuthFailureError {
                Map<String, String> params = new HashMap<>();
                params.put("data", data);
                params.put("sign", sign);
                return params;
            }

            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                Map<String, String> headers = new HashMap<>();
                headers.put("Authorization", Rc4.decry_RC4(SharePreferenceDataUtil.getSharedStringData(UserActivity.this, "Authorization", ""),Constant.d));  /*设置其他请求头*/
                return headers;
            }
        };
        mQueue.add(stringRequest);

    }

    /*扫码登录响应成功*/
    public void empowerResponse(String response,String passWord) {
        //Log.i(TAG, "empowerResponse: " + response);
        String RC4KEY = Rc4.decry_RC4(SharePreferenceDataUtil.getSharedStringData(this, Constant.kd, ""),Constant.d);
        String RSAKEY = Rc4.decry_RC4(SharePreferenceDataUtil.getSharedStringData(this, Constant.tb, ""),Constant.d);
        String AESKEY = Rc4.decry_RC4(SharePreferenceDataUtil.getSharedStringData(this, Constant.um, ""),Constant.d);
        String AESIV = Rc4.decry_RC4(SharePreferenceDataUtil.getSharedStringData(this, Constant.im, ""),Constant.d);
        Utils.loadingClose_Tv();
        try {
            JSONObject jSONObject = new JSONObject(response);
            int code = jSONObject.optInt("code");
            if (code == 200){
                int miType = SharePreferenceDataUtil.getSharedIntData(this, Constant.ue, 1);
                JSONObject msg = null;
                if (miType == 1) {
                    msg = new JSONObject(Rc4.decry_RC4(jSONObject.optString("msg"),RC4KEY));
                } else if (miType == 2) {
                    msg = new JSONObject(Rsa.decrypt_Rsa(jSONObject.optString("msg"),RSAKEY));
                } else if (miType == 3) {
                    msg = new JSONObject(AES.decrypt_Aes(AESKEY,jSONObject.optString("msg"), AESIV));
                }
                String token = msg.optString("token");
                JSONObject info = msg.getJSONObject("info");
                String id = info.getString("id");
                String pic = info.getString("pic");
                String name = info.getString("name");
                String vip = info.getString("vip");
                String fen = info.getString("fen");
                String email = info.getString("email");
                String user = info.getString("user");
                int phone = info.getInt("phone");
                tv_user_name.setText(name);
                tv_no_data.setText(getString(R.string.user) + ":" + user + getString(R.string.Logged_in));
                if (email.equals("")){
                    type = "邮箱";
                    mediaHandler.sendEmptyMessage(5);
                }
                if (phone == 0){
                    type = "手机";
                    mediaHandler.sendEmptyMessage(5);
                }
                sp.edit()
                        .putString("userName", user)/*帐号*/
                        .putString("passWord", passWord)/*密码*/
                        .putString("ckinfo", token)/*token*/
                        .putString("vip", vip)/*vip时间*/
                        .putString("fen", fen)/*积分数量*/
                        .commit();
                if (mDialog != null && mDialog.isShowing()) {
                    mDialog.dismiss();
                }
            }else{
                int miType = SharePreferenceDataUtil.getSharedIntData(this, Constant.ue, 1);
                if (miType == 1) {
                    Msg = URLDecoder.decode(Rc4.decry_RC4(jSONObject.optString("msg"),RC4KEY), "UTF-8");
                } else if (miType == 2) {
                    Msg = URLDecoder.decode(Rsa.decrypt_Rsa(jSONObject.optString("msg"),RSAKEY), "UTF-8");
                } else if (miType == 3) {
                    Msg = URLDecoder.decode(AES.decrypt_Aes(AESKEY,jSONObject.optString("msg"), AESIV), "UTF-8");
                }
                mediaHandler.sendEmptyMessage(9);
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /*绑定提示*/
    private void showUpdateDialog(Context conetxt,final String type){
        HomeDialog.Builder builder = new HomeDialog.Builder(conetxt);
        builder.setTitle(R.string.Tips);
        builder.setMessage(getString(R.string.Detecting_accounts) + type + getString(R.string.blackspot));
        builder.setPositiveButton(R.string.binding, new DialogInterface.OnClickListener()
        {
            public void onClick(DialogInterface dialog, int which)
            {
                showUserDialogbinding(type);
                dialog.dismiss();
            }
        });
        builder.setNeutralButton(R.string.popup_confirmation_dialog_Negative, new DialogInterface.OnClickListener()
        {
            public void onClick(DialogInterface dialog, int which)
            {
                dialog.dismiss();
                finish();
            }
        });
        builder.creates().show();
    }

    /*绑定邮箱*/
    private void showUserDialogbinding(final String type) {
        final String User_url = Rc4.decry_RC4(SharePreferenceDataUtil.getSharedStringData(this, Constant.s, ""),Constant.d);
        Builder builder = new Builder(context);
        View mView = View.inflate(context, R.layout.binding_form, null);//绑定邮箱
        final TextView bind_name_et = (TextView) mView.findViewById(R.id.bind_name_et);//帐号
        final EditText bind_phone_et = (EditText) mView.findViewById(R.id.bind_phone_et);//邮箱号
        final EditText bind_code_et = (EditText) mView.findViewById(R.id.bind_code_et);//验证码
        send_code_bt = (Button) mView.findViewById(R.id.send_code_bt);//获取验证码
        String string = sp.getString("userName", null);
        bind_name_et.setText("" + string);

        builder.setContentView(mView);
        builder.setPositiveButton(R.string.popup_confirmation_dialog_Positive, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                String trim = bind_name_et.getText().toString().trim();
                final String email = bind_phone_et.getText().toString().trim();
                final String send_code = bind_code_et.getText().toString().trim();
                if (TextUtils.isEmpty(trim)) {
                    Utils.showToast(UserActivity.this, R.string.No_account_entered, R.drawable.toast_err);
                } else if (TextUtils.isEmpty(email)) {
                    Utils.showToast(UserActivity.this, getString(R.string.no_input) + type, R.drawable.toast_err);
                }else if (email.length() <= 5) {
                    Utils.showToast(UserActivity.this, type + getString(R.string.Length_Error), R.drawable.toast_err);
                }else if (!Utils.isEmail(email)) {
                    Utils.showToast(UserActivity.this, type + getString(R.string.format_error), R.drawable.toast_err);
                }else if(TextUtils.isEmpty(send_code)) {
                    Utils.showToast(UserActivity.this, R.string.Verification_code_error, R.drawable.toast_err);
                }else{
                    email_bind(bind_phone_et,bind_code_et, User_url + "/api?app=" + Api.APPID + "&act=email_bind");
                }
            }
        });
        builder.setNeutralButton(R.string.popup_confirmation_dialog_Negative, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });

        /*验证码按钮*/
        send_code_bt.setOnClickListener(new OnClickListener() {
            public void onClick(View arg0) {
                String trim = bind_name_et.getText().toString().trim();
                final String email = bind_phone_et.getText().toString().trim();
                if (TextUtils.isEmpty(trim)) {
                    Utils.showToast(UserActivity.this, R.string.No_account_entered, R.drawable.toast_err);
                } else if (TextUtils.isEmpty(email)) {
                    Utils.showToast(UserActivity.this, getString(R.string.no_input) + type, R.drawable.toast_err);
                }else if (email.length() <= 5) {
                    Utils.showToast(UserActivity.this, type + getString(R.string.Length_Error), R.drawable.toast_err);
                }else if (!Utils.isEmail(email)) {
                    Utils.showToast(UserActivity.this, type + getString(R.string.format_error), R.drawable.toast_err);
                }else{
                    send_code(bind_phone_et, User_url + "/api?app=" + Api.APPID + "&act=afcrc","bind");
                }
            }
        });
        mDialog = builder.creates();
        mDialog.show();
    }

    /*绑定邮箱请求*/
    private void email_bind(EditText uNameET, EditText bind_code_et , String requestUrl) {
        mQueue = Volley.newRequestQueue(this, new ExtHttpStack());
        String RC4KEY = Rc4.decry_RC4(SharePreferenceDataUtil.getSharedStringData(this, Constant.kd, ""),Constant.d);
        String RSAKEY = Rc4.decry_RC4(SharePreferenceDataUtil.getSharedStringData(this, Constant.tb, ""),Constant.d);
        String AESKEY = Rc4.decry_RC4(SharePreferenceDataUtil.getSharedStringData(this, Constant.um, ""),Constant.d);
        String AESIV = Rc4.decry_RC4(SharePreferenceDataUtil.getSharedStringData(this, Constant.im, ""),Constant.d);
        String Appkey = Rc4.decry_RC4(SharePreferenceDataUtil.getSharedStringData(this, Constant.yk, ""),Constant.d);
        String email = uNameET.getText().toString().trim();
        String crc = bind_code_et.getText().toString().trim();
        String logindata = "token=" + sp.getString("ckinfo", null)+"&email=" + email + "&crc=" + crc + "&t=" + GetTimeStamp.timeStamp();
        int miType = SharePreferenceDataUtil.getSharedIntData(this, Constant.ue, 1);
        if (miType == 1) {
            data = Rc4.encry_RC4_string(logindata, RC4KEY);
        } else if (miType == 2) {
            try {
                data = Rsa.encrypt_Rsa(logindata, RSAKEY);
            } catch (Exception e) {
                e.printStackTrace();
            }
        } else if (miType == 3) {
            data = AES.encrypt_Aes(AESKEY,logindata, AESIV);
        }
        sign = Md5Encoder.encode(new StringBuilder(String.valueOf(logindata)).append("&").append(Appkey).toString());
        Utils.loadingShow_tv(context, R.string.loading);
        StringRequest stringRequest = new StringRequest(Request.Method.POST, requestUrl,
                new com.android.volley.Response.Listener<String>() {
                    public void onResponse(String response) {
                        BindResponse(response);
                    }
                }, new com.android.volley.Response.ErrorListener() {
            public void onErrorResponse(VolleyError error) {
                Error(error);
            }
        }) {
            @Override
            protected Map<String, String> getParams() throws AuthFailureError {
                Map<String, String> params = new HashMap<>();
                params.put("data", data);
                params.put("sign", sign);
                return params;
            }

            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                Map<String, String> headers = new HashMap<>();
                headers.put("Authorization", Rc4.decry_RC4(SharePreferenceDataUtil.getSharedStringData(UserActivity.this, "Authorization", ""),Constant.d));  /*设置其他请求头*/
                return headers;
            }
        };
        mQueue.add(stringRequest);
    }

    /*绑定邮箱响应*/
    public void BindResponse(String response) {
        Utils.loadingClose_Tv();
        //Log.i(TAG, "BindResponse: " + response);
        String RC4KEY = Rc4.decry_RC4(SharePreferenceDataUtil.getSharedStringData(this, Constant.kd, ""),Constant.d);
        String RSAKEY = Rc4.decry_RC4(SharePreferenceDataUtil.getSharedStringData(this, Constant.tb, ""),Constant.d);
        String AESKEY = Rc4.decry_RC4(SharePreferenceDataUtil.getSharedStringData(this, Constant.um, ""),Constant.d);
        String AESIV = Rc4.decry_RC4(SharePreferenceDataUtil.getSharedStringData(this, Constant.im, ""),Constant.d);
        try {
            JSONObject jo = new JSONObject(response);
            int code = jo.optInt("code");
            int miType = SharePreferenceDataUtil.getSharedIntData(this, Constant.ue, 1);
            String msg = null;
            if (miType == 1) {
                msg = Rc4.decry_RC4(jo.optString("msg"), RC4KEY);
            } else if (miType == 2) {
                msg = Rsa.decrypt_Rsa(jo.optString("msg"), RSAKEY);
            } else if (miType == 3) {
                msg = AES.decrypt_Aes(AESKEY,jo.optString("msg"), AESIV);
            }
            if (code == 200) {
                Msg = URLDecoder.decode(msg, "UTF-8");
                clearTimer();
                mediaHandler.sendEmptyMessage(8);
                if (mDialog != null && mDialog.isShowing()) {
                    mDialog.dismiss();
                }
            }else{
                Msg = URLDecoder.decode(msg, "UTF-8");
                mediaHandler.sendEmptyMessage(9);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /*响应失败*/
    public void Error(VolleyError volleyError) {
        Utils.loadingClose_Tv();
        //Log.i(TAG, "RequestError: " + volleyError);
        if (volleyError instanceof TimeoutError) {
            //Utils.showToast(UserActivity.this, "糟糕,服务器请求没成功", R.drawable.toast_shut);
//            System.out.println("请求超时");
        }
        if (volleyError instanceof AuthFailureError) {
            //Utils.showToast(UserActivity.this, "糟糕,服务器请求没成功", R.drawable.toast_shut);
            //System.out.println("身份验证失败错误");
        }
        if(volleyError instanceof NetworkError) {
            //Utils.showToast(UserActivity.this, "糟糕,服务器请求没成功", R.drawable.toast_shut);
//            System.out.println("请检查网络");
        }
        if(volleyError instanceof ServerError) {
            //Utils.showToast(UserActivity.this, "糟糕,服务器请求没成功", R.drawable.toast_shut);
            //System.out.println("错误404");
        }

    }

    /*设置计时器*/
    private class MyCountDownTimer extends CountDownTimer {
        public MyCountDownTimer(long millisInFuture, long countDownInterval) {
            super(millisInFuture, countDownInterval);
        }
        //计时过程
        @Override
        public void onTick(long millisInFuture) {
            //防止计时过程中重复点击
            send_code_bt.setFocusable(false);
            send_code_bt.setEnabled(false);
            send_code_bt.setText(millisInFuture /1000 + "秒");
        }
        //计时完毕的方法
        @Override
        public void onFinish() {
            //重新给Button设置文字
            send_code_bt.setText(R.string.send_code);
            //设置可点击
            send_code_bt.setFocusable(true);
            send_code_bt.setEnabled(true);
            clearTimer();
        }
    }

    /*清除计时器*/
    private void clearTimer() {
        if (myCountDownTimer != null) {
            myCountDownTimer.cancel();
            myCountDownTimer = null;
        }
    }

    /**
     * 自定义目录显示数据列表
     * 优化：使用ViewHolder模式，避免重复inflate和findViewById
     *
     * @return
     */
    class MyAdapter extends BaseAdapter {
        ArrayList<String> mylist;
        private Context context;
        private LayoutInflater mInflater;

        public MyAdapter(Context context, ArrayList<String> mylist) {
            this.context = context;
            this.mylist = mylist;
            this.mInflater = LayoutInflater.from(context);
        }

        @Override
        public int getCount() {
            return mylist.size();
        }

        @Override
        public Object getItem(int position) {
            return mylist.get(position);
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            // 优化：使用ViewHolder模式
            ViewHolder holder;
            if (convertView == null) {
                convertView = mInflater.inflate(R.layout.mv_controler_menu_item, parent, false);
                holder = new ViewHolder();
                holder.textView = (TextView) convertView.findViewById(R.id.tv_menu_item);
                convertView.setTag(holder);
            } else {
                holder = (ViewHolder) convertView.getTag();
            }
            holder.textView.setText(mylist.get(position));
            return convertView;
        }
        
        // 优化：ViewHolder内部类（非静态内部类中不能定义静态内部类）
        class ViewHolder {
            TextView textView;
        }
    }

}
