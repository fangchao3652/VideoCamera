package com.fang.videocamera.fragment;


import android.graphics.Bitmap;
import android.media.ThumbnailUtils;
import android.provider.MediaStore;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;

import com.fang.videocamera.R;
import com.fang.videocamera.activity.MainActivity;
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
import org.androidannotations.annotations.EFragment;
import org.androidannotations.annotations.FragmentArg;
import org.androidannotations.annotations.ViewById;

@EFragment(R.layout.fragment_video_recode)
public class VideoRecodeFragment extends BaseFragment implements RecordingButtonInterface, VideoRecorderInterface {
     VideoRecorder mVideoRecorder;
    @FragmentArg
    String filename;
    @FragmentArg
     boolean mVideoRecorded = false;
    @FragmentArg
    VideoFile mVideoFile = null;
    @FragmentArg
     CaptureConfiguration mCaptureConfiguration;
    @ViewById(R.id.videoviewmain)
    VideoCaptureView mVideoCaptureView;
    @ViewById(R.id.iv_thumbnail)
    ImageView iv_thumbnail;

@AfterViews
void init (){
    ( (MainActivity)getActivity()).setRecordingButtonInterface(this);
    initializeRecordingUI();
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

    public Bitmap getVideoThumbnail() {
        final Bitmap thumbnail = ThumbnailUtils.createVideoThumbnail(mVideoFile.getFullPath(),
                MediaStore.Video.Thumbnails.FULL_SCREEN_KIND);
        if (thumbnail == null) {
            CLog.d(CLog.ACTIVITY, "Failed to generate video preview");
        }
        return thumbnail;
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
    public void onAniButtonClicked() {

    }

    @Override
    public void onDeclineButtonClicked() {
        finishCancelled();
    }

    @Override
    public void onAcceptButtonClicked() {

    }

    @Override
    public void onRecordingStopped(String message) {
        mVideoCaptureView.updateUIRecordingFinished(getVideoThumbnail());
    }

    @Override
    public void onRecordingStarted() {

      /*  ((MainActivity)getActivity()).btn_start.setClickable(false);
        btn_stop.setClickable(true);*/
        mVideoCaptureView.updateUIRecordingOngoing();
    }

    @Override
    public void onRecordingSuccess() {
        /*btn_start.setClickable(true);
        btn_stop.setClickable(false);*/
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
    public void onDestroy() {
        super.onDestroy();
        releaseAllResources();
    }
}
