package com.shenma.tvlauncher;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnKeyListener;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemLongClickListener;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.BaseAdapter;
import android.widget.GridView;
import android.widget.LinearLayout;
import android.widget.LinearLayout.LayoutParams;
import android.widget.ListView;
import android.widget.PopupWindow;
import android.widget.TextView;


import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.HurlStack;
import com.shenma.tvlauncher.network.ExtHttpStack;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.shenma.tvlauncher.adapter.UserTypeAdapter;
import com.shenma.tvlauncher.utils.AES;
import com.shenma.tvlauncher.utils.GetTimeStamp;
import com.shenma.tvlauncher.utils.Logger;
import com.shenma.tvlauncher.utils.Md5Encoder;
import com.shenma.tvlauncher.utils.Rc4;
import com.shenma.tvlauncher.utils.Rsa;
import com.shenma.tvlauncher.utils.SharePreferenceDataUtil;
import com.shenma.tvlauncher.utils.Utils;
import com.shenma.tvlauncher.vod.VideoDetailsActivity;
import com.shenma.tvlauncher.vod.dao.VodDao;
import com.shenma.tvlauncher.vod.db.Album;

import com.shenma.tvlauncher.utils.Constant;

import org.json.JSONException;
import org.json.JSONObject;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author joychang
 * @Description 历史记录
 */

public class HistoryActivity extends BaseActivity {
    private final String TAG = "HistoryActivity";
    protected SharedPreferences sp;
    public static SharedPreferences Sp;
    protected ImageLoader imageLoader = ImageLoader.getInstance();
    private List<Album> Albumls = null;
    private Boolean ISAPP = Boolean.valueOf(true);
//    private int USER_TYPE;
    private VodDao dao;
    private GridView history_grid;
    private LinearLayout ll_type_details;
    private MyAdapter mAdapter = null;
    private int mPosition = -1;
    private ListView menulist;
    private PopupWindow menupopupWindow;
    private TextView type_details_sum;
    private UserTypeAdapter userTypeAdapter = null;
    private TextView tv_no_data;
    private Boolean isDestroy = Boolean.valueOf(false);
    private TextView history_details_sum;
    private int vipstate;
    private int trystate = SharePreferenceDataUtil.getSharedIntData(this, "Trystate", 0);
    public RequestQueue mQueue;
    private final Handler mediaHandler = new Handler() {
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case 1:
                    Utils.showToast(context, R.string.request_failure, R.drawable.toast_err);
                    return;
                case 2:
                    Utils.showToast(context,R.string.Account_expiration, R.drawable.toast_err);
                    startActivity(new Intent(context, EmpowerActivity.class));
                    return;
                case 3:
                    Utils.showToast(context,R.string.disconnect, R.drawable.toast_err);
                    startActivity(new Intent(context, UserActivity.class));
                    return;
                case 4:
                    Utils.showToast(context,R.string.Account_has_been_disabled, R.drawable.toast_err);
                    startActivity(new Intent(context, UserActivity.class));
                    return;
                case 5:
                    Utils.showToast(context,R.string.request_failures, R.drawable.toast_shut);
                    return;
                case 6:
                    Utils.showToast(context,R.string.Account_information_has_expired, R.drawable.toast_err);
                    startActivity(new Intent(context, UserActivity.class));
                    return;
                case 7:
                    Utils.showToast(context,R.string.Account_information_error, R.drawable.toast_err);
                    startActivity(new Intent(context, UserActivity.class));
                    return;
                case 8:
                    Utils.showToast(context,R.string.Please_log_in_to_your_account_first, R.drawable.toast_err);
                    startActivity(new Intent(context, UserActivity.class));
                    return;
                default:
                    return;
            }
        }
    };

    /**
     * 退出后台事件处理线程
     */
    protected void onDestroy() {
        super.onDestroy();
        isDestroy = Boolean.valueOf(true);
        overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
        //Logger.d(TAG, "onDestroy");
    }

    @Override
    public void onResume() {
        super.onResume();
        initData();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_history);
        dao = new VodDao(this);
        initView();
        initData();
        sp = getSharedPreferences("shenma", 0);
        Sp = getSharedPreferences("initData", MODE_PRIVATE);
    }

    //初始化数据
    private void initData() {
        ISAPP = Boolean.valueOf(false);
        Albumls = dao.queryAllAppsByType(2);
        //Collections.reverse(Albumls);//将影片内容倒序
        if (Albumls == null || Albumls.size() <= 0) {
            tv_no_data.setVisibility(View.VISIBLE);
            tv_no_data.setText(R.string.No_record);
            if (userTypeAdapter != null) {
                userTypeAdapter.clearDatas();
                userTypeAdapter.notifyDataSetChanged();
                return;
            }
            return;
        }
        history_details_sum.setText(getString(R.string.common) + " " + Albumls.size() + " " + getString(R.string.Film));
        tv_no_data.setVisibility(View.GONE);
        userTypeAdapter = new UserTypeAdapter(this, Albumls, imageLoader, ISAPP);
        if (history_grid.getVisibility() != View.VISIBLE) {
            history_grid.setVisibility(View.VISIBLE);
        }
        history_grid.setAdapter(userTypeAdapter);
    }

    protected void setListener() {
        history_grid.setOnItemSelectedListener(new OnItemSelectedListener() {
            public void onItemSelected(AdapterView<?> adapterView, View v, int position, long arg3) {
                mPosition = position;
            }

            public void onNothingSelected(AdapterView<?> adapterView) {
            }
        });
        history_grid.setOnItemLongClickListener(new OnItemLongClickListener() {
            public boolean onItemLongClick(AdapterView<?> adapterView, View arg1, int position, long arg3) {
                if (!ISAPP.booleanValue()) {
                    mPosition = position;
                    showMenu();
                }
                return true;
            }
        });
        history_grid.setOnItemClickListener(new OnItemClickListener() {
            public void onItemClick(AdapterView<?> adapterView, View v, int position, long arg3) {
                String username = sp.getString("userName", null);
                if (username != null) {
                    GetMotion(position);
                } else if (username == null) {
                    mediaHandler.sendEmptyMessage(8);
                    overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
                }
            }
        });
    }

    //初始化menu
    private void showMenu() {
        //System.out.println("menu1");
        if (menupopupWindow != null) {
            //System.out.println("menu2");
            mAdapter = new MyAdapter(this, Utils.getUserData(0));
            menulist.setAdapter(mAdapter);
            menupopupWindow.setAnimationStyle(R.style.AnimationMenu);
            menupopupWindow.showAtLocation(ll_type_details, 53, 0, 0);
            menupopupWindow.update(0, 0, getResources().getDimensionPixelSize(R.dimen.sm_350), mHeight);
        }
    }

    //隐藏meun
    private void hideMenu() {
        if (menupopupWindow.isShowing()) {
            menupopupWindow.dismiss();
        }
    }

    protected void initView() {
        findViewById();
        loadViewLayout();
        setListener();
    }

    protected void loadViewLayout() {
        onCreateMenu();
    }

    protected void findViewById() {
        findViewById(R.id.vod_histiry).setBackgroundResource(R.drawable.video_details_bg);
        history_details_sum = findViewById(R.id.history_details_sum);
        tv_no_data = findViewById(R.id.tv_no_data);
        history_grid = findViewById(R.id.history_grid);
        history_grid.setSelector(new ColorDrawable(0));
        ll_type_details = findViewById(R.id.ll_type_details);
    }

    @SuppressLint("RestrictedApi")
    @Override
    public boolean dispatchKeyEvent(KeyEvent event) {
        int keyCode = event.getKeyCode();
        switch (keyCode) {
            case KeyEvent.KEYCODE_MENU:
                hideMenu();
                break;
        }
        return super.dispatchKeyEvent(event);
    }

    /**
     * 初始化menu
     */
    public void onCreateMenu() {
        View menuView = View.inflate(this, R.layout.mv_controler_menu, null);
        menulist = menuView.findViewById(R.id.media_controler_menu);
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
                            Albumls = dao.queryAllAppsByType(album.getTypeId());
                            userTypeAdapter.remove(mPosition);
                            userTypeAdapter.notifyDataSetChanged();
                            //user_type_details_sum.setText("共"+userTypeAdapter.vodDatas.size()+"部");
                        } else {
                            Utils.showToast(HistoryActivity.this, R.string.No_selection, R.drawable.toast_smile);
                        }
                        mPosition = -1;
                        hideMenu();
                        break;
                    //全部删除
                    case 1:
                        Albumls = dao.queryAllAppsByType(Constant.TYPE_LS);
                        if (null != Albumls && Albumls.size() > 0) {
                            userTypeAdapter.clearDatas();
                            userTypeAdapter.notifyDataSetChanged();
                        } else {
                            Utils.showToast(HistoryActivity.this, R.string.No_deletion, R.drawable.toast_shut);
                        }
                        dao.deleteAllByWhere(Constant.TYPE_LS);
                        mPosition = -1;
                        hideMenu();
                        break;
                }
            }

        });
    }

    /**
     * 自定义目录显示数据列表
     *
     * @return
     */
    class MyAdapter extends BaseAdapter {
        private final Context context;
        ArrayList<String> mylist;

        public MyAdapter(Context context, ArrayList<String> mylist) {
            this.context = context;
            this.mylist = mylist;
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
            View v = LayoutInflater.from(context).inflate(R.layout.mv_controler_menu_item, null);
            TextView tv = v.findViewById(R.id.tv_menu_item);
            tv.setText(mylist.get(position));
            return v;
        }

    }

    /*心跳*/
    private void GetMotion(final int position) {
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        while (true) {
            try {
                if (new Date(System.currentTimeMillis()).getTime() < simpleDateFormat.parse(GetTimeStamp.timeStamp2Date(sp.getString("vip", null), "")).getTime() || sp.getString("vip", null).equals("999999999")) {
                    vipstate = 1;/*没到期*/
                } else {
                    vipstate = 0;/*已到期*/
                }
                mQueue = Volley.newRequestQueue(this, new ExtHttpStack());
                String User_url = Rc4.decry_RC4(SharePreferenceDataUtil.getSharedStringData(this, Constant.s, ""),Constant.d);
                final String RC4KEY = Rc4.decry_RC4(SharePreferenceDataUtil.getSharedStringData(this, Constant.kd, ""),Constant.d);
                final String RSAKEY = Rc4.decry_RC4(SharePreferenceDataUtil.getSharedStringData(this, Constant.tb, ""),Constant.d);
                final String AESKEY = Rc4.decry_RC4(SharePreferenceDataUtil.getSharedStringData(this, Constant.um, ""),Constant.d);
                final String AESIV = Rc4.decry_RC4(SharePreferenceDataUtil.getSharedStringData(this, Constant.im, ""),Constant.d);
                final String Appkey = Rc4.decry_RC4(SharePreferenceDataUtil.getSharedStringData(this, Constant.yk, ""),Constant.d);
                int miType = SharePreferenceDataUtil.getSharedIntData(this, Constant.ue, 1);
                StringRequest stringRequest = new StringRequest(Request.Method.POST, User_url + "/api?app=" + Api.APPID + "&act=motion",
                        new com.android.volley.Response.Listener<String>() {
                            public void onResponse(String response) {
                                GetMotionResponse(response,position);
                            }
                        }, new com.android.volley.Response.ErrorListener() {
                    public void onErrorResponse(VolleyError error) {
                        mediaHandler.sendEmptyMessage(1);
                    }
                }) {
                    @Override
                    protected Map<String, String> getParams() throws AuthFailureError {
                        String codedata = "token=" + sp.getString("ckinfo", null) + "&t=" + GetTimeStamp.timeStamp();
                        String rc4data = null;
                        if (miType == 1) {
                            rc4data = Rc4.encry_RC4_string(codedata, RC4KEY);
                        } else if (miType == 2) {
                            try {
                                rc4data = Rsa.encrypt_Rsa(codedata, RSAKEY);
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                        } else if (miType == 3) {
                            rc4data = AES.encrypt_Aes(AESKEY,codedata, AESIV);
                        }

                        String sign = Md5Encoder.encode(new StringBuilder(String.valueOf(codedata)).append("&").append(Appkey).toString());
                        Map<String, String> params = new HashMap<>();
                        params.put("data", rc4data);
                        params.put("sign", sign);
                        return params;
                    }

                    @Override
                    public Map<String, String> getHeaders() throws AuthFailureError {
                        Map<String, String> headers = new HashMap<>();
                        headers.put("Authorization", Rc4.decry_RC4(SharePreferenceDataUtil.getSharedStringData(HistoryActivity.this, "Authorization", ""),Constant.d));  /*设置其他请求头*/
                        return headers;
                    }
                };
                mQueue.add(stringRequest);
                return;
            } catch (ParseException ex) {
                ex.printStackTrace();
                continue;
            }
        }



    }

    /*心跳响应*/
    public void GetMotionResponse(String response,final int position) {
        String RC4KEY = Rc4.decry_RC4(SharePreferenceDataUtil.getSharedStringData(this, Constant.kd, ""),Constant.d);
        String RSAKEY = Rc4.decry_RC4(SharePreferenceDataUtil.getSharedStringData(this, Constant.tb, ""),Constant.d);
        String AESKEY = Rc4.decry_RC4(SharePreferenceDataUtil.getSharedStringData(this, Constant.um, ""),Constant.d);
        String AESIV = Rc4.decry_RC4(SharePreferenceDataUtil.getSharedStringData(this, Constant.im, ""),Constant.d);
        //Log.i(TAG, "GetMotionResponse: " + response);
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
                String vip = msg.optString("vip");
                int Try = msg.optInt("Try");
                int Clientmode = msg.optInt("Clientmode");
                trystate = Try;
                sp.edit().putString("vip", vip).commit();
                Sp.edit()
                        .putInt("Submission_method", Clientmode)
                        .putInt("Trystate", Try)
                        .commit();
            }else if (code == 127) {/*其他设备登录*/
                mediaHandler.sendEmptyMessage(3);
                sp.edit().putString("userName", null).putString("passWord", null).putString("vip", null).putString("fen", null).putString("ckinfo", null).commit();
                return;
            }else if (code == 114) {/*账户封禁*/
                mediaHandler.sendEmptyMessage(4);
                sp.edit().putString("userName", null).putString("passWord", null).putString("vip", null).putString("fen", null).putString("ckinfo", null).commit();
                return;
            }else if (code == 125) {/*账户信息错误*/
                mediaHandler.sendEmptyMessage(7);
                sp.edit().putString("userName", null).putString("passWord", null).putString("vip", null).putString("fen", null).putString("ckinfo", null).commit();
                return;
            }else if (code == 127) {/*账户信息失效*/
                mediaHandler.sendEmptyMessage(6);
                sp.edit().putString("userName", null).putString("passWord", null).putString("vip", null).putString("fen", null).putString("ckinfo", null).commit();
                return;
            }else if (code == 201){/*201心跳失败*/
                mediaHandler.sendEmptyMessage(5);
                return;
            }
            if (vipstate == 1 || trystate == 1) {
                Intent intent = new Intent(HistoryActivity.this, VideoDetailsActivity.class);
                intent.putExtra("vodtype", Albumls.get(position).getAlbumType());
                intent.putExtra("vodstate", Albumls.get(position).getAlbumState());
                intent.putExtra("nextlink", Albumls.get(position).getNextLink());
                startActivity(intent);
                overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
            }else{
                mediaHandler.sendEmptyMessage(2);
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
