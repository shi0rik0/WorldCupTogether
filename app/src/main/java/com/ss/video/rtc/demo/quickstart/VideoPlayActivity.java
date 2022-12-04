package com.ss.video.rtc.demo.quickstart;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.graphics.Point;
import android.media.MediaMetadataRetriever;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.Display;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.VideoView;

public class VideoPlayActivity extends AppCompatActivity {
    VideoView mVideoView;
    ImageView pauseView, startView, stopView;
    boolean visibility = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_FULLSCREEN);
        setContentView(R.layout.activity_vedio);
        mVideoView = findViewById(R.id.video_play_view);
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
        ViewGroup.LayoutParams currParam = mVideoView.getLayoutParams();
        if (videoRatio > scrRatio){
            currParam.height = (int) (point.x/videoRatio);
            currParam.width = point.x;
        }
        else {
            currParam.width = (int) (point.y * videoRatio);
            currParam.height = point.y;
        }
        mVideoView.setLayoutParams(currParam);
    }

    public void showControls(View view) {
        if (!visibility){
            pauseView.setVisibility(View.VISIBLE);
            startView.setVisibility(View.VISIBLE);
            stopView.setVisibility(View.VISIBLE);
            visibility = true;
        }
        else{
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
        finish();
    }
}