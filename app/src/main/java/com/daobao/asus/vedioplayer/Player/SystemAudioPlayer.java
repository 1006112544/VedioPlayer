package com.daobao.asus.vedioplayer.Player;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.Nullable;
import android.text.format.Formatter;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.CompoundButton;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.SeekBar;
import android.widget.Switch;
import android.widget.TextView;
import com.daobao.asus.vedioplayer.R;
import com.daobao.asus.vedioplayer.activity.MainActivity;
import com.daobao.asus.vedioplayer.domain.MedioItem;
import com.daobao.asus.vedioplayer.pager.AudioPager;
import com.daobao.asus.vedioplayer.pager.SearchPager;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Random;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;


/**
 * Created by ASUS on 2017/6/30.
 */

public class SystemAudioPlayer extends Activity implements View.OnClickListener {
    private TextView music_name;
    private TextView music_airtist;
    private TextView music_time;
    private TextView music_nowtime;
    private ImageButton previous;
    private ImageButton next;
    private ImageButton start;
    private ImageButton loop;
    private ImageButton back_power;
    private SeekBar seekBar;
    private MediaPlayer mediaPlayer;
    private ImageButton audio_function;
    private LinearLayout audio_function_panel;
    private TextView close_15;
    private TextView close_30;
    private TextView close_45;
    private TextView close_60;
    private LinearLayout audio_close;
    private ImageButton music_list_btn;
    private LinearLayout music_list;
    private ListView music_list_listview;
    private ImageButton music_list_cancel;
    private TextView music_list_summusic;
    private SeekBar music_sound_seekbar;
    private Switch mSwitch;
    private TextView audio_close_time;
    private AudioManager mAudioManager;//音量管理器
    private MyVolumeReceiver mVolumeReceiver;//系统音量改变广播
    private ArrayList<MedioItem> medioItems;
    private int position;
    private int state;
    private int Loopstate = 1;
    public static int LOOP = 1, RANDOM = 2, SINGLE = 3;
    public static int PLAY = 1, STOP = 2, EMPTY = 3;
    private MedioItem medioItem;
    private String FromPager;
    private boolean SeekBarFlag;//用来控制seekbar移动的标志
    private boolean IsClose = false;//用于监听用户是否设置了定时关机
    private boolean Function_IsShow = false;//用于监控function_panel是否显示
    private boolean MusicList_IsShow = false;//用于监控musicList是否显示
    private long CloseTime = 15*60000;
    int maxVolume;//最大音量(音量调节)
    int currentVolume;//当前音量(音量调节)
    ExecutorService es = Executors.newSingleThreadExecutor();//创建一个单线程的线程池,确保线程只启动一次
    ExecutorService TimeEs = Executors.newSingleThreadExecutor();//创建一个单线程的线程池,确保线程只启动一次
    private Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            if(msg.what==1)
            {
                music_nowtime.setText(formatTime(mediaPlayer.getCurrentPosition()));
            }
            else if(msg.what==2)
            {
                if(CloseTime!=0)
                {
                    audio_close_time.setText(formatTime(CloseTime));
                }
                else
                {
                    MainActivity.agentApplication.onTerminate();
                }
            }
        }
    };

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.systemplayer_audio_layout);
        init();
        initAudioManager();
        initRegisterReceiver();
        Bundle bundle = this.getIntent().getExtras();
        MainActivity.agentApplication.addActivity(this);
        position = bundle.getInt("AudioPager");
        FromPager =  bundle.getString("FromPager");
        if(FromPager.equals("AudioPager"))
        {
            medioItems = AudioPager.medioItems;
        }
        else if(FromPager.equals("SearchPager"))
        {
            medioItems = SearchPager.medioItems;
        }
        music_list_listview.setAdapter(new MusicListAdapter());
        music_list_listview.setOnItemClickListener(new MusicListItemOnClickListener());
        music_list_summusic.setText("共 " + medioItems.size() + " 首");
        start();
    }
    public void init() {
        music_name = findViewById(R.id.music_name);
        music_airtist = findViewById(R.id.music_airtist);
        music_time = findViewById(R.id.music_time);
        previous = findViewById(R.id.btn_previous);
        next = findViewById(R.id.btn_next);
        start = findViewById(R.id.btn_start);
        loop = findViewById(R.id.loop);
        seekBar = findViewById(R.id.music_seekbar);
        back_power = findViewById(R.id.back_power);
        music_nowtime = findViewById(R.id.music_nowtime);
        audio_function = findViewById(R.id.audio_function);
        audio_function_panel = findViewById(R.id.audio_function_panel);
        close_15 = findViewById(R.id.close_15);
        close_30 = findViewById(R.id.close_30);
        close_45 = findViewById(R.id.close_45);
        close_60 = findViewById(R.id.close_60);
        audio_close = findViewById(R.id.audio_close);
        music_list_btn = findViewById(R.id.music_list_btn);
        music_list = findViewById(R.id.music_list);
        music_list_listview = findViewById(R.id.music_list_listview);
        music_list_cancel = findViewById(R.id.music_list_cancel);
        music_list_summusic = findViewById(R.id.music_list_summusic);
        music_sound_seekbar = findViewById(R.id.music_sound_seekbar);
        audio_close_time = findViewById(R.id.audio_close_time);
        mSwitch = findViewById(R.id.mSwitch);
        MyCloseListenner myCloseListenner = new MyCloseListenner();
        close_15.setOnClickListener(myCloseListenner);
        close_30.setOnClickListener(myCloseListenner);
        close_45.setOnClickListener(myCloseListenner);
        close_60.setOnClickListener(myCloseListenner);
        music_list_cancel.setOnClickListener(myCloseListenner);
        music_list_btn.setOnClickListener(myCloseListenner);
        audio_function.setOnClickListener(this);
        start.setOnClickListener(this);
        next.setOnClickListener(this);
        previous.setOnClickListener(this);
        loop.setOnClickListener(this);
        back_power.setOnClickListener(this);
        mSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean isCheched) {
                if(isCheched)
                {
                    audio_close.setVisibility(View.VISIBLE);
                    if(!IsClose)
                    {
                        CreatCloseThread();
                    }
                    IsClose = true;
                }
                else
                {
                    IsClose = false;
                    audio_close.setVisibility(View.GONE);
                }
            }
        });
        music_sound_seekbar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                if (fromUser) {
                    mAudioManager.setStreamVolume(AudioManager.STREAM_MUSIC, progress, 0);
                }
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });
        seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                if (fromUser && state != EMPTY) {
                    mediaPlayer.seekTo(progress);
                }
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
                if (mediaPlayer != null) {
                    mediaPlayer.pause();
                }
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                if (mediaPlayer != null) {
                    mediaPlayer.start();
                }
            }
        });
    }

    //监听定时关闭及musicList按钮
    class MyCloseListenner implements View.OnClickListener {

        @Override
        public void onClick(View view) {
            if (view.getId() == R.id.close_15) {
                audio_close.setVisibility(View.VISIBLE);
                if(!IsClose)
                {
                    CloseTime = 15*60000;
                    CreatCloseThread();
                    mSwitch.setChecked(true);
                }
                else CloseTime = 15*60000;
                IsClose = true;
            } else if (view.getId() == R.id.close_30) {
                audio_close.setVisibility(View.VISIBLE);
                if(!IsClose)
                {
                    CloseTime = 30*60000;
                    CreatCloseThread();
                    mSwitch.setChecked(true);
                }
                else CloseTime = 30*60000;
                IsClose = true;
            } else if (view.getId() == R.id.close_45) {
                audio_close.setVisibility(View.VISIBLE);
                if(!IsClose)
                {
                    CloseTime = 45*60000;
                    CreatCloseThread();
                    mSwitch.setChecked(true);
                }
                else CloseTime = 45*60000;
                IsClose = true;
            } else if (view.getId() == R.id.close_60) {
                audio_close.setVisibility(View.VISIBLE);
                if(!IsClose)
                {
                    CloseTime = 60*60000;
                    CreatCloseThread();
                    mSwitch.setChecked(true);
                }
                else CloseTime = 60*60000;
                IsClose = true;
            }
            else if (view.getId() == R.id.music_list_btn) {
                if (!MusicList_IsShow) {
                    music_list.setVisibility(View.VISIBLE);
                    MusicList_IsShow = true;
                }
            } else if (view.getId() == R.id.music_list_cancel) {
                music_list.setVisibility(View.GONE);
                MusicList_IsShow = false;
            }
        }
    }

    public void initMediaPlayer() {
        mediaPlayer = new MediaPlayer();
        mediaPlayer.setOnErrorListener(new MediaPlayer.OnErrorListener() {
            public boolean onError(MediaPlayer mediaPlayer, int i, int i1) {
                mediaPlayer.reset();
                next();
                return false;
            }
        });
        mediaPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
            @Override
            public void onCompletion(MediaPlayer mediaPlayer) {
                if (Loopstate == LOOP) {
                    next();
                } else if (Loopstate == RANDOM) {
                    Random r = new Random();
                    position = r.nextInt(medioItems.size() - 1);
                    start();
                } else if (Loopstate == SINGLE) {
                    start();
                }
            }
        });
    }

    @Override
    public void onClick(View view) {
        if (view.getId() == R.id.btn_next) {
            if (state == STOP) {
                state = PLAY;
                start.setImageResource(R.drawable.stop);
            }
            next();
        } else if (view.getId() == R.id.btn_previous) {
            if (state == STOP) {
                state = PLAY;
                start.setImageResource(R.drawable.stop);
            }
            previous();
        } else if (view.getId() == R.id.btn_start) {
            if (state == STOP) {
                mediaPlayer.start();
                start.setImageResource(R.drawable.stop);
                state = PLAY;
            } else if (state == PLAY) {
                mediaPlayer.pause();
                start.setImageResource(R.drawable.start);
                state = STOP;
            }
        } else if (view.getId() == R.id.loop) {
            if (Loopstate == LOOP) {
                loop.setImageResource(R.drawable.random);
                Loopstate = RANDOM;
            } else if (Loopstate == RANDOM) {
                loop.setImageResource(R.drawable.single_loop);
                Loopstate = SINGLE;
            } else if (Loopstate == SINGLE) {
                loop.setImageResource(R.drawable.loop);
                Loopstate = LOOP;
            }
        } else if (view.getId() == R.id.back_power) {
            onDestroy();
            finish();
        } else if (view.getId() == R.id.audio_function) {
            if (!Function_IsShow) {
                audio_function_panel.setVisibility(View.VISIBLE);
                if (IsClose) {
                    audio_close.setVisibility(View.VISIBLE);
                }
                Function_IsShow = true;
            } else {
                audio_function_panel.setVisibility(View.GONE);
                if (IsClose) {
                    audio_close.setVisibility(View.GONE);
                }
                Function_IsShow = false;
            }
        }
    }

    public void start() {
        if (position < medioItems.size()) {
            SeekBarFlag = true;
            if (mediaPlayer == null) {
                initMediaPlayer();
            }
            state = PLAY;
            medioItem = medioItems.get(position);
            mediaPlayer.reset();//重置播放器使其回到空闲状态
            try {
                mediaPlayer.setDataSource(medioItem.getData());
                mediaPlayer.prepare();
                mediaPlayer.start();
                music_name.setText(medioItem.getName());
                music_airtist.setText(medioItem.getArtist());
                music_time.setText(formatTime(medioItem.getDuration()));
                //初始化seekbar
                initSeekBar();
                //启动线程保证只有一个线程执行
                es.execute(new SeekBarThread());
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        /**
         * 通过Handler控制当前播放时间更新
                */
        new Thread() {
            public void run() {
                while (SeekBarFlag) {
                    handler.sendEmptyMessage(1);
                    try {
                        Thread.sleep(1000);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }
        }.start();
    }

    public void next() {
        if (Loopstate == RANDOM) {
            Random r = new Random();
            position = r.nextInt(medioItems.size() - 1);
            start();
        } else {
            if (position < medioItems.size() - 1) {
                position++;
            } else {
                position = 0;
            }
            start();
        }
    }

    public void previous() {
        if (position > 0) {
            position--;
        } else {
            position = medioItems.size() - 1;
        }
        start();
    }

    public void initSeekBar() {
        seekBar.setMax((int) medioItem.getDuration());
        seekBar.setProgress(0);
    }

    class SeekBarThread implements Runnable {
        public SeekBarThread() {
            SeekBarFlag = true;
        }

        public void run() {
            while (SeekBarFlag) {
                if (mediaPlayer.getCurrentPosition() < medioItem.getDuration()) {
                    seekBar.setProgress(mediaPlayer.getCurrentPosition());
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

    /**
     * tool 格式化时间
     */
    public String formatTime(long time) {
        time = time / 1000;
        String strHour = "" + (time / 3600);
        String strMinute = "" + time % 3600 / 60;
        String strSecond = "" + time % 3600 % 60;

        strHour = strHour.length() < 2 ? "0" + strHour : strHour;
        strMinute = strMinute.length() < 2 ? "0" + strMinute : strMinute;
        strSecond = strSecond.length() < 2 ? "0" + strSecond : strSecond;

        String strRsult = "";

        if (!strHour.equals("00")) {
            strRsult += strHour + ":";
        }

        if (!strMinute.equals("00")) {
            strRsult += strMinute + ":";
        }
        strRsult += strSecond;
        if (strMinute.equals("00") && !strSecond.equals(00)) {
            strRsult = "00:" + strSecond;
        }
        return strRsult;
    }

    @Override
    protected void onDestroy() {
        if (mediaPlayer != null) {
            state = EMPTY;
            SeekBarFlag = false;
            seekBar.setProgress(0);
            mediaPlayer.stop();
            mediaPlayer.release();
            mediaPlayer = null;
        }
        super.onDestroy();
    }

    class MusicListAdapter extends BaseAdapter {

        @Override
        public int getCount() {
            return medioItems.size();
        }

        @Override
        public Object getItem(int i) {
            return null;
        }

        @Override
        public long getItemId(int i) {
            return 0;
        }

        @Override
        public View getView(int position, View view, ViewGroup viewGroup) {
            ViewHolder viewHolder = new ViewHolder();
            if (view == null) {
                view = View.inflate(SystemAudioPlayer.this, R.layout.audio_item_layout, null);
                viewHolder.audio_name = view.findViewById(R.id.audio_name);
                viewHolder.audio_duration = view.findViewById(R.id.audio_duration);
                viewHolder.audio_size = view.findViewById(R.id.audio_size);
                viewHolder.audio_artist = view.findViewById(R.id.audio_airtist);
                view.setTag(viewHolder);
            } else {
                viewHolder = (ViewHolder) view.getTag();
            }
            MedioItem medioItem = medioItems.get(position);
            viewHolder.audio_name.setText(medioItem.getName());
            viewHolder.audio_duration.setText(formatTime(medioItem.getDuration()));
            viewHolder.audio_size.setText(Formatter.formatFileSize(SystemAudioPlayer.this, medioItem.getSize()));
            viewHolder.audio_artist.setText(medioItem.getArtist());
            return view;
        }
    }

    class ViewHolder {
        TextView audio_name;
        TextView audio_duration;
        TextView audio_size;
        TextView audio_artist;
    }

    class MusicListItemOnClickListener implements AdapterView.OnItemClickListener {
        @Override
        public void onItemClick(AdapterView<?> adapterView, View view, int nowPosition, long l) {
            position = nowPosition;
            start();
            music_list.setVisibility(View.GONE);
            music_list_btn.setVisibility(View.VISIBLE);
            MusicList_IsShow = false;
        }
    }

    public void initAudioManager() {
        //音量控制,初始化定义
        mAudioManager = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
        //最大音量
        maxVolume = mAudioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC);
        //当前音量
        currentVolume = mAudioManager.getStreamVolume(AudioManager.STREAM_MUSIC);
        music_sound_seekbar.setProgress(currentVolume);
        music_sound_seekbar.setMax(maxVolume);
    }

    /**
     * 注册当音量发生变化时接收的系统广播
     */
    private void initRegisterReceiver() {
        mVolumeReceiver = new MyVolumeReceiver();
        IntentFilter filter = new IntentFilter();
        filter.addAction("android.media.VOLUME_CHANGED_ACTION");
        registerReceiver(mVolumeReceiver, filter);
    }

    /**
     * 处理音量变化时的界面显示
     *
     * @author long
     */
    private class MyVolumeReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            //如果音量发生变化则更改seekbar的位置
            if (intent.getAction().equals("android.media.VOLUME_CHANGED_ACTION")) {
                int currVolume = mAudioManager.getStreamVolume(AudioManager.STREAM_MUSIC);// 当前的媒体音量
                music_sound_seekbar.setProgress(currVolume);
            }
        }
    }
    public void CreatCloseThread()
    {
        TimeEs.execute(new Runnable() {
            @Override
            public void run() {
                while(IsClose)
                {
                    handler.sendEmptyMessage(2);
                    try {
                        Thread.sleep(1000);
                        CloseTime-=1000;
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }
        });
    }
}