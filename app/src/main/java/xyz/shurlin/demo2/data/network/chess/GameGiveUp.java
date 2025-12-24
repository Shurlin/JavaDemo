package xyz.shurlin.demo2.data.network.chess;

public class GameGiveUp {
    public String type;
    public Long gameId;
    public String loser;

    public GameGiveUp(Long gameId, String loser) {
        this.type = "GAME_GIVE_UP";
        this.gameId = gameId;
        this.loser = loser;
    }
}
