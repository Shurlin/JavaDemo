package xyz.shurlin.demo2.ui.list.ai;

import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import xyz.shurlin.demo2.R;
import xyz.shurlin.demo2.data.network.ChatRequest;
import xyz.shurlin.demo2.data.network.ChatResponse;
import xyz.shurlin.demo2.network.ApiClient;
import xyz.shurlin.demo2.network.ApiService;

public class AIChatActivity extends AppCompatActivity {

    private EditText etInput;
    private TextView tvResult;
    private ApiService api;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_ai_chat);

        etInput = findViewById(R.id.etInput);
        tvResult = findViewById(R.id.tvResult);
        Button btnSend = findViewById(R.id.btnSend);

        api = ApiClient.getApiService();

        btnSend.setOnClickListener(v -> send());
    }

    private void send() {
        ChatRequest req = new ChatRequest();
        req.message = etInput.getText().toString();

        tvResult.setText("思考中...");

        api.chat(req).enqueue(new Callback<>() {
            @Override
            public void onResponse(Call<ChatResponse> call, Response<ChatResponse> response) {
                if (response.isSuccessful()) {
                    tvResult.setText(response.body().reply);
                } else {
                    tvResult.setText("请求失败");
                }
            }

            @Override
            public void onFailure(Call<ChatResponse> call, Throwable t) {
                tvResult.setText("网络错误");
            }
        });
    }
}
