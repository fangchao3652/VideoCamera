package com.fang.videocamera.activity;


import android.app.Activity;
import android.app.Fragment;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.Intent;
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
import com.fang.videocamera.fragment.ButtonPlayInterface;
import com.fang.videocamera.fragment.ChartFragment;
import com.fang.videocamera.fragment.ChartFragment_;
import com.fang.videocamera.fragment.VideoPlayFragment;
import com.fang.videocamera.fragment.VideoPlayFragment_;
import com.fang.videocamera.fragment.VideoRecodeFragment;
import com.fang.videocamera.fragment.VideoRecodeFragment_;
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

import java.io.IOException;
import java.io.InputStream;
import java.util.UUID;

@EActivity(R.layout.activity_main)
public class MainActivity extends Activity {

    private String filename = null;
    public static final int RESULT_ERROR = 753245;

    public static final String EXTRA_OUTPUT_FILENAME = "com.fangchao.extraoutputfilename";
    public static final String EXTRA_CAPTURE_CONFIGURATION = "com.fangchao.extracaptureconfiguration";
    public static final String EXTRA_ERROR_MESSAGE = "com.fangchao.extraerrormessage";

    private static final String SAVED_RECORDED_BOOLEAN = "com.fangchao.savedrecordedboolean";
    protected static final String SAVED_OUTPUT_FILENAME = "com.fangchao.savedoutputfilename";

    private boolean mVideoRecorded = false;
    VideoFile mVideoFile = null;
    private CaptureConfiguration mCaptureConfiguration;
    private RecordingButtonInterface mRecordingInterface;
    private ButtonPlayInterface playInterface;
    VideoRecodeFragment videoRecodeFragment;
    private VideoRecorder mVideoRecorder;

    @ViewById(R.id.btn_start)
    Button btn_start;
    @ViewById(R.id.btn_stop)
    Button btn_stop;
    @ViewById(R.id.btn_set)
    Button btn_set;
    @ViewById(R.id.btn_ani)
    Button btn_ani;
    FragmentManager fm;
    FragmentTransaction tx;
    Fragment frag = null;

    VideoRecodeFragment recodeFragment;
    VideoPlayFragment playFragment;
    ChartFragment chartFragment;
    private BluetoothSocket socket = null;
    private BluetoothDevice device = null;
    private ClientThread clientConnectThread = null;
    private readThread mreadThread = null;
    private BluetoothAdapter mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
    //static String BlueToothAddress = "CC:07:E4:85:4F:18";
    static String BlueToothAddress = "C4:00:00:57:6A:E4";

    @AfterViews
    void init() {//在oncreate 之后执行
        btn_stop.setClickable(false);
        fm = getFragmentManager();
        tx = fm.beginTransaction();
        recodeFragment = VideoRecodeFragment_.builder().mVideoFile(mVideoFile).mCaptureConfiguration(mCaptureConfiguration).build();
        playFragment = VideoPlayFragment_.builder().filename(mVideoFile.getFullPath()).build();
        chartFragment = ChartFragment_.builder().build();
        tx.add(R.id.id_content_chart, chartFragment, "CHART");
        tx.add(R.id.id_content, recodeFragment, "ONE");
        tx.add(R.id.id_content, playFragment, "TWO").hide(playFragment);
        tx.commit();
        initBT();
    }

    /**
     * 蓝牙操作
     */
    private void initBT() {
       /* if (!mBluetoothAdapter.isEnabled()) {
            Intent enableIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(enableIntent, 3);
        }*/
        device = mBluetoothAdapter.getRemoteDevice(BlueToothAddress);
        clientConnectThread = new ClientThread();
        clientConnectThread.start();
    }

    public void switchContent(Fragment from, Fragment to) {
        if (frag != to) {
            frag = to;
            FragmentTransaction transaction = fm.beginTransaction();
            if (!to.isAdded()) {    // 先判断是否被add过
                transaction.hide(from).add(R.id.id_content, to).commit(); // 隐藏当前的fragment，add下一个到Activity中
            } else {
                transaction.hide(from).show(to).commit(); // 隐藏当前的fragment，显示下一个
            }
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        CLog.toggleLogging(this);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        initializeCaptureConfiguration(savedInstanceState);

    }

    public void setPlayInterface(ButtonPlayInterface playInterface) {
        this.playInterface = playInterface;
    }

    @Click({R.id.btn_start, R.id.btn_stop, R.id.btn_ani, R.id.btn_set})
    void onclick(View view) {

        switch (view.getId()) {
            case R.id.btn_start:
                switchContent(playFragment, recodeFragment);
                frag = fm.findFragmentByTag("ONE");
                if (frag instanceof VideoRecodeFragment) {
                    this.mRecordingInterface.onRecordButtonClicked();
                    btn_start.setClickable(false);
                    btn_stop.setClickable(true);
                }
                // mVideoCaptureView.doClick(0);
                break;
            case R.id.btn_stop:
                // mVideoCaptureView.doClick(1);
                if (frag instanceof VideoRecodeFragment) {
                    btn_start.setClickable(true);
                    btn_stop.setClickable(false);
                    this.mRecordingInterface.onStopButtonClicked();
                }
                break;
            case R.id.btn_ani:
                switchContent(recodeFragment, playFragment);
                if (frag instanceof VideoPlayFragment) {
                    playInterface.onPlayBtnClicked();
                    Log.e("fc", "分析。。。。");
                }
                break;
        }
    }

    public void setRecordingButtonInterface(RecordingButtonInterface mBtnInterface) {
        this.mRecordingInterface = mBtnInterface;
    }

    private void initializeCaptureConfiguration(final Bundle savedInstanceState) {
        mCaptureConfiguration = generateCaptureConfiguration();
        mVideoRecorded = generateVideoRecorded(savedInstanceState);
        mVideoFile = generateOutputFile(savedInstanceState);
    }
/*
    private void initializeRecordingUI() {
        CameraWrapper cameraWrapper=new CameraWrapper();

        mVideoRecorder = new VideoRecorder(this, mCaptureConfiguration, mVideoFile, cameraWrapper,
                mVideoCaptureView.getPreviewSurfaceHolder());


        if (mVideoRecorded) {
            mVideoCaptureView.updateUIRecordingFinished(getVideoThumbnail());
        } else {
            mVideoCaptureView.updateUINotRecording();
        }
    }*/

    @Override
    protected void onPause() {
        /*if (mVideoRecorder != null) {
            mVideoRecorder.stopRecording(null);
        }*/
        //releaseAllResources();
        super.onPause();
    }

    @Override
    public void onBackPressed() {
       /* if (mVideoRecorder.isRecording()) {

            // finishCancelled();
        }*/
        finish();

    }

    class ClientThread extends Thread {
        @Override
        public void run() {
            super.run();
            try {
                socket = device.createRfcommSocketToServiceRecord(UUID.fromString("00001101-0000-1000-8000-00805F9B34FB"));
                socket.connect();
                //启动接受数据
                mreadThread = new readThread();
                mreadThread.start();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private class readThread extends Thread {
        public void run() {

            byte[] buffer = new byte[1024];
            int bytes;
            InputStream mmInStream = null;

            try {
                mmInStream = socket.getInputStream();
            } catch (IOException e1) {

                e1.printStackTrace();
            }
            while (true) {
                try {
                    // Read from the InputStream
                    if ((bytes = mmInStream.read(buffer)) > 0) {
                        byte[] buf_data = new byte[bytes];
                        for (int i = 0; i < bytes; i++) {
                            buf_data[i] = buffer[i];
                        }
                        String s = new String(buf_data);
                        //fc 接收到的数据
                        Fragment f = fm.findFragmentByTag("CHART");
                        if (f instanceof ChartFragment) {
                            ((ChartFragment) f).addData(Float.parseFloat(s));
                        }
                        // Toast.makeText(getApplicationContext(),s,Toast.LENGTH_SHORT).show();

                    }
                } catch (IOException e) {
                    try {
                        mmInStream.close();
                    } catch (IOException e1) {
                        // TODO Auto-generated catch block
                        e1.printStackTrace();
                    }
                    break;
                }
            }
        }
    }
    /*private void finishCompleted() {
     *//*   final Intent result = new Intent();
        result.putExtra(EXTRA_OUTPUT_FILENAME, mVideoFile.getFullPath());
        this.setResult(RESULT_OK, result);
        finish();*//*
        mVideoCaptureView.setVisibility(View.GONE);
        iv_thumbnail.setVisibility(View.VISIBLE);
        filename = mVideoFile.getFullPath();
        Log.d("fc_main", filename);
        updateStatusAndThumbnail();
    }

    private void finishCancelled() {
        *//*this.setResult(RESULT_CANCELED);
        finish();*//*
        filename = null;
        updateStatusAndThumbnail();

    }

    private void finishError(final String message) {
      *//*  Toast.makeText(getApplicationContext(), "Can't capture video: " + message, Toast.LENGTH_LONG).show();

        final Intent result = new Intent();
        result.putExtra(EXTRA_ERROR_MESSAGE, message);
        this.setResult(RESULT_ERROR, result);
        finish();*//*
        filename = null;
        updateStatusAndThumbnail();
    }
*/
   /* private void updateStatusAndThumbnail() {
        if (statusMessage == null) {
            statusMessage = "未 捕获 ";
        }

        final Bitmap thumbnail = getVideoThumbnail();

        if (thumbnail != null) {
            iv_thumbnail.setImageBitmap(thumbnail);
            DisplayMetrics dm = new DisplayMetrics();
           *//* // 取得窗口属性
            this.getWindowManager().getDefaultDisplay().getMetrics(dm);
            ViewGroup.LayoutParams mParams2 = mVideoCaptureView.getLayoutParams();
            mParams2.width = dm.widthPixels;
            mParams2.height = dm.widthPixels * 1080 / 1920;
            // Toast.makeText(getActivity(), thumbnail.getHeight() + "  " +thumbnail.getWidth(), Toast.LENGTH_LONG).show();
            mVideoCaptureView.setLayoutParams(mParams2);*//*
        } else {
            iv_thumbnail.setImageResource(R.drawable.thumbnail_placeholder);
        }

    }

    private void releaseAllResources() {
        if (mVideoRecorder != null) {
            mVideoRecorder.releaseAllResources();
        }
    }*/

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
    protected void onDestroy() {
        super.onDestroy();

        shutdownClient();
    }

    /* 停止客户端连接 */
    private void shutdownClient() {
        new Thread() {
            public void run() {
                if(clientConnectThread!=null)
                {
                    clientConnectThread.interrupt();
                    clientConnectThread= null;
                }
                if(mreadThread != null)
                {
                    mreadThread.interrupt();
                    mreadThread = null;
                }
                if (socket != null) {
                    try {
                        socket.close();
                    } catch (IOException e) {
                        // TODO Auto-generated catch block
                        e.printStackTrace();
                    }
                    socket = null;
                }
            };
        }.start();
    }
}