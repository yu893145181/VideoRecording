package com.yus.videorecording;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.yus.videorecording.view.TextureVideoView;
import com.yus.videorecording.view.VideoViewTouch;

/**
 * 播放界面
 * Created by yufs on 2017/7/6.
 */

public class VideoPlayActivity extends Activity implements MediaPlayer.OnPreparedListener, TextureVideoView.OnPlayStateListener, MediaPlayer.OnInfoListener, MediaPlayer.OnVideoSizeChangedListener, MediaPlayer.OnErrorListener, MediaPlayer.OnSeekCompleteListener {
    private VideoViewTouch mVideoView;
    private RelativeLayout rl_loading;
    private Context mContext;
    /** 播放路径 */
    private String mSourcePath;
    private Intent uploadService;//视频上传服务

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_video_preview);
        initView();
        setListener();
        initData();
    }


    private void initData() {
        //本地视频路径
        mSourcePath=getIntent().getStringExtra("source");
        //播放视屏
        parseIntentUrl(getIntent());
    }

    private void parseIntentUrl(Intent intent) {
        mVideoView.setVideoPath(mSourcePath);
    }

    private void setListener() {



        mVideoView.setOnPreparedListener(this);
        mVideoView.setOnPlayStateListener(this);
        mVideoView.setOnTouchEventListener(mOnVideoTouchListener);
        mVideoView.setOnInfoListener(this);
        mVideoView.setOnVideoSizeChangedListener(this);
        mVideoView.setOnErrorListener(this);
        mVideoView.setOnSeekCompleteListener(this);

    }

    private void initView() {
        mVideoView= (VideoViewTouch) findViewById(R.id.preview);
        rl_loading= (RelativeLayout) findViewById(R.id.video_loading);

        View preview_layout = findViewById(R.id.preview_layout);
        preview_layout.setVisibility(View.VISIBLE);
    }


    private VideoViewTouch.OnTouchEventListener mOnVideoTouchListener = new VideoViewTouch.OnTouchEventListener() {

        @Override
        public boolean onClick() {

            if (mVideoView.isPlaying()) {
                mVideoView.pauseClearDelayed();
            } else {
                mVideoView.start();
            }
            return true;
        }

        @Override
        public void onVideoViewDown() {
        }

        @Override
        public void onVideoViewUp() {

        }
    };

    @Override
    public void onPrepared(MediaPlayer mp) {
        //获取视频的宽和高
        int videoWidth = mp.getVideoWidth();
        int videoHeight = mp.getVideoHeight();
        Log.e("yufs","视频的宽:"+videoWidth+",视频的高:"+videoHeight);
        //重新设置TextureView宽和高
        mVideoView.resize();
        //开始播放
        rl_loading.setVisibility(View.GONE);
        mVideoView.start();
    }

    @Override
    public void onStateChanged(boolean isPlaying) {

    }

    @Override
    public boolean onInfo(MediaPlayer mp, int what, int extra) {
        return false;
    }

    @Override
    public void onVideoSizeChanged(MediaPlayer mp, int width, int height) {

    }

    @Override
    public boolean onError(MediaPlayer mp, int what, int extra) {
        return false;
    }

    @Override
    public void onSeekComplete(MediaPlayer mp) {

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }
}
