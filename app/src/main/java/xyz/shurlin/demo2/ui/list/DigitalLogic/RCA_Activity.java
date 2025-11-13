package xyz.shurlin.demo2.ui.list.DigitalLogic;

import android.os.Bundle;
import android.widget.Button;
import android.widget.ImageView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import xyz.shurlin.demo2.R;

public class RCA_Activity extends AppCompatActivity {
    private ImageView imageView;
    private Button prevButton;  // 上一页按钮
    private Button nextButton;  // 下一页按钮
    private int currentIndex = 0;

    // 图片资源ID数组
    private int[] imageResources = {
            R.drawable.rca1,
            R.drawable.rca2,
            R.drawable.rca3,
            // add more png
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_rca);

        // 导航栏注册
        Toolbar toolbar = findViewById(R.id.toolbar_rca);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        // 初始化视图
        imageView = findViewById(R.id.imageView);
        prevButton = findViewById(R.id.prevButton);                  // 初始按钮
        nextButton = findViewById(R.id.nextButton);

        // 设置初始图片
        updateImage();

        // 设置按钮点击事件
        prevButton.setOnClickListener(v -> showPrevImage());    // 点击上一页
        nextButton.setOnClickListener(v -> showNextImage());    // 点击下一页

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
    }

    private void updateImage() {
        imageView.setImageResource(imageResources[currentIndex]);   // 设置当前图片
        updateButtonStates();                                       // 更新按钮状态
    }

    // 更新按钮状态的方法
    private void updateButtonStates() {
        if (currentIndex == 0) {                            // 如果是第一张图片，禁用"上一页"按钮
            prevButton.setEnabled(false);
            prevButton.setAlpha(0.5f);
        } else {
            prevButton.setEnabled(true);
            prevButton.setAlpha(1.0f);
        }

        if (currentIndex == imageResources.length - 1) {    // 如果是最后一张图片，禁用"下一页"按钮
            nextButton.setEnabled(false);
            nextButton.setAlpha(0.5f);
        } else {
            nextButton.setEnabled(true);
            nextButton.setAlpha(1.0f);
        }
    }

    // 显示下一张图片
    private void showNextImage() {
        if (currentIndex < imageResources.length - 1) {
            currentIndex++;
            updateImage();
        }
    }

    // 显示上一张图片
    private void showPrevImage() {
        if (currentIndex > 0) {
            currentIndex--;
            updateImage();
        }
    }

    @Override
    public boolean onSupportNavigateUp() {
        finish();
        return true;
    }
}