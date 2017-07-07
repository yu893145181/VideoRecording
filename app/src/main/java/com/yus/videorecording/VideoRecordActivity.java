package com.yus.videorecording;

import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Intent;
import android.hardware.Camera;
import android.media.CamcorderProfile;
import android.media.MediaRecorder;
import android.os.Bundle;
import android.os.Environment;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.OrientationEventListener;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.view.animation.DecelerateInterpolator;
import android.widget.ImageView;
import android.widget.Toast;

import com.yus.videorecording.view.CircleButtonView;

import java.io.File;
import java.io.IOException;

/**
 * Created by yufs on 2017/7/5.
 */

public class VideoRecordActivity extends Activity implements MediaRecorder.OnErrorListener, View.OnClickListener {
    private ImageView iv_cancel,iv_save;
    private SurfaceView mSurfaceView;
    private SurfaceHolder mSurfaceHolder;
    private MediaRecorder mMediaRecorder;
    private Camera mCamera;

    private CircleButtonView cbv_record;
    private File mRecordFile = null;// 文件
    private int mRotationRecord=90;//视频输出角度 0横屏  90竖屏  180反横屏
    private boolean isRecording;//正在录制
    private float mWindowW;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        //全屏无标题
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams. FLAG_FULLSCREEN ,
                WindowManager.LayoutParams. FLAG_FULLSCREEN);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.video_record_activity);
        initView();
        initData();
        setListener();
    }

    private void initData() {
        DisplayMetrics metric = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(metric);
        mWindowW = metric.widthPixels;     // 屏幕宽度（像素）

    }

    @Override
    protected void onResume() {
        super.onResume();
        cbv_record.setVisibility(View.VISIBLE);
        iv_cancel.setVisibility(View.GONE);
        iv_save.setVisibility(View.GONE);
        if(mRecordFile!=null){
            mRecordFile.getAbsoluteFile().delete();
        }
    }

    private void setListener() {
        cbv_record.setOnLongClickListener(new CircleButtonView.OnLongClickListener() {
            @Override
            public void onLongClick() {
                isRecording=true;
                startRecord();
            }

            @Override
            public void onNoMinRecord(int currentTime) {
                isRecording=false;
                Toast.makeText(VideoRecordActivity.this, "录制视频太短", Toast.LENGTH_SHORT).show();
                if(mRecordFile.getAbsoluteFile()!=null){
                    mRecordFile.getAbsoluteFile().delete();
                }
            }

            @Override
            public void onRecordFinishedListener() {
                Log.e("yufs","视频录制完成:path=="+mRecordFile.getAbsolutePath());
                isRecording=false;
                stopRecord();
                cbv_record.setVisibility(View.GONE);
                iv_cancel.setVisibility(View.VISIBLE);
                iv_save.setVisibility(View.VISIBLE);
            }
        });

        //手机旋转监听
        OrientationEventListener orientationEventListener=new OrientationEventListener(this) {
            @Override
            public void onOrientationChanged(int rotation) {
                //录制的过程不改变
                if(isRecording){
                    return;
                }
                if (((rotation >= 0) && (rotation <= 30)) || (rotation >= 330)) {
                    // 竖屏拍摄
                    mRotationRecord=90;
                } else if (((rotation >= 230) && (rotation <= 310))) {
                    // 横屏拍摄
                    mRotationRecord=0;
                } else if (rotation > 30 && rotation < 95) {
                    // 反横屏拍摄
                    mRotationRecord=180;
                }
            }
        };
        orientationEventListener.enable();

        iv_cancel.setOnClickListener(this);
        iv_save.setOnClickListener(this);
    }

    private void startRecord() {
        //初始录制视频保存路径
        createRecordDir();
        try {
            initCamera();
            initRecord();
        }catch (Exception e){
            e.printStackTrace();
        }
    }

    private void initView() {
        iv_cancel= (ImageView) findViewById(R.id.iv_cancel);
        iv_save= (ImageView) findViewById(R.id.iv_save);
        mSurfaceView= (SurfaceView) findViewById(R.id.sv_video);
        cbv_record= (CircleButtonView) findViewById(R.id.cbv_record);
        mSurfaceHolder = mSurfaceView.getHolder();
        mSurfaceHolder.addCallback(new CustomCallBack());
        mSurfaceHolder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.iv_cancel:
                cbv_record.setVisibility(View.VISIBLE);
                mRecordFile.getAbsoluteFile().delete();
                iv_cancel.setVisibility(View.GONE);
                iv_save.setVisibility(View.GONE);
                break;
            case R.id.iv_save:
                Intent intent=new Intent(this,VideoPlayActivity.class);
                intent.putExtra("source",mRecordFile.getAbsolutePath());
                startActivity(intent);
                break;
        }
    }

    private class CustomCallBack implements SurfaceHolder.Callback {

        @Override
        public void surfaceCreated(SurfaceHolder holder) {

            try {
                initCamera();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        @Override
        public void surfaceChanged(SurfaceHolder holder, int format, int width,
                                   int height) {

        }

        @Override
        public void surfaceDestroyed(SurfaceHolder holder) {
            freeCamera();
        }

    }

    /**
     * 初始化
     * @throws IOException
     */
    @SuppressLint("NewApi")
    private void initRecord() throws IOException {
        mMediaRecorder = new MediaRecorder();
        mMediaRecorder.reset();
        if (mCamera != null)
            mMediaRecorder.setCamera(mCamera);
        mMediaRecorder.setOnErrorListener(this);
        mMediaRecorder.setPreviewDisplay(mSurfaceHolder.getSurface());
        mMediaRecorder.setVideoSource(MediaRecorder.VideoSource.CAMERA);// 视频源
        mMediaRecorder.setAudioSource(MediaRecorder.AudioSource.MIC);// 音频源

        //注释掉的代码官方说这样的配置方法是Android2.2以下使用
//		mMediaRecorder.setOutputFormat(OutputFormat.MPEG_4);// 视频输出格式
//		mMediaRecorder.setAudioEncoder(AudioEncoder.AAC);// 音频格式:AAC兼容会高点
//      mMediaRecorder.setVideoEncoder(MediaRecorder.VideoEncoder.H264);//设置视频编码：h264在常见的网页上都可播放
//		mMediaRecorder.setVideoSize(mWidth, mHeight);// 设置分辨率：
//		mMediaRecorder.setVideoEncodingBitRate(1 * 1024 * 1024*100);// 设置帧频率

        //Android2.2以上直接用MediaRecorder.setProfile得到统一的配置
        mMediaRecorder.setProfile(CamcorderProfile.get(CamcorderProfile.QUALITY_720P));
        mMediaRecorder.setOrientationHint(mRotationRecord);// 输出旋转90度，保持竖屏录制
        mMediaRecorder.setOutputFile(mRecordFile.getAbsolutePath());
        try {
            mMediaRecorder.prepare();
            mMediaRecorder.start();
        } catch (IllegalStateException e) {
            e.printStackTrace();
        } catch (RuntimeException e) {
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 初始化摄像头
     */
    private void initCamera() throws IOException {
        if (mCamera != null) {
            freeCamera();
        }
        try {
            mCamera = Camera.open();
        } catch (Exception e) {
            e.printStackTrace();
            freeCamera();
        }
        if (mCamera == null)
            return;
        mCamera.setDisplayOrientation(90);
        mCamera.setPreviewDisplay(mSurfaceHolder);
        mCamera.startPreview();
        mCamera.unlock();
    }

    private void createRecordDir() {
        //录制的视频保存文件夹
        File videoFolder = new File(Environment.getExternalStorageDirectory()
                + File.separator + "yufs/");//录制视频的保存地址
        if (!videoFolder.exists()) {
            videoFolder.mkdirs();
        }
        File recordDir = videoFolder;
        try {
            // mp4格式的录制的视频文件
            mRecordFile = File.createTempFile("recording", ".mp4", recordDir);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * 释放摄像头资源
     */
    private void freeCamera() {
        try {
            if (mCamera != null) {
                mCamera.setPreviewCallback(null);
                mCamera.stopPreview();
                mCamera.lock();
                mCamera.release();
                mCamera = null;
            }
        }catch (Exception e){
            //视频成功录制了，资源回收的时候偶尔会崩溃
        }
    }

    /**
     * 停止录制
     */
    public void stopRecord() {
        if (mMediaRecorder != null) {
            // 设置后不会崩
            mMediaRecorder.setOnErrorListener(null);
            mMediaRecorder.setPreviewDisplay(null);
            try {
                mMediaRecorder.stop();
                mMediaRecorder.release();
            } catch (IllegalStateException e) {
                e.printStackTrace();
            } catch (RuntimeException e) {
                e.printStackTrace();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public void onError(MediaRecorder mr, int what, int extra) {
        try {
            if (mr != null)
                mr.reset();
        } catch (IllegalStateException e) {
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * @param v
     */
    private void startAnimator(View v){

        AnimatorSet animatorSet = new AnimatorSet();//组合动画
        ObjectAnimator scaleX = ObjectAnimator.ofFloat(v, "scaleX", 0, 1f);
        ObjectAnimator scaleY = ObjectAnimator.ofFloat(v, "scaleY", 0, 1f);

        animatorSet.setDuration(1000);
        animatorSet.setInterpolator(new DecelerateInterpolator());
        animatorSet.play(scaleX).with(scaleY);//两个动画同时开始
        animatorSet.start();
    }
}
