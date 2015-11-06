package com.fang.videocamera.fragment;

import android.media.AudioManager;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.ViewGroup;

import com.fang.videocamera.R;
import com.fang.videocamera.activity.MainActivity;

import org.androidannotations.annotations.AfterViews;
import org.androidannotations.annotations.EFragment;
import org.androidannotations.annotations.FragmentArg;
import org.androidannotations.annotations.ViewById;

@EFragment(R.layout.fragment_video_play)
public class VideoPlayFragment extends BaseFragment implements SurfaceHolder.Callback,ButtonPlayInterface {
    @ViewById(R.id.surfaceview)
    SurfaceView surfaceview;
    MediaPlayer player;
 @FragmentArg
String filename;
    private SurfaceHolder holder = null;

    @AfterViews
    void init(){
        Log.e("fc","init");
        holder = surfaceview.getHolder();
        holder.addCallback(this);
        holder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ((MainActivity)getActivity()).setPlayInterface(this);
        Log.e("fc","oncreate");

    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        Log.e("fc","onCreateView");  return super.onCreateView(inflater, container, savedInstanceState);


    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        Log.e("fc","onViewCreated");

    }

    @Override
    public void onStart() {
        Log.e("fc","onStart");

        super.onStart();
    }

    @Override
    public void onResume() {
        Log.e("fc","onResume");

        super.onResume();
    }

    @Override
    public void surfaceCreated(SurfaceHolder holder12) {
        Log.e("fc","surfaceCreated");

        player = new MediaPlayer();
        player.setAudioStreamType(AudioManager.STREAM_MUSIC);
        player.setDisplay(holder);

        //设置显示视频显示在SurfaceView上
        try {
            if(player!=null){
            player.setDataSource(filename);
            player.prepare();}
        } catch (Exception e) {
            e.printStackTrace();
        }
      play();

    }

    private void play() {
        if(player!=null)
            player.start();
    }

    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
        Log.e("fc","surfaceChanged");
    }

    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {
        Log.e("fc","surfaceDestroyed");

    }


   /* @Override
    public void onDestroy() {
        super.onDestroy();
        if(player!=null){
        if (player.isPlaying()) {
            player.stop();
        }
        player.release();}
    }*/



    @Override
    public void onPlayBtnClicked() {

        if(player!=null)
            player.start();
        else{}
        surfaceview.refreshDrawableState();
    }
}
