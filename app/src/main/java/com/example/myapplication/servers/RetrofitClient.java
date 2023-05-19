package com.example.myapplication.servers;

import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

/**
 * 请求
 * @author XiaoYang
 * @date 2023/5/17 17:07
 */
public class RetrofitClient {
    private static final String BASE_URL = "http://101.43.28.137:8081/oauth-api/";
    private static Retrofit retrofit;

    public static synchronized Retrofit getInstance() {
        if (retrofit == null) {
            retrofit = new Retrofit.Builder()
                    .baseUrl(BASE_URL)
                    .addConverterFactory(GsonConverterFactory.create())
                    .build();
        }
        return retrofit;
    }
}
