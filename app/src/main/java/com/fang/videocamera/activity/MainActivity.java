package com.fang.videocamera.activity;


import android.app.Activity;
import android.graphics.Bitmap;
import android.media.MediaPlayer;
import android.media.ThumbnailUtils;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

import com.fang.videocamera.R;
import com.jmolsmobile.landscapevideocapture.CLog;
import com.jmolsmobile.landscapevideocapture.VideoFile;
import com.jmolsmobile.landscapevideocapture.camera.CameraWrapper;
import com.jmolsmobile.landscapevideocapture.configuration.CaptureConfiguration;
import com.jmolsmobile.landscapevideocapture.recorder.AlreadyUsedException;
import com.jmolsmobile.landscapevideocapture.recorder.VideoRecorder;
import com.jmolsmobile.landscapevideocapture.recorder.VideoRecorderInterface;
import com.jmolsmobile.landscapevideocapture.view.RecordingButtonInterface;
import com.jmolsmobile.landscapevideocapture.view.VideoCaptureView;

import org.androidannotations.annotations.AfterViews;
import org.androidannotations.annotations.Click;
import org.androidannotations.annotations.EActivity;
import org.androidannotations.annotations.ViewById;

@EActivity(R.layout.activity_main)
public class MainActivity extends Activity implements RecordingButtonInterface, VideoRecorderInterface,SurfaceHolder.Callback {
    private String statusMessage = null;
    private String filename = null;
    public static final int RESULT_ERROR = 753245;

    public static final String EXTRA_OUTPUT_FILENAME = "com.jmolsmobile.extraoutputfilename";
    public static final String EXTRA_CAPTURE_CONFIGURATION = "com.jmolsmobile.extracaptureconfiguration";
    public static final String EXTRA_ERROR_MESSAGE = "com.jmolsmobile.extraerrormessage";

    private static final String SAVED_RECORDED_BOOLEAN = "com.jmolsmobile.savedrecordedboolean";
    protected static final String SAVED_OUTPUT_FILENAME = "com.jmolsmobile.savedoutputfilename";

    private boolean mVideoRecorded = false;
    VideoFile mVideoFile = null;
    private CaptureConfiguration mCaptureConfiguration;


    private VideoRecorder mVideoRecorder;
    @ViewById(R.id.videoviewmain)
    VideoCaptureView mVideoCaptureView;
    @ViewById(R.id.iv_thumbnail)
    ImageView iv_thumbnail;
    @ViewById(R.id.btn_start)
    Button btn_start;
    @ViewById(R.id.btn_stop)
    Button btn_stop;
    @ViewById(R.id.btn_set)
    Button btn_set;
    @ViewById(R.id.btn_ani)
    Button btn_ani;

    @AfterViews
    void init() {//在oncreate 之后执行
        btn_stop.setClickable(false);
        if (mVideoCaptureView == null) return; // Wrong orientation
        initializeRecordingUI();

    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        CLog.toggleLogging(this);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        initializeCaptureConfiguration(savedInstanceState);

    }

    @Override
    protected void onStart() {
        super.onStart();
        mVideoCaptureView.setRecordingButtonInterface(this);
    }

    @Click({R.id.btn_start, R.id.btn_stop, R.id.btn_ani, R.id.btn_set})
    void onclick(View view) {
        switch (view.getId()) {
            case R.id.btn_start:
                mVideoCaptureView.doClick(0);
                break;
            case R.id.btn_stop:
                mVideoCaptureView.doClick(1);
                break;
            case R.id.btn_ani:
                /*surfaceview.setVisibility(View.VISIBLE);
                thumbnailIv.setVisibility(View.GONE);
                if(mVideoRecorder.getPlayer()!=null)
               mVideoRecorder.getPlayer().start();*/
                break;
        }
    }

    private void initializeCaptureConfiguration(final Bundle savedInstanceState) {
        mCaptureConfiguration = generateCaptureConfiguration();
        mVideoRecorded = generateVideoRecorded(savedInstanceState);
        mVideoFile = generateOutputFile(savedInstanceState);
    }

    private void initializeRecordingUI() {
        CameraWrapper cameraWrapper=new CameraWrapper();

        mVideoRecorder = new VideoRecorder(this, mCaptureConfiguration, mVideoFile, cameraWrapper,
                mVideoCaptureView.getPreviewSurfaceHolder());


        if (mVideoRecorded) {
            mVideoCaptureView.updateUIRecordingFinished(getVideoThumbnail());
        } else {
            mVideoCaptureView.updateUINotRecording();
        }
    }

    @Override
    protected void onPause() {
        if (mVideoRecorder != null) {
            mVideoRecorder.stopRecording(null);
        }
        releaseAllResources();
        super.onPause();
    }

    @Override
    public void onBackPressed() {
        if (mVideoRecorder.isRecording()) {

            finishCancelled();
        }
        finish();

    }

    @Override
    public void onRecordButtonClicked() {
        try {
            iv_thumbnail.setVisibility(View.GONE);
            mVideoCaptureView.setVisibility(View.VISIBLE);
            mVideoRecorder.toggleRecording();

        } catch (AlreadyUsedException e) {
            CLog.d(CLog.ACTIVITY, "Cannot toggle recording after cleaning up all resources");
        }
    }

    @Override
    public void onStopButtonClicked() {
        try {

            mVideoRecorder.toggleRecording();
        } catch (AlreadyUsedException e) {
            e.printStackTrace();
        }
        finishCompleted();
    }

    @Override
    public void onSettingButtonClicked() {

    }

    @Override
    public void onAcceptButtonClicked() {

    }

    @Override
    public void onAniButtonClicked() {

    }

    @Override
    public void onDeclineButtonClicked() {
        finishCancelled();
    }

    @Override
    public void onRecordingStarted() {
        btn_start.setClickable(false);
        btn_stop.setClickable(true);
        mVideoCaptureView.updateUIRecordingOngoing();
    }

    @Override
    public void onRecordingStopped(String message) {

        if (message != null) {
            Toast.makeText(this, message, Toast.LENGTH_LONG).show();
        }

        mVideoCaptureView.updateUIRecordingFinished(getVideoThumbnail());
        // releaseAllResources();
    }

    @Override
    public void onRecordingSuccess() {
        btn_start.setClickable(true);
        btn_stop.setClickable(false);
        mVideoRecorded = true;
    }

    @Override
    public void onRecordingFailed(String message) {
        finishError(message);
    }

    private void finishCompleted() {
     /*   final Intent result = new Intent();
        result.putExtra(EXTRA_OUTPUT_FILENAME, mVideoFile.getFullPath());
        this.setResult(RESULT_OK, result);
        finish();*/
        mVideoCaptureView.setVisibility(View.GONE);
        iv_thumbnail.setVisibility(View.VISIBLE);
        filename = mVideoFile.getFullPath();
        Log.d("fc_main", filename);
        updateStatusAndThumbnail();
    }

    private void finishCancelled() {
        /*this.setResult(RESULT_CANCELED);
        finish();*/
        filename = null;
        updateStatusAndThumbnail();

    }

    private void finishError(final String message) {
      /*  Toast.makeText(getApplicationContext(), "Can't capture video: " + message, Toast.LENGTH_LONG).show();

        final Intent result = new Intent();
        result.putExtra(EXTRA_ERROR_MESSAGE, message);
        this.setResult(RESULT_ERROR, result);
        finish();*/
        filename = null;
        updateStatusAndThumbnail();
    }

    private void updateStatusAndThumbnail() {
        if (statusMessage == null) {
            statusMessage = "未 捕获 ";
        }

        final Bitmap thumbnail = getVideoThumbnail();

        if (thumbnail != null) {
            iv_thumbnail.setImageBitmap(thumbnail);
            DisplayMetrics dm = new DisplayMetrics();
           /* // 取得窗口属性
            this.getWindowManager().getDefaultDisplay().getMetrics(dm);
            ViewGroup.LayoutParams mParams2 = mVideoCaptureView.getLayoutParams();
            mParams2.width = dm.widthPixels;
            mParams2.height = dm.widthPixels * 1080 / 1920;
            // Toast.makeText(getActivity(), thumbnail.getHeight() + "  " +thumbnail.getWidth(), Toast.LENGTH_LONG).show();
            mVideoCaptureView.setLayoutParams(mParams2);*/
        } else {
            iv_thumbnail.setImageResource(R.drawable.thumbnail_placeholder);
        }

    }

    private void releaseAllResources() {
        if (mVideoRecorder != null) {
            mVideoRecorder.releaseAllResources();
        }
    }

    @Override
    public void onSaveInstanceState(Bundle savedInstanceState) {
        savedInstanceState.putBoolean(SAVED_RECORDED_BOOLEAN, mVideoRecorded);
        savedInstanceState.putString(SAVED_OUTPUT_FILENAME, mVideoFile.getFullPath());
        super.onSaveInstanceState(savedInstanceState);
    }

    protected CaptureConfiguration generateCaptureConfiguration() {
        CaptureConfiguration returnConfiguration = this.getIntent().getParcelableExtra(EXTRA_CAPTURE_CONFIGURATION);
        if (returnConfiguration == null) {
            returnConfiguration = new CaptureConfiguration();
            CLog.d(CLog.ACTIVITY, "No captureconfiguration passed - using default configuration");
        }
        return returnConfiguration;
    }

    private boolean generateVideoRecorded(final Bundle savedInstanceState) {
        if (savedInstanceState == null) return false;
        return savedInstanceState.getBoolean(SAVED_RECORDED_BOOLEAN, false);
    }

    protected VideoFile generateOutputFile(Bundle savedInstanceState) {
        VideoFile returnFile = null;
        if (savedInstanceState != null) {
            returnFile = new VideoFile(savedInstanceState.getString(SAVED_OUTPUT_FILENAME));
        } else {
            returnFile = new VideoFile(null);
        }
        // TODO: add checks to see if outputfile is writeable
        return returnFile;
    }

    public Bitmap getVideoThumbnail() {
        final Bitmap thumbnail = ThumbnailUtils.createVideoThumbnail(mVideoFile.getFullPath(),
                MediaStore.Video.Thumbnails.FULL_SCREEN_KIND);
        if (thumbnail == null) {
            CLog.d(CLog.ACTIVITY, "Failed to generate video preview");
        }
        return thumbnail;
    }

    @Override
    public void surfaceCreated(SurfaceHolder holder) {

    }

    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {

    }

    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {

    }
}