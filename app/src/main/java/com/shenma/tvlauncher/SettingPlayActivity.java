package com.shenma.tvlauncher;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
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

import com.shenma.tvlauncher.utils.Constant;
import com.shenma.tvlauncher.utils.SharePreferenceDataUtil;
import com.shenma.tvlauncher.utils.Utils;

/**
 * @author joychang
 * @Description 播放设置
 */
public class SettingPlayActivity extends BaseActivity {
    private ImageButton bt_decode_left_arrows;
    private ImageButton bt_decode_right_arrows;
    private ImageButton bt_jump_left_arrows;
    private ImageButton bt_jump_right_arrows;
    private ImageButton bt_jump_end_left_arrows;
    private ImageButton bt_jump_end_right_arrows;
    private ImageButton bt_core_left_arrows;//
    private ImageButton bt_live_core_left_arrows;////
    private ImageButton bt_core_right_arrows;//
    private ImageButton bt_live_core_right_arrows;////
    private ImageButton bt_playratio_left_arrows;
    private ImageButton bt_playratio_right_arrows;
    private ImageButton bt_theme_left_arrows;
    private ImageButton bt_theme_right_arrows;
    private String[] mAll_play_setting_decode;
    private String[] mAll_play_setting_definition;
    private String[] mAll_play_setting_jump;
    private String[] mAll_play_setting_jump_end;
    private String[] mAll_play_setting_playratio;
    private String[] mAll_play_setting_core;//
    private String[] mAll_play_setting_live_core;////
    private String[] mAll_play_setting_theme;
    private RelativeLayout rl_play_setting_content_decode;
    private RelativeLayout rl_play_setting_content_jump;
    private RelativeLayout rl_play_setting_content_jump_end;
    private RelativeLayout rl_play_setting_content_core;//
    private RelativeLayout rl_play_setting_content_live_core;////
    private RelativeLayout rl_play_setting_content_playratio;
    private RelativeLayout rl_play_setting_content_theme;
    private TextView tv_play_setting_content_decode_text;
    private TextView tv_play_setting_content_jump_text;
    private TextView tv_play_setting_content_jump_end_text;
    private TextView tv_play_setting_content_core_text;//
    private TextView tv_play_setting_content_live_core_text;////
    private TextView tv_play_setting_content_playratio_text;
    private TextView tv_play_setting_content_theme_text;

//    private String theme = sp.getString("play_theme", "经典");

    private String play_theme;

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.layout_setting_play);
        findViewById(R.id.setting_play).setBackgroundResource(R.drawable.video_details_bg);
        initView();
        initData();
    }

    public void onBackPressed() {
        super.onBackPressed();
        finish();
    }

    protected void initView() {
        sp = getSharedPreferences("shenma", 0);
        loadViewLayout();
        findViewById();
        setListener();
    }

    protected void loadViewLayout() {
    }

    protected void findViewById() {
        rl_play_setting_content_decode = (RelativeLayout) findViewById(R.id.play_setting_content_decode);
        rl_play_setting_content_playratio = (RelativeLayout) findViewById(R.id.play_setting_content_playratio);
        rl_play_setting_content_jump = (RelativeLayout) findViewById(R.id.play_setting_content_jump);
        rl_play_setting_content_jump_end = (RelativeLayout) findViewById(R.id.play_setting_content_jump_end);
        rl_play_setting_content_theme = (RelativeLayout) findViewById(R.id.play_setting_content_theme);
        rl_play_setting_content_core = (RelativeLayout) findViewById(R.id.play_setting_content_core);
        rl_play_setting_content_live_core = (RelativeLayout) findViewById(R.id.play_setting_content_live_core);////
        tv_play_setting_content_decode_text = (TextView) findViewById(R.id.play_setting_content_decode_text);
        tv_play_setting_content_jump_text = (TextView) findViewById(R.id.play_setting_content_jump_text);
        tv_play_setting_content_jump_end_text = (TextView) findViewById(R.id.play_setting_content_jump_end_text);
        tv_play_setting_content_theme_text = (TextView) findViewById(R.id.play_setting_content_theme_text);
        tv_play_setting_content_core_text = (TextView) findViewById(R.id.play_setting_content_core_text);
        tv_play_setting_content_live_core_text = (TextView) findViewById(R.id.play_setting_content_live_core_text);////
        tv_play_setting_content_playratio_text = (TextView) findViewById(R.id.play_setting_content_playratio_text);
        bt_decode_left_arrows = (ImageButton) findViewById(R.id.play_setting_content_decode_left_arrows);
        bt_decode_right_arrows = (ImageButton) findViewById(R.id.play_setting_content_decode_right_arrows);
        bt_playratio_left_arrows = (ImageButton) findViewById(R.id.play_setting_content_playratio_left_arrows);
        bt_playratio_right_arrows = (ImageButton) findViewById(R.id.play_setting_content_playratio_right_arrows);
        bt_jump_left_arrows = (ImageButton) findViewById(R.id.play_setting_content_jump_left_arrows);
        bt_jump_right_arrows = (ImageButton) findViewById(R.id.play_setting_content_jump_right_arrows);
        bt_jump_end_left_arrows = (ImageButton) findViewById(R.id.play_setting_content_jump_end_left_arrows);
        bt_jump_end_right_arrows = (ImageButton) findViewById(R.id.play_setting_content_jump_end_right_arrows);
        bt_theme_left_arrows = (ImageButton) findViewById(R.id.play_setting_content_theme_left_arrows);
        bt_theme_right_arrows = (ImageButton) findViewById(R.id.play_setting_content_theme_right_arrows);
        bt_core_left_arrows = (ImageButton) findViewById(R.id.play_setting_content_core_left_arrows);
        bt_live_core_left_arrows = (ImageButton) findViewById(R.id.play_setting_content_live_core_left_arrows);////
        bt_core_right_arrows = (ImageButton) findViewById(R.id.play_setting_content_core_right_arrows);
        bt_live_core_right_arrows = (ImageButton) findViewById(R.id.play_setting_content_live_core_right_arrows);////

        if (SharePreferenceDataUtil.getSharedIntData(this, "Allow_changing_styles", 0) == 0){
            rl_play_setting_content_theme.setVisibility(View.GONE);
        }

    }

    protected void setListener() {
        /*解码点击*/
        bt_decode_left_arrows.setOnClickListener(new OnClickListener() {
            public void onClick(View v) {
                String mtv_play_decode = (String) tv_play_setting_content_decode_text.getText();
                int index = 0;
                int i = 0;
                while (i < mAll_play_setting_decode.length) {
                    if (mtv_play_decode != null && mtv_play_decode.equals(mAll_play_setting_decode[i])) {
                        index = i;
                    }
                    i++;
                }
                if (index == 0) {
                    tv_play_setting_content_decode_text.setText(mAll_play_setting_decode[mAll_play_setting_decode.length - 1]);
                } else {
                    tv_play_setting_content_decode_text.setText(mAll_play_setting_decode[index - 1]);
                }
            }
        });
        bt_decode_right_arrows.setOnClickListener(new OnClickListener() {
            public void onClick(View v) {
                String mtv_play_decode = (String) tv_play_setting_content_decode_text.getText();
                int index = 0;
                int i = 0;
                while (i < mAll_play_setting_decode.length) {
                    if (mtv_play_decode != null && mtv_play_decode.equals(mAll_play_setting_decode[i])) {
                        index = i;
                    }
                    i++;
                }
                if (index == mAll_play_setting_decode.length - 1) {
                    tv_play_setting_content_decode_text.setText(mAll_play_setting_decode[0]);
                } else {
                    tv_play_setting_content_decode_text.setText(mAll_play_setting_decode[index + 1]);
                }
            }
        });

        /*画面比例点击*/
        bt_playratio_left_arrows.setOnClickListener(new OnClickListener() {
            public void onClick(View v) {
                String tv_play_playratio = (String) tv_play_setting_content_playratio_text.getText();
                int index = 0;
                int i = 0;
                while (i < mAll_play_setting_playratio.length) {
                    if (tv_play_playratio != null && tv_play_playratio.equals(mAll_play_setting_playratio[i])) {
                        index = i;
                    }
                    i++;
                }
                if (index == 0) {
                    tv_play_setting_content_playratio_text.setText(mAll_play_setting_playratio[mAll_play_setting_playratio.length - 1]);
                } else {
                    tv_play_setting_content_playratio_text.setText(mAll_play_setting_playratio[index - 1]);
                }
            }
        });
        bt_playratio_right_arrows.setOnClickListener(new OnClickListener() {
            public void onClick(View v) {
                String tv_play_playratio = (String) tv_play_setting_content_playratio_text.getText();
                int index = 0;
                int i = 0;
                while (i < mAll_play_setting_playratio.length) {
                    if (tv_play_playratio != null && tv_play_playratio.equals(mAll_play_setting_playratio[i])) {
                        index = i;
                    }
                    i++;
                }
                if (index == mAll_play_setting_playratio.length - 1) {
                    tv_play_setting_content_playratio_text.setText(mAll_play_setting_playratio[0]);
                } else {
                    tv_play_setting_content_playratio_text.setText(mAll_play_setting_playratio[index + 1]);
                }
            }
        });

        /*跳片头点击*/
        bt_jump_left_arrows.setOnClickListener(new OnClickListener() {
            public void onClick(View v) {
                String tv_play_jump = (String) tv_play_setting_content_jump_text.getText();
                int index = 0;
                int i = 0;
                while (i < mAll_play_setting_jump.length) {
                    if (tv_play_jump != null && tv_play_jump.equals(mAll_play_setting_jump[i])) {
                        index = i;
                    }
                    i++;
                }
                if (index == 0) {
                    tv_play_setting_content_jump_text.setText(mAll_play_setting_jump[mAll_play_setting_jump.length - 1]);
                } else {
                    tv_play_setting_content_jump_text.setText(mAll_play_setting_jump[index - 1]);
                }
            }
        });
        bt_jump_right_arrows.setOnClickListener(new OnClickListener() {
            public void onClick(View v) {
                String tv_play_jump = (String) tv_play_setting_content_jump_text.getText();
                int index = 0;
                int i = 0;
                while (i < mAll_play_setting_jump.length) {
                    if (tv_play_jump != null && tv_play_jump.equals(mAll_play_setting_jump[i])) {
                        index = i;
                    }
                    i++;
                }
                if (index == mAll_play_setting_jump.length - 1) {
                    tv_play_setting_content_jump_text.setText(mAll_play_setting_jump[0]);
                } else {
                    tv_play_setting_content_jump_text.setText(mAll_play_setting_jump[index + 1]);
                }
            }
        });

        /*点播内核点击*/
        bt_core_left_arrows.setOnClickListener(new OnClickListener() {
            public void onClick(View v) {
                String tv_play_core = (String) tv_play_setting_content_core_text.getText();
                int index = 0;
                int i = 0;
                while (i < mAll_play_setting_core.length) {
                    if (tv_play_core != null && tv_play_core.equals(mAll_play_setting_core[i])) {
                        index = i;
                    }
                    i++;
                }
                if (index == 0) {
                    tv_play_setting_content_core_text.setText(mAll_play_setting_core[mAll_play_setting_core.length - 1]);
                } else {
                    tv_play_setting_content_core_text.setText(mAll_play_setting_core[index - 1]);
                }
            }
        });
        bt_core_right_arrows.setOnClickListener(new OnClickListener() {
            public void onClick(View v) {
                String tv_play_core = (String) tv_play_setting_content_core_text.getText();
                int index = 0;
                int i = 0;
                while (i < mAll_play_setting_core.length) {
                    if (tv_play_core != null && tv_play_core.equals(mAll_play_setting_core[i])) {
                        index = i;
                    }
                    i++;
                }
                if (index == mAll_play_setting_core.length - 1) {
                    tv_play_setting_content_core_text.setText(mAll_play_setting_core[0]);
                } else {
                    tv_play_setting_content_core_text.setText(mAll_play_setting_core[index + 1]);
                }
            }
        });

        /*直播内核点击*/
        bt_live_core_left_arrows.setOnClickListener(new OnClickListener() {
            public void onClick(View v) {
                String tv_play_core = (String) tv_play_setting_content_live_core_text.getText();
                int index = 0;
                int i = 0;
                while (i < mAll_play_setting_live_core.length) {
                    if (tv_play_core != null && tv_play_core.equals(mAll_play_setting_live_core[i])) {
                        index = i;
                    }
                    i++;
                }
                if (index == 0) {
                    tv_play_setting_content_live_core_text.setText(mAll_play_setting_live_core[mAll_play_setting_live_core.length - 1]);
                } else {
                    tv_play_setting_content_live_core_text.setText(mAll_play_setting_live_core[index - 1]);
                }
            }
        });
        bt_live_core_right_arrows.setOnClickListener(new OnClickListener() {
            public void onClick(View v) {
                String tv_play_core = (String) tv_play_setting_content_live_core_text.getText();
                int index = 0;
                int i = 0;
                while (i < mAll_play_setting_live_core.length) {
                    if (tv_play_core != null && tv_play_core.equals(mAll_play_setting_live_core[i])) {
                        index = i;
                    }
                    i++;
                }
                if (index == mAll_play_setting_live_core.length - 1) {
                    tv_play_setting_content_live_core_text.setText(mAll_play_setting_live_core[0]);
                } else {
                    tv_play_setting_content_live_core_text.setText(mAll_play_setting_live_core[index + 1]);
                }
            }
        });

        /*跳片尾点击*/
        bt_jump_end_left_arrows.setOnClickListener(new OnClickListener() {
            public void onClick(View v) {
                String tv_play_jump_end = (String) tv_play_setting_content_jump_end_text.getText();
                int index = 0;
                int i = 0;
                while (i < mAll_play_setting_jump_end.length) {
                    if (tv_play_jump_end != null && tv_play_jump_end.equals(mAll_play_setting_jump_end[i])) {
                        index = i;
                    }
                    i++;
                }
                if (index == 0) {
                    tv_play_setting_content_jump_end_text.setText(mAll_play_setting_jump_end[mAll_play_setting_jump_end.length - 1]);
                } else {
                    tv_play_setting_content_jump_end_text.setText(mAll_play_setting_jump_end[index - 1]);
                }
            }
        });
        bt_jump_end_right_arrows.setOnClickListener(new OnClickListener() {
            public void onClick(View v) {
                String tv_play_jump_end = (String) tv_play_setting_content_jump_end_text.getText();
                int index = 0;
                int i = 0;
                while (i < mAll_play_setting_jump_end.length) {
                    if (tv_play_jump_end != null && tv_play_jump_end.equals(mAll_play_setting_jump_end[i])) {
                        index = i;
                    }
                    i++;
                }
                if (index == mAll_play_setting_jump_end.length - 1) {
                    tv_play_setting_content_jump_end_text.setText(mAll_play_setting_jump_end[0]);
                } else {
                    tv_play_setting_content_jump_end_text.setText(mAll_play_setting_jump_end[index + 1]);
                }
            }
        });

        /*更换主题点击*/
        bt_theme_left_arrows.setOnClickListener(new OnClickListener() {
            public void onClick(View v) {
                String tv_play_theme = (String) tv_play_setting_content_theme_text.getText();
                int index = 0;
                int i = 0;
                while (i < mAll_play_setting_theme.length) {
                    if (tv_play_theme != null && tv_play_theme.equals(mAll_play_setting_theme[i])) {
                        index = i;
                    }
                    i++;
                }
                if (index == 0) {
                    tv_play_setting_content_theme_text.setText(mAll_play_setting_theme[mAll_play_setting_theme.length - 1]);
                } else {
                    tv_play_setting_content_theme_text.setText(mAll_play_setting_theme[index - 1]);
                }
                Utils.showToast(context, R.string.Theme_successful, R.drawable.toast_smile);
            }
        });
        bt_theme_right_arrows.setOnClickListener(new OnClickListener() {
            public void onClick(View v) {
                String tv_play_theme = (String) tv_play_setting_content_theme_text.getText();
                int index = 0;
                int i = 0;
                while (i < mAll_play_setting_theme.length) {
                    if (tv_play_theme != null && tv_play_theme.equals(mAll_play_setting_theme[i])) {
                        index = i;
                    }
                    i++;
                }
                if (index == mAll_play_setting_theme.length - 1) {
                    tv_play_setting_content_theme_text.setText(mAll_play_setting_theme[0]);
                } else {
                    tv_play_setting_content_theme_text.setText(mAll_play_setting_theme[index + 1]);
                }
                Utils.showToast(context, R.string.Theme_successful, R.drawable.toast_smile);
            }
        });


        //解码 键盘
        rl_play_setting_content_decode.setOnKeyListener(new OnKeyListener() {

            @Override
            public boolean onKey(View v, int keyCode, KeyEvent event) {
                // TODO Auto-generated method stub
                String mtv_play_decode = (String) tv_play_setting_content_decode_text.getText();
                int index = 0;
                if (event.getAction() == KeyEvent.ACTION_DOWN) {
                    switch (keyCode) {
                        case KeyEvent.KEYCODE_DPAD_LEFT:
                            for (int i = 0; i < mAll_play_setting_decode.length; i++) {
                                if (mtv_play_decode != null
                                        && mtv_play_decode
                                        .equals(mAll_play_setting_decode[i])) {
                                    index = i;
                                }
                            }
                            if (index == 0) {
                                tv_play_setting_content_decode_text
                                        .setText(mAll_play_setting_decode[mAll_play_setting_decode.length - 1]);
                            } else {
                                tv_play_setting_content_decode_text.setText(mAll_play_setting_decode[index - 1]);
                            }
                            bt_decode_left_arrows
                                    .setImageResource(R.drawable.select_left_arrows_f);
                            break;
                        case KeyEvent.KEYCODE_DPAD_RIGHT:
                            for (int i = 0; i < mAll_play_setting_decode.length; i++) {
                                if (mtv_play_decode != null
                                        && mtv_play_decode
                                        .equals(mAll_play_setting_decode[i])) {
                                    index = i;
                                }
                            }
                            if (index == mAll_play_setting_decode.length - 1) {
                                tv_play_setting_content_decode_text.setText(mAll_play_setting_decode[0]);
                            } else {
                                tv_play_setting_content_decode_text.setText(mAll_play_setting_decode[index + 1]);
                            }
                            bt_decode_right_arrows
                                    .setImageResource(R.drawable.select_right_arrows_f);
                            break;
                    }
                } else if (event.getAction() == KeyEvent.ACTION_UP) {
                    switch (keyCode) {
                        case KeyEvent.KEYCODE_DPAD_LEFT:
                            bt_decode_left_arrows
                                    .setImageResource(R.drawable.select_left_arrows_n);
                            break;
                        case KeyEvent.KEYCODE_DPAD_RIGHT:
                            bt_decode_right_arrows
                                    .setImageResource(R.drawable.select_right_arrows_n);
                            break;
                    }
                }
                return false;
            }
        });

        //画面比例 键盘
        rl_play_setting_content_playratio.setOnKeyListener(new OnKeyListener() {

            @Override
            public boolean onKey(View v, int keyCode, KeyEvent event) {
                // TODO Auto-generated method stub
                String tv_play_playratio = (String) tv_play_setting_content_playratio_text.getText();
                int index = 0;
                if (event.getAction() == KeyEvent.ACTION_DOWN) {
                    switch (keyCode) {
                        case KeyEvent.KEYCODE_DPAD_LEFT:
                            for (int i = 0; i < mAll_play_setting_playratio.length; i++) {
                                if (tv_play_playratio != null
                                        && tv_play_playratio
                                        .equals(mAll_play_setting_playratio[i])) {
                                    index = i;
                                }
                            }
                            if (index == 0) {
                                tv_play_setting_content_playratio_text
                                        .setText(mAll_play_setting_playratio[mAll_play_setting_playratio.length - 1]);
                            } else {
                                tv_play_setting_content_playratio_text.setText(mAll_play_setting_playratio[index - 1]);
                            }
                            bt_playratio_left_arrows
                                    .setImageResource(R.drawable.select_left_arrows_f);
                            break;
                        case KeyEvent.KEYCODE_DPAD_RIGHT:
                            for (int i = 0; i < mAll_play_setting_playratio.length; i++) {
                                if (tv_play_playratio != null
                                        && tv_play_playratio
                                        .equals(mAll_play_setting_playratio[i])) {
                                    index = i;
                                }
                            }
                            if (index == mAll_play_setting_playratio.length - 1) {
                                tv_play_setting_content_playratio_text.setText(mAll_play_setting_playratio[0]);
                            } else {
                                tv_play_setting_content_playratio_text.setText(mAll_play_setting_playratio[index + 1]);
                            }
                            bt_playratio_right_arrows
                                    .setImageResource(R.drawable.select_right_arrows_f);
                            break;
                    }
                } else if (event.getAction() == KeyEvent.ACTION_UP) {
                    switch (keyCode) {
                        case KeyEvent.KEYCODE_DPAD_LEFT:
                            bt_playratio_left_arrows
                                    .setImageResource(R.drawable.select_left_arrows_n);
                            break;
                        case KeyEvent.KEYCODE_DPAD_RIGHT:
                            bt_playratio_right_arrows
                                    .setImageResource(R.drawable.select_right_arrows_n);
                            break;
                    }
                }
                return false;

            }
        });

        //跳过片头时间 键盘
        rl_play_setting_content_jump.setOnKeyListener(new OnKeyListener() {

            @Override
            public boolean onKey(View v, int keyCode, KeyEvent event) {
                // TODO Auto-generated method stub
                String tv_play_jump = (String) tv_play_setting_content_jump_text.getText();
                int index = 0;
                if (event.getAction() == KeyEvent.ACTION_DOWN) {
                    switch (keyCode) {
                        case KeyEvent.KEYCODE_DPAD_LEFT:
                            for (int i = 0; i < mAll_play_setting_jump.length; i++) {
                                if (tv_play_jump != null
                                        && tv_play_jump.equals(mAll_play_setting_jump[i])) {
                                    index = i;
                                }
                            }
                            if (index == 0) {
                                tv_play_setting_content_jump_text.setText(mAll_play_setting_jump[mAll_play_setting_jump.length - 1]);
                            } else {
                                tv_play_setting_content_jump_text.setText(mAll_play_setting_jump[index - 1]);
                            }
                            bt_jump_left_arrows
                                    .setImageResource(R.drawable.select_left_arrows_f);
                            break;
                        case KeyEvent.KEYCODE_DPAD_RIGHT:
                            for (int i = 0; i < mAll_play_setting_jump.length; i++) {
                                if (tv_play_jump != null
                                        && tv_play_jump.equals(mAll_play_setting_jump[i])) {
                                    index = i;
                                }
                            }
                            if (index == mAll_play_setting_jump.length - 1) {
                                tv_play_setting_content_jump_text.setText(mAll_play_setting_jump[0]);
                            } else {
                                tv_play_setting_content_jump_text.setText(mAll_play_setting_jump[index + 1]);
                            }
                            bt_jump_right_arrows
                                    .setImageResource(R.drawable.select_right_arrows_f);
                            break;
                    }
                } else if (event.getAction() == KeyEvent.ACTION_UP) {
                    switch (keyCode) {
                        case KeyEvent.KEYCODE_DPAD_LEFT:
                            bt_jump_left_arrows
                                    .setImageResource(R.drawable.select_left_arrows_n);
                            break;
                        case KeyEvent.KEYCODE_DPAD_RIGHT:
                            bt_jump_right_arrows
                                    .setImageResource(R.drawable.select_right_arrows_n);
                            break;
                    }
                }
                return false;
            }
        });

        //点播内核 键盘
        rl_play_setting_content_core.setOnKeyListener(new OnKeyListener() {
            @Override
            public boolean onKey(View v, int keyCode, KeyEvent event) {
                // TODO Auto-generated method stub
                String tv_play_core = (String) tv_play_setting_content_core_text.getText();
                int index = 0;
                if (event.getAction() == KeyEvent.ACTION_DOWN) {
                    switch (keyCode) {
                        case KeyEvent.KEYCODE_DPAD_LEFT:
                            for (int i = 0; i < mAll_play_setting_core.length; i++) {
                                if (tv_play_core != null
                                        && tv_play_core.equals(mAll_play_setting_core[i])) {
                                    index = i;
                                }
                            }
                            if (index == 0) {
                                tv_play_setting_content_core_text.setText(mAll_play_setting_core[mAll_play_setting_core.length - 1]);
                            } else {
                                tv_play_setting_content_core_text.setText(mAll_play_setting_core[index - 1]);
                            }
                            bt_core_left_arrows
                                    .setImageResource(R.drawable.select_left_arrows_f);
                            break;
                        case KeyEvent.KEYCODE_DPAD_RIGHT:
                            for (int i = 0; i < mAll_play_setting_core.length; i++) {
                                if (tv_play_core != null
                                        && tv_play_core.equals(mAll_play_setting_core[i])) {
                                    index = i;
                                }
                            }
                            if (index == mAll_play_setting_core.length - 1) {
                                tv_play_setting_content_core_text.setText(mAll_play_setting_core[0]);
                            } else {
                                tv_play_setting_content_core_text.setText(mAll_play_setting_core[index + 1]);
                            }
                            bt_core_right_arrows
                                    .setImageResource(R.drawable.select_right_arrows_f);
                            break;
                    }
                } else if (event.getAction() == KeyEvent.ACTION_UP) {
                    switch (keyCode) {
                        case KeyEvent.KEYCODE_DPAD_LEFT:
                            bt_core_left_arrows
                                    .setImageResource(R.drawable.select_left_arrows_n);
                            break;
                        case KeyEvent.KEYCODE_DPAD_RIGHT:
                            bt_core_right_arrows
                                    .setImageResource(R.drawable.select_right_arrows_n);
                            break;
                    }
                }
                return false;
            }
        });

        //直播内核 键盘
        rl_play_setting_content_live_core.setOnKeyListener(new OnKeyListener() {
            @Override
            public boolean onKey(View v, int keyCode, KeyEvent event) {
                // TODO Auto-generated method stub
                String tv_play_core = (String) tv_play_setting_content_live_core_text.getText();
                int index = 0;
                if (event.getAction() == KeyEvent.ACTION_DOWN) {
                    switch (keyCode) {
                        case KeyEvent.KEYCODE_DPAD_LEFT:
                            for (int i = 0; i < mAll_play_setting_live_core.length; i++) {
                                if (tv_play_core != null
                                        && tv_play_core.equals(mAll_play_setting_live_core[i])) {
                                    index = i;
                                }
                            }
                            if (index == 0) {
                                tv_play_setting_content_live_core_text.setText(mAll_play_setting_live_core[mAll_play_setting_live_core.length - 1]);
                            } else {
                                tv_play_setting_content_live_core_text.setText(mAll_play_setting_live_core[index - 1]);
                            }
                            bt_live_core_left_arrows
                                    .setImageResource(R.drawable.select_left_arrows_f);
                            break;
                        case KeyEvent.KEYCODE_DPAD_RIGHT:
                            for (int i = 0; i < mAll_play_setting_live_core.length; i++) {
                                if (tv_play_core != null
                                        && tv_play_core.equals(mAll_play_setting_live_core[i])) {
                                    index = i;
                                }
                            }
                            if (index == mAll_play_setting_live_core.length - 1) {
                                tv_play_setting_content_live_core_text.setText(mAll_play_setting_live_core[0]);
                            } else {
                                tv_play_setting_content_live_core_text.setText(mAll_play_setting_live_core[index + 1]);
                            }
                            bt_live_core_right_arrows
                                    .setImageResource(R.drawable.select_right_arrows_f);
                            break;
                    }
                } else if (event.getAction() == KeyEvent.ACTION_UP) {
                    switch (keyCode) {
                        case KeyEvent.KEYCODE_DPAD_LEFT:
                            bt_live_core_left_arrows
                                    .setImageResource(R.drawable.select_left_arrows_n);
                            break;
                        case KeyEvent.KEYCODE_DPAD_RIGHT:
                            bt_live_core_right_arrows
                                    .setImageResource(R.drawable.select_right_arrows_n);
                            break;
                    }
                }
                return false;
            }
        });

        //跳过片尾时间 键盘
        rl_play_setting_content_jump_end.setOnKeyListener(new OnKeyListener() {

            @Override
            public boolean onKey(View v, int keyCode, KeyEvent event) {
                // TODO Auto-generated method stub
                String tv_play_jump_end = (String) tv_play_setting_content_jump_end_text.getText();
                int index = 0;
                if (event.getAction() == KeyEvent.ACTION_DOWN) {
                    switch (keyCode) {
                        case KeyEvent.KEYCODE_DPAD_LEFT:
                            for (int i = 0; i < mAll_play_setting_jump_end.length; i++) {
                                if (tv_play_jump_end != null
                                        && tv_play_jump_end.equals(mAll_play_setting_jump_end[i])) {
                                    index = i;
                                }
                            }
                            if (index == 0) {
                                tv_play_setting_content_jump_end_text.setText(mAll_play_setting_jump_end[mAll_play_setting_jump_end.length - 1]);
                            } else {
                                tv_play_setting_content_jump_end_text.setText(mAll_play_setting_jump_end[index - 1]);
                            }
                            bt_jump_end_left_arrows
                                    .setImageResource(R.drawable.select_left_arrows_f);
                            break;
                        case KeyEvent.KEYCODE_DPAD_RIGHT:
                            for (int i = 0; i < mAll_play_setting_jump_end.length; i++) {
                                if (tv_play_jump_end != null
                                        && tv_play_jump_end.equals(mAll_play_setting_jump_end[i])) {
                                    index = i;
                                }
                            }
                            if (index == mAll_play_setting_jump_end.length - 1) {
                                tv_play_setting_content_jump_end_text.setText(mAll_play_setting_jump_end[0]);
                            } else {
                                tv_play_setting_content_jump_end_text.setText(mAll_play_setting_jump_end[index + 1]);
                            }
                            bt_jump_end_right_arrows
                                    .setImageResource(R.drawable.select_right_arrows_f);
                            break;
                    }
                } else if (event.getAction() == KeyEvent.ACTION_UP) {
                    switch (keyCode) {
                        case KeyEvent.KEYCODE_DPAD_LEFT:
                            bt_jump_end_left_arrows
                                    .setImageResource(R.drawable.select_left_arrows_n);
                            break;
                        case KeyEvent.KEYCODE_DPAD_RIGHT:
                            bt_jump_end_right_arrows
                                    .setImageResource(R.drawable.select_right_arrows_n);
                            break;
                    }
                }
                return false;
            }
        });

        //更换主题 键盘
        rl_play_setting_content_theme.setOnKeyListener(new OnKeyListener() {

            @Override
            public boolean onKey(View v, int keyCode, KeyEvent event) {
                // TODO Auto-generated method stub
                String tv_play_theme = (String) tv_play_setting_content_theme_text.getText();
                int index = 0;
                if (event.getAction() == KeyEvent.ACTION_DOWN) {
                    switch (keyCode) {
                        case KeyEvent.KEYCODE_DPAD_LEFT:
                            for (int i = 0; i < mAll_play_setting_theme.length; i++) {
                                if (tv_play_theme != null
                                        && tv_play_theme.equals(mAll_play_setting_theme[i])) {
                                    index = i;
                                }
                            }
                            if (index == 0) {
                                tv_play_setting_content_theme_text.setText(mAll_play_setting_theme[mAll_play_setting_theme.length - 1]);
                            } else {
                                tv_play_setting_content_theme_text.setText(mAll_play_setting_theme[index - 1]);
                            }
                            bt_theme_left_arrows
                                    .setImageResource(R.drawable.select_left_arrows_f);
                            Utils.showToast(context, R.string.Theme_successful, R.drawable.toast_smile);
                            break;
                        case KeyEvent.KEYCODE_DPAD_RIGHT:
                            for (int i = 0; i < mAll_play_setting_theme.length; i++) {
                                if (tv_play_theme != null
                                        && tv_play_theme.equals(mAll_play_setting_theme[i])) {
                                    index = i;
                                }
                            }
                            if (index == mAll_play_setting_theme.length - 1) {
                                tv_play_setting_content_theme_text.setText(mAll_play_setting_theme[0]);
                            } else {
                                tv_play_setting_content_theme_text.setText(mAll_play_setting_theme[index + 1]);
                            }
                            bt_theme_right_arrows
                                    .setImageResource(R.drawable.select_right_arrows_f);
                            Utils.showToast(context, R.string.Theme_successful, R.drawable.toast_smile);
                            break;
                    }
                } else if (event.getAction() == KeyEvent.ACTION_UP) {
                    switch (keyCode) {
                        case KeyEvent.KEYCODE_DPAD_LEFT:
                            bt_theme_left_arrows
                                    .setImageResource(R.drawable.select_left_arrows_n);
                            break;
                        case KeyEvent.KEYCODE_DPAD_RIGHT:
                            bt_theme_right_arrows
                                    .setImageResource(R.drawable.select_right_arrows_n);
                            break;
                    }
                }
                return false;
            }
        });


    }

    private void initData() {
        mAll_play_setting_decode = getResources().getStringArray(R.array.play_setting_decode);
        mAll_play_setting_playratio = getResources().getStringArray(R.array.play_setting_playratio);
        mAll_play_setting_definition = getResources().getStringArray(R.array.play_setting_definition);
        mAll_play_setting_jump = getResources().getStringArray(R.array.play_setting_jump);
        mAll_play_setting_jump_end = getResources().getStringArray(R.array.play_setting_jump_end);
        mAll_play_setting_core = getResources().getStringArray(R.array.play_setting_core);
        mAll_play_setting_live_core = getResources().getStringArray(R.array.play_setting_live_core);////
        mAll_play_setting_theme = getResources().getStringArray(R.array.play_setting_theme);
        String play_decode = sp.getString(Constant.mg, mAll_play_setting_decode[1]);
        String play_ratio = sp.getString(Constant.oh, mAll_play_setting_playratio[3]);
        String play_definition = sp.getString("play_definition", mAll_play_setting_definition[0]);
        String play_jump = sp.getString("play_jump", mAll_play_setting_jump[0]);
        String play_jump_end = sp.getString("play_jump_end", mAll_play_setting_jump_end[0]);
        String play_core = sp.getString(Constant.hd, mAll_play_setting_core[2]);
        String live_core = sp.getString(Constant.vy, mAll_play_setting_live_core[2]);////
//        String play_theme = sp.getString("play_theme", mAll_play_setting_theme[0]);
        int Interface_Style = SharePreferenceDataUtil.getSharedIntData(context, "Interface_Style", 4);

        String Style = "";
        if (Interface_Style == 0){
            Style = mAll_play_setting_theme[0];
        }else if (Interface_Style == 1){
            Style = mAll_play_setting_theme[1];
        }else if (Interface_Style == 2){
            Style = mAll_play_setting_theme[2];
        }else if (Interface_Style == 3){
            Style = mAll_play_setting_theme[3];
        }else if (Interface_Style == 4){
            Style = mAll_play_setting_theme[4];
        }
        play_theme = sp.getString("play_theme", Style);

        tv_play_setting_content_decode_text.setText(play_decode);
        tv_play_setting_content_playratio_text.setText(play_ratio);
        tv_play_setting_content_jump_text.setText(play_jump);
        tv_play_setting_content_jump_end_text.setText(play_jump_end);
        tv_play_setting_content_core_text.setText(play_core);
        tv_play_setting_content_live_core_text.setText(live_core);////
        tv_play_setting_content_theme_text.setText(play_theme);
    }

    private void savePlaySettingInfo() {
        Editor editor = sp.edit();
        if ("软解码".equals((String) tv_play_setting_content_decode_text.getText())) {
            editor.putInt("mIsHwDecode", 0);
        } else {
            editor.putInt("mIsHwDecode", 1);
        }


        if ("经典".equals((String) tv_play_setting_content_theme_text.getText())) {
            SharePreferenceDataUtil.setSharedIntData(SettingPlayActivity.this, "Interface_Style",0);
            SharePreferenceDataUtil.setSharedStringData(SettingPlayActivity.this, "User_Style","0");
        }else if("现代".equals((String) tv_play_setting_content_theme_text.getText())){
            SharePreferenceDataUtil.setSharedIntData(SettingPlayActivity.this, "Interface_Style",1);
            SharePreferenceDataUtil.setSharedStringData(SettingPlayActivity.this, "User_Style","1");
        }else if("现代圆角".equals((String) tv_play_setting_content_theme_text.getText())){
            SharePreferenceDataUtil.setSharedIntData(SettingPlayActivity.this, "Interface_Style",2);
            SharePreferenceDataUtil.setSharedStringData(SettingPlayActivity.this, "User_Style","2");
        }else if("经典圆角".equals((String) tv_play_setting_content_theme_text.getText())){
            SharePreferenceDataUtil.setSharedIntData(SettingPlayActivity.this, "Interface_Style",3);
            SharePreferenceDataUtil.setSharedStringData(SettingPlayActivity.this, "User_Style","3");
        }else if("无界".equals((String) tv_play_setting_content_theme_text.getText())){
            SharePreferenceDataUtil.setSharedIntData(SettingPlayActivity.this, "Interface_Style",4);
            SharePreferenceDataUtil.setSharedStringData(SettingPlayActivity.this, "User_Style","4");
        }

        editor.putString(Constant.mg, (String) tv_play_setting_content_decode_text.getText());
        editor.putString(Constant.oh, (String) tv_play_setting_content_playratio_text.getText());
        editor.putString("play_jump", (String) tv_play_setting_content_jump_text.getText());
        editor.putString("play_jump_end", (String) tv_play_setting_content_jump_end_text.getText());
        editor.putString(Constant.hd, (String) tv_play_setting_content_core_text.getText());
        editor.putString(Constant.vy, (String) tv_play_setting_content_live_core_text.getText());////
        editor.putString("play_theme", (String) tv_play_setting_content_theme_text.getText());
        editor.commit();


        if (!play_theme.equals((String) tv_play_setting_content_theme_text.getText())){
//            Intent intent = getBaseContext().getPackageManager().getLaunchIntentForPackage(getBaseContext().getPackageName());
//            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
//            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
//            PendingIntent pendingIntent = PendingIntent.getActivity(getBaseContext(), 0, intent, PendingIntent.FLAG_ONE_SHOT);
//            AlarmManager alarmManager = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
////        alarmManager.set(AlarmManager.RTC, System.currentTimeMillis() + 100, pendingIntent);
//            alarmManager.set(AlarmManager.RTC, System.currentTimeMillis() + 5, pendingIntent);
//            System.exit(1);
        }
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
}
