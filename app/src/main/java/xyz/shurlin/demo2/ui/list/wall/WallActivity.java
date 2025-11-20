package xyz.shurlin.demo2.ui.list.wall;

import android.app.AlertDialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import xyz.shurlin.demo2.R;
import xyz.shurlin.demo2.data.network.PageResponse;
import xyz.shurlin.demo2.data.network.WallCreateRequest;
import xyz.shurlin.demo2.data.network.WallCreateResponse;
import xyz.shurlin.demo2.data.network.WallFetchResponse;
import xyz.shurlin.demo2.network.ApiClient;
import xyz.shurlin.demo2.network.ApiService;
import xyz.shurlin.demo2.utils.Utils;

public class WallActivity extends AppCompatActivity {

    private RecyclerView recyclerView;
    private WallAdapter adapter;
    private SwipeRefreshLayout swipeRefresh;
    private ApiService api;

    private int currentPage = 0;
    private final int pageSize = 10;
    private boolean isRequestRunning = false;
    private boolean isLastPage = false; //


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_wall);

        // 导航栏注册
        Toolbar toolbar = findViewById(R.id.toolbar_wall);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        recyclerView = findViewById(R.id.wallRecyclerView);
        swipeRefresh = findViewById(R.id.wallSwipeRefresh);
        findViewById(R.id.wallPost).setOnClickListener(v -> showPostDialog());

        adapter = new WallAdapter();
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(adapter);

        api = ApiClient.getApiService();

        swipeRefresh.setOnRefreshListener(this::refreshData);

        recyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrolled(@NonNull RecyclerView rv, int dx, int dy) {
                if (!isRequestRunning && !isLastPage) {
                    LinearLayoutManager lm = (LinearLayoutManager) rv.getLayoutManager();
                    if (lm != null && lm.findLastVisibleItemPosition() >= adapter.getItemCount() - 2) {
                        loadMore();
                    }
                }
            }
        });

        // 初始加载
        swipeRefresh.setRefreshing(true);
        refreshData();
    }

    private void refreshData() {
        if (isRequestRunning) return;
        isRequestRunning = true;
        isLastPage = false;
        currentPage = 0;

        Call<PageResponse<WallFetchResponse>> call = api.list(currentPage, pageSize);
        call.enqueue(new Callback<PageResponse<WallFetchResponse>>() {
            @Override
            public void onResponse(Call<PageResponse<WallFetchResponse>> call, Response<PageResponse<WallFetchResponse>> response) {
                isRequestRunning = false;
                swipeRefresh.setRefreshing(false);
                if (response.isSuccessful() && response.body() != null) {
                    List<WallFetchResponse> list = response.body().getContent();
                    adapter.setData(list);

                    isLastPage = list == null || list.size() < pageSize;
                } else {
                    Toast.makeText(WallActivity.this, "获取失败", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<PageResponse<WallFetchResponse>> call, Throwable t) {
                swipeRefresh.setRefreshing(false);
                isRequestRunning = false;
//                Log.d("a",t.getMessage());
                Toast.makeText(WallActivity.this, "网络错误: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void loadMore() {
        if (isRequestRunning || isLastPage) return;
        isRequestRunning = true;

        final int nextPage = currentPage + 1;
        Call<PageResponse<WallFetchResponse>> call = api.list(nextPage, pageSize);
        call.enqueue(new Callback<PageResponse<WallFetchResponse>>() {
            @Override
            public void onResponse(Call<PageResponse<WallFetchResponse>> call, Response<PageResponse<WallFetchResponse>> response) {
                isRequestRunning = false;
                if (response.isSuccessful() && response.body() != null) {
                    List<WallFetchResponse> list = response.body().getContent();
                    if (list != null && !list.isEmpty()) {
                        adapter.addData(list); //TODO unique
                        currentPage = nextPage;
//                        Toast.makeText(WallActivity.this, String.valueOf(list.size()), Toast.LENGTH_SHORT).show();
                        if (list.size() < pageSize) {

                            isLastPage = true;
                        }
                    } else {
                        isLastPage = true;
                    }
                }
            }

            @Override
            public void onFailure(Call<PageResponse<WallFetchResponse>> call, Throwable t) {
                isRequestRunning = false;
            }
        });
    }

    private void showPostDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("发布表白");

        View view = LayoutInflater.from(this).inflate(R.layout.dialog_wall_post, null);
        EditText etTitle = view.findViewById(R.id.etTitle);
        EditText etContent = view.findViewById(R.id.etContent);
        builder.setView(view);

        builder.setPositiveButton("发布", (dialog, which) -> {
            String title = etTitle.getText().toString().trim();
            String content = etContent.getText().toString().trim();
            if (title.isEmpty()) {
                Toast.makeText(this, "标题不能为空", Toast.LENGTH_SHORT).show();
                return;
            }
            if (title.length() > 16) {
                Toast.makeText(this, "标题不能超过16个字", Toast.LENGTH_SHORT).show();
                return;
            }
            if (content.isEmpty()) {
                Toast.makeText(this, "内容不能为空", Toast.LENGTH_SHORT).show();
                return;
            }
            SharedPreferences sp = getSharedPreferences("profile", Context.MODE_PRIVATE);
            String username = sp.getString("username", null);
            if (username == null) {
                username = Utils.getLocalIPv4();
            }


            WallCreateRequest req = new WallCreateRequest(title, content, username);
            Call<WallCreateResponse> call = api.create(req);
            Toast.makeText(this, "正在发布...", Toast.LENGTH_SHORT).show();
            call.enqueue(new Callback<WallCreateResponse>() {
                @Override
                public void onResponse(Call<WallCreateResponse> call, Response<WallCreateResponse> response) {
                    if (response.isSuccessful()) {
                        Toast.makeText(WallActivity.this, "发布成功", Toast.LENGTH_SHORT).show();
                        // 刷新列表
                        swipeRefresh.setRefreshing(true);
                        refreshData();
                    } else {
                        Toast.makeText(WallActivity.this, "发布失败", Toast.LENGTH_SHORT).show();
                    }
                }

                @Override
                public void onFailure(Call<WallCreateResponse> call, Throwable t) {
                    Toast.makeText(WallActivity.this, "网络失败: " + t.getMessage(), Toast.LENGTH_SHORT).show();
                }
            });
        });

        builder.setNegativeButton("取消", null);
        builder.show();
    }

    @Override
    public boolean onSupportNavigateUp() {
        finish();
        return true;
    }
}

