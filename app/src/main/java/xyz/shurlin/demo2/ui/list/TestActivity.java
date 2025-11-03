package xyz.shurlin.demo2.ui.list;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Date;
import java.util.Random;

import xyz.shurlin.demo2.R;

public class TestActivity extends AppCompatActivity {

    private int time = 0;


    @SuppressLint("DefaultLocale")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_test);

        Toolbar toolbar = findViewById(R.id.toolbar_test);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);


        time = 0;
        Random random = new Random();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

        TextView textViewSum = findViewById(R.id.test_test1);
        TextView textViewEvery = findViewById(R.id.test_test2);

        Button btn = findViewById(R.id.button1);
        btn.setOnClickListener((e)->{
            LocalDateTime now = LocalDateTime.now();
            int ctime = random.nextInt(150) + 30;
            time += ctime;
            textViewSum.setText(String.format("cd一共c了%d分钟",time));
            textViewEvery.setText(String.format("%s\ncd又去图书馆c了%d分钟；%s",textViewEvery.getText(),ctime,now.format(formatter)));
            textViewSum.refreshDrawableState();
            textViewEvery.refreshDrawableState();
        });
    }

    @Override
    public boolean onSupportNavigateUp() {
        finish();
        return true;
    }
}