package com.daobao.asus.vedioplayer.Player;

import android.app.Activity;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.Nullable;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ImageButton;
import android.widget.RelativeLayout;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.VideoView;

import com.daobao.asus.vedioplayer.R;
import com.daobao.asus.vedioplayer.domain.MedioItem;
import com.daobao.asus.vedioplayer.pager.SearchPager;
import com.daobao.asus.vedioplayer.pager.VedioPager;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Created by ASUS on 2017/6/30.
 */

public class SystemVedioPlayer extends Activity implements View.OnClickListener{
    private Uri uri;
    private VideoView videoView;
    private ImageButton videoplayer_back;
    private ImageButton videoplayer_start;
    private SeekBar videoplayer_seekbar;
    private TextView videoplayer_systemtime;
    private TextView videoplayer_sumtime;
    private TextView videoplayer_nowtime;
    private TextView videoplayer_title;
    private ImageButton videoplayer_previous;
    private ImageButton videoplayer_next;
    private ImageButton IsLock;
    private TextView time_text;
    private int position;
    private RelativeLayout myController;
    boolean isVisiblity = false;
    private int state;//用于监听播放器所处阶段
    public static int PLAY=1,STOP=2;
    private boolean SeekBarFlag = true;
    private boolean Lock = false;//监听是否锁定MediaController
    private ArrayList<MedioItem> medioItems;
    private String FromPager;
    Handler handler = new Handler(){
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            videoplayer_nowtime.setText(formatTime(videoView.getCurrentPosition()));
        }
    };
    ExecutorService es = Executors.newSingleThreadExecutor();//创建一个单线程的线程池,确保线程只启动一次
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.systemplayer_layout);
        init();
        uri = getIntent().getData();
        Bundle bundle = this.getIntent().getExtras();
        position = bundle.getInt("vedioActivity");
        FromPager = bundle.getString("FromPager");
        if(FromPager.equals("VideoPager"))
        {
            medioItems = VedioPager.medioItems;
        }
        else if(FromPager.equals("SearchPager"))
        {
            medioItems = SearchPager.medioItems;
        }
        if(uri!=null)
        {
            videoView.setVideoURI(uri);
        }
    }
    public void init()
    {
        myController = findViewById(R.id.myController);
        videoView = (VideoView) findViewById(R.id.videoView);
        //准备好监听
        videoView.setOnPreparedListener(new MyOnPreparedListener());
        //播放出错监听
        videoView.setOnErrorListener(new MyOnErrorListener());
        //播放完成监听
        videoView.setOnCompletionListener(new MyOnCompletionListener());
        videoplayer_back = findViewById(R.id.videoplayer_back);
        videoplayer_start = findViewById(R.id.videoplayer_start);
        videoplayer_seekbar = findViewById(R.id.videoplayer_seekbar);
        videoplayer_systemtime = findViewById(R.id.videoplayer_systemtime);
        videoplayer_sumtime = findViewById(R.id.videoplayer_sumtime);
        videoplayer_title = findViewById(R.id.videoplayer_title);
        videoplayer_next = findViewById(R.id.videoplayer_next);
        videoplayer_previous = findViewById(R.id.videoplayer_previous);
        videoplayer_nowtime = findViewById(R.id.videoplayer_nowtime);
        IsLock = findViewById(R.id.IsLock);
        time_text = findViewById(R.id.time_text);
        IsLock.setOnClickListener(this);
        videoplayer_back.setOnClickListener(this);
        videoplayer_start.setOnClickListener(this);
        videoplayer_next.setOnClickListener(this);
        videoplayer_previous.setOnClickListener(this);
        videoplayer_seekbar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int i, boolean fromUser) {
                if(fromUser)
                {
                    videoView.seekTo(i);
                }
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
                videoView.pause();
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                videoView.start();
            }
        });
    }


    public void onClick(View view) {
        if(view.getId()==R.id.videoplayer_back)
        {
            finish();
        }
        else if(view.getId()==R.id.videoplayer_start)
        {
            if(state == PLAY)
            {
                state = STOP;
                videoplayer_start.setImageResource(R.drawable.video_start);
                videoView.pause();
            }
            else if(state == STOP)
            {
                state = PLAY;
                videoplayer_start.setImageResource(R.drawable.video_stop);
                videoView.start();
            }
        }
        else if(view.getId()==R.id.videoplayer_next)
        {
            next();
            videoplayer_start.setImageResource(R.drawable.video_stop);
        }
        else if(view.getId()==R.id.videoplayer_previous)
        {
            previous();
            videoplayer_start.setImageResource(R.drawable.video_stop);
        }
        else if(view.getId()==R.id.IsLock)
        {
            if(Lock)
            {
                IsLock.setImageResource(R.drawable.unlock);
                Lock = false;
            }
            else
            {
                IsLock.setImageResource(R.drawable.lock);
                Lock = true;
            }
        }
    }

    class MyOnPreparedListener implements MediaPlayer.OnPreparedListener
    {
        //资源准备好后调用
        @Override
        public void onPrepared(MediaPlayer mediaPlayer) {
            videoView.start();
            videoplayer_title.setText(medioItems.get(position).getName());
            state = PLAY;
            videoplayer_sumtime.setText(formatTime(videoView.getDuration()));
            initSeekBar();
            //启动线程保证只有一个线程执行
            es.execute(new SeekBarThread());
            /**
             * 通过handler来控制当前播放时间的更新
             */
            new Thread(){
                public void run()
                {
                    try {
                        while (SeekBarFlag)
                        {
                            handler.sendEmptyMessage(1);
                            Thread.sleep(1000);
                        }
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }.start();
        }
    }
    class MyOnErrorListener implements MediaPlayer.OnErrorListener
    {
        @Override
        public boolean onError(MediaPlayer mediaPlayer, int i, int i1) {
            Toast.makeText(SystemVedioPlayer.this, "播放出错", Toast.LENGTH_SHORT).show();
            return false;
        }
    }
    class MyOnCompletionListener implements MediaPlayer.OnCompletionListener
    {
        @Override
        public void onCompletion(MediaPlayer mediaPlayer) {
            Toast.makeText(SystemVedioPlayer.this, "播放完成", Toast.LENGTH_SHORT).show();
            next();
        }
    }
    private float startX,startY,offsetX,offsetY;
    public boolean onTouchEvent(MotionEvent event) {
        showMediaController();
        switch(event.getAction())
        {
            case MotionEvent.ACTION_DOWN:
                startX = event.getX();
                startY = event.getY();
                break;
            case MotionEvent.ACTION_UP:
                offsetX = event.getX()-startX;
                offsetY = event.getY()-startY;
                if(Math.abs(offsetX)>Math.abs(offsetY))
                {
                    if(offsetX<-10&&!Lock)
                    {
                        if(videoView.isPlaying ())
                        {
                            videoView.seekTo(videoView.getCurrentPosition()-(int)Math.abs(offsetX)*70);
                            time_text.setText("退后了"+(int)Math.abs(offsetX)*70/1000+"秒");
                            time_text.setVisibility(View.VISIBLE);
                            new Handler().postDelayed(new Runnable(){
                                public void run()
                                {
                                    time_text.setVisibility(View.GONE);
                                }
                            },1000);
                        }
                    }
                    else if(offsetX>10&&!Lock)
                    {
                        if(videoView.isPlaying ())
                        {
                            videoView.seekTo(videoView.getCurrentPosition()+(int)offsetX*70);
                            time_text.setText("快进了"+(int)offsetX*70/1000+"秒");
                            time_text.setVisibility(View.VISIBLE);
                            new Handler().postDelayed(new Runnable(){
                                public void run()
                                {
                                    time_text.setVisibility(View.GONE);
                                }
                            },1000);
                        }
                    }
                }
        }
        return super.onTouchEvent(event);
    }
    public void showMediaController()
    {
        Date date = new Date();
        videoplayer_systemtime.setText(new SimpleDateFormat("HH:mm").format(date));
        if(!isVisiblity)
        {
            IsLock.setVisibility(View.VISIBLE);
            if(!Lock)
            {
                myController.setVisibility(View.VISIBLE);
            }
            new Thread(){
                public void run()
                {
                    try {
                        Thread.sleep(500);
                        isVisiblity = true;
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }.start();
        }
        else
        {
            IsLock.setVisibility(View.GONE);
            myController.setVisibility(View.GONE);
            new Thread(){
                public void run()
                {
                    try {
                        Thread.sleep(500);
                        isVisiblity = false;
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }.start();
        }
    }
    public void initSeekBar()
    {
        videoplayer_seekbar.setMax(videoView.getDuration());
        videoplayer_seekbar.setProgress(0);
    }
    class SeekBarThread implements Runnable
    {
        public SeekBarThread()
        {
            SeekBarFlag = true;
        }
        public void run() {
            while (SeekBarFlag) {
                if (videoView.getCurrentPosition() < videoView.getDuration()) {
                    videoplayer_seekbar.setProgress(videoView.getCurrentPosition());
                } else {
                    SeekBarFlag = false;
                }
                try {
                    Thread.sleep(500);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }

    }
    public void next()
    {
        if(position<medioItems.size()-1)
        {
            position++;
        }
        else
        {
            position=0;
        }
        videoView.setVideoURI(Uri.parse(medioItems.get(position).getData()));
    }
    public void previous()
    {
        if(position>0)
        {
            position--;
        }
        else
        {
            position=medioItems.size()-1;
        }
        videoView.setVideoURI(Uri.parse(medioItems.get(position).getData()));
    }
    /**
     * tool 格式化时间
     */
    public String formatTime(long time)
    {
        time = time/ 1000;
        String strHour = "" + (time/3600);
        String strMinute = "" + time%3600/60;
        String strSecond = "" + time%3600%60;

        strHour = strHour.length() < 2? "0" + strHour: strHour;
        strMinute = strMinute.length() < 2? "0" + strMinute: strMinute;
        strSecond = strSecond.length() < 2? "0" + strSecond: strSecond;

        String strRsult = "";

        if (!strHour.equals("00"))
        {
            strRsult += strHour + ":";
        }

        if (!strMinute.equals("00"))
        {
            strRsult += strMinute + ":";
        }

        strRsult += strSecond;
        if(strMinute.equals("00")&&!strSecond.equals(00))
        {
            strRsult = "00:"+strSecond;
        }
        return strRsult;
    }
}
