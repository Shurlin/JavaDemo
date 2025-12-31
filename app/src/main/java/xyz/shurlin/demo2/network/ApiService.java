package xyz.shurlin.demo2.network;

import java.util.List;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.http.*;
import xyz.shurlin.demo2.data.ChessHistoryItem;
import xyz.shurlin.demo2.data.network.*;
import xyz.shurlin.demo2.data.network.chess.ChessStateDto;
import xyz.shurlin.demo2.data.network.decision_tree.DecisionTreeResponse;
import xyz.shurlin.demo2.data.network.decision_tree.DecisionTreesResponse;
import xyz.shurlin.demo2.data.network.decision_tree.NodeCreateRequest;
import xyz.shurlin.demo2.data.network.decision_tree.NodeCreateResponse;
import xyz.shurlin.demo2.data.network.decision_tree.TreeCreateRequest;
import xyz.shurlin.demo2.data.network.decision_tree.TreeCreateResponse;

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

    @GET("/game/chess/listHistory")
    Call<List<ChessHistoryItem>> listChessHistory(@Query("username") String username);

    @POST("/ai/chat")
    Call<ChatResponse> chat(@Body ChatRequest request);

    @GET("/ai/dec_tree/all")
    Call<DecisionTreesResponse> getAllTree(@Query("username") String username);

    @GET("/ai/dec_tree/{treeId}")
    @Headers("Accept: application/json")
    Call<DecisionTreeResponse> getDecisionTree(@Path("treeId") Long treeId);

    @POST("/ai/dec_tree/create_tree")
    Call<TreeCreateResponse> createTree(@Body TreeCreateRequest req);

    @POST("/ai/dec_tree/create_node")
    Call<NodeCreateResponse> createNode(@Body NodeCreateRequest req);

}
