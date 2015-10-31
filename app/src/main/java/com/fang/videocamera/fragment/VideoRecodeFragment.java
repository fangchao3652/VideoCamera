package com.fang.videocamera.fragment;


import android.widget.ImageView;

import com.fang.videocamera.R;
import com.jmolsmobile.landscapevideocapture.view.VideoCaptureView;

import org.androidannotations.annotations.EFragment;
import org.androidannotations.annotations.ViewById;

@EFragment(R.layout.fragment_video_recode)
public class VideoRecodeFragment extends BaseFragment {
    @ViewById(R.id.videoviewmain)
    VideoCaptureView mVideoCaptureView;
    @ViewById(R.id.iv_thumbnail)
    ImageView iv_thumbnail;
}
