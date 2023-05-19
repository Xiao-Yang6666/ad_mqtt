package com.example.myapplication.utils;

import android.annotation.SuppressLint;
import android.content.pm.ActivityInfo;
import android.content.res.Resources;
import android.media.MediaPlayer;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.*;
import android.widget.MediaController;
import android.widget.RelativeLayout;
import android.widget.VideoView;
import androidx.constraintlayout.widget.ConstraintLayout;

import java.io.File;

import static android.content.ContentValues.TAG;

/**
 * 视频播放工具类
 * @author XiaoYang
 * @date 2023/5/17 10:32
 */
public class VideoUtil {

    @SuppressLint("ClickableViewAccessibility")
    public void setVideoView(String filePath, VideoView videoView, MediaController mediaController) {
        mediaController.setAnchorView(videoView);
        mediaController.setVisibility(View.GONE); // 隐藏进度条
        videoView.setMediaController(mediaController);
        videoView.setVideoPath(filePath); // 设置视频路径
        videoView.start(); // 开始播放

        videoView.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
            @Override
            public void onCompletion(MediaPlayer mp) {
                videoView.start(); // 循环播放
            }
        });

        videoView.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                switch (event.getAction()) {
                    case MotionEvent.ACTION_DOWN:
                        if (videoView.isPlaying()) {
                            videoView.pause();
                        } else {
                            videoView.start();
                        }
                        break;
                    case MotionEvent.ACTION_UP:
                        if (videoView.isPlaying()) {
                            videoView.pause();
                            // 进入全屏模式
                            videoView.setLayoutParams(new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
                        } else {
                            videoView.start();
                        }
                        break;
                }
                return true;
            }
        });
    }

    /**
     * 切换视频
     * @param filePath
     * @param videoView
     */
    public void switchVideo(String filePath, VideoView videoView) {
        File file1 = new File(filePath);
        if (!file1.exists()) {
            Log.e(TAG, "switchVideo: 文件不存在, " + filePath);
            return;
        }
        videoView.stopPlayback(); // 停止当前播放
        videoView.setVideoPath(filePath); // 设置新的视频路径
        videoView.start(); // 开始新的播放
    }

}
