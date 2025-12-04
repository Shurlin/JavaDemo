package xyz.shurlin.demo2.ui.list.chess;

import okhttp3.Response;
import xyz.shurlin.demo2.data.network.chess.ChessStateDto;
import xyz.shurlin.demo2.data.network.chess.MovePayload;
import xyz.shurlin.demo2.data.network.chess.MoveRequest;
import xyz.shurlin.demo2.network.GameWebSocketClient;
import xyz.shurlin.demo2.utils.Constants;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.squareup.moshi.JsonAdapter;
import com.squareup.moshi.Moshi;

import xyz.shurlin.demo2.R;

public class OnlineChessActivity extends AppCompatActivity {
    private static final String TAG = "ChessActivity";
    private ChessView chessView;
    private TextView tvTurn;
    private GameWebSocketClient client;
    private Moshi moshi = new Moshi.Builder().build();
    private JsonAdapter<MoveRequest> reqAdapter = moshi.adapter(MoveRequest.class);

    private long gameId = 1L;
    private String wsUrl = "ws://" + Constants.server_ip + ":8080/ws/chess?gameId=";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_online_chess);

        chessView = findViewById(R.id.chess_view);
        tvTurn = findViewById(R.id.tvTurn);

        Intent intent = getIntent();
        if (intent == null || !intent.hasExtra("id")) {
            Log.i(TAG, "no game id provided");
            return;
        }
        gameId = intent.getLongExtra("id", 1L);
        wsUrl += gameId;


        chessView.setOnLocalMoveListener((fromX, fromY, toX, toY) -> {
            // 本地走子后发送给服务器
            MoveRequest req = new MoveRequest(gameId, fromX, fromY, toX, toY);
            String msg = reqAdapter.toJson(req);
            client.sendJson(msg);
//            Log.i(TAG, "sent move: " + msg);
        });

        // 建立 WebSocket 连接
        client = new GameWebSocketClient(wsUrl, new GameWebSocketClient.Callback() {
            @Override
            public void onOpen() {
                Log.i(TAG, "ws open");
            }

            @Override
            public void onBoardState(ChessStateDto state) {
                // 将服务器的 board 字符串矩阵映射为 Piece[][] 并设置到 ChessView
                chessView.setBoardFromServer(state.board);
            }

            @Override
            public void onMoveApplied(MovePayload payload) {
                // 更新单步到 ChessView（如果你是乐观更新也可以验证）
                chessView.applyServerMove(payload);
                boolean isRedTurn = payload.next.equals("red");
                // 更新回合显示
                tvTurn.setText(isRedTurn ? "红方回合" : "黑方回合");
                tvTurn.setTextColor(isRedTurn ? Color.RED : Color.BLACK);
            }

            @Override
            public void onClosed(int code, String reason) {
                Log.i(TAG, "ws closed");
            }

            @Override
            public void onFailure(Throwable t, Response response) {
                Log.w(TAG, "ws fail", t);
            }
        });
        client.connect();

        //多人观战，使用api


    }
}