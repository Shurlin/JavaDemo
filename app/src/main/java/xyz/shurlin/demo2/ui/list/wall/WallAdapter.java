package xyz.shurlin.demo2.ui.list.wall;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.recyclerview.widget.RecyclerView;
import xyz.shurlin.demo2.R;
import xyz.shurlin.demo2.data.network.WallFetchResponse;

import java.time.format.DateTimeFormatterBuilder;
import java.util.ArrayList;
import java.util.List;

public class WallAdapter extends RecyclerView.Adapter<WallAdapter.VH> {

    private List<WallFetchResponse> datas = new ArrayList<>();
    private TextView bottomTextView;
    private boolean showBottomTextView = false; //TODO

    public void setData(List<WallFetchResponse> list) {
        datas.clear();
        if (list != null) datas.addAll(list);
        notifyDataSetChanged();
    }

    public void addData(List<WallFetchResponse> list) {
        int start = datas.size();
        datas.addAll(list);
        notifyItemRangeInserted(start, list.size());
    }

    @Override
    public VH onCreateViewHolder(ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_wall_data, parent, false);
        return new VH(v);
    }

    @Override
    public void onBindViewHolder(VH holder, int position) {
        WallFetchResponse data = datas.get(position);
        holder.tvWallTitle.setText(data.getTitle());
        holder.tvWallTime.setText(data.getCreatedAt().replace('T', ' '));
        holder.tvWallContent.setText(data.getContent());
    }

    @Override
    public int getItemCount() {
        return datas.size();
    }

    static class VH extends RecyclerView.ViewHolder {
        TextView tvWallTitle, tvWallTime, tvWallContent;
        VH(View v) {
            super(v);
            tvWallTitle = v.findViewById(R.id.tvWallTitle);
            tvWallTime = v.findViewById(R.id.tvWallTime);
            tvWallContent = v.findViewById(R.id.tvWallContent);
        }
    }
}
