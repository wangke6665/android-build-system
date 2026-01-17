package com.shenma.tvlauncher;

import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.widget.ImageView;
import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.HurlStack;
import com.shenma.tvlauncher.network.ExtHttpStack;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.bumptech.glide.Glide;
import com.shenma.tvlauncher.utils.Constant;
import com.shenma.tvlauncher.utils.GetTimeStamp;
import com.shenma.tvlauncher.utils.Rc4;
import com.shenma.tvlauncher.utils.SharePreferenceDataUtil;
import org.json.JSONException;
import org.json.JSONObject;
import java.util.HashMap;
import java.util.Map;

import static com.shenma.tvlauncher.utils.Rc4.encry_RC4_string;

/**
 * @author joychang
 * @Description 关于我们
 */
public class AboutActivity extends BaseActivity {
	private final String TAG = "SearchActivity";
	public RequestQueue mQueue;
	private ImageView about;
	private String url;
	private final Handler mediaHandler = new Handler() {
		public void handleMessage(Message msg) {
			switch (msg.what) {
				case 1:
					loadImg();
					return;
				default:
					return;
			}
		}
	};
	private String Api_url = Rc4.decry_RC4(SharePreferenceDataUtil.getSharedStringData(this, "Api_url", ""),Constant.d);
	private String BASE_HOST = Rc4.decry_RC4(SharePreferenceDataUtil.getSharedStringData(this, "BASE_HOST", ""), Constant.d);

	/*创建时的回调函数*/
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.layout_setting_about);
		findViewById();
		initData();
	}

	/*初始化视图*/
	protected void initView() {
	}

	/*加载视图布局*/
	protected void loadViewLayout() {
	}

	/*按ID查找视图*/
	protected void findViewById() {
		findViewById(R.id.setting_about).setBackgroundResource(R.drawable.video_details_bg);
		about = (ImageView) findViewById(R.id.about_iv);
	}

	/*设置侦听器*/
	protected void setListener() {
	}

	/*初始化数据*/
	private void initData() {
		GetAbout();
	}

	/*请求关于*/
	private void GetAbout() {
		mQueue = Volley.newRequestQueue(this, new ExtHttpStack());
		StringRequest stringRequest = new StringRequest(Request.Method.POST, Api_url + "/api.php/" + BASE_HOST +"/about?app=" + Api.APPID,
				new com.android.volley.Response.Listener<String>() {
					public void onResponse(String response) {
						GetAboutResponse(response);
					}
				}, new com.android.volley.Response.ErrorListener() {
			public void onErrorResponse(VolleyError error) {
				//Error(error);
			}
		}) {
			@Override
			protected Map<String, String> getParams() throws AuthFailureError {
				Map<String, String> params = new HashMap<>();
				params.put("time", GetTimeStamp.timeStamp());
				params.put("key", encry_RC4_string(GetTimeStamp.timeStamp(),GetTimeStamp.timeStamp()));
				return params;
			}

			@Override
			public Map<String, String> getHeaders() throws AuthFailureError {
				Map<String, String> headers = new HashMap<>();
				headers.put("Authorization", Rc4.decry_RC4(SharePreferenceDataUtil.getSharedStringData(AboutActivity.this, "Authorization", ""),Constant.d));  /*设置其他请求头*/
				return headers;
			}
		};
		mQueue.add(stringRequest);

	}

	/*关于响应*/
	public void GetAboutResponse(String response) {
		//Log.i(TAG, "GetAboutResponse: " + response);
		try {
			JSONObject jSONObject = new JSONObject(response);
			int code = jSONObject.optInt("code");/*状态码*/
			if (code == 200){
				url = jSONObject.optString("url");/*消息*/
				mediaHandler.sendEmptyMessage(1);
			}
		} catch (JSONException e) {
			e.printStackTrace();
		}
	}

	/*加载图片*/
	private void loadImg() {
		Glide.with(this).load(url).into(about);
	}

	/*按下返回键时*/
	@Override
	public void onBackPressed() {
		super.onBackPressed();
		finish();
	}

}
