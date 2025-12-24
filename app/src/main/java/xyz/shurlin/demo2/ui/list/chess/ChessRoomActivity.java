package xyz.shurlin.demo2.ui.list.chess;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import xyz.shurlin.demo2.R;
import xyz.shurlin.demo2.data.ChessHistoryItem;
import xyz.shurlin.demo2.network.ApiClient;
import xyz.shurlin.demo2.network.ApiService;

public class ChessRoomActivity extends AppCompatActivity {
    private EditText etChessGameId;
    private Button btnCreateChessRoom;
    private TextView tvTipLogin;
    private SwipeRefreshLayout refreshLayout;
    private RecyclerView rvChessHistory;
    private ChessHistoryAdapter chessHistoryAdapter;
    private String username;
    private boolean connected = false;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chess_room);

        etChessGameId = findViewById(R.id.etChessGameId);
        btnCreateChessRoom = findViewById(R.id.btnCreateChessRoom);
        tvTipLogin = findViewById(R.id.tvTipLogin);

        SharedPreferences sp = getSharedPreferences("profile", Context.MODE_PRIVATE);
        username = sp.getString("username", null);
        if (username == null) {
            tvTipLogin.setVisibility(View.VISIBLE);
            btnCreateChessRoom.setEnabled(false);
        } else {
            tvTipLogin.setVisibility(View.GONE);
        }

        btnCreateChessRoom.setOnClickListener(v -> {
            String gameIdString = etChessGameId.getText().toString().trim();
            if (!gameIdString.isEmpty()) {
                if (gameIdString.length() > 4) {
                    etChessGameId.setError("房间号不能超过四位");
                } else {
                    if (connected){
                        Long gameId = Long.parseLong(gameIdString);
                        Intent intent = new Intent(ChessRoomActivity.this, OnlineChessActivity.class);
                        intent.putExtra("id", gameId);
                        intent.putExtra("username", username);
                        startActivity(intent);
                    } else {
                        tvTipLogin.setText("服务器未连接");
                        tvTipLogin.setVisibility(View.VISIBLE);
                    }
                }
            } else {
                etChessGameId.setError("房间号不能为空");
            }
        });

        refreshLayout = findViewById(R.id.srlChessHistory);
        chessHistoryAdapter = new ChessHistoryAdapter();
        rvChessHistory = findViewById(R.id.rvChessHistory);
        rvChessHistory.setLayoutManager(new LinearLayoutManager(this));
        rvChessHistory.setAdapter(chessHistoryAdapter);

        updateChessHistory();

        refreshLayout.setOnRefreshListener(() -> {
            updateChessHistory();
            refreshLayout.setRefreshing(false);
        });

        rvChessHistory.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);
                //划动渐隐效果
//                int firstVisibleItemPosition = ((LinearLayoutManager) recyclerView.getLayoutManager()).findFirstVisibleItemPosition();
//                View firstVisibleChildView = recyclerView.getChildAt(0);
//                if (firstVisibleChildView != null) {
//                    float top = firstVisibleChildView.getTop();
//                    float alpha;
//                    if (top < 0) {
//                        alpha = 1.0f + top / firstVisibleChildView.getHeight();
//                    } else{
//                        alpha = 1.0f;
//                    }
//                    firstVisibleChildView.setAlpha(alpha);
//
//                }
                RecyclerView.LayoutManager layoutManager = recyclerView.getLayoutManager();
                if (layoutManager == null) return;

                for (int i = 0; i < layoutManager.getChildCount(); i++) {
                    View child = layoutManager.getChildAt(i);
                    if (child == null) continue;

                    float top = child.getTop();
                    float height = child.getHeight();

                    // 根据 top 计算 alpha，上滑渐隐
                    float alpha;
                    if (top < 0) {
                        alpha = (float) (1f - Math.pow(-top / height, 0.8));
                    } else {
                        alpha = 1f;
                    }
                    child.setAlpha(alpha);
                }
            }
        });
    }

    private void updateChessHistory() {
        ApiService api = ApiClient.getApiService();
        Call<List<ChessHistoryItem>> call = api.listChessHistory(username);
        call.enqueue(new Callback<>() {
            @Override
            public void onResponse(Call<List<ChessHistoryItem>> call, Response<List<ChessHistoryItem>> response) {
                if (response.isSuccessful() && response.body() != null) {
//                    Log.d("ChessRoomActivity", "Response: " + response.body());
                    List<ChessHistoryItem> historyList = response.body();
//                    for(ChessHistoryItem item : historyList) {
//                        Log.d("ChessRoomActivity", "History Item - User1: " + item.user1 + ", User2: " + item.user2 + ", State: " + item.state + ", Time: " + item.time);
//                    }
                    chessHistoryAdapter.setData(historyList);
                    connected = true;
                } else {
                    Log.e("ChessRoomActivity", "Failed to fetch chess history: " + response.message());
                }
            }

            @Override
            public void onFailure(Call<List<ChessHistoryItem>> call, Throwable t) {
                // Handle failure
                Log.e("ChessRoomActivity", "Failed to fetch chess history", t);
                connected = false;
            }
        });
    }
}