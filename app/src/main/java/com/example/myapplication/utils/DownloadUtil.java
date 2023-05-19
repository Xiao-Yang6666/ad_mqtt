package com.example.myapplication.utils;

import android.app.DownloadManager;
import android.content.Context;
import android.net.Uri;
import android.os.Environment;
import android.util.Log;
import android.widget.Toast;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;

import static android.content.ContentValues.TAG;

/**
 * 下载文件
 * @author XiaoYang
 * @date 2023/5/17 14:51
 */
public class DownloadUtil {
    /**
     * 下载视频文件到指定路径
     * @param context 上下文对象
     * @param fileUrl 视频文件的url
     * @param fileName 视频文件名
     * @return 是否下载成功
     */
    public static boolean downloadVideo(Context context, String fileUrl, String fileName) {
        try {
            String filePath = Environment.getExternalStorageDirectory().getAbsolutePath() + "/cc";
            File folder = new File(filePath);
            if (!folder.exists()) {
                boolean success = folder.mkdir(); // 创建文件夹
                if (!success) {
                    Log.e("TAG", "Failed to create folder");
                }
            }

            File file1 = new File(filePath + "/" + fileName);
            if (file1.exists()) {
                return true;
            }

            URL url = new URL(fileUrl);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("GET");
            conn.setConnectTimeout(5 * 1000);
            InputStream inputStream = conn.getInputStream();

            // 判断SD卡是否存在并且挂载
            if (Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)) {
                File file = new File(filePath, fileName);
                FileOutputStream outputStream = new FileOutputStream(file);
                byte[] buffer = new byte[1024];
                int len;
                while ((len = inputStream.read(buffer)) != -1) {
                    outputStream.write(buffer, 0, len);
                }
                outputStream.close();
                inputStream.close();
                return true;
            } else {
                Log.e(TAG, "downloadVideo: SD卡不存在或未挂载");
                return false;
            }
        } catch (Exception e) {
            e.printStackTrace();
            Log.e(TAG, "downloadVideo: 下载视频失败");
            return false;
        }
    }
}
