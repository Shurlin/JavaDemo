package xyz.shurlin.demo2.ui.list;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.os.CountDownTimer;
import android.widget.Button;
import android.widget.TextView;

import xyz.shurlin.demo2.R;

public class SpeedTestActivity extends AppCompatActivity {

    private int clickCount = 0;
    private boolean testing = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_speed_test);

        TextView txtStatus = findViewById(R.id.txt_status);
        Button btnStart = findViewById(R.id.btn_start_test);
        Button btnClick = findViewById(R.id.btn_click_area);

        btnStart.setOnClickListener(v -> {
            btnStart.setEnabled(false);
            txtStatus.setText("准备…3");
            new CountDownTimer(3000, 1000) {
                int n = 3;
                @Override
                public void onTick(long millisUntilFinished) {
                    txtStatus.setText("准备…" + n);
                    n--;
                }
                @Override
                public void onFinish() {
                    txtStatus.setText("开始狂点！");
                    btnClick.setVisibility(Button.VISIBLE);
                    clickCount = 0;
                    testing = true;

                    // 5秒测试计时器
                    new CountDownTimer(5000, 1000) {
                        @Override
                        public void onTick(long millisUntilFinished) {
                            txtStatus.setText("剩余 " + (millisUntilFinished / 1000) + " 秒");
                        }

                        @Override
                        public void onFinish() {
                            testing = false;
                            btnClick.setVisibility(Button.GONE);
                            btnStart.setText("再来一次");
                            btnStart.setEnabled(true);

                            double cps = clickCount / 5.0;

                            txtStatus.setText(
                                    String.format("测试结束！\n总点击：%d 次\n平均手速：%.2f 次/秒 (CPS)", clickCount, cps)
                            );
                        }
                    }.start();
                }
            }.start();
        });

        btnClick.setOnClickListener(v -> {
            if (testing) {
                clickCount++;
            }
        });
    }
}
