package xyz.shurlin.demo2.ui.list.chess;

import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import java.text.SimpleDateFormat;
import java.time.format.DateTimeFormatterBuilder;
import java.util.ArrayList;
import java.util.List;

import xyz.shurlin.demo2.R;
import xyz.shurlin.demo2.data.ChessHistoryItem;

public class ChessHistoryAdapter extends RecyclerView.Adapter<ChessHistoryAdapter.ViewHolder> {
    private List<ChessHistoryItem> dataList = new ArrayList<>();

    public void setData(List<ChessHistoryItem> list) {
        dataList = list;
        notifyDataSetChanged();
    }

    @Override
    public ViewHolder onCreateViewHolder(android.view.ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_chess_history, parent, false);
        return new ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        ChessHistoryItem data = dataList.get(position);
        holder.user1.setText(data.user1);
        holder.user2.setText(data.user2);
        String stateText;
        int stateColor;
        switch (data.state){
            case "WIN" :
                stateText = "胜利";
                stateColor = Color.RED;
                break;
            case "LOSE" :
                stateText = "败北";
                stateColor = Color.BLUE;
                break;
            case "DISCONNECT" :
                stateText = "断线";
                stateColor = Color.GRAY;
                break;
            default:
                stateText = "未知";
                stateColor = Color.WHITE;
        }
        holder.state.setText(stateText);
        holder.state.setTextColor(stateColor);
        holder.time.setText(data.time.replace("T", " "));
    }

    @Override
    public int getItemCount() {
        return dataList.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView user1, user2, state, time;

        public ViewHolder(View itemView) {
            super(itemView);
            user1 = itemView.findViewById(R.id.itemChessHistoryUser1);
            user2 = itemView.findViewById(R.id.itemChessHistoryUser2);
            state = itemView.findViewById(R.id.itemChessHistoryState);
            time = itemView.findViewById(R.id.itemChessHistoryTime);
        }
    }

}
