package com.shenma.tvlauncher;

import android.content.Intent;
import android.content.SharedPreferences.Editor;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnKeyListener;
import android.widget.ImageButton;
import android.widget.RelativeLayout;
import android.widget.TextView;
import com.shenma.tvlauncher.utils.Logger;

/**
 * @author joychang
 * @Description 其他设置
 */
public class OtherActivity extends BaseActivity {
    private String blur_set;
    private RelativeLayout other_setting_bgblur_rl;
    private ImageButton other_setting_bgblur_rl_left_arrows;
    private ImageButton other_setting_bgblur_rl_right_arrows;
    private TextView other_setting_bgblur_rl_text;
    private RelativeLayout other_setting_content_decode;
    private ImageButton other_setting_content_decode_left_arrows;
    private ImageButton other_setting_content_decode_right_arrows;
    private TextView other_setting_content_decode_text;
    private RelativeLayout other_setting_content_definition;
    private ImageButton other_setting_content_definition_left_arrows;
    private ImageButton other_setting_content_definition_right_arrows;
    private TextView other_setting_content_definition_text;
    private RelativeLayout other_setting_tvlive_rl;
    private String[] strs;
    private String[] tvlive_server;

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.layout_setting_other);
        findViewById(R.id.setting_other).setBackgroundResource(R.drawable.video_details_bg);
        initView();
        initData();
    }

    private void initData() {
        strs = getResources().getStringArray(R.array.setting_turn_off);
        tvlive_server = getResources().getStringArray(R.array.setting_tvlive_server);
        String open_ring = sp.getString("open_ring", strs[0]);
        String open_piano = sp.getString("open_effciency", strs[0]);
        String open_blur = sp.getString("open_blur", strs[1]);
        blur_set = open_blur;
        other_setting_content_decode_text.setText(open_ring);
        other_setting_content_definition_text.setText(open_piano);
        other_setting_bgblur_rl_text.setText(open_blur);
    }

    public boolean onKeyUp(int keyCode, KeyEvent event) {
        if (keyCode == 4 && event.getAction() == 1) {
            savePlaySettingInfo();
            finish();
            return true;
        }
        super.onKeyUp(keyCode, event);
        return false;
    }


    /**
     * 保存其他设置
     */
    private void savePlaySettingInfo() {
        Editor editor = sp.edit();
        editor.putString("open_ring", (String) other_setting_content_decode_text.getText());
        editor.putString("open_effciency", (String) other_setting_content_definition_text.getText());
        editor.commit();
    }

    /**
     * 保存
     *
     * @param key
     * @param value
     */
    private void saveInitSetting(String key, String value) {
        sp.edit().putString(key, value).commit();
    }

    protected void onDestroy() {
        String open_blur = other_setting_bgblur_rl_text.getText().toString();
        if (!blur_set.equals(open_blur)) {
            saveInitSetting("open_blur", open_blur);
            Logger.d("zhouchuan", open_blur);
            Intent mIntent = new Intent();
            mIntent.setAction("com.hd.changewallpaper");
            mIntent.putExtra("wallpaperFileName", sp.getString("wallpaperFileName", null));
            sendBroadcast(mIntent);
        }
        super.onDestroy();
    }

    protected void initView() {
        findViewById();
        setListener();
    }

    protected void loadViewLayout() {
    }

    protected void findViewById() {
        other_setting_content_decode = (RelativeLayout) findViewById(R.id.other_setting_content_decode);
        other_setting_content_definition = (RelativeLayout) findViewById(R.id.other_setting_content_definition);
        other_setting_bgblur_rl = (RelativeLayout) findViewById(R.id.other_setting_bgblur_rl);
        other_setting_content_decode_text = (TextView) findViewById(R.id.other_setting_content_decode_text);
        other_setting_content_definition_text = (TextView) findViewById(R.id.other_setting_content_definition_text);
        other_setting_bgblur_rl_text = (TextView) findViewById(R.id.other_setting_bgblur_rl_text);
        other_setting_content_decode_left_arrows = (ImageButton) findViewById(R.id.other_setting_content_decode_left_arrows);
        other_setting_content_decode_right_arrows = (ImageButton) findViewById(R.id.other_setting_content_decode_right_arrows);
        other_setting_content_definition_left_arrows = (ImageButton) findViewById(R.id.other_setting_content_definition_left_arrows);
        other_setting_content_definition_right_arrows = (ImageButton) findViewById(R.id.other_setting_content_definition_right_arrows);
        other_setting_bgblur_rl_left_arrows = (ImageButton) findViewById(R.id.other_setting_bgblur_rl_left_arrows);
        other_setting_bgblur_rl_right_arrows = (ImageButton) findViewById(R.id.other_setting_bgblur_rl_right_arrows);
    }

    protected void setListener() {
        other_setting_content_decode_left_arrows.setOnClickListener(new OnClickListener() {
            public void onClick(View v) {
                String open_ring = other_setting_content_decode_text.getText().toString();
                int index = 0;
                int i = 0;
                while (i < strs.length) {
                    if (open_ring != null && open_ring.equals(strs[i])) {
                        index = i;
                    }
                    i++;
                }
                if (index == 0) {
                    other_setting_content_decode_text.setText(strs[strs.length - 1]);
                } else {
                    other_setting_content_decode_text.setText(strs[index - 1]);
                }
            }
        });
        other_setting_content_decode_right_arrows.setOnClickListener(new OnClickListener() {
            public void onClick(View v) {
                String open_ring = other_setting_content_decode_text.getText().toString();
                int index = 0;
                int i = 0;
                while (i < strs.length) {
                    if (open_ring != null && open_ring.equals(strs[i])) {
                        index = i;
                    }
                    i++;
                }
                if (index == strs.length - 1) {
                    other_setting_content_decode_text.setText(strs[0]);
                } else {
                    other_setting_content_decode_text.setText(strs[index + 1]);
                }
            }
        });
        other_setting_content_definition_left_arrows.setOnClickListener(new OnClickListener() {
            public void onClick(View v) {
                String open_piano = other_setting_content_definition_text.getText().toString();
                int index = 0;
                int i = 0;
                while (i < strs.length) {
                    if (open_piano != null && open_piano.equals(strs[i])) {
                        index = i;
                    }
                    i++;
                }
                if (index == 0) {
                    other_setting_content_definition_text.setText(strs[strs.length - 1]);
                } else {
                    other_setting_content_definition_text.setText(strs[index - 1]);
                }
            }
        });
        other_setting_content_definition_right_arrows.setOnClickListener(new OnClickListener() {
            public void onClick(View v) {
                String open_piano = other_setting_content_definition_text.getText().toString();
                int index = 0;
                int i = 0;
                while (i < strs.length) {
                    if (open_piano != null && open_piano.equals(strs[i])) {
                        index = i;
                    }
                    i++;
                }
                if (index == strs.length - 1) {
                    other_setting_content_definition_text.setText(strs[0]);
                } else {
                    other_setting_content_definition_text.setText(strs[index + 1]);
                }
            }
        });
        other_setting_bgblur_rl_left_arrows.setOnClickListener(new OnClickListener() {
            public void onClick(View v) {
                String open_blur = other_setting_bgblur_rl_text.getText().toString();
                int index = 0;
                int i = 0;
                while (i < strs.length) {
                    if (open_blur != null && open_blur.equals(strs[i])) {
                        index = i;
                    }
                    i++;
                }
                if (index == 0) {
                    other_setting_bgblur_rl_text.setText(strs[strs.length - 1]);
                } else {
                    other_setting_bgblur_rl_text.setText(strs[index - 1]);
                }
            }
        });
        other_setting_bgblur_rl_right_arrows.setOnClickListener(new OnClickListener() {
            public void onClick(View v) {
                String open_blur = other_setting_bgblur_rl_text.getText().toString();
                int index = 0;
                int i = 0;
                while (i < strs.length) {
                    if (open_blur != null && open_blur.equals(strs[i])) {
                        index = i;
                    }
                    i++;
                }
                if (index == strs.length - 1) {
                    other_setting_bgblur_rl_text.setText(strs[0]);
                } else {
                    other_setting_bgblur_rl_text.setText(strs[index + 1]);
                }
            }
        });
        other_setting_content_decode.setOnKeyListener(new OnKeyListener() {
            @Override
            public boolean onKey(View v, int keyCode, KeyEvent event) {
                String open_ring = other_setting_content_decode_text.getText().toString();
                int index = 0;
                if (event.getAction() == KeyEvent.ACTION_DOWN) {
                    switch (keyCode) {
                        case KeyEvent.KEYCODE_DPAD_LEFT:
                            for (int i = 0; i < strs.length; i++) {
                                if (open_ring != null && open_ring.equals(strs[i])) {
                                    index = i;
                                }
                            }
                            if (index == 0) {
                                other_setting_content_decode_text.setText(strs[strs.length - 1]);
                            } else {
                                other_setting_content_decode_text.setText(strs[index - 1]);
                            }
                            other_setting_content_decode_left_arrows.setImageResource(R.drawable.select_left_arrows_f);
                            break;

                        case KeyEvent.KEYCODE_DPAD_RIGHT:
                            for (int i = 0; i < strs.length; i++) {
                                if (open_ring != null && open_ring.equals(strs[i])) {
                                    index = i;
                                }
                            }
                            if (index == strs.length - 1) {
                                other_setting_content_decode_text.setText(strs[0]);
                            } else {
                                other_setting_content_decode_text.setText(strs[index + 1]);
                            }
                            other_setting_content_decode_right_arrows.setImageResource(R.drawable.select_right_arrows_f);
                            break;
                    }
                } else if (event.getAction() == KeyEvent.ACTION_UP) {
                    switch (keyCode) {
                        case KeyEvent.KEYCODE_DPAD_LEFT:
                            other_setting_content_decode_left_arrows.setImageResource(R.drawable.select_left_arrows_n);
                            break;
                        case KeyEvent.KEYCODE_DPAD_RIGHT:
                            other_setting_content_decode_right_arrows.setImageResource(R.drawable.select_right_arrows_n);
                            break;
                    }
                }
                return false;
            }
        });

        other_setting_content_definition.setOnKeyListener(new OnKeyListener() {
            @Override
            public boolean onKey(View v, int keyCode, KeyEvent event) {
                String open_piano = other_setting_content_definition_text.getText().toString();
                int index = 0;
                if (event.getAction() == KeyEvent.ACTION_DOWN) {
                    switch (keyCode) {
                        case KeyEvent.KEYCODE_DPAD_LEFT:
                            for (int i = 0; i < strs.length; i++) {
                                if (open_piano != null && open_piano.equals(strs[i])) {
                                    index = i;
                                }
                            }
                            if (index == 0) {
                                other_setting_content_definition_text.setText(strs[strs.length - 1]);
                            } else {
                                other_setting_content_definition_text.setText(strs[index - 1]);
                            }
                            other_setting_content_definition_left_arrows.setImageResource(R.drawable.select_left_arrows_f);
                            break;

                        case KeyEvent.KEYCODE_DPAD_RIGHT:
                            for (int i = 0; i < strs.length; i++) {
                                if (open_piano != null && open_piano.equals(strs[i])) {
                                    index = i;
                                }
                            }
                            if (index == strs.length - 1) {
                                other_setting_content_definition_text.setText(strs[0]);
                            } else {
                                other_setting_content_definition_text.setText(strs[index + 1]);
                            }
                            other_setting_content_definition_right_arrows.setImageResource(R.drawable.select_right_arrows_f);
                            break;
                    }
                } else if (event.getAction() == KeyEvent.ACTION_UP) {
                    switch (keyCode) {
                        case KeyEvent.KEYCODE_DPAD_LEFT:
                            other_setting_content_definition_left_arrows.setImageResource(R.drawable.select_left_arrows_n);
                            break;
                        case KeyEvent.KEYCODE_DPAD_RIGHT:
                            other_setting_content_definition_right_arrows.setImageResource(R.drawable.select_right_arrows_n);
                            break;
                    }
                }
                return false;
            }
        });
        other_setting_bgblur_rl.setOnKeyListener(new OnKeyListener() {
            @Override
            public boolean onKey(View v, int keyCode, KeyEvent event) {
                String open_blur = other_setting_bgblur_rl_text.getText().toString();
                int index = 0;
                if (event.getAction() == KeyEvent.ACTION_DOWN) {
                    switch (keyCode) {
                        case KeyEvent.KEYCODE_DPAD_LEFT:
                            for (int i = 0; i < strs.length; i++) {
                                if (open_blur != null && open_blur.equals(strs[i])) {
                                    index = i;
                                }
                            }
                            if (index == 0) {
                                other_setting_bgblur_rl_text.setText(strs[strs.length - 1]);
                            } else {
                                other_setting_bgblur_rl_text.setText(strs[index - 1]);
                            }
                            other_setting_bgblur_rl_left_arrows.setImageResource(R.drawable.select_left_arrows_f);
                            break;

                        case KeyEvent.KEYCODE_DPAD_RIGHT:
                            for (int i = 0; i < strs.length; i++) {
                                if (open_blur != null && open_blur.equals(strs[i])) {
                                    index = i;
                                }
                            }
                            if (index == strs.length - 1) {
                                other_setting_bgblur_rl_text.setText(strs[0]);
                            } else {
                                other_setting_bgblur_rl_text.setText(strs[index + 1]);
                            }
                            other_setting_bgblur_rl_right_arrows.setImageResource(R.drawable.select_right_arrows_f);
                            break;
                    }
                } else if (event.getAction() == KeyEvent.ACTION_UP) {
                    switch (keyCode) {
                        case KeyEvent.KEYCODE_DPAD_LEFT:
                            other_setting_bgblur_rl_left_arrows.setImageResource(R.drawable.select_left_arrows_n);
                            break;
                        case KeyEvent.KEYCODE_DPAD_RIGHT:
                            other_setting_bgblur_rl_right_arrows.setImageResource(R.drawable.select_right_arrows_n);
                            break;
                    }
                }
                return false;
            }
        });
    }
}
