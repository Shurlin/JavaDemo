package xyz.shurlin.demo2.ui.list.chess;

import okhttp3.Response;
import xyz.shurlin.demo2.data.network.chess.ChessStateDto;
import xyz.shurlin.demo2.data.network.chess.GameEnd;
import xyz.shurlin.demo2.data.network.chess.GameRestart;
import xyz.shurlin.demo2.data.network.chess.MovePayload;
import xyz.shurlin.demo2.data.network.chess.MoveRequest;
import xyz.shurlin.demo2.network.GameWebSocketClient;
import xyz.shurlin.demo2.utils.Constants;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.media.AudioAttributes;
import android.media.AudioFormat;
import android.media.AudioManager;
import android.media.AudioRecord;
import android.media.AudioTrack;
import android.media.MediaRecorder;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import com.squareup.moshi.JsonAdapter;
import com.squareup.moshi.Moshi;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.util.Arrays;
import java.util.List;

import xyz.shurlin.demo2.R;

public class OnlineChessActivity extends AppCompatActivity {
    private static final String TAG = "ChessActivity";
    private String username;
    private ChessView chessView;
    private String playerRed;
    private String playerBlack;
    private TextView tvChessWinTip;
    private TextView tvChessPlayerRed;
    private TextView tvChessPlayerBlack;
    private TextView tvChessTurnBlack;
    private TextView tvChessTurnRed;
    private TextView tvChessMeBlack;
    private TextView tvChessMeRed;
    private Button btnChessRestart;
    private ImageButton btnChessVoice;
    private ImageButton btnChessVolume;
    private GameWebSocketClient client;
    private Moshi moshi = new Moshi.Builder().build();
    private JsonAdapter<MoveRequest> reqAdapter = moshi.adapter(MoveRequest.class);
    private JsonAdapter<GameRestart> restartAdapter = moshi.adapter(GameRestart.class);

    private long gameId = 1L;
    private String wsUrl = "ws://" + Constants.SERVER_IP + ":8080/ws/chess?gameId=";

    // Audio
    private AudioRecord audioRecord;
    private final int voice_on = R.drawable.voice_on;
    private final int voice_off = R.drawable.voice_off;
    private final int volume_on = R.drawable.volume_on;
    private final int volume_off = R.drawable.volume_off;
    private boolean voiceEnabled = false;
    private boolean volumeEnabled = true;
    private static final int SAMPLE_RATE = 16000;
    private static final int CHANNEL = AudioFormat.CHANNEL_IN_MONO;
    private static final int ENCODING = AudioFormat.ENCODING_PCM_16BIT;
    //    private final int bufferSize = AudioRecord.getMinBufferSize(SAMPLE_RATE, CHANNEL, ENCODING);
    private final int bufferSize = 4080;
    private AudioTrack audioTrack;

    private DatagramSocket socket;
    private InetAddress serverAddress;
    private final int serverPort = 50123;
    private boolean roomExists = true;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_online_chess);

        chessView = findViewById(R.id.chess_view);
        tvChessWinTip = findViewById(R.id.tvChessWinTip);
        tvChessPlayerRed = findViewById(R.id.tvChessPlayerRed);
        tvChessPlayerBlack = findViewById(R.id.tvChessPlayerBlack);
        btnChessRestart = findViewById(R.id.btnChessRestart);
        btnChessVoice = findViewById(R.id.btnChessVoice);
        btnChessVolume = findViewById(R.id.btnChessVolume);
        tvChessTurnBlack = findViewById(R.id.tvChessTurnBlack);
        tvChessTurnRed = findViewById(R.id.tvChessTurnRed);
        tvChessMeBlack = findViewById(R.id.tvChessMeBlack);
        tvChessMeRed = findViewById(R.id.tvChessMeRed);

        Intent intent = getIntent();
        if (intent == null || !intent.hasExtra("id")) {
            Log.i(TAG, "no game id provided");
            return;
        }
        gameId = intent.getLongExtra("id", 1L);
        username = intent.getStringExtra("username");
        wsUrl += gameId;


        chessView.setOnLocalMoveListener((fromX, fromY, toX, toY) -> {
            // 本地走子后发送给服务器
            MoveRequest req = new MoveRequest(gameId, username, fromX, fromY, toX, toY);
            client.sendJson(reqAdapter.toJson(req));
        });

        btnChessRestart.setOnClickListener(v -> {
            GameRestart restart = new GameRestart(gameId);
            client.sendJson(restartAdapter.toJson(restart));
//            btnChessRestart.setVisibility(View.GONE);
        });

        btnChessVoice.setOnClickListener(v -> {
            voiceEnabled = !voiceEnabled;
            btnChessVoice.setImageResource(voiceEnabled ? voice_on : voice_off);
            if (voiceEnabled) {
//                Log.i(TAG, "start voice");
                if (ActivityCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED) {
                    ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.RECORD_AUDIO}, PackageManager.PERMISSION_GRANTED);
                } else {
                    startRecord();
                }
            }
        });

        btnChessVolume.setOnClickListener(v -> {
            volumeEnabled = !volumeEnabled;
            btnChessVolume.setImageResource(volumeEnabled ? volume_on : volume_off);
//            if (volumeEnabled) {
//                new Thread(new AudioPlayer()).start();
//            }
        });

        // 建立 WebSocket 连接
        client = new GameWebSocketClient(createURL(gameId, username), new GameWebSocketClient.Callback() {
            @Override
            public void onOpen() {
                Log.i(TAG, "ws open");
            }

            @Override
            public void onBoardState(ChessStateDto state) {
                // 将服务器的 board 字符串矩阵映射为 Piece[][] 并设置到 ChessView
                chessView.setBoardFromServer(state.board);
                playerRed = state.playerRed;
                playerBlack = state.playerBlack;
                boolean isRedTurn = state.turn.equals("red");
                chessView.isRedTurn = isRedTurn;
                runOnUiThread(() -> {
                    resetUI();
                    tvChessPlayerRed.setText(playerRed);
                    tvChessPlayerBlack.setText(playerBlack);
                    tvChessTurnBlack.setVisibility(isRedTurn ? TextView.INVISIBLE : TextView.VISIBLE);
                    tvChessTurnRed.setVisibility(isRedTurn ? TextView.VISIBLE : TextView.INVISIBLE);
                    tvChessMeBlack.setVisibility(username.equals(playerBlack) ? TextView.VISIBLE : TextView.INVISIBLE);
                    tvChessMeRed.setVisibility(username.equals(playerRed) ? TextView.VISIBLE : TextView.INVISIBLE);
                    chessView.pickable = isRedTurn && username.equals(playerRed) || !isRedTurn && username.equals(playerBlack);
                });

            }

            @Override
            public void onMoveApplied(MovePayload payload) {
                // 更新单步到 ChessView（如果你是乐观更新也可以验证）
                chessView.applyServerMove(payload);
                boolean isRedTurn = payload.next.equals("red");
                chessView.isRedTurn = isRedTurn;
                runOnUiThread(() -> {
                    // 更新回合显示
                    tvChessPlayerRed.setText(playerRed);
                    tvChessPlayerBlack.setText(playerBlack);
                    tvChessTurnBlack.setVisibility(isRedTurn ? TextView.INVISIBLE : TextView.VISIBLE);
                    tvChessTurnRed.setVisibility(isRedTurn ? TextView.VISIBLE : TextView.INVISIBLE);
                    chessView.pickable = isRedTurn && username.equals(playerRed) || !isRedTurn && username.equals(playerBlack);
                });

            }

            @Override
            public void onGameEnd(GameEnd gameEnd) {
                String winner = gameEnd.winner;
                String msg;
                if (winner.equals(username)) {
                    msg = "你赢了!";
                } else {
                    msg = "你输了!";
                }
                runOnUiThread(() -> {
                    if (winner.equals(playerRed)) {
                        tvChessPlayerRed.setTextColor(Color.YELLOW);
                        tvChessPlayerRed.setTextSize(40);
                    } else if (winner.equals(playerBlack)) {
                        tvChessPlayerBlack.setTextColor(Color.YELLOW);
                        tvChessPlayerBlack.setTextSize(40);
                    }
                    tvChessWinTip.setVisibility(TextView.VISIBLE);
                    tvChessWinTip.setText(msg);
                    chessView.pickable = false;
                    btnChessRestart.setVisibility(View.VISIBLE);
                });
            }

            @Override
            public void onClosed(int code, String reason) {
                roomExists = false;
                Log.i(TAG, "ws closed");
            }

            @Override
            public void onFailure(Throwable t, Response response) {
                Log.w(TAG, "ws fail", t);
            }
        });
        client.connect();

        audioTrack = new AudioTrack.Builder()
                .setAudioAttributes(new android.media.AudioAttributes.Builder()
                        .setUsage(AudioAttributes.USAGE_MEDIA)
                        .setContentType(AudioAttributes.CONTENT_TYPE_SPEECH)
                        .build())
                .setAudioFormat(new AudioFormat.Builder()
                        .setEncoding(ENCODING)
                        .setSampleRate(SAMPLE_RATE)
                        .setChannelMask(AudioFormat.CHANNEL_OUT_MONO)
                        .build())
                .setBufferSizeInBytes(bufferSize)
                .setTransferMode(AudioTrack.MODE_STREAM)
                .build();


        new Thread(() -> {
            //初始化语音服务与发送进入房间语音包
            try {
                if (socket == null || socket.isClosed()) {
                    socket = new DatagramSocket();
                    serverAddress = InetAddress.getByName(Constants.SERVER_IP);
                }
                byte[] header1 = String.format("%04d", gameId).getBytes();
                byte[] header2 = String.format("%-12s", username).getBytes();
                byte[] packetEnterData = new byte[header1.length + header2.length];
                System.arraycopy(header1, 0, packetEnterData, 0, header1.length);
                System.arraycopy(header2, 0, packetEnterData, header1.length, header2.length);
                DatagramPacket packetEnter = new DatagramPacket(packetEnterData, header1.length + header2.length, serverAddress, serverPort);
                socket.send(packetEnter);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }).start();
        new Thread(new AudioPlayer()).start();
    }

    private void resetUI() {
        tvChessWinTip.setVisibility(TextView.GONE);
        btnChessRestart.setVisibility(View.GONE);
        tvChessPlayerRed.setVisibility(TextView.VISIBLE);
        tvChessPlayerRed.setTextColor(Color.RED);
        tvChessPlayerBlack.setVisibility(TextView.VISIBLE);
        tvChessPlayerBlack.setTextColor(Color.BLACK);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        client.close();
        if (audioRecord != null) {
            audioRecord.stop();
            audioRecord.release();
        }
        if (socket != null && !socket.isClosed()) {
            socket.close();
        }
        if (audioTrack != null) {
            audioTrack.stop();
            audioTrack.release();
        }
    }

    private static String createURL(Long gameId, String username) {
        return "ws://" + Constants.SERVER_IP + ":8080/ws/chess?gameId=" + gameId + "&username=" + username;
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
        audioRecord = new AudioRecord(MediaRecorder.AudioSource.MIC, SAMPLE_RATE, CHANNEL, ENCODING, bufferSize);
        audioRecord.startRecording();
        new Thread(new AudioRecorder()).start();
    }

    class AudioRecorder implements Runnable {
        @Override
        public void run() {
            Log.i(TAG, "audio recording started");
            audioRecord.startRecording();
            try {
                // 包头
                byte[] header1 = String.format("%04d", gameId).getBytes();
                byte[] header2 = String.format("%-12s", username).getBytes();

                byte[] buffer = new byte[bufferSize];
                while (voiceEnabled) {
                    int read = audioRecord.read(buffer, 0, bufferSize);
                    if (read > 0) {
                        byte[] packetData = new byte[header1.length + header2.length + read];
                        System.arraycopy(header1, 0, packetData, 0, header1.length);
                        System.arraycopy(header2, 0, packetData, header1.length, header2.length);
                        System.arraycopy(buffer, 0, packetData, header1.length + header2.length, read);

                        // 发送音频数据到服务器
                        DatagramPacket packet = new DatagramPacket(packetData, packetData.length, serverAddress, serverPort);
                        socket.send(packet);
//                        Log.i(TAG, "audio sent" + Arrays.toString(buffer));
                    }
                }
                audioRecord.stop();
                Log.i(TAG, "audio recording stopped");
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    class AudioPlayer implements Runnable {

        @Override
        public void run() {
            Log.i(TAG, "audio playing started");
            byte[] buffer = new byte[bufferSize + 16]; // header + pcm
            DatagramPacket packet = new DatagramPacket(buffer, buffer.length);
            try {
                if (audioTrack == null) {
                    Log.e(TAG, "AudioTrack is null, abort AudioPlayer");
                    return;
                }
                audioTrack.play();
                while (roomExists) {
                    try {
                        socket.receive(packet); // 阻塞点
                    } catch (SocketException e) {
                        // ★ socket 被关闭时一定会走到这里
                        Log.i(TAG, "socket closed, audio thread exiting");
                        break;
                    }

                    int totalLen = packet.getLength();
                    int audioLen = totalLen - 16;
                    byte[] audioData = new byte[audioLen];


                    if (volumeEnabled){
                        Log.i(TAG, "audio received:" + totalLen);
                        if (totalLen <= 16) {
                            continue; // 非法包
                        }
                        System.arraycopy(packet.getData(), 16, audioData, 0, audioLen);
                    }else {
                        // 静音处理
                        for (int i = 0; i < audioLen; i++) {
                            audioData[i] = 0;
                        }
                    }

                    // 写入 AudioTrack 播放，状态保护
                    if (audioTrack.getState() != AudioTrack.STATE_INITIALIZED) {
                        Log.w(TAG, "AudioTrack not initialized, skip frame");
                        continue;
                    }
                    audioTrack.write(audioData, 0, audioLen);
                }

            } catch (Exception e) {
                Log.e(TAG, "AudioPlayer error", e);
            } finally {
                try {
                    if (audioTrack != null) {
                        audioTrack.pause();
                        audioTrack.flush();
                    }
                } catch (Exception ignored) {}
            }
        }
    }

}