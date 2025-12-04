package xyz.shurlin.demo2.ui.list.chess;

public class Piece {
    public final PieceType type;
    public final boolean isRed;

    public Piece(PieceType type, boolean isRed) {
        this.type = type;
        this.isRed = isRed;
    }

    public String getDisplayName() {
        switch (type) {
            case JIANG:
                return isRed ? "帅" : "将";
            case SHI:
                return isRed ? "仕" : "士";
            case XIANG:
                return isRed ? "相" : "象";
            case CHE:
                return isRed ? "俥" : "车";
            case MA:
                return isRed ? "傌" : "马";
            case PAO:
                return isRed ? "炮" : "砲";
            case BING:
                return isRed ? "兵" : "卒";
            default:
                return "?";
        }
    }

    public static Piece[][] getOriginChess() {
        Piece[][] board = new Piece[9][10];
        boolean black = false;
        board[0][0] = new Piece(PieceType.CHE, black);
        board[8][0] = new Piece(PieceType.CHE, black);
        board[1][0] = new Piece(PieceType.MA, black);
        board[7][0] = new Piece(PieceType.MA, black);
        board[2][0] = new Piece(PieceType.XIANG, black);
        board[6][0] = new Piece(PieceType.XIANG, black);
        board[3][0] = new Piece(PieceType.SHI, black);
        board[5][0] = new Piece(PieceType.SHI, black);
        board[4][0] = new Piece(PieceType.JIANG, black);
        board[1][2] = new Piece(PieceType.PAO, black);
        board[7][2] = new Piece(PieceType.PAO, black);
        board[0][3] = new Piece(PieceType.BING, black);
        board[2][3] = new Piece(PieceType.BING, black);
        board[4][3] = new Piece(PieceType.BING, black);
        board[6][3] = new Piece(PieceType.BING, black);
        board[8][3] = new Piece(PieceType.BING, black);

        boolean red = true;
        board[0][9] = new Piece(PieceType.CHE, red);
        board[8][9] = new Piece(PieceType.CHE, red);
        board[1][9] = new Piece(PieceType.MA, red);
        board[7][9] = new Piece(PieceType.MA, red);
        board[2][9] = new Piece(PieceType.XIANG, red);
        board[6][9] = new Piece(PieceType.XIANG, red);
        board[3][9] = new Piece(PieceType.SHI, red);
        board[5][9] = new Piece(PieceType.SHI, red);
        board[4][9] = new Piece(PieceType.JIANG, red);
        board[1][7] = new Piece(PieceType.PAO, red);
        board[7][7] = new Piece(PieceType.PAO, red);
        board[0][6] = new Piece(PieceType.BING, red);
        board[2][6] = new Piece(PieceType.BING, red);
        board[4][6] = new Piece(PieceType.BING, red);
        board[6][6] = new Piece(PieceType.BING, red);
        board[8][6] = new Piece(PieceType.BING, red);

        return board;
    }

    public boolean canMove(int ox, int oy, int tx, int ty, Piece[][] chess) {
        if (ox == tx && oy == ty) return false;
        if (tx < 0 || tx >= 9 || ty < 0 || ty >= 10) return false;
        int dx = Math.abs(tx - ox);
        int dy = Math.abs(ty - oy);

        switch (type) {
            case CHE:
                if (ox != tx && oy != ty) return false;
                if (ox == tx) {
                    int cnt = 0;
                    for (int y = Math.min(oy, ty) + 1; y < Math.max(oy, ty); y++)
                        if (chess[ox][y] != null) cnt++;
                    return cnt == 0;
                } else {
                    int cnt = 0;
                    for (int x = Math.min(ox, tx) + 1; x < Math.max(ox, tx); x++)
                        if (chess[x][oy] != null) cnt++;
                    return cnt == 0;
                }
            case MA:
                return dx == 2 && dy == 1 && chess[(ox + tx) / 2][oy] == null || dy == 2 && dx == 1 && chess[ox][(oy + ty) / 2] == null;
            case PAO:
                if (ox != tx && oy != ty) return false;
                int cnt = 0;
                if (ox == tx)
                    for (int i = Math.min(oy, ty) + 1; i < Math.max(oy, ty); i++)
                        if (chess[ox][i] != null) cnt++;
                if (oy == ty)
                    for (int j = Math.min(ox, tx) + 1; j < Math.max(ox, tx); j++)
                        if (chess[j][oy] != null) cnt++;
                if (chess[tx][ty] == null) return cnt == 0;
                return cnt == 1;
            case XIANG:
                if (!(dx == 2 && dy == 2)) return false;
                if (isRed) {
                    if (ty < 5) return false;
                } else {
                    if (ty > 4) return false;
                }
                int midX = (ox + tx) / 2;
                int midY = (oy + ty) / 2;
                return chess[midX][midY] == null;
            case SHI:
                if (!(dx == 1 && dy == 1)) return false;
                if (tx < 3 || tx > 5) return false;
                if (isRed) return ty >= 7;
                else return ty <= 2;
            case JIANG:
                if (!((dx == 1 && dy == 0) || (dx == 0 && dy == 1))) return false;
                if (tx < 3 || tx > 5) return false;
                if (isRed) return ty >= 7;
                else return ty <= 2;
            case BING:
                if (isRed) {
                    if (oy > 4) return ox == tx && ty == oy - 1;
                    else return tx == ox && ty == oy - 1
                            || ty == oy && Math.abs(tx - ox) == 1;

                } else {
                    if (oy < 5) return ox == tx && ty == oy + 1;
                    else return tx == ox && ty == oy + 1
                            || ty == oy && Math.abs(tx - ox) == 1;
                }
            default:
                return false;
        }
    }

    enum PieceType {
        CHE, MA, PAO, XIANG, SHI, JIANG, BING
    }
}
