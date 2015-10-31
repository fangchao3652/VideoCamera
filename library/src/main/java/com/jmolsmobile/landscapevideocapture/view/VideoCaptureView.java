/**
 * Copyright 2014 Jeroen Mols
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.jmolsmobile.landscapevideocapture.view;

import android.content.Context;
import android.graphics.Bitmap;
import android.util.AttributeSet;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.ImageView.ScaleType;

import com.jmolsmobile.landscapevideocapture.R;
import com.jmolsmobile.landscapevideocapture.R.id;

public class VideoCaptureView extends FrameLayout  {


	private SurfaceView					mSurfaceView;
	private ImageView					mThumbnailIv;

	private RecordingButtonInterface	mRecordingInterface;

	public VideoCaptureView(Context context) {
		super(context);
		initialize(context);
	}

	public VideoCaptureView(Context context, AttributeSet attrs) {
		super(context, attrs);
		initialize(context);
	}

	public VideoCaptureView(Context context, AttributeSet attrs, int defStyleAttr) {
		super(context, attrs, defStyleAttr);
		initialize(context);
	}

	private void initialize(Context context) {
		final View videoCapture = View.inflate(context, R.layout.view_videocapture, this);


		mThumbnailIv = (ImageView) videoCapture.findViewById(R.id.videocapture_preview_iv);
		mSurfaceView = (SurfaceView) videoCapture.findViewById(R.id.videocapture_preview_sv);
	}

	public void setRecordingButtonInterface(RecordingButtonInterface mBtnInterface) {
		this.mRecordingInterface = mBtnInterface;
	}

	public SurfaceHolder getPreviewSurfaceHolder() {
		return mSurfaceView.getHolder();
	}

	public void updateUINotRecording() {

		mThumbnailIv.setVisibility(View.GONE);
		mSurfaceView.setVisibility(View.VISIBLE);
	}

	public void updateUIRecordingOngoing() {

		mThumbnailIv.setVisibility(View.GONE);
		mSurfaceView.setVisibility(View.VISIBLE);
	}

	public void updateUIRecordingFinished(Bitmap videoThumbnail) {

		mSurfaceView.setVisibility(View.GONE);
		final Bitmap thumbnail = videoThumbnail;
		if (thumbnail != null) {
			mThumbnailIv.setScaleType(ScaleType.CENTER_CROP);
			mThumbnailIv.setImageBitmap(videoThumbnail);
		}
	}


    public void doClick(int i) {
		if (mRecordingInterface == null) return;
switch (i){
    case 0://开始训练
        mRecordingInterface.onRecordButtonClicked();
        break;
    case 1://训练结束
        mRecordingInterface.onStopButtonClicked();
        break;
    case 2://设置
        mRecordingInterface.onSettingButtonClicked();
        break;
    case 3://分析
        mRecordingInterface.onAniButtonClicked();
        break;
}

	}

}
