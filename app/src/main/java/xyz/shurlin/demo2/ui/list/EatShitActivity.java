package xyz.shurlin.demo2.ui.list;


import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.Random;

import xyz.shurlin.demo2.R;
import xyz.shurlin.demo2.data.CanteenItem;

public class EatShitActivity extends AppCompatActivity {

    private ArrayList<CanteenItem> items;
    private CanteenAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_eat_shit);

        // 导航栏注册
        Toolbar toolbar = findViewById(R.id.toolbar_eat_shit);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        SharedPreferences sp = getSharedPreferences("canteen_pref", Context.MODE_PRIVATE);
        items = createItems(sp);
        adapter = new CanteenAdapter(items);

        RecyclerView recyclerView = findViewById(R.id.recyclerCanteens);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(adapter);

        Button button = findViewById(R.id.buttonEatShit);
        TextView textView = findViewById(R.id.eatShitRes);
        button.setOnClickListener(v -> {
            ArrayList<String> options = new ArrayList<>();
            StringBuilder prefs = new StringBuilder();
            for (CanteenItem item : items) {
                if (item.checked) {
                    options.add(item.text);
                    prefs.append("*");
                } else {
                    prefs.append("_");
                }
            }
            sp.edit().putString("pref", prefs.toString()).apply();
            if (!options.isEmpty()) {
                Random random = new Random();
                textView.setText(options.get(random.nextInt(options.size())));
            }

        });
    }

    private static ArrayList<CanteenItem> createItems(SharedPreferences sp) {
        String[] canteens = new String[]{
                "八分饱-美食精选",
                "八分饱-麻辣烫",
                "八分饱-煎饼侠",
                "八分饱-照烧铁板",
                "八分饱-南粉北面",
                "八分饱-烧腊",
                "八分饱-自选称重",
                "曙光-烤盘饭",
                "曙光-牛肉饭",
                "曙光-西餐",
                "袁庚-猪肚鸡",
                "袁庚-唐厨一号小炒",
                "先行-东北菜/湘菜",
                "外面-拉面",
                "外面-外卖"
        };
        int size = canteens.length;
        String pref_ori = "*".repeat(size);

        String pref = sp.getString("pref", pref_ori);
        if (pref.length() != size)
            pref = pref_ori;

        ArrayList<CanteenItem> items = new ArrayList<>();
        for (int i = 0; i < size; i++)
            items.add(new CanteenItem(canteens[i], pref.charAt(i) == '*'));
        return items;
    }

    @Override
    public boolean onSupportNavigateUp() {
        finish();
        return true;
    }
}