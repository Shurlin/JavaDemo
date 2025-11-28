package xyz.shurlin.demo2.ui.list;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.media.AudioFormat;
import android.media.AudioRecord;
import android.media.MediaRecorder;
import android.os.Bundle;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.app.ActivityCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import xyz.shurlin.demo2.R;

public class LouderActivity extends AppCompatActivity {
    private AudioRecord audioRecord;
    private boolean isRecording;
    private double maxVolume = 0d;
    private ConstraintLayout layout;
    private TextView num;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_louder);

        layout = findViewById(R.id.louder_main);
        num = findViewById(R.id.louder_vol);

        // 导航栏注册
//        Toolbar toolbar = findViewById(R.id.toolbar_louder);
//        setSupportActionBar(toolbar);
//        getSupportActionBar().setDisplayHomeAsUpEnabled(true);


        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.RECORD_AUDIO}, PackageManager.PERMISSION_GRANTED);
        } else {
            startRecord();
        }

    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == 1) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                startRecord();
            }
        }
    }

    @SuppressLint("MissingPermission")
    void startRecord() {
        int sampleRate = 44100;
        int channelConfig = AudioFormat.CHANNEL_IN_MONO;
        int audioFormat = AudioFormat.ENCODING_PCM_16BIT;
        int bufferSize = AudioRecord.getMinBufferSize(sampleRate, channelConfig, audioFormat);
        audioRecord = new AudioRecord(MediaRecorder.AudioSource.MIC, sampleRate, channelConfig, audioFormat, bufferSize);
        audioRecord.startRecording();
        isRecording = true;
        new Thread(() -> {
            byte[] buffer = new byte[bufferSize];
            while (isRecording) {
                int read = audioRecord.read(buffer, 0, bufferSize);
                if (read > 0) {
                    try {
                        long sum = 0;
                        for (int i = 0; i < bufferSize; i += 2) {
                            short sample = (short) ((buffer[i] & 0xff) | (buffer[i + 1] << 8));
                            sum += sample * sample;
                        }
                        double mean = sum / (bufferSize / 2.0);
                        double volume = Math.sqrt(mean);
                        num.setText(String.format("%.2f", volume));
                        if (volume > maxVolume) maxVolume = volume;
                        int c = (int) (255 * volume / maxVolume);
                        layout.setBackgroundColor(Color.rgb(c, c, c));
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }
        }).start();
    }


    @Override
    public void finish() {
        super.finish();
        if (audioRecord != null) {
            audioRecord.stop();
            audioRecord.release();
        }
    }

//    @Override
//    public boolean onSupportNavigateUp() {
//        finish();
//        return true;
//    }
}