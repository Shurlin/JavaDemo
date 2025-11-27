package xyz.shurlin.demo2.ui.list;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import xyz.shurlin.demo2.R;
import xyz.shurlin.demo2.data.network.PageResponse;
import xyz.shurlin.demo2.data.network.SpeedRank;
import xyz.shurlin.demo2.data.network.SpeedRankPostRequest;
import xyz.shurlin.demo2.data.network.WallFetchResponse;
import xyz.shurlin.demo2.network.ApiClient;
import xyz.shurlin.demo2.network.ApiService;
import xyz.shurlin.demo2.ui.list.wall.WallActivity;
import xyz.shurlin.demo2.ui.list.wall.WallAdapter;

public class SpeedTestActivity extends AppCompatActivity {
    private int clickCount = 0;
    private boolean testing = false;
    private ApiService api;
    private RecyclerView recyclerView;
    private RankAdapter rankAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_speed_test);

        TextView txtStatus = findViewById(R.id.txt_status);
        Button btnStart = findViewById(R.id.btn_start_test);
        Button btnClick = findViewById(R.id.btn_click_area);
        btnClick.setEnabled(false);

        rankAdapter = new RankAdapter();
        recyclerView = findViewById(R.id.rankSpeedTest);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(rankAdapter);


        api = ApiClient.getApiService();

        btnStart.setOnClickListener(v -> {
            btnStart.setEnabled(false);
            txtStatus.setText("准备…3");
            new CountDownTimer(3000, 1000) {
                int n = 3;

                @Override
                public void onTick(long millisUntilFinished) {
                    txtStatus.setText("准备…" + n);
                    n--;
                }

                @Override
                public void onFinish() {
                    txtStatus.setText("开始狂点！");
                    btnClick.setEnabled(true);
                    clickCount = 0;
                    testing = true;

                    // 5秒测试计时器
                    new CountDownTimer(5000, 1000) {
                        @Override
                        public void onTick(long millisUntilFinished) {
                            txtStatus.setText("剩余 " + (millisUntilFinished / 1000) + " 秒");
                        }

                        @Override
                        public void onFinish() {
                            testing = false;
                            btnStart.setText("再来一次");
                            btnStart.setEnabled(true);
                            btnClick.setEnabled(false);

                            double cps = clickCount / 5.0;
                            txtStatus.setText(
                                    String.format("测试结束！\n总点击：%d 次\n平均手速：%.2f 次/秒 (CPS)", clickCount, cps)
                            );

                            postAndGet();

                        }
                    }.start();
                }
            }.start();
        });

        btnClick.setOnClickListener(v -> {
            if (testing) {
                clickCount++;
            }
        });
    }

    private void postAndGet() {
        SharedPreferences sp = getSharedPreferences("profile", Context.MODE_PRIVATE);
        String username = sp.getString("username", null);
        if (username != null) {
            Call<String> postCall = api.postRank(new SpeedRankPostRequest(username, clickCount));
            postCall.enqueue(new Callback<String>() {
                @Override
                public void onResponse(Call<String> call, Response<String> response) {
                    getRanks();
                }

                @Override
                public void onFailure(Call<String> call, Throwable t) {
                    getRanks();
                }
            });
        } else {
            Toast.makeText(SpeedTestActivity.this, "登录后可参与排名", Toast.LENGTH_SHORT).show();
        }
    }

    private void getRanks() {
        Call<List<SpeedRank>> listCall = api.listRank();
        listCall.enqueue(new Callback<List<SpeedRank>>() {
            @Override
            public void onResponse(Call<List<SpeedRank>> call, Response<List<SpeedRank>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    List<SpeedRank> data = response.body();
//                    for(SpeedRank rank:data)
//                        Log.v("aaa", rank.getUsername()!=null?rank.getUsername():"000");
                    data.add(0, new SpeedRank());
                    rankAdapter.setData(data);
                    recyclerView.setVisibility(View.VISIBLE);

                    if (clickCount < data.get(1).getScore())
                        Toast.makeText(SpeedTestActivity.this, "菜就多练", Toast.LENGTH_LONG).show();
                } else {
//                    Toast.makeText(SpeedTestActivity.this, "获取失败", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<List<SpeedRank>> call, Throwable t) {
                Toast.makeText(SpeedTestActivity.this, "网络错误: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }
}
