package xyz.shurlin.demo2.data.network;

public class SpeedRankPostRequest {
    private String username;
    private int score;

    public SpeedRankPostRequest(String username, int score) {
        this.username = username;
        this.score = score;
    }

    public String getUsername() {
        return username;
    }

    public int getScore() {
        return score;
    }
}
