package xyz.shurlin.demo2.data.network.chess;


public class ChessStateDto {
    public String type = "CHESS_STATE";
    public Long gameId;
    public String[][] board; // [9][10]
    public int lastMoveIndex; // -1 表示无步
    public String turn; // "red" or "black"

}
