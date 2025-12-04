package xyz.shurlin.demo2.ui.list.chess;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;

import androidx.appcompat.app.AppCompatActivity;

import xyz.shurlin.demo2.R;

public class ChessRoomActivity extends AppCompatActivity {
    private EditText etChessGameId;
    private Button btnCreateChessRoom;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chess_room);

        etChessGameId = findViewById(R.id.etChessGameId);
        btnCreateChessRoom = findViewById(R.id.btnCreateChessRoom);

        btnCreateChessRoom.setOnClickListener(v -> {
            String gameIdString = etChessGameId.getText().toString().trim();
            if (!gameIdString.isEmpty()) {
                Long gameId = Long.parseLong(gameIdString);
                Intent intent = new Intent(ChessRoomActivity.this, OnlineChessActivity.class);
                intent.putExtra("id", gameId);
                startActivity(intent);

            } else {
                etChessGameId.setError("Game ID cannot be empty");
            }
        });

    }
}