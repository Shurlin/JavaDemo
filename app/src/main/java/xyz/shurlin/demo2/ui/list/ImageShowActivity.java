package xyz.shurlin.demo2.ui.list;

import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.view.WindowInsets;
import android.view.WindowInsetsController;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.android.material.floatingactionbutton.FloatingActionButton;

import xyz.shurlin.demo2.R;

public class ImageShowActivity extends AppCompatActivity {
    private ImageView image;
    private FloatingActionButton button;
    private ActivityResultLauncher<String[]> openDocumentLauncher;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Window window = getWindow();
        WindowCompat.setDecorFitsSystemWindows(window, false);
        window.setStatusBarColor(Color.TRANSPARENT);
        window.setNavigationBarColor(Color.TRANSPARENT);

        setContentView(R.layout.activity_image_show);

        final WindowInsetsController controller = getWindow().getInsetsController();
        if (controller != null) {
            // 隐藏状态栏和导航栏
//            controller.hide(WindowInsets.Type.statusBars() | WindowInsets.Type.navigationBars());
            // 在用户向内交互后不自动显示系统栏（或可改为 SHOW_TRANSIENT_BARS_BY_SWIPE）
            controller.setSystemBarsBehavior(WindowInsetsController.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE);
        }

        image = findViewById(R.id.image_xk);
        button = findViewById(R.id.btnFindImage);

        SharedPreferences prefs = getSharedPreferences("image_xk_url", MODE_PRIVATE);
        String last = prefs.getString("uri", null);
        if (last != null) {
            try {
                image.setImageURI(Uri.parse(last));
            } catch (Exception e) {
                Toast.makeText(this, "无法显示图片，可能已被移动或权限失效，请重新选择", Toast.LENGTH_SHORT).show();
                prefs.edit().remove("uri").apply();
            }
        }

        // 1) 注册 OpenDocument launcher（允许持久读取权限）
        openDocumentLauncher = registerForActivityResult(
                new ActivityResultContracts.OpenDocument(),
                uri -> {
                    if (uri == null) {
                        Toast.makeText(ImageShowActivity.this, "未选中图片", Toast.LENGTH_SHORT).show();
                        return;
                    }
                    // 请求长期读取权限（如果需要）
                    try {
                        final int takeFlags = Intent.FLAG_GRANT_READ_URI_PERMISSION;
                        getContentResolver().takePersistableUriPermission(uri, takeFlags);
                    } catch (Exception e) {
                        Log.w("ImageShowActivity", "无法持久化 URI 权限（可能不支持）", e);
                    }

                    // 把图片设置给 PhotoView（PhotoView 继承自 ImageView）
                    image.setImageURI(uri);
                    prefs.edit().putString("uri", uri.toString()).apply();
                }
        );
        button.setOnClickListener(v -> openDocumentLauncher.launch(new String[]{"image/*"}));
    }
}