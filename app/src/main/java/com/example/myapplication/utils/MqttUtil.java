package com.example.myapplication.utils;

import android.content.Context;
import android.os.Handler;
import android.util.Log;
import com.example.myapplication.ui.VideoActivity;
import org.eclipse.paho.client.mqttv3.*;
import org.eclipse.paho.client.mqttv3.persist.MemoryPersistence;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import static android.content.ContentValues.TAG;

/**
 * @author XiaoYang
 * @date 2023/5/19 09:37
 */
public class MqttUtil {
    private static final String url = "tcp://1.117.158.2:1883";
    private MqttClient mqttClient;
    private MqttConnectOptions options;

    private static MqttUtil mqttUtil;
    // 消息处理器
    private static Map<String, Handler> messageHandlers = new HashMap<>();

    public ExecutorService downloadScheduler = Executors.newFixedThreadPool(3);

    /**
     * 单例模式获取对象
     * @param context 以为需要获取设备唯一id 所以需要一个上下文
     * @return mqttUtil对象
     */
    public synchronized static MqttUtil getMqttUtil(Context context) {
        if (mqttUtil == null) {
            mqttUtil = new MqttUtil(context);
        }

        return mqttUtil;
    }

    private MqttUtil(Context context) {
        try {
            // 获取设备id
            String clientId = DeviceInfoUtil.getDeviceId(context);
            // 准备连接参数
            mqttClient = new MqttClient(url, clientId, new MemoryPersistence());
            options = new MqttConnectOptions();
            options.setCleanSession(false);
            options.setKeepAliveInterval(20);
            options.setConnectionTimeout(10);
            // 设置回调
            mqttClient.setCallback(new MqttCallback() {
                @Override
                public void connectionLost(Throwable cause) {
                    Log.d(TAG, "断链重新连接.");
                    // 重新链接
                    connect();
                }

                @Override
                public void messageArrived(String topic, MqttMessage message) throws Exception {
                    // 接收到消息
                    Log.d(TAG, "接收到消息: " + message);
                    processMessage(topic, message);
                }

                @Override
                public void deliveryComplete(IMqttDeliveryToken token) {
                    // 不知道干嘛的
                    Log.d(TAG, "不知道干嘛的: " + token);
                }
            });
        } catch (MqttException e) {
            e.printStackTrace();
        }
        // 获取链接
        connect();
        // 启动保持连接线程
        startReconnect();
    }

    /**
     * 接收消息处理器
     *
     * @param topic 主题
     * @param message 消息
     */
    public void processMessage(String topic, MqttMessage message) {
        if (messageHandlers.containsKey(topic)) {
            Handler handler = messageHandlers.get(topic);
            try {
                JSONObject jsonObject = new JSONObject(new String(message.getPayload()));
                switch (topic) {
                    // 切换视频播放
                    case "switchVideo":
                            Log.e(TAG, "processMessage -- switchVideo: " + jsonObject);
                            assert handler != null;
                            // 切换横竖屏
                            if (jsonObject.getBoolean("isLandscape")) {
                                handler.sendMessage(handler.obtainMessage(2, ""));
                            } else {
                                handler.sendMessage(handler.obtainMessage(3, ""));
                            }
                            String fileName = jsonObject.getString("fileName");
                            if (!fileName.equals("")) {
                                handler.sendMessageDelayed(handler.obtainMessage(1, fileName), 5000);
                            }
                        break;
                    // 更新视频
                    case "updVideo":
                        Log.e(TAG, "processMessage -- updVideo: " + jsonObject);
                        JSONArray data = jsonObject.getJSONArray("data");
                        for (int i = 0; i < data.length(); i++) {
                            JSONObject file = data.getJSONObject(i);
                            String fileName1 = file.getString("fileName");
                            String url = file.getString("url");
                            downloadScheduler.submit(() -> {
                                Log.d(TAG, "processMessage -- updVideo: " + fileName1 + ", 开始下载");
                                boolean downloadVideo = DownloadUtil.downloadVideo(null, url, fileName1);
                                Log.d(TAG, "processMessage -- updVideo: " + fileName1 + ", " + downloadVideo);
                            });
                        }
                        break;
                }
            } catch (JSONException e) {
                Log.e(TAG, "processMessage: json格式异常", e);
            }
        } else {
            Log.e(TAG, "No handler found for topic: " + topic);
        }
    }

    /**
     * 订阅主题
     * @param topic 主题
     * @param qos 等级
     */
    public MqttUtil subscribe(String topic, int qos, Handler handler) {
        try {
            if (!mqttClient.isConnected()) {
               connect();
            }
            if (mqttClient.isConnected()) {
                mqttClient.subscribe(topic, qos);
                // 消息处理器绑定
                messageHandlers.put(topic, handler);
                Log.d(TAG, "subscribe: 订阅成功.");
            } else {
                Log.e(TAG, "subscribe: 重试后仍然没有连接到服务器.");
            }
        } catch (MqttException e) {
            e.printStackTrace();
        }
        return mqttUtil;
    }

    /**
     * 开始重新连接
     */
    private void startReconnect() {
        ScheduledExecutorService scheduler = Executors.newSingleThreadScheduledExecutor();
        scheduler.scheduleAtFixedRate(() -> {
//            Log.e(TAG, "subscribe连接状态: " + mqttClient.isConnected());
            if (!mqttClient.isConnected()) {
                connect();
            }
        }, 0, 10 * 1000, TimeUnit.MILLISECONDS);
    }


    /**
     * 连接mqtt服务器
     */
    private void connect() {
        if (!mqttClient.isConnected()) {
            try {
                mqttClient.connect(options);
            } catch (MqttException e) {
                e.printStackTrace();
            }
        }
    }


}
