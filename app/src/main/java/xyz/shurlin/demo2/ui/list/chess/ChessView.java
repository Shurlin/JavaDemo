package xyz.shurlin.demo2.ui.list.chess;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.res.ResourcesCompat;

import xyz.shurlin.demo2.R;
import xyz.shurlin.demo2.data.network.chess.MovePayload;

/**
 * 自定义 View：绘制中国象棋棋盘，并处理触摸选子/走子
 * <p>
 * 逻辑：
 * - board: pieces[9][10]，索引 (x:0..8, y:0..9)
 * - touch -> 计算格坐标 -> 选子/走子
 */
public class ChessView extends View {
    private final Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private Piece[][] pieces;
    private boolean isRedTurn = true; // 当前轮到红方

    private int selectedX = -1, selectedY = -1;// 选中格子
    private float boardLeft, boardTop, cellSize; // 棋盘参数
    private float radius; // 棋子半径

    public ChessView(Context context) {
        this(context, null);
    }

    public ChessView(Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public ChessView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    private void init() {
        pieces = Piece.getOriginChess(); // 使用 model 提供的起始布局
        paint.setTextAlign(Paint.Align.CENTER);
        paint.setTextSize(36f);
    }

    // 在 size 更改时计算棋盘参数
    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        float padding = Math.min(w, h) * 0.07f;
        float usableW = w - padding * 2;
        float usableH = h - padding * 2;
        // 9 列，10 行 -> 8 单元间隔横向、9 纵向间隔；我们以 cellSize 为基准
        cellSize = Math.min(usableW / 8f, usableH / 9f);
        // 为了格子居中，重新计算左上
        float boardWidth = cellSize * 8f;
        float boardHeight = cellSize * 9f;
        boardLeft = (w - boardWidth) / 2f;
        boardTop = (h - boardHeight) / 2f;
        radius = cellSize * 0.4f;
        paint.setTextSize(cellSize * 0.6f);
    }

    @Override
    protected void onDraw(@NonNull Canvas canvas) {
        super.onDraw(canvas);
        drawBoard(canvas);
        drawPieces(canvas);
        drawSelected(canvas);
    }

    private void drawBoard(Canvas c) {
        paint.setColor(Color.BLACK);
        paint.setStrokeWidth(Math.max(2f, cellSize * 0.04f));

        for (int j = 0; j < 10; j++) { // 横线
            float y = boardTop + j * cellSize;
            c.drawLine(boardLeft, y, boardLeft + 8 * cellSize, y, paint);
        }
        for (int i = 0; i < 9; i++) { // 竖线
            float x = boardLeft + i * cellSize;
            if (i == 0 || i == 8) {
                c.drawLine(x, boardTop, x, boardTop + 9 * cellSize, paint);
            } else {
                c.drawLine(x, boardTop, x, boardTop + 4 * cellSize, paint);
                c.drawLine(x, boardTop + 5 * cellSize, x, boardTop + 9 * cellSize, paint);
            }

        }

        //画米字线
        c.drawLine(boardLeft + 3 * cellSize, boardTop, boardLeft + 5 * cellSize, boardTop + 2 * cellSize, paint);
        c.drawLine(boardLeft + 5 * cellSize, boardTop, boardLeft + 3 * cellSize, boardTop + 2 * cellSize, paint);
        c.drawLine(boardLeft + 3 * cellSize, boardTop + 7 * cellSize, boardLeft + 5 * cellSize, boardTop + 9 * cellSize, paint);
        c.drawLine(boardLeft + 5 * cellSize, boardTop + 7 * cellSize, boardLeft + 3 * cellSize, boardTop + 9 * cellSize, paint);

        // 楚河汉界文字（简单）
        paint.setTextSize(cellSize * 0.6f);
        paint.setTextAlign(Paint.Align.CENTER);
        //设为楷体
        paint.setTypeface(ResourcesCompat.getFont(getContext(), R.font.kaiti));

        c.drawText("楚河", boardLeft + 2.5f * cellSize, boardTop + 4.5f * cellSize + cellSize * 0.3f, paint);
        c.drawText("汉界", boardLeft + 5.5f * cellSize, boardTop + 4.5f * cellSize + cellSize * 0.3f, paint);
    }

    private void drawPieces(Canvas c) {
        // draw each piece if not null
        for (int x = 0; x < 9; x++) {
            for (int y = 0; y < 10; y++) {
                Piece p = pieces[x][y];
                if (p == null) continue;
                float cx = boardLeft + x * cellSize;
                float cy = boardTop + y * cellSize;
                // outer circle
                paint.setStyle(Paint.Style.STROKE);
                paint.setStrokeWidth(cellSize * (p.isRed ? 0.06f : 0.12f));
                paint.setColor(Color.BLACK);
                c.drawCircle(cx, cy, radius - (p.isRed ? 0 : cellSize * 0.06f), paint);

                // inner fill (red/black)
                paint.setStyle(Paint.Style.FILL);
                paint.setColor(p.isRed ? Color.parseColor("#FF0000") : Color.parseColor("#D2B48C"));
                c.drawCircle(cx, cy, radius - cellSize * (p.isRed ? 0.02f : 0.08f), paint);

                // text
                paint.setColor(Color.BLACK);
                paint.setTextSize(cellSize * 0.6f);
                Rect r = new Rect();
                String txt = p.getDisplayName();
                paint.getTextBounds(txt, 0, txt.length(), r);
                float textY = cy - (r.top + r.bottom) / 2f;
                c.drawText(txt, cx - 2, textY, paint);
            }
        }
    }

    private void drawSelected(Canvas c) {
        if (selectedX >= 0 && selectedY >= 0) {
            float pickedX1 = boardLeft + selectedX * cellSize;
            float pickedY1 = boardTop + selectedY * cellSize;
            float l = cellSize * 0.16f;
            paint.setColor(Color.YELLOW);
            c.drawLine(pickedX1 - radius, pickedY1 - radius, pickedX1 - radius + l, pickedY1 - radius, paint);
            c.drawLine(pickedX1 - radius, pickedY1 - radius, pickedX1 - radius, pickedY1 - radius + l, paint);
            c.drawLine(pickedX1 + radius, pickedY1 - radius, pickedX1 + radius - l, pickedY1 - radius, paint);
            c.drawLine(pickedX1 + radius, pickedY1 - radius, pickedX1 + radius, pickedY1 - radius + l, paint);
            c.drawLine(pickedX1 - radius, pickedY1 + radius, pickedX1 - radius + l, pickedY1 + radius, paint);
            c.drawLine(pickedX1 - radius, pickedY1 + radius, pickedX1 - radius, pickedY1 + radius - l, paint);
            c.drawLine(pickedX1 + radius, pickedY1 + radius, pickedX1 + radius - l, pickedY1 + radius, paint);
            c.drawLine(pickedX1 + radius, pickedY1 + radius, pickedX1 + radius, pickedY1 + radius - l, paint);
        }
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (event.getAction() != MotionEvent.ACTION_DOWN) return true;
        float tx = event.getX();
        float ty = event.getY();
        int gx = Math.round((tx - boardLeft) / cellSize);
        int gy = Math.round((ty - boardTop) / cellSize);

        if (!inBoard(gx, gy)) // 点击棋盘外
            return true;

        // 选子或走子逻辑
        if (selectedX == -1) { // 未选中，尝试选子
            if (pieces[gx][gy] != null && pieces[gx][gy].isRed == isRedTurn) {
                selectedX = gx;
                selectedY = gy;
                invalidate();
            }
        } else {
            // 已选中，尝试移动
            if (tryMove(selectedX, selectedY, gx, gy)) {
                selectedX = -1;
                selectedY = -1;
//                invalidate();

            } else {
                // 如果点击的是己方另一子，则切换
                Piece curSelected = pieces[selectedX][selectedY];
                Piece target = pieces[gx][gy];
                if (target != null && curSelected != null && target.isRed == curSelected.isRed && target.isRed == isRedTurn) {
                    selectedX = gx;
                    selectedY = gy;
                    invalidate();
                    return true;
                }
            }

        }
        return true;
    }

    private boolean tryMove(int ox, int oy, int tx, int ty) {
        if (!inBoard(ox, oy) || !inBoard(tx, ty) || ox == tx && oy == ty) return false;
        Piece src = pieces[ox][oy];
        if (src == null) return false;
        Piece dst = pieces[tx][ty];
        if (dst != null && dst.isRed == src.isRed) return false;
        if (!src.canMove(ox, oy, tx, ty, pieces)) {
            return false;
        }

//        pieces[tx][ty] = src;
//        pieces[ox][oy] = null;

        this.localMoveListener.onMove(ox, oy, tx, ty);

        return true;
    }

    private boolean inBoard(int x, int y) {
        return x >= 0 && x < 9 && y >= 0 && y < 10;
    }

    //activity使用chessview操作
    public interface OnLocalMoveListener {
        void onMove(int fromX, int fromY, int toX, int toY);
    }

    private OnLocalMoveListener localMoveListener;

    public void setOnLocalMoveListener(@NonNull OnLocalMoveListener l) {
        this.localMoveListener = l;
        Log.d("ChessView", "LocalMoveListener set");
    }

    // 同步服务器棋盘
    public void setBoardFromServer(String[][] boardStrings) {
        Piece[][] newBoard = new Piece[9][10];
        for (int i = 0; i < 9 && i < boardStrings.length; i++) {
            for (int j = 0; j < 10 && j < boardStrings[i].length; j++) {
                if (boardStrings[i][j] == null || boardStrings[i][j].isEmpty()) {
                    newBoard[i][j] = null;
                    continue;
                }
                String[] parts = boardStrings[i][j].split("_");
                newBoard[i][j] = new Piece(Piece.PieceType.valueOf(parts[0]), parts[1].equals("R"));
            }
        }
        this.pieces = newBoard;
        this.selectedX = -1;
        this.selectedY = -1;
        postInvalidate();
    }

    public void applyServerMove(MovePayload move) {
        if (move == null) return;
        int ox = move.from.x, oy = move.from.y, tx = move.to.x, ty = move.to.y;
        pieces[tx][ty] = pieces[ox][oy];
        pieces[ox][oy] = null;
        isRedTurn = move.next.equals("red");
        postInvalidate();
    }
}
