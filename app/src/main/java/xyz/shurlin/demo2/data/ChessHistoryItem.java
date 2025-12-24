package xyz.shurlin.demo2.data;

import java.time.LocalDateTime;

public class ChessHistoryItem {
    public String user1;
    public String user2;
    public String state;
    public String time;

    public ChessHistoryItem(String user1, String user2, String state, String time) {
        this.user1 = user1;
        this.user2 = user2;
        this.time = time;
        this.state = state;
    }
}
