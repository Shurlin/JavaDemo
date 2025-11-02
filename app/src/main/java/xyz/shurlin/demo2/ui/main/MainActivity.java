package xyz.shurlin.demo2.ui.main;

import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import xyz.shurlin.demo2.data.MenuItem;
import xyz.shurlin.demo2.R;
import xyz.shurlin.demo2.ui.list.TestActivity;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    private RecyclerView recycler;
    private MenuAdapter adapter;
    private List<MenuItem> menuList = new ArrayList<>();

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        recycler = findViewById(R.id.recyclerTools);

        // 2 列网格
        GridLayoutManager glm = new GridLayoutManager(this, 2);
        recycler.setLayoutManager(glm);

        // 准备数据（新增栏目只在这里添加）
        prepareMenu();

        adapter = new MenuAdapter(this, menuList, item -> {
            // 点击跳转
            Class<?> cls = item.getTarget();
            if (cls != null) {
                Intent it = new Intent(MainActivity.this, cls);
                // 可额外传递参数
                it.putExtra("menu_id", item.getId());
                startActivity(it);
            }
        });

        recycler.setAdapter(adapter);
    }

    private void prepareMenu() {
        menuList.clear();
        // 示例栏目
        menuList.add(new MenuItem("test", "测试", "test", R.drawable.feedback, TestActivity.class));
    }
}
