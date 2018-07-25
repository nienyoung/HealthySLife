package com.example.admin.healthyslife_android.music;

import android.Manifest;
import android.app.Activity;
import android.app.ActivityManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ComponentName;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.pm.PackageManager;
import android.media.MediaPlayer;
import android.os.Handler;
import android.os.IBinder;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.ListView;
import android.widget.AdapterView;

import com.example.admin.healthyslife_android.MainActivity;
import com.example.admin.healthyslife_android.R;
import com.example.admin.healthyslife_android.adapter.MusicListAdapter;
import com.example.admin.healthyslife_android.bean.Song;
import com.example.admin.healthyslife_android.myWidget.MarqueTextView;
import com.example.admin.healthyslife_android.utils.MusicUtils;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;

public class MusicActivity extends AppCompatActivity {

    private ImageView isPlay;
    private ImageView previous;
    private ImageView next;
    private ImageView albumImage;

    // private ObjectAnimator animator;
    private int flag = 0;

    private TextView totalTime;
    private TextView playingTime;
    private SeekBar seekBar;

    private ListView mListView;
    private List<Song> list;
    private MusicListAdapter adapter;
    private int currentSongIndex;

    private MarqueTextView playingSong;
    private TextView playingSinger;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_music);

        bindServiceConnection();

        initView();
        initListView();
        setListener();

            //默认播放第一首
            musicService = new MusicService();
            musicService.resetPlayer();
            if (list.get(0) != null) {
                String firstPath = list.get(0).path;
                seekBar.setProgress(0);
                musicService.initPlayer(firstPath);
                seekBar.setMax(musicService.mediaPlayer.getDuration());
                playingSong.setText(list.get(0).song);
                playingSinger.setText(list.get(0).singer);
                list.get(0).thumBitmap = MusicUtils.getArtwork(MusicActivity.this, list.get(0).songId, list.get(0).albumId, false, false);
                if (list.get(0).thumBitmap != null)
                    albumImage.setImageBitmap(list.get(0).thumBitmap);
            }




        //设置列表循环播放
        musicService.mediaPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
            public void onCompletion(MediaPlayer mp) {
                currentSongIndex++;
                if (currentSongIndex > list.size()-3) {
                    currentSongIndex = 0;
                }
                play();
            }
        });

    }

    private void initView(){
        isPlay = (ImageView) findViewById(R.id.isPlayButton);
        previous = (ImageView) findViewById(R.id.previousButton);
        next = (ImageView) findViewById(R.id.nextButton);
        albumImage = (ImageView) findViewById(R.id.albumImage);

        seekBar = (SeekBar) findViewById(R.id.seekBar);

        totalTime = (TextView) findViewById(R.id.totalTime);
        playingTime = (TextView) findViewById(R.id.playingTime);
        playingSong = (MarqueTextView) findViewById(R.id.playingSong);
        playingSinger = (TextView) findViewById(R.id.playingSinger);
    }

    private void initListView() {
        mListView = (ListView) findViewById(R.id.main_listview);
        list = new ArrayList<>();
        //把扫描到的音乐赋值给list
        list = MusicUtils.getMusicData(this);
        adapter = new MusicListAdapter(this,list);
        mListView.setAdapter(adapter);
    }

    private void setListener(){
        isPlay.setOnClickListener(new myOnClickListener());
        previous.setOnClickListener(new myOnClickListener());
        next.setOnClickListener(new myOnClickListener());
        InnerItemOnCLickListener listener = new InnerItemOnCLickListener();
        //给 ListView 添加点击（点击音乐即可播放）
       mListView.setOnItemClickListener(listener);
    }

    private class InnerItemOnCLickListener implements AdapterView.OnItemClickListener {
        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
            //获取点击的列表的中音乐的位置，赋值给当前播放音乐
            currentSongIndex = position;
            if(currentSongIndex > list.size()-3){}
            else
            play();
        }
    }

    private class myOnClickListener implements View.OnClickListener {
        @Override
        public void onClick(View view) {
            switch (view.getId()) {
                case R.id.isPlayButton:
                    musicService.playOrPause();
                    break;
                case R.id.previousButton:
                    previous();
                    break;
                case R.id.nextButton:
                    next();
                    break;
                default:
                    break;
            }
        }
    }

    private void previous() {

            //当前音乐播放位置--（上一曲）
            currentSongIndex--;
            if (currentSongIndex < 0) {
                currentSongIndex = list.size() - 3;
            }
                play();
    }

    private void next() {

        //当前音乐播放位置--（上一曲）
        currentSongIndex++;
        if (currentSongIndex > list.size()-3) {
            currentSongIndex = 0;
        }
            play();
    }


    private void setPlayButton(){
        if(musicService.mediaPlayer.isPlaying()){
            isPlay.setImageDrawable(getResources().getDrawable(R.drawable.play));
        } else {
            isPlay.setImageDrawable(getResources().getDrawable(R.drawable.pause));

        }
    }


    private void play() {
        //重置
        musicService.resetPlayer();
        try {
            //设置音乐文件来源
            Song cur = list.get(currentSongIndex);
            //设置seekbar长度
            seekBar.setProgress(0);
            musicService.playNewSong(cur.path);
            seekBar.setMax(musicService.mediaPlayer.getDuration());

            cur.thumBitmap = MusicUtils.getArtwork(MusicActivity.this,cur.songId,cur.albumId,false,true);
            if(cur.thumBitmap!=null)
                albumImage.setImageBitmap(cur.thumBitmap);

            playingSong.setText(cur.song);
            playingSinger.setText(cur.singer);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    private MusicService musicService;
    private SimpleDateFormat time = new SimpleDateFormat("mm:ss");
    private ServiceConnection sc = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName componentName, IBinder iBinder) {
            musicService = ((MusicService.MyBinder) iBinder).getService();
        }

        @Override
        public void onServiceDisconnected(ComponentName componentName) {
            musicService = null;
        }
    };

    private void bindServiceConnection() {
        Intent intent = new Intent(this, MusicService.class);
        startService(intent);
        bindService(intent, sc, this.BIND_AUTO_CREATE);
    }
    public Handler handler = new Handler();
    public Runnable runnable = new Runnable() {
        @Override
        public void run() {

            setPlayButton();

            playingTime.setText(time.format(musicService.mediaPlayer.getCurrentPosition()));
            totalTime.setText(time.format(musicService.mediaPlayer.getDuration()));
            seekBar.setProgress(musicService.mediaPlayer.getCurrentPosition());
            seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
                @Override
                public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                    if (fromUser) {
                        musicService.mediaPlayer.seekTo(seekBar.getProgress());
                    }
                }

                @Override
                public void onStartTrackingTouch(SeekBar seekBar) {

                }

                @Override
                public void onStopTrackingTouch(SeekBar seekBar) {

                }
            });

            handler.postDelayed(runnable, 100);
        }
    };

    /*
    返回键跳转*/
    public void onBackPressed() {
        Intent intent = new Intent(MusicActivity.this,MainActivity.class);
        startActivity(intent);
        //super.onBackPressed();

        overridePendingTransition(R.anim.leftin, R.anim.leftout);
    }

    @Override
    public void onPause(){
        super.onPause();
        if(isApplicationBroughtToBackground()) {
            musicService.isReturnTo = 1;
            Log.e("b","后台中");
        }
    }

    @Override
    public void onRestart() {
        super.onRestart();
        musicService.isReturnTo = 1;
    }

    @Override
    protected void onResume() {

        verifyStoragePermissions(this);

        seekBar.setProgress(musicService.mediaPlayer.getCurrentPosition());
        seekBar.setMax(musicService.mediaPlayer.getDuration());
        handler.post(runnable);
        super.onResume();
        Log.d("hint", "handler post runnable");
    }


    @Override
    public void onStop() {
        super.onStop();
    }
    @Override
    public void onDestroy() {
        //unbindService(sc);
        super.onDestroy();
    }


    private boolean isApplicationBroughtToBackground() {
        ActivityManager am = (ActivityManager) getSystemService(Context.ACTIVITY_SERVICE);
        List<ActivityManager.RunningTaskInfo> tasks = am.getRunningTasks(1);
        if (!tasks.isEmpty()) {
            ComponentName topActivity = tasks.get(0).topActivity;
            if (!topActivity.getPackageName().equals(getPackageName())) {
                return true;
            }
        }
        return false;
    }

    // Storage Permissions
    private static final int REQUEST_EXTERNAL_STORAGE = 1;
    private static String[] PERMISSIONS_STORAGE = {
            Manifest.permission.READ_EXTERNAL_STORAGE,
            Manifest.permission.WRITE_EXTERNAL_STORAGE
    };

    /**
     * Checks if the app has permission to write to device storage
     *
     * If the app does not has permission then the user will be prompted to grant permissions
     *
     * @param activity
     */
    public static void verifyStoragePermissions(Activity activity) {
        // Check if we have write permission
        int permission = ActivityCompat.checkSelfPermission(activity, Manifest.permission.WRITE_EXTERNAL_STORAGE);

        if (permission != PackageManager.PERMISSION_GRANTED) {
            // We don't have permission so prompt the user
            ActivityCompat.requestPermissions(
                    activity,
                    PERMISSIONS_STORAGE,
                    REQUEST_EXTERNAL_STORAGE
            );
        }
    }

}
