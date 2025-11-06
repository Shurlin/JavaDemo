package xyz.shurlin.demo2.network;

import retrofit2.Call;
import retrofit2.http.GET;

public interface PingService {

    @GET("/ping")
    Call<Void> pingServer();
}
