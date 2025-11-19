package xyz.shurlin.demo2.network;

import retrofit2.Call;
import retrofit2.http.*;
import xyz.shurlin.demo2.data.network.*;

public interface ApiService {

    @POST("/users/login")
    @Headers({
            "Accept: application/json",
            "Content-Type: application/json"
    })
    Call<LoginResponse> login(@Body LoginRequest request);

    @POST("/users/register")
    @Headers({
            "Accept: application/json",
            "Content-Type: application/json"
    })
    Call<RegisterResponse> register(@Body RegisterRequest request);


    @GET("/wall_data/get")
    Call<PageResponse<WallFetchResponse>> list(
            @Query("page") int page,
            @Query("size") int size
    );

    @GET("/wall_data/get/{id}")
    Call<WallFetchResponse> fetch(@Path("id") long id);

    @POST("/wall_data/create")
    Call<WallCreateResponse> create(@Body WallCreateRequest request);

}
