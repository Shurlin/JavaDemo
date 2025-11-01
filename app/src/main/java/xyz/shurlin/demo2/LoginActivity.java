package xyz.shurlin.demo2;

import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

public class LoginActivity extends AppCompatActivity {

    private EditText usernameInput;
    private EditText passwordInput;
    private Button submitButton;
    private Button backButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_login);

        // 处理 WindowInsets（如果需要）
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.login_root), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        usernameInput = findViewById(R.id.username_input);
        passwordInput = findViewById(R.id.password_input);
        submitButton = findViewById(R.id.login_submit_button);
        backButton = findViewById(R.id.back_button);

        submitButton.setOnClickListener(v -> {
            String user = usernameInput.getText().toString().trim();
            String pass = passwordInput.getText().toString().trim();
            // 示例：这里只做简单提示（实际应调用后端或本地验证）
            Toast.makeText(LoginActivity.this, "用户名: " + user + "\n密码长度: " + pass.length(), Toast.LENGTH_SHORT).show();
        });

        // 左下角“上一页”按钮：返回上一个 Activity（MainActivity）
        backButton.setOnClickListener(v -> {
            finish(); // 结束当前 Activity，回到上一个（MainActivity）
        });
    }
}
