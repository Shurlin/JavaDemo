package xyz.shurlin.demo2.network;

import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;
import okhttp3.OkHttpClient;
import xyz.shurlin.demo2.utils.Constants;

public class ApiClient {
    private static final String BASE_URL = "http://" + Constants.server_ip + ":8080"; // 改为后端地址或 IP
    private static ApiService apiService;

    public static ApiService getApiService() {
        if (apiService == null) {
            OkHttpClient client = new OkHttpClient.Builder()
                    .build();

            Retrofit retrofit = new Retrofit.Builder()
                    .baseUrl(BASE_URL)
                    .client(client)
                    .addConverterFactory(GsonConverterFactory.create())
                    .build();

            apiService = retrofit.create(ApiService.class);
        }
        return apiService;
    }
}
