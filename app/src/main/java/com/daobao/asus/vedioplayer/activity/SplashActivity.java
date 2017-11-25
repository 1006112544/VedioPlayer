package com.daobao.asus.vedioplayer.activity;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Handler;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.MotionEvent;

import com.daobao.asus.vedioplayer.R;

import java.util.Timer;
import java.util.TimerTask;


public class SplashActivity extends Activity {
    private boolean hadPower = false;
    private Timer timer;
    private  TimerTask task;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);
        timer = new Timer();
        task = new TimerTask() {
            @Override
            public void run() {
                setMainActivity();
            }
        };
        timer.schedule(task,2000);
        requestPermission();
    }
    boolean startMainActivity = false;
    private void setMainActivity() {
        if(!startMainActivity) {
            startMainActivity = true;
            Intent intent = new Intent(this,MainActivity.class);
            intent.putExtra("SplashActivity",hadPower);
            startActivity(intent);
            finish();
        }
    }

    public boolean onTouchEvent(MotionEvent event) {
        setMainActivity();
        return super.onTouchEvent(event);
    }
    /**
     * 请求授权
     */
    private void requestPermission(){
        if (Build.VERSION.SDK_INT >= 23) {
            int checkCallPhonePermission = ContextCompat.checkSelfPermission
                    (this, Manifest.permission.CALL_PHONE);
            if (checkCallPhonePermission != PackageManager.PERMISSION_GRANTED) {
                task.cancel();
                ActivityCompat.requestPermissions(this,
                        new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, 1);
            }
            else{
                //sendHomework();
            }
        }
        else {
            hadPower = true;
        }
    }
    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions,int[] grantResults) {
        switch (requestCode) {
            case 1:
                if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    //sendHomework();
                    hadPower = true;
                    setMainActivity();
                }
                else {
                    //showToast("fail");
                    setMainActivity();
                }
                break;
            default:
                super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        }
    }
}


