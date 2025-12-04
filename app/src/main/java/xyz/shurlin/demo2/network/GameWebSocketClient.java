package xyz.shurlin.demo2.network;

import android.os.Handler;
import android.os.Looper;
import android.util.Log;

import androidx.annotation.NonNull;

import com.squareup.moshi.JsonAdapter;
import com.squareup.moshi.Moshi;

import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.WebSocket;
import okhttp3.WebSocketListener;
import okio.ByteString;
import xyz.shurlin.demo2.data.network.chess.ChessStateDto;
import xyz.shurlin.demo2.data.network.chess.MovePayload;

public class GameWebSocketClient {
    private static final String TAG = "GameWebSocketClient";
    private final OkHttpClient client;
    private final String wsUrl; // e.g. "ws://10.0.2.2:8080/ws/chess?gameId=1"
    private final Moshi moshi = new Moshi.Builder().build();
    private WebSocket ws;
    private final Handler main = new Handler(Looper.getMainLooper());
    private final AtomicBoolean closedByUser = new AtomicBoolean(false);

    // 外部回调
    public interface Callback {
        void onOpen();

        void onBoardState(ChessStateDto boardState);

        void onMoveApplied(MovePayload payload);

        void onClosed(int code, String reason);

        void onFailure(Throwable t, Response response);
    }

    private final Callback callback;

    public GameWebSocketClient(String wsUrl, Callback cb) {
        this.wsUrl = wsUrl;
        this.callback = cb;
        this.client = new OkHttpClient.Builder()
                .pingInterval(30, TimeUnit.SECONDS)
                .retryOnConnectionFailure(true)
                .build();
    }

    public void connect() {
        closedByUser.set(false);
        Request req = new Request.Builder().url(wsUrl).build();
        ws = client.newWebSocket(req, listener);
        Log.d(TAG, "ws connecting to " + wsUrl);
    }

    public void close() {
        closedByUser.set(true);
        if (ws != null) ws.close(1000, "client close");
    }

    public void sendJson(String json) {
        if (ws != null) ws.send(json);
    }

    // parse helpers
    private final JsonAdapter<ChessStateDto> boardAdapter = moshi.adapter(ChessStateDto.class);
    private final JsonAdapter<MovePayload> payloadAdapter = moshi.adapter(MovePayload.class);

    private final WebSocketListener listener = new WebSocketListener() {
        @Override
        public void onOpen(@NonNull WebSocket webSocket, @NonNull Response response) {
            Log.i(TAG, "ws opened");
            if (callback != null) main.post(callback::onOpen);
        }

        @Override
        public void onMessage(@NonNull WebSocket webSocket, @NonNull String text) {
            // try parse as board state first
            try {
                ChessStateDto state = boardAdapter.fromJson(text);
                Log.i(TAG, "ws msg: " + text);
                if (state != null && "CHESS_STATE".equalsIgnoreCase(state.type)) {
                    Log.i(TAG, "ws board state: " + state.turn);
                    if (callback != null) main.post(() -> callback.onBoardState(state));
                    return;
                }
            } catch (Exception ignored) {
            }

            // try move applied
            try {
                MovePayload ap = payloadAdapter.fromJson(text);
                if (ap != null && "MOVE_APPLIED".equalsIgnoreCase(ap.type)) {
                    if (callback != null) main.post(() -> callback.onMoveApplied(ap));
                    return;
                }
            } catch (Exception ignored) {
            }

            // 未识别消息：忽略或扩展处理
            Log.d(TAG, "unk ws msg: " + text);
        }

        @Override
        public void onMessage(@NonNull WebSocket webSocket, ByteString bytes) {
            onMessage(webSocket, bytes.utf8());
        }

        @Override
        public void onClosing(@NonNull WebSocket webSocket, int code, @NonNull String reason) {
            Log.i(TAG, "ws closing " + code + " " + reason);
        }

        @Override
        public void onClosed(@NonNull WebSocket webSocket, int code, @NonNull String reason) {
            Log.i(TAG, "ws closed " + code + " " + reason);
            if (callback != null) main.post(() -> callback.onClosed(code, reason));
            // 如果不是用户主动关闭，尝试重连（这里不自动重连，保持简单）
        }

        @Override
        public void onFailure(@NonNull WebSocket webSocket, Throwable t, Response response) {
            Log.w(TAG, "ws failure", t);
            if (callback != null) main.post(() -> callback.onFailure(t, response));
        }
    };
}
