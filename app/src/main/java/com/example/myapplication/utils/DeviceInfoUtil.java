package com.example.myapplication.utils;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.ActivityManager;
import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationManager;
import android.os.Build;
import android.os.Environment;
import android.os.StatFs;
import android.provider.Settings;
import androidx.core.app.ActivityCompat;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.UUID;

/**
 * @author XiaoYang
 * @date 2023/5/18 14:16
 */
public class DeviceInfoUtil {
    private static final String TAG = DeviceInfoUtil.class.getSimpleName();

    private static final String UNKNOWN = "unknown";
    private static final String WIFI = "wifi";
    private static final String ETHERNET = "ethernet";
    private static final String MOBILE = "mobile";

    /**
     * 获取设备区域
     *
     * @param context 上下文
     * @return 区域代码
     */
    public static String getDeviceRegion(Context context) {
        String region = UNKNOWN;
        try {
            region = context.getResources().getConfiguration().locale.getCountry();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return region;
    }

    /**
     * 获取设备所在的地址信息
     *
     * @param context 上下文
     * @return 地址信息
     */
    public static String getDeviceLocation(Context context) {
        String location = UNKNOWN;
        try {
            LocationManager locationManager = (LocationManager) context.getSystemService(Context.LOCATION_SERVICE);
            if (ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                Location lastKnownLocation = locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
                if (lastKnownLocation != null) {
                    Geocoder geocoder = new Geocoder(context, Locale.getDefault());
                    List<Address> addresses = geocoder.getFromLocation(lastKnownLocation.getLatitude(), lastKnownLocation.getLongitude(), 1);
                    if (addresses.size() > 0) {
                        Address address = addresses.get(0);
                        StringBuilder sb = new StringBuilder();
                        if (address.getMaxAddressLineIndex() > 0) {
                            sb.append(address.getAddressLine(0)).append(",");
                        }
                        if (address.getLocality() != null) {
                            sb.append(address.getLocality()).append(",");
                        }
                        if (address.getAdminArea() != null) {
                            sb.append(address.getAdminArea()).append(",");
                        }
                        if (address.getCountryName() != null) {
                            sb.append(address.getCountryName());
                        }
                        location = sb.toString();
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return location;
    }

    /**
     * 获取设备IP地址
     *
     * @return IP地址
     */
    public static String getDeviceIpAddress() {
        String ipAddress = UNKNOWN;
        try {
            List<NetworkInterface> interfaces = Collections.list(NetworkInterface.getNetworkInterfaces());
            for (NetworkInterface iface : interfaces) {
                List<InetAddress> addresses = Collections.list(iface.getInetAddresses());
                for (InetAddress address : addresses) {
                    if (!address.isLinkLocalAddress() && !address.isLoopbackAddress()) {
                        ipAddress = address.getHostAddress();
                    }
                }
            }
        } catch (SocketException e) {
            e.printStackTrace();
        }
        return ipAddress;
    }

    /**
     * 获取设备IPv4地址
     *
     * @return IPv4地址
     */
    public static String getDeviceIPv4Address() {
        String ipAddress = UNKNOWN;
        try {
            List<NetworkInterface> interfaces = Collections.list(NetworkInterface.getNetworkInterfaces());
            for (NetworkInterface iface : interfaces) {
                List<InetAddress> addresses = Collections.list(iface.getInetAddresses());
                for (InetAddress address : addresses) {
                    if (address instanceof Inet4Address) { // 只返回IPv4地址
                        if (!address.isLinkLocalAddress() && !address.isLoopbackAddress()) {
                            ipAddress = address.getHostAddress();
                        }
                    }
                }
            }
        } catch (SocketException e) {
            e.printStackTrace();
        }
        return ipAddress;
    }

    /**
     * 获取设备型号
     *
     * @return 设备型号
     */
    public static String getDeviceModel() {
        String model = UNKNOWN;
        try {
            model = Build.MODEL;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return model;
    }

    /**
     * 获取设备内存 信息
     *
     * @return 内存信息
     */
    public static long[] getMemoryInfo(Context context) {
        ActivityManager activityManager = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
        ActivityManager.MemoryInfo memoryInfo = new ActivityManager.MemoryInfo();
        activityManager.getMemoryInfo(memoryInfo);

        long[] memoryArray = new long[2];
        memoryArray[0] = memoryInfo.availMem; // 可用内存
        memoryArray[1] = memoryInfo.totalMem; // 总内存
        return memoryArray;
    }

    /**
     * 获取当前应用版本号
     *
     * @param context 上下文
     * @return 版本号
     */
    public static String getAppVersion(Context context) {
        String version = UNKNOWN;
        try {
            PackageInfo pInfo = context.getPackageManager().getPackageInfo(context.getPackageName(), 0);
            version = pInfo.versionName;
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }
        return version;
    }

    /**
     * 获取设备唯一ID
     *
     * @param context 上下文
     * @return 设备唯一ID
     */
    @SuppressLint("HardwareIds")
    public static String getDeviceId(Context context) {
        String deviceId = UNKNOWN;
        try {
            deviceId = Settings.Secure.getString(context.getContentResolver(), Settings.Secure.ANDROID_ID);
            if (deviceId == null || deviceId.equals("9774d56d682e549c")) {
                deviceId = UUID.randomUUID().toString();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return deviceId;
    }
    /**
     * 获取当前网络类型
     *
     * @param context 上下文
     * @return 网络类型
     */
    public static String getNetworkType(Context context) {
        String networkType = UNKNOWN;
        try {
            if (ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_NETWORK_STATE) == PackageManager.PERMISSION_GRANTED) {
                android.net.ConnectivityManager cm = (android.net.ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
                android.net.NetworkInfo activeNetwork = cm.getActiveNetworkInfo();
                if (activeNetwork != null) {
                    if (activeNetwork.getType() == android.net.ConnectivityManager.TYPE_WIFI) {
                        networkType = WIFI;
                    } else if (activeNetwork.getType() == android.net.ConnectivityManager.TYPE_ETHERNET) {
                        networkType = ETHERNET;
                    } else if (activeNetwork.getType() == android.net.ConnectivityManager.TYPE_MOBILE) {
                        networkType = MOBILE;
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return networkType;
    }

    /**
     * 获取外部可用空间大小
     *
     * @return 可用空间大小（单位：字节）
     */
    public static long getExternalStorageSize() {
        File path = Environment.getExternalStorageDirectory();
        StatFs stat = new StatFs(path.getPath());
        long blockSize = stat.getBlockSizeLong();
        long availableBlocks = stat.getAvailableBlocksLong();
        return availableBlocks * blockSize;
    }

    /**
     * 获取外部存储空间总大小
     *
     * @return 外部存储空间总大小（单位：字节）
     */
    public static long getExternalStorageTotalSize() {
        File path = Environment.getExternalStorageDirectory();
        StatFs stat = new StatFs(path.getPath());
        long blockSize = stat.getBlockSizeLong();
        long totalBlocks = stat.getBlockCountLong();
        return totalBlocks * blockSize;
    }

    /**
     * 将字节数转换为带有单位的字符串
     *
     * @param bytes 字节数
     * @return 带有单位的字符串
     */
    public static String formatSize(long bytes) {
        final String[] units = new String[]{"B", "KB", "MB", "GB", "TB"};
        int unitIndex = 0;
        double size = bytes;
        while (size >= 1024 && unitIndex < units.length - 1) {
            size /= 1024;
            unitIndex++;
        }
        return String.format(Locale.getDefault(), "%.2f %s", size, units[unitIndex]);
    }


    /**
     * 组成JSON格式的设备信息字符串
     *
     * @param context 上下文
     * @return JSON格式的设备信息字符串
     */
    public static JSONObject getDeviceInfoJson(Context context) {
        JSONObject jsonObject = new JSONObject();
        try {
//            jsonObject.put("region", getDeviceRegion(context));
            jsonObject.put("location", getDeviceLocation(context));
            jsonObject.put("ip_address_v6", getDeviceIpAddress());
            jsonObject.put("ip_address_v4", getDeviceIPv4Address());
            jsonObject.put("model", getDeviceModel());
            jsonObject.put("app_version", getAppVersion(context));
            jsonObject.put("device_id", getDeviceId(context));
            jsonObject.put("network_type", getNetworkType(context));
            jsonObject.put("external_storage_size", formatSize(getExternalStorageSize()));
            jsonObject.put("external_storage_total_size", formatSize(getExternalStorageTotalSize()));

            long[] memoryArray = getMemoryInfo(context);
            String availableMemoryStr = formatSize(memoryArray[0]);
            String totalMemoryStr = formatSize(memoryArray[1]);
            jsonObject.put("available_memory", availableMemoryStr);
            jsonObject.put("total_memory", totalMemoryStr);

//            jsonObject.put("external_storage_size", getExternalStorageSize());
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return jsonObject;
    }
}
