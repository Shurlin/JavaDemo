package xyz.shurlin.demo2.ui.main;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.Fragment;

import com.google.android.material.bottomnavigation.BottomNavigationView;

import androidx.annotation.Nullable;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import java.io.IOException;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import xyz.shurlin.demo2.R;
import xyz.shurlin.demo2.network.ApiClient;
import xyz.shurlin.demo2.network.ApiService;
import xyz.shurlin.demo2.network.PingService;
import xyz.shurlin.demo2.ui.login.LoginActivity;

public class MainActivity extends AppCompatActivity {

    private View statusDot;
    private boolean serverOn = false;
    private SwipeRefreshLayout swipeRefreshLayout;
    private BottomNavigationView bottomNav;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);


        //底部导航栏
        bottomNav = findViewById(R.id.bottom_nav);
        bottomNav.setOnItemSelectedListener(item -> {
            int id = item.getItemId();

            if (id == R.id.nav_home) {
                loadFragment(new HomeFragment());
                return true;
            }
            if (id == R.id.nav_user) {
                loadFragment(new UserFragment());
                return true;
            }
            return false;
        });


        statusDot = findViewById(R.id.server_status_dot);
        swipeRefreshLayout = findViewById(R.id.swipeHome);
        Fragment userFragment = getSupportFragmentManager().findFragmentById(R.id.user_fragment);


        swipeRefreshLayout.setOnRefreshListener(() -> {
            swipeRefreshLayout.setRefreshing(true);
            checkServer(() -> {
                if (bottomNav.getSelectedItemId() == R.id.nav_user) {
                    if (userFragment instanceof UserFragment)
                        ((UserFragment) userFragment).updateDisplay();
                }
                Toast.makeText(MainActivity.this, "刷新成功", Toast.LENGTH_SHORT).show();
                swipeRefreshLayout.setRefreshing(false);
            }, () -> {
                Toast.makeText(MainActivity.this, "服务器未连接", Toast.LENGTH_SHORT).show();
                swipeRefreshLayout.setRefreshing(false);
            });


        });

        if (savedInstanceState == null) {
            bottomNav.setSelectedItemId(R.id.nav_home);
        }

        checkServer(null, null);
    }

    private void loadFragment(Fragment fragment) {
        getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.fragment_container, fragment)
                .commit();
    }

    void checkServer(Runnable onSuccess, Runnable onFailure) {
        PingService ping = ApiClient.getPingService();
        Call<Void> call = ping.pingServer();

        call.enqueue(new Callback<Void>() {
            @Override
            public void onResponse(@NonNull Call<Void> call, @NonNull Response<Void> response) {
                runOnUiThread(() -> {
                    Log.d("a", String.valueOf(response.code()));
                    if (response.isSuccessful()) {
                        statusDot.setBackgroundResource(R.drawable.status_dot_on);
                        serverOn = true;
                        if (onSuccess != null)
                            onSuccess.run();
                    } else {
                        statusDot.setBackgroundResource(R.drawable.status_dot_off);
                        serverOn = false;
                        if (onFailure != null)
                            onFailure.run();
                    }
                });
            }

            @Override
            public void onFailure(@NonNull Call<Void> call, Throwable t) {
                runOnUiThread(() -> {
                    statusDot.setBackgroundResource(R.drawable.status_dot_off);
                    serverOn = false;
                    if (onFailure != null)
                        onFailure.run();
                });

            }
        });
    }
}
