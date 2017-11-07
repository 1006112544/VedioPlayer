package com.daobao.asus.vedioplayer.pager;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.format.Formatter;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;


import com.daobao.asus.vedioplayer.Player.SystemAudioPlayer;
import com.daobao.asus.vedioplayer.Player.SystemVedioPlayer;
import com.daobao.asus.vedioplayer.R;
import com.daobao.asus.vedioplayer.domain.MedioItem;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

/**
 * Created by ASUS on 2017/7/3.
 */

public class SearchPager extends Activity{
    public static ArrayList<MedioItem> medioItems;
    private ListView searchlistview;
    private Handler handler;
    private ImageButton search_back;
    private TextView search_textview;
    String name;
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.searchpager_layout);
        searchlistview = findViewById(R.id.search_listView);
        search_back = findViewById(R.id.search_back);
        search_textview = findViewById(R.id.search_textview);
        handler = new Handler(){
            @Override
            public void handleMessage(Message msg) {
                super.handleMessage(msg);
                if(medioItems!=null&&medioItems.size()>0)
                {
                    //有数据设置适配器
                    SearchPagerAdapter searchPagerAdapter = new SearchPagerAdapter();
                    searchlistview.setAdapter(searchPagerAdapter);
                }
            }
        };
        Bundle bundle = this.getIntent().getExtras();
        name = bundle.getString("Search");
        search();
        searchlistview.setOnItemClickListener(new SearchOnItemClickListener());
        search_back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });
    }
    class SearchOnItemClickListener implements AdapterView.OnItemClickListener
    {
        @Override
        public void onItemClick(AdapterView<?> adapterView, View view, int position, long l) {
            MedioItem medioItem = medioItems.get(position);
            if(medioItem.isMusic())
            {
                Toast.makeText(SearchPager.this,"开始播放",Toast.LENGTH_SHORT).show();
                //显示意图
                Intent intent = new Intent(SearchPager.this,SystemAudioPlayer.class);
                intent.putExtra("AudioPager",position);
                intent.putExtra("FromPager","SearchPager");
                intent.setData(Uri.parse(medioItem.getData()));
                startActivity(intent);
            }
            else
            {
                Toast.makeText(SearchPager.this,"开始播放",Toast.LENGTH_SHORT).show();
                //显示意图播放
                Intent intent = new Intent(SearchPager.this,SystemVedioPlayer.class);
                intent.putExtra("vedioActivity",position);
                intent.putExtra("FromPager","SearchPager");
                intent.setDataAndType(Uri.parse(medioItem.getData()),"video/*");
                startActivity(intent);
            }
        }
    }
    public void search()
    {
        medioItems = new ArrayList<>();
       if(name!=null&&!name.equals(""))
       {
           if(VedioPager.medioItems!=null)
           {
               //查找视频
               for (int i=0;i<VedioPager.medioItems.size();i++)
               {
                   if(VedioPager.medioItems.get(i).getName().indexOf(name)!=-1)
                   {
                       MedioItem medioItem = VedioPager.medioItems.get(i);
                       medioItem.setMusic(false);
                       medioItems.add(medioItem);
                   }
               }
           }
           if(AudioPager.medioItems!=null)
           {
              //查找音乐
              for (int i=0;i<AudioPager.medioItems.size();i++)
              {
                  if(AudioPager.medioItems.get(i).getName().indexOf(name)!=-1)
                  {
                      MedioItem medioItem = AudioPager.medioItems.get(i);
                      medioItem.setMusic(true);
                      medioItems.add(medioItem);
                      Log.d("cc",  medioItem .getName());
                  }
              }
           }
           handler.sendEmptyMessage(1);
       }
       if(medioItems.size()==0)
       {
           search_textview.setVisibility(View.VISIBLE);
           searchlistview.setVisibility(View.GONE);
       }
    }
    class SearchPagerAdapter extends BaseAdapter
    {

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
            if(view == null)
            {
                view = View.inflate(SearchPager.this,R.layout.search_item_layout,null);
                viewHolder.search_name  = view.findViewById(R.id.search_name);
                viewHolder.search_size = view.findViewById(R.id.search_size);
                viewHolder.search_flag = view.findViewById(R.id.search_flag);
                view.setTag(viewHolder);
            }
            else
            {
                viewHolder = (ViewHolder) view.getTag();
            }
            MedioItem medioItem = medioItems.get(position);
            viewHolder.search_name.setText(medioItem.getName());
            viewHolder.search_size.setText(Formatter.formatFileSize(SearchPager.this,medioItem.getSize()));
            if(medioItem.isMusic())
            {
                viewHolder.search_flag.setText("音乐");
            }
            else viewHolder.search_flag.setText("视频");
            return view;
        }
    }
    class ViewHolder {
        TextView search_name;
        TextView search_size;
        TextView search_flag;
    }
}
