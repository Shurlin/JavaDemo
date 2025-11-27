package xyz.shurlin.demo2.ui.list;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

import xyz.shurlin.demo2.R;
import xyz.shurlin.demo2.data.network.SpeedRank;

public class RankAdapter extends RecyclerView.Adapter<RankAdapter.VH> {

        private List<SpeedRank> datas = new ArrayList<>();

        public void setData(List<SpeedRank> list) {
            datas.clear();
            if (list != null) datas.addAll(list);
            notifyDataSetChanged();
        }

        public void addData(List<SpeedRank> list) {
            int start = datas.size();
            datas.addAll(list);
            notifyItemRangeInserted(start, list.size());
        }

        @Override
        public VH onCreateViewHolder(ViewGroup parent, int viewType) {
            View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_rank, parent, false);
            return new RankAdapter.VH(v);
        }

        @Override
        public void onBindViewHolder(RankAdapter.VH holder, int position) {
            if(position==0){
                holder.rank_rank.setText("排名");
                holder.rank_username.setText("用户名");
                holder.rank_score.setText("分数");
                return;
            }
            SpeedRank data = datas.get(position);
            Log.v("dd", data.getUsername());
            holder.rank_rank.setText(String.valueOf(position));
            holder.rank_username.setText(data.getUsername());
            holder.rank_score.setText(String.valueOf(data.getScore()));
        }

        @Override
        public int getItemCount() {
            return datas.size();
        }

        static class VH extends RecyclerView.ViewHolder {
            TextView rank_rank, rank_username, rank_score;
            VH(View v) {
                super(v);
                rank_rank = v.findViewById(R.id.rank_rank);
                rank_username = v.findViewById(R.id.rank_username);
                rank_score = v.findViewById(R.id.rank_score);
            }
        }
    }