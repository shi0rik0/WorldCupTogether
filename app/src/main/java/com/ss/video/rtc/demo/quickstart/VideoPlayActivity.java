package com.ss.video.rtc.demo.quickstart;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.graphics.Point;
import android.media.MediaMetadataRetriever;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.Display;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.VideoView;

import java.util.ArrayList;

public class VideoPlayActivity extends AppCompatActivity {
    VideoView mVideoView;
    RelativeLayout mVideoViewContainer;
    ImageView pauseView, startView, stopView;
    boolean visibility = false;
    Handler animationHandler = new Handler();
    Runnable animationRunnable;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_FULLSCREEN);
        setContentView(R.layout.activity_vedio);
        if (mVideoViewContainer != null) {
            mVideoViewContainer.removeAllViews();
        }
        mVideoViewContainer = findViewById(R.id.video_view_container);
        mVideoView = new VideoView(getApplicationContext());
        mVideoViewContainer.addView(mVideoView);
        mVideoView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showControls(view);
            }
        });

        pauseView = findViewById(R.id.video_pause);
        startView = findViewById(R.id.video_start);
        stopView = findViewById(R.id.video_cast_stop);
        Intent query = getIntent();
        String path = query.getStringExtra("path");
        MediaMetadataRetriever metadataRetriever = new MediaMetadataRetriever();
        metadataRetriever.setDataSource(path);
        int rotation, width, height;
        rotation = Integer.parseInt(metadataRetriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_VIDEO_ROTATION));
        width = Integer.parseInt(metadataRetriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_VIDEO_WIDTH));
        height = Integer.parseInt(metadataRetriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_VIDEO_HEIGHT));
        setVideoViewPatterns(width, height, rotation);
        mVideoView.setVideoPath(path);
        Log.d("VideoPlay", path);
        mVideoView.start();
    }

    private void setVideoViewPatterns(int width, int height, int rotation){
        if (rotation%180 != 0) {
            int tmp = width;
            width = height;
            height = tmp;
        }
        Display display = getWindowManager().getDefaultDisplay();
        Point point = new Point();
        display.getRealSize(point);
        float videoRatio = (float)width/height;
        float scrRatio = (float) point.x/point.y;
        RelativeLayout.LayoutParams currParam = (RelativeLayout.LayoutParams) mVideoView.getLayoutParams();
        if (videoRatio > scrRatio){
            currParam.height = (int) (point.x/videoRatio);
            currParam.width = point.x;
        }
        else {
            currParam.width = (int) (point.y * videoRatio);
            currParam.height = point.y;
        }
        currParam.addRule(RelativeLayout.CENTER_IN_PARENT);
        mVideoView.setLayoutParams(currParam);
    }

    private void setVisibilityGone(Animation animation){
        Log.d("Anime", "Animation Stops");
        pauseView.setVisibility(View.GONE);
        startView.setVisibility(View.GONE);
        stopView.setVisibility(View.GONE);
        animation.reset();
        visibility = false;
    }

    public void showControls(View view) {
        if (!visibility){
            pauseView.setVisibility(View.VISIBLE);
            startView.setVisibility(View.VISIBLE);
            stopView.setVisibility(View.VISIBLE);
            visibility = true;
            Animation disappear = new AlphaAnimation(1f, 0f);
            disappear.setDuration(1000);
            disappear.setAnimationListener(new Animation.AnimationListener() {
                @Override
                public void onAnimationStart(Animation animation) {
                    Log.d("Anime", "Animation Starts");

                }

                @Override
                public void onAnimationEnd(Animation animation) {
                    setVisibilityGone(animation);
                }

                @Override
                public void onAnimationRepeat(Animation animation) {

                }
            });

            animationRunnable = new Runnable() {
                @Override
                public void run() {
                    pauseView.startAnimation(disappear);
                    stopView.startAnimation(disappear);
                    startView.startAnimation(disappear);

                    Log.d("Anime", "SetAnimationStarts");
                }
            };
            animationHandler.postDelayed(animationRunnable, 3000);
        }
        else{
            Log.d("Anime", "Click to Remove bars");
            animationHandler.removeCallbacks(animationRunnable);
            animationRunnable = null;
            pauseView.setVisibility(View.GONE);
            startView.setVisibility(View.GONE);
            stopView.setVisibility(View.GONE);
            visibility = false;
        }
    }


    public void pauseVideo(View view) {
        mVideoView.pause();
    }

    public void startVideo(View view) {
        mVideoView.start();
    }

    public void stopCastVideo(View view) {
        setResult(RESULT_OK);
        finish();
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        stopCastVideo(mVideoView);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mVideoViewContainer.removeAllViews();
        mVideoView.setOnCompletionListener(null);
        mVideoView.setOnErrorListener(null);
        mVideoView.stopPlayback();
        mVideoView = null;
    }


}