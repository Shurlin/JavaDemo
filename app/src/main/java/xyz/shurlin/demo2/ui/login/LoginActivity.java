package xyz.shurlin.demo2.ui.login;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.*;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import xyz.shurlin.demo2.R;
import xyz.shurlin.demo2.network.ApiClient;
import xyz.shurlin.demo2.network.ApiService;
import xyz.shurlin.demo2.data.network.*;
import xyz.shurlin.demo2.ui.main.MainActivity;

public class LoginActivity extends AppCompatActivity {

    private EditText etUsername, etPassword;
    private Button btnLogin, btnRegister;
    private ProgressBar progress;
    private TextView tips;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        etUsername = findViewById(R.id.etUsername);
        etPassword = findViewById(R.id.etPassword);
        btnLogin = findViewById(R.id.btnLogin);
        btnRegister = findViewById(R.id.btnRegister);
        progress = findViewById(R.id.progress);
        tips = findViewById(R.id.tips);

        Toolbar toolbar = findViewById(R.id.toolbar_login);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        btnLogin.setOnClickListener(v -> doLogin());

        btnRegister.setOnClickListener(view -> {
            Intent it = new Intent(LoginActivity.this, RegisterActivity.class);
            startActivity(it);
        });
    }

    private void doLogin() {
        String username = etUsername.getText().toString().trim();
        String password = etPassword.getText().toString();

        if (username.isEmpty() || password.isEmpty()) {
//            Toast.makeText(this, "请输入用户名和密码", Toast.LENGTH_SHORT).show();
            tips.setText("请输入用户名和密码");
            tips.setVisibility(View.VISIBLE);
            return;
        }

        progress.setVisibility(View.VISIBLE);
        btnLogin.setEnabled(false);

        ApiService service = ApiClient.getApiService();
        Call<LoginResponse> call = service.login(new LoginRequest(username, password));
        call.enqueue(new Callback<LoginResponse>() {
            @Override
            public void onResponse(Call<LoginResponse> call, Response<LoginResponse> response) {
                progress.setVisibility(View.GONE);
                btnLogin.setEnabled(true);

                if (response.isSuccessful() && response.body() != null) {
                    LoginResponse body = response.body();
                    saveToken(body.getToken());
                    Toast.makeText(LoginActivity.this, "登录成功", Toast.LENGTH_SHORT).show();
                    saveLoginInfo(body);

                    Intent resultIntent = new Intent();
                    setResult(RESULT_OK, resultIntent);
                    finish();
                } else {
                    String msg = "登录失败";
                    if (response.errorBody() != null) {
                        try {
                            msg = response.errorBody().string().split("\"")[3];
//                            Log.v("a", msg);
                        } catch (Exception ignored) {
                        }
                    }
                    tips.setText(msg);
                    tips.setVisibility(View.VISIBLE);
//                    Toast.makeText(LoginActivity.this, msg, Toast.LENGTH_LONG).show();
                }
            }

            @Override
            public void onFailure(Call<LoginResponse> call, Throwable t) {
                progress.setVisibility(View.GONE);
                btnLogin.setEnabled(true);
//                Toast.makeText(LoginActivity.this, "网络错误: " + t.getMessage(), Toast.LENGTH_LONG).show();
                tips.setText("网络错误: " + t.getMessage());
                tips.setVisibility(View.VISIBLE);
            }
        });
    }

    private void saveToken(String token) {
        SharedPreferences sp = getSharedPreferences("app_prefs", MODE_PRIVATE);
        sp.edit().putString("jwt_token", token).apply();
    }

    private void saveLoginInfo(LoginResponse response) {
        SharedPreferences sp = getSharedPreferences("profile", MODE_PRIVATE);
        sp.edit()
                .putString("username", response.getUsername())
                .putString("displayName", response.getDisplayName())
                .putString("email", response.getEmail())
                .putString("token", response.getToken())
                .apply();
        Log.v("a",response.getEmail());
    }

    @Override
    public boolean onSupportNavigateUp() {
        finish();
        return true;
    }
}
