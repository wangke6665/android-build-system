package com.shenma.tvlauncher;

import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnFocusChangeListener;
import android.widget.RelativeLayout;
import android.widget.TextView;
import com.shenma.tvlauncher.utils.CacheDataManager;
import com.shenma.tvlauncher.utils.Constant;
import com.shenma.tvlauncher.utils.SharePreferenceDataUtil;
import com.shenma.tvlauncher.utils.Utils;
import com.shenma.tvlauncher.vod.dao.VodDao;

/**
 * @author joychang
 * @Description 清理记录
 */
public class ClearActivity extends BaseActivity {

    private RelativeLayout clear_setting_content_decode, clear_setting_content_definition,
            clear_setting_content_playratio, clear_setting_content_jump, clear_setting_other ,clear_setting_theme;
    private TextView all_cache_clear_tv;

    private VodDao dao;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.layout_setting_clear);
        findViewById(R.id.setting_clear).setBackgroundResource(R.drawable.video_details_bg);
        initView();
    }

    @Override
    protected void initView() {
        dao = new VodDao(this);
        findViewById();
        setListener();
    }

    @Override
    protected void loadViewLayout() {

    }

    @Override
    protected void findViewById() {
        clear_setting_content_decode = (RelativeLayout) findViewById(R.id.clear_setting_content_decode);
        clear_setting_content_definition = (RelativeLayout) findViewById(R.id.clear_setting_content_definition);
        clear_setting_content_playratio = (RelativeLayout) findViewById(R.id.clear_setting_content_playratio);
        clear_setting_content_jump = (RelativeLayout) findViewById(R.id.clear_setting_content_jump);
        clear_setting_other = (RelativeLayout) findViewById(R.id.clear_setting_other);
        all_cache_clear_tv = (TextView) findViewById(R.id.all_cache_clear_tv);
//        clear_setting_theme = (RelativeLayout) findViewById(R.id.clear_setting_theme);//更换主题
//        if (SharePreferenceDataUtil.getSharedIntData(this, "Allow_changing_styles", 0) == 0){
//            clear_setting_theme.setVisibility(View.GONE);
//        }
    }

    @Override
    protected void setListener() {
        clear_setting_content_decode.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                sp.edit().putString("wallpaperFileName", "").commit();
                Utils.showToast(context, R.string.Personality_settings_cleared_successfully, R.drawable.toast_smile);
            }
        });
        clear_setting_content_definition.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                dao.deleteAllByWhere(Constant.TYPE_SC);
                Utils.showToast(context, R.string.Collection_record_cleaning_successful, R.drawable.toast_smile);
            }
        });
        clear_setting_content_playratio.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                dao.deleteAllByWhere(Constant.TYPE_LS);
                Utils.showToast(context, R.string.Playback_record_cleaning_successful, R.drawable.toast_smile);
            }
        });
        clear_setting_content_jump.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                dao.deleteAllByWhere(Constant.TYPE_ZJ);
                Utils.showToast(context, R.string.Successfully_cleared_drama_tracking_records, R.drawable.toast_smile);
            }
        });
        all_cache_clear_tv.setOnFocusChangeListener(new OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if (hasFocus) {
                    all_cache_clear_tv.setTextColor(0xFF000000);
                } else {
                    all_cache_clear_tv.setTextColor(0xFFFFFFFF);
                }
            }
        });
        clear_setting_other.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                CacheDataManager.clearAllCache(context);
                Utils.showToast(ClearActivity.this.context, R.string.Data_cache_cleaning_successful, R.drawable.toast_smile);
            }
        });
//        clear_setting_theme.setOnClickListener(new OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                if (SharePreferenceDataUtil.getSharedIntData(ClearActivity.this, "Interface_Style", 0) == 0 ){
//                    SharePreferenceDataUtil.setSharedIntData(ClearActivity.this, "Interface_Style",1);//// 1=新ui
//                    SharePreferenceDataUtil.setSharedStringData(ClearActivity.this, "User_Style","1");
//                }else {
//                    SharePreferenceDataUtil.setSharedIntData(ClearActivity.this, "Interface_Style",0);//// 0=旧ui
//                    SharePreferenceDataUtil.setSharedStringData(ClearActivity.this, "User_Style","0");
//                }
//                Utils.showToast(context, "主题更换成功,重启APP后生效!", R.drawable.toast_smile);
//            }
//        });

        all_cache_clear_tv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                sp.edit().putString("wallpaperFileName", "").commit();
                ClearActivity.this.dao.deleteAllByWhere(0);
                ClearActivity.this.dao.deleteAllByWhere(1);
                ClearActivity.this.dao.deleteAllByWhere(2);
                CacheDataManager.clearAllCache(context);
                Utils.showToast(ClearActivity.this.context, R.string.Cleanup_successful, R.drawable.toast_smile);
            }
        });
    }

}
