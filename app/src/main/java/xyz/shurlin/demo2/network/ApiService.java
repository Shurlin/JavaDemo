package xyz.shurlin.demo2.network;

import java.util.List;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.http.*;
import xyz.shurlin.demo2.data.network.*;
import xyz.shurlin.demo2.data.network.chess.ChessStateDto;

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
    @Headers({
            "Accept: application/json",
            "Content-Type: application/json"
    })
    Call<WallCreateResponse> create(@Body WallCreateRequest request);

    @POST("/game/speed_test/post")
    Call<String> postRank(@Body SpeedRankPostRequest request);

    @GET("/game/speed_test/get")
    Call<List<SpeedRank>> listRank();

    @GET("/update/version")
    Call<String> getLatestVersion();

    @Streaming
    @GET("/update/download")
    Call<ResponseBody> downloadApk();

    @GET("/game/chess/listRooms")
    Call<List<Long>> listChessRooms();

    @GET("/game/chess/{gameId}/board")
    Call<ChessStateDto> getChessBoard(@Path("gameId") long gameId);

}
