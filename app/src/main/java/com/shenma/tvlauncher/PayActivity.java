package com.shenma.tvlauncher;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Button;
import com.shenma.tvlauncher.utils.Utils;

/**
 * 支付页面 - WebView加载支付链接
 */
public class PayActivity extends Activity {

    public static final String EXTRA_PAY_URL = "pay_url";

    private WebView payWebView;
    private Button payBackBtn;
    private Button payRefreshBtn;
    private String payUrl;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pay);

        initViews();
        loadPayPage();
    }

    @SuppressLint("SetJavaScriptEnabled")
    private void initViews() {
        payWebView = (WebView) findViewById(R.id.pay_webview);
        payBackBtn = (Button) findViewById(R.id.pay_back_btn);
        payRefreshBtn = (Button) findViewById(R.id.pay_refresh_btn);

        /*WebView设置*/
        WebSettings webSettings = payWebView.getSettings();
        webSettings.setJavaScriptEnabled(true);
        webSettings.setDomStorageEnabled(true);
        webSettings.setUseWideViewPort(true);
        webSettings.setLoadWithOverviewMode(true);
        webSettings.setSupportZoom(true);
        webSettings.setBuiltInZoomControls(true);
        webSettings.setDisplayZoomControls(false);
        /*Android 5.0以上支持混合内容*/
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.LOLLIPOP) {
            webSettings.setMixedContentMode(WebSettings.MIXED_CONTENT_ALWAYS_ALLOW);
        }

        payWebView.setWebViewClient(new WebViewClient() {
            @Override
            public boolean shouldOverrideUrlLoading(WebView view, String url) {
                /*拦截微信/支付宝scheme跳转*/
                if (url.startsWith("weixin://") || url.startsWith("alipays://") || url.startsWith("alipay://")) {
                    try {
                        Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
                        startActivity(intent);
                    } catch (Exception e) {
                        Utils.showToast(PayActivity.this, "请安装微信或支付宝", R.drawable.toast_err);
                    }
                    return true;
                }
                return false;
            }
        });

        payBackBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                /*直接关闭Activity，不管WebView历史*/
                payWebView.stopLoading();
                payWebView.loadUrl("about:blank");
                finish();
            }
        });

        payRefreshBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (payWebView != null) {
                    payWebView.reload();
                }
            }
        });
    }

    private void loadPayPage() {
        payUrl = getIntent().getStringExtra(EXTRA_PAY_URL);
        if (payUrl != null && !payUrl.isEmpty()) {
            payWebView.loadUrl(payUrl);
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (payWebView != null) {
            payWebView.destroy();
        }
    }

    @Override
    public void onBackPressed() {
        finish();
    }
}
