package com.daobao.asus.vedioplayer.pager;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.graphics.Bitmap;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.WindowManager;
import android.webkit.WebChromeClient;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.daobao.asus.vedioplayer.R;


/**
 * Created by ASUS on 2017/7/14.
 */

public class NetActivity extends Activity implements View.OnClickListener{
    private WebView webView;
    private ImageButton net_back;
    private ImageButton net_next;
    private ImageButton net_home;
    private RelativeLayout net_Internet;
    private LinearLayout net_tapbar;
    private ProgressDialog dialog;
    private boolean HaveNet = false;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.net);
        Log.d("网络娱乐", "网络娱乐数据更新");
        net_back = findViewById(R.id.net_back);
        net_next = findViewById(R.id.net_next);
        net_Internet = findViewById(R.id.net_Internet);
        net_tapbar = findViewById(R.id.net_tapbar);
        webView = findViewById(R.id.net_webView);
        net_home = findViewById(R.id.net_home);
        net_home.setOnClickListener(this);
        net_back.setOnClickListener(this);
        net_next.setOnClickListener(this);
        HaveInternet();
        if(HaveNet)
        {
            initWebView();
        }
    }
    public void initWebView()
    {
        String url = "http://m.neihanshequ.com";
        //是webview可以响应javaScript
        webView.getSettings().setJavaScriptEnabled(true);
        webView.loadUrl(url);
        webView.setWebViewClient(new WebViewClient(){
            @Override
            public boolean shouldOverrideUrlLoading(WebView view, String url) {
                //如果返回true则由webview打开页面 否则调用第三方浏览器
                view.loadUrl(url);
                return true;
            }
        });
        webView.setWebChromeClient(new WebChromeClient(){
            @Override
            public void onProgressChanged(WebView view, int newProgress) {
                //newProgress 1-100的整数
                if(newProgress==100)
                {
                    //网页加载完毕,关闭ProgressDialog
                    closeDialog();
                }
                else
                {
                    //网页正在加载,开启ProgressDialog
                    openDialog(newProgress);
                }
            }
            private void closeDialog()
            {
                if(dialog!=null&&dialog.isShowing())
                {
                    dialog.dismiss();
                    dialog = null;
                }
            }
            private void openDialog(int newProgress)
            {
                if(dialog==null)
                {
                    dialog = new ProgressDialog(NetActivity.this);
                    dialog.setTitle("正在加载");
                    dialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
                    dialog.setProgress(newProgress);
                    dialog.show();
                }
                else
                {
                    dialog.setProgress(newProgress);
                }
            }
        });
    }
    @Override
    public void onClick(View view) {
        if(view.getId() == R.id.net_back)
        {
            if(webView.canGoBack())
            {
                webView.goBack();
            }
        }
        else if(view.getId() == R.id.net_next)
        {
            if(webView.canGoForward())
            {
                webView.goForward();
            }
        }
        else if(view.getId() == R.id.net_home)
        {
            finish();
        }
    }
    private void HaveInternet()
    {
        if(!isNetworkAvailable(NetActivity.this))
        {
            net_Internet.setVisibility(View.VISIBLE);
            webView.setVisibility(View.GONE);
            net_tapbar.setVisibility(View.GONE);
        }
        else HaveNet = true;
    }
    //判断是否有网络连接
    public boolean isNetworkAvailable(Activity activity)
    {
        Context context = activity.getApplicationContext();
        // 获取手机所有连接管理对象（包括对wi-fi,net等连接的管理）
        ConnectivityManager connectivityManager =
                (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);

        if (connectivityManager == null)
        {
            return false;
        }
        else
        {
            // 获取NetworkInfo对象
            NetworkInfo[] networkInfo = connectivityManager.getAllNetworkInfo();

            if (networkInfo != null && networkInfo.length > 0)
            {
                for (int i = 0; i < networkInfo.length; i++)
                {
                    // 判断当前网络状态是否为连接状态
                    if (networkInfo[i].getState() == NetworkInfo.State.CONNECTED)
                    {
                        return true;
                    }
                }
            }
        }
        return false;
    }
    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if(keyCode == KeyEvent.KEYCODE_BACK)
        {
            if(webView.canGoBack())
            {
                webView.goBack();
                return true;
            }
            else
            {
                finish();
                return true;
            }
        }
        return true;
    }
    @Override
    protected void onDestroy() {
        webView.destroy();
        super.onDestroy();
    }
}
