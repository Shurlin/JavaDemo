package xyz.shurlin.demo2.data.network.chess;

public class GameRestart {
    public String type;
    public Long gameId;

    public GameRestart(Long gameId) {
        this.type = "GAME_RESTART";
        this.gameId = gameId;
    }
}
