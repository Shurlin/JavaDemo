package xyz.shurlin.demo2.network;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.Headers;
import retrofit2.http.POST;
import xyz.shurlin.demo2.data.network.*;

public interface ApiService {

    @POST("/users/login")
    @Headers({
            "Accept: application/json",
            "Content-Type: application/json"
    })
    Call<LoginResponse> login(@Body LoginRequest request);
}
