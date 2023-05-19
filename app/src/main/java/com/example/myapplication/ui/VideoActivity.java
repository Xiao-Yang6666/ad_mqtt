package com.example.myapplication.ui;

import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.os.*;
import android.provider.Settings;
import android.util.Log;
import android.view.WindowManager;
import android.widget.MediaController;
import android.widget.VideoView;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import com.example.myapplication.R;
import com.example.myapplication.servers.ApiService;
import com.example.myapplication.servers.RetrofitClient;
import com.example.myapplication.utils.DownloadUtil;
import com.example.myapplication.utils.MqttUtil;
import com.example.myapplication.utils.StatusBarUtil;
import com.example.myapplication.utils.VideoUtil;
import com.google.gson.JsonObject;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;


public class VideoActivity extends AppCompatActivity {

    private static final String TAG = "VideoActivity--> ";
    private VideoView videoView;
    private static VideoUtil videoUtil;

    public Handler handler = new Handler(Looper.getMainLooper()) {
        @Override
        public void handleMessage(@NonNull Message msg) {
            super.handleMessage(msg);
            int what = msg.what;
            Log.e(TAG, "handleMessage: 接收到消息" + what);
            switch (what) {
                // 1 为切换视频播放
                case 1 :
                    Log.e(TAG, "handleMessage: 接收到消息: " + msg);
                    String filePath = Environment.getExternalStorageDirectory().getAbsolutePath() + "/cc/" + msg.obj;
                    // 切换视频播放
                    videoUtil.switchVideo(filePath, videoView);
                    break;
                case 2:
                    // 切换为横屏
                    setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
                    break;
                case 3:
                    // 切换为竖屏
                    setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED);
                    break;
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // 设置永不息屏
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        // 隐藏状态拦和底栏
        new StatusBarUtil().statusBar(this.getWindow());
        // 隐藏状态拦 避免黑色留白的问题
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            getWindow().getAttributes().layoutInDisplayCutoutMode = WindowManager.LayoutParams.LAYOUT_IN_DISPLAY_CUTOUT_MODE_SHORT_EDGES;
        }
        setContentView(R.layout.activity_video);

        // 获取videoView 播放默认视频
        videoView = findViewById(R.id.videoView);
        videoUtil = new VideoUtil();
        startVideo();

        // 订阅switchVideo主题 切换视频
        MqttUtil.getMqttUtil(this).subscribe("switchVideo", 1, handler);
    }

    /**
     * 开始播放视频
     */
    private void startVideo() {
        // 播放视频
        String filePath = Environment.getExternalStorageDirectory().getAbsolutePath() + "/" + "aaa.mp4";
        // 播放视频
        videoUtil.setVideoView(filePath, videoView, new MediaController(this));
    }

    protected void getDownUrl() {
        Retrofit retrofit = RetrofitClient.getInstance();

        ApiService service = retrofit.create(ApiService.class);
        Call<String> call = service.getUser("aaa");
        call.enqueue(new Callback<String>() {
            @Override
            public void onResponse(Call<String> call, Response<String> response) {
                Log.d(TAG, "onResponse: " + response);
                if (response.isSuccessful()) {
                    String user = response.body();
                    Log.d(TAG, "onResponse: " + user);
                    // 处理获取到的数据
                } else {
                    // 处理请求失败的情况
                    Log.e(TAG, "onResponse: 请求失败>>>");
                }
            }

            @Override
            public void onFailure(Call<String> call, Throwable t) {
                System.out.println(t);
                // 处理请求失败的情况
                Log.e(TAG, "onResponse: 请求失败");
            }
        });

    }

}