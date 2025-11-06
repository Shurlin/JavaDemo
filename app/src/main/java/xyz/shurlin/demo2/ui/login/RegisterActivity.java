package xyz.shurlin.demo2.ui.login;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import xyz.shurlin.demo2.R;
import xyz.shurlin.demo2.data.network.LoginRequest;
import xyz.shurlin.demo2.data.network.LoginResponse;
import xyz.shurlin.demo2.data.network.RegisterRequest;
import xyz.shurlin.demo2.data.network.RegisterResponse;
import xyz.shurlin.demo2.network.ApiClient;
import xyz.shurlin.demo2.network.ApiService;

public class RegisterActivity extends AppCompatActivity {

    private EditText etUsername, etEmail,etDisplayName,etPassword,etPasswordConfirm;
    private Button btnRegister;
    private ProgressBar progress;
    private TextView tips;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        etUsername = findViewById(R.id.etUsername);

        etEmail = findViewById(R.id.etEmail);
        etDisplayName = findViewById(R.id.etDisplayName);
        etPassword = findViewById(R.id.etPassword);
        etPasswordConfirm = findViewById(R.id.etPasswordConfirm);
        btnRegister = findViewById(R.id.btnRegister);
        progress = findViewById(R.id.progress);
        tips = findViewById(R.id.tips);

        Toolbar toolbar = findViewById(R.id.toolbar_register);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        btnRegister.setOnClickListener(e->doRegister());
    }

    private void doRegister(){
        String username = etUsername.getText().toString().trim();
        String email = etEmail.getText().toString().trim();
        String displayName = etDisplayName.getText().toString().trim();
        String password = etPassword.getText().toString();
        String passwordConfirm = etPasswordConfirm.getText().toString();

        if (username.isEmpty()) {
            tips.setText("请输入用户名");
            tips.setVisibility(View.VISIBLE);
            return;
        }
        if (email.isEmpty()) {
            tips.setText("请输入邮箱");
            tips.setVisibility(View.VISIBLE);
            return;
        }
        if (!email.contains("@") && !email.split("@")[1].contains(".")) {
            tips.setText("请输入正确的邮箱格式");
            tips.setVisibility(View.VISIBLE);
            return;
        }
        if (displayName.isEmpty()) {
            tips.setText("请输入昵称");
            tips.setVisibility(View.VISIBLE);
            return;
        }
        if (password.isEmpty()) {
            tips.setText("请输入密码");
            tips.setVisibility(View.VISIBLE);
            return;
        }
        if (password.length() < 8 || password.length() > 16) {
            tips.setText("请输入8-16位的密码");
            tips.setVisibility(View.VISIBLE);
            return;
        }
        if (!password.equals(passwordConfirm)) {
            tips.setText("两次输入的密码不一致");
            tips.setVisibility(View.VISIBLE);
            return;
        }

        progress.setVisibility(View.VISIBLE);
        btnRegister.setEnabled(false);

        ApiService service = ApiClient.getApiService();
        Call<RegisterResponse> call = service.register(new RegisterRequest(username, password, displayName, email));
        call.enqueue(new Callback<RegisterResponse>() {
            @Override
            public void onResponse(Call<RegisterResponse> call, Response<RegisterResponse> response) {
                progress.setVisibility(View.GONE);
                btnRegister.setEnabled(true);

                if (response.isSuccessful() && response.body() != null) {
                    RegisterResponse body = response.body();
                    saveToken(body.getToken());
                    Toast.makeText(RegisterActivity.this, "注册成功", Toast.LENGTH_SHORT).show();

                    finish();
                } else {
                    String msg = "注册失败";
                    if (response.errorBody() != null) {
                        try {
                            msg = response.errorBody().string().split("\"")[3];
                            Log.v("a", msg);
                        } catch (Exception ignored) {
                        }
                    }
                    tips.setText(msg);
                    tips.setVisibility(View.VISIBLE);
//                    Toast.makeText(LoginActivity.this, msg, Toast.LENGTH_LONG).show();

                }
            }

            @Override
            public void onFailure(Call<RegisterResponse> call, Throwable t) {
                progress.setVisibility(View.GONE);
                btnRegister.setEnabled(true);
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

    @Override
    public boolean onSupportNavigateUp() {
        finish();
        return true;
    }
}