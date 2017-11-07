package com.daobao.asus.vedioplayer.pager;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.webkit.WebChromeClient;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;

import com.daobao.asus.vedioplayer.R;

/**
 * Created by ASUS on 2017/7/15.
 */

public class NetVedioActivity extends Activity implements View.OnClickListener{
    private WebView webView;
    private ImageButton netvedio_back;
    private ImageButton netvedio_next;
    private ImageButton netvedio_home;
    private RelativeLayout netvedio_Internet;
    private LinearLayout netvedio_tapbar;
    private boolean HaveNet = false;
    private ProgressDialog dialog;
    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.netvedio);
        Log.d("网络视频", "网络视频数据更新");
        webView = findViewById(R.id.netvedio_webView);
        netvedio_back = findViewById(R.id.netvedio_back);
        netvedio_next = findViewById(R.id.netvedio_next);
        netvedio_Internet = findViewById(R.id.netvedio_Internet);
        netvedio_tapbar = findViewById(R.id.netvedio_tapbar);
        netvedio_home = findViewById(R.id.netvedio_home);
        netvedio_home.setOnClickListener(this);
        netvedio_back.setOnClickListener(this);
        netvedio_next.setOnClickListener(this);
        HaveInternet();
        if(HaveNet)
        {
            initWebView();
        }
    }
    public void initWebView()
    {
        webView.getSettings().setJavaScriptEnabled(true);
        webView.loadUrl("http://video.m.baidu.com/#/");
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
                    dialog = new ProgressDialog(NetVedioActivity.this);
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
    private void HaveInternet()
    {
        if(!isNetworkAvailable(NetVedioActivity.this))
        {
            netvedio_Internet.setVisibility(View.VISIBLE);
            webView.setVisibility(View.GONE);
            netvedio_tapbar.setVisibility(View.GONE);
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
                    System.out.println(i + "===状态===" + networkInfo[i].getState());
                    System.out.println(i + "===类型===" + networkInfo[i].getTypeName());
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
    public void onClick(View view) {
        if(view.getId() == R.id.netvedio_back)
        {
            if(webView.canGoBack())
            {
                webView.goBack();
            }
        }
        else if(view.getId() == R.id.netvedio_next)
        {
            if(webView.canGoForward())
            {
                webView.goForward();
            }
        }
        else if(view.getId() == R.id.netvedio_home)
        {
            finish();
        }
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
