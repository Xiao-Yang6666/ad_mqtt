package com.example.myapplication.ui;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.*;
import android.provider.Settings;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import com.example.myapplication.R;
import com.example.myapplication.servers.ApiService;
import com.example.myapplication.servers.RetrofitClient;
import com.example.myapplication.utils.DeviceInfoUtil;
import com.example.myapplication.utils.MqttUtil;
import org.eclipse.paho.client.mqttv3.*;
import org.eclipse.paho.client.mqttv3.persist.MemoryPersistence;
import org.json.JSONObject;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = "MainActivity";
    private static final long HEARTBEAT_INTERVAL = 10L; // 5秒发送一次心跳
    private static final TimeUnit HEARTBEAT_TIMEUNIT = TimeUnit.SECONDS;
    private final ScheduledExecutorService executorService = Executors.newSingleThreadScheduledExecutor();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // 申请完全存储权限
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R && !Environment.isExternalStorageManager()) {
            Intent intent = new Intent(Settings.ACTION_MANAGE_ALL_FILES_ACCESS_PERMISSION);
            startActivity(intent);
        }
        // 申请GPS定位权限
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 0);
        }
        // mqtt相关权限
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.WAKE_LOCK) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.WAKE_LOCK}, 0);
        }

        // 发送心跳到服务端
        executorService.scheduleWithFixedDelay(() -> {
            Call<String> heartbeat = RetrofitClient.getInstance().create(ApiService.class).sendHeartbeat(DeviceInfoUtil.getDeviceId(this));
            heartbeat.enqueue(new Callback<String>() {
                @Override
                public void onResponse(Call<String> call, Response<String> response) {
                    if (!response.isSuccessful()) {
                        // 发送心跳失败，处理错误情况
                        Log.e(TAG, "onFailure: 心跳发送失败. response: " + response);
                    }
                }
                @Override
                public void onFailure(Call<String> call, Throwable t) {
                    Log.e(TAG, "onFailure: 心跳发送失败...", t);
                }

            });
        }, 0L, HEARTBEAT_INTERVAL, HEARTBEAT_TIMEUNIT);

        // 上报设备信息到服务端
        JSONObject deviceInfoJson = DeviceInfoUtil.getDeviceInfoJson(this);
        Log.d(TAG, "onCreate: " + deviceInfoJson);
        Call<Void> voidCall = RetrofitClient.getInstance().create(ApiService.class).reportDeviceInfo(deviceInfoJson);
        voidCall.enqueue(new Callback<Void>() {
            @Override
            public void onResponse(Call<Void> call, Response<Void> response) {
                if (!response.isSuccessful()) {
                    // 发送心跳失败，处理错误情况
                    Log.e(TAG, "onResponse: 设备信息上报失败.response: " + response);
                }
            }

            @Override
            public void onFailure(Call<Void> call, Throwable t) {

            }
        });

        // 订阅updVideo主题 更新视频到本地
        MqttUtil.getMqttUtil(this).subscribe("updVideo", 1, null);

        // 显示转圈效果
        ProgressBar progressBar = findViewById(R.id.progress_bar);
        progressBar.setVisibility(View.VISIBLE);

        // 创建Handler
        Handler handler = new Handler();
        // 设置1秒后执行的任务
        handler.postDelayed(() -> {
            // 取消转圈效果
            progressBar.setVisibility(View.GONE);
            // 跳转页面
            Intent intent = new Intent(MainActivity.this, VideoActivity.class);
            startActivity(intent);
        }, 1000);
    }

}