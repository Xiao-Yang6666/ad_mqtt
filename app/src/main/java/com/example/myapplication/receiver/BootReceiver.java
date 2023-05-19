package com.example.myapplication.receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;
import com.example.myapplication.ui.VideoActivity;

/**
 * 开机广播接收器
 * @author XiaoYang
 * @date 2023/5/17 10:58
 */
public class BootReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        Log.e("tgggg", "来广播");
        if (intent.getAction().equals("android.intent.action.BOOT_COMPLETED")) {
            Log.e("tgggg", "onReceive: 接收到开机广播");
            Intent i = new Intent(context, VideoActivity.class);
            i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            Log.e("tgggg", "onReceive: 开机自启动");
            context.startActivity(i);
        }
    }
}

