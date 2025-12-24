package xyz.shurlin.demo2.data.network.chess;

public class MoveRequest {
    public String type;
    public Long gameId;
    public String username;
    public Position from;
    public Position to;

    public MoveRequest(Long gameId, String username, int fromX, int fromY, int toX, int toY) {
        this.type = "MOVE";
        this.gameId = gameId;
        this.username = username;
        this.from = new Position(fromX, fromY);
        this.to = new Position(toX, toY);
    }
}

