package xyz.shurlin.demo2.ui.list.DigitalLogic;

import android.os.Bundle;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

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
    private Button firstButton;
    private int currentIndex = 0;
    private int currentPage;
    private String page;
    private TextView showPage;

    // 图片资源ID数组
    private int[] imageResources = {
            R.drawable.rca1,
            R.drawable.rca2,
            R.drawable.rca3,
            R.drawable.rca4,
            R.drawable.rca5,
            R.drawable.rca6,
            R.drawable.rca7,
            R.drawable.rca8,
            R.drawable.rca9,
            R.drawable.rca10,
            R.drawable.rca11,
            R.drawable.rca12,
            R.drawable.rca13,
            R.drawable.rca14,
            R.drawable.rca15,
            R.drawable.rca16,
            R.drawable.rca17,
            R.drawable.rca18,
            R.drawable.rca19,
            R.drawable.rca20,
            R.drawable.rca21,
            R.drawable.rca22
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_rca);

        // 导航栏注册
        Toolbar toolbar = findViewById(R.id.toolbar_rca);
        toolbar.setTitleTextAppearance(this, R.style.ToolbarLongTitleAppearance);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        // 初始化视图
        imageView = findViewById(R.id.imageView);
        prevButton = findViewById(R.id.prevButton);                  // 初始按钮
        nextButton = findViewById(R.id.nextButton);
        firstButton = findViewById(R.id.firstButton);
        showPage = findViewById(R.id.pageView);
        currentPage = currentIndex + 1;

        // 设置初始图片
        updateImage();

        // 设置按钮点击事件
        prevButton.setOnClickListener(v -> showPrevImage());    // 点击上一页
        nextButton.setOnClickListener(v -> showNextImage());    // 点击下一页
        firstButton.setOnClickListener(v -> showFirstImage());

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
    }

    private void updateImage() {
        imageView.setImageResource(imageResources[currentIndex]);   // 设置当前图片
        updateButtonStates();                                       // 更新按钮状态
        updatePage();
    }

    // 更新按钮状态的方法
    private void updateButtonStates() {
        if (currentIndex == 0) {                            // 如果是第一张图片，禁用"上一页"按钮
            prevButton.setEnabled(false);
            prevButton.setAlpha(0.5f);
            firstButton.setEnabled(false);
            firstButton.setAlpha(0.5f);
        } else {
            prevButton.setEnabled(true);
            prevButton.setAlpha(1.0f);
            firstButton.setEnabled(true);
            firstButton.setAlpha(1.0f);
        }

        if (currentIndex == imageResources.length - 1) {    // 如果是最后一张图片，禁用"下一页"按钮
            nextButton.setEnabled(false);
            nextButton.setAlpha(0.5f);
        } else {
            nextButton.setEnabled(true);
            nextButton.setAlpha(1.0f);
        }
    }

    private void updatePage() {
        currentPage = currentIndex + 1;
        page = "第"+currentPage+"页/"+imageResources.length+"页";
        showPage.setText(page);
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

    private void showFirstImage() {
        if (currentIndex > 0) {
            currentIndex = 0;
            updateImage();
        }
    }

    @Override
    public boolean onSupportNavigateUp() {
        finish();
        return true;
    }
}