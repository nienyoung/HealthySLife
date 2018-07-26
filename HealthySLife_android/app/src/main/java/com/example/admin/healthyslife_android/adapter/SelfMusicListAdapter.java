package com.example.admin.healthyslife_android.adapter;

import android.app.AlertDialog;
import android.content.Context;
import android.support.v4.app.Fragment;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.admin.healthyslife_android.R;
import com.example.admin.healthyslife_android.bean.Song;
import com.example.admin.healthyslife_android.music.MusicService;
import com.example.admin.healthyslife_android.music.SelfMusicActivity;
import com.example.admin.healthyslife_android.utils.MusicUtils;

import java.util.List;

public class SelfMusicListAdapter extends BaseAdapter {
    private Context context;
    private List<Song> list;
    public SelfMusicListAdapter(SelfMusicActivity musicActivity, List<Song> list) {
        this.context = musicActivity;
        this.list = list;

    }

    @Override
    public int getCount() {
        return list.size();
    }

    @Override
    public Object getItem(int i) {
        return list.get(i);
    }

    @Override
    public long getItemId(int i) {
        return i;
    }

    @Override
    public View getView(final int i, View view, ViewGroup viewGroup) {
        ViewHolder holder = null;
        if (view == null) {
            holder = new ViewHolder();
            //引入布局
            view = View.inflate(context, R.layout.item_music_self_listview, null);
            //实例化对象
            holder.song = (TextView) view.findViewById(R.id.item_mymusic_song1);
            holder.singer = (TextView) view.findViewById(R.id.item_mymusic_singer1);
            holder.duration = (TextView) view.findViewById(R.id.item_mymusic_duration1);
            holder.position = (TextView) view.findViewById(R.id.item_mymusic_postion1);
            holder.minus = (ImageView)view.findViewById(R.id.minusImage);
            view.setTag(holder);
        } else {
            holder = (ViewHolder) view.getTag();
        }

        holder.minus.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                list.remove(i);
                notifyDataSetChanged();
            }
        });

        if(list.get(i)==null) {

            if(i<list.size()-1) {
                holder.song.setText(null);
            }else{
                if(list.size()==2)
                    holder.song.setText("暂无音乐,请前往默认列表进行添加。");
                else
                    holder.song.setText(null);
            }
            holder.singer.setText(null);
            holder.duration.setText(null);
            holder.position.setText(null);
            holder.minus.setImageBitmap(null);

        }else {
            //给控件赋值
            holder.song.setText(list.get(i).song.toString());
            holder.singer.setText(list.get(i).singer.toString());
            //时间需要转换一下
            int duration = list.get(i).duration;
            String time = MusicUtils.formatTime(duration);
            holder.duration.setText(time);
            holder.position.setText(i+1+"");
        }

        return view;
    }
    class ViewHolder{
        TextView song;//歌曲名
        TextView singer;//歌手
        TextView duration;//时长
        TextView position;//序号
        ImageView minus;
    }

}
