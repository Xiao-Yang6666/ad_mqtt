package com.example.myapplication.servers;

import com.google.gson.JsonObject;
import lombok.Data;
import org.json.JSONObject;
import retrofit2.Call;
import retrofit2.http.*;

/**
 * api接口
 * @author XiaoYang
 * @date 2023/5/17 17:05
 */
public interface ApiService {
    @GET("s")
    Call<String> getUser(@Query("wd") String username);

    @POST("oauth/getMobilePhoneV2/{userId}/{appId}")
    Call<JsonObject> getMobilePhoneV2(@Path("userId") String userId, @Path("appId") String appId, @Body GetMobilePhoneV2Body body);

    @GET("heartbeat")
    Call<String> sendHeartbeat(@Query("deviceId") String deviceId);

    @POST("device/info")
    Call<Void> reportDeviceInfo(@Body JSONObject deviceInfo);

    @Data
    class GetMobilePhoneV2Body {
        String token;
        String userInformation;
        String sign;
        String encrypt;
    }
}
