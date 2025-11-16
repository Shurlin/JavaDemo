package xyz.shurlin.demo2.ui.list;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

import xyz.shurlin.demo2.R;
import xyz.shurlin.demo2.data.CanteenItem;

public class CanteenAdapter extends RecyclerView.Adapter<CanteenAdapter.ViewHolder> {
    private final List<CanteenItem> items;

    public CanteenAdapter(List<CanteenItem> items) {
        this.items = items;
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        CheckBox checkBox;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            checkBox = itemView.findViewById(R.id.canteen_item);
        }
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_checkbox, parent, false);
        return new ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        CanteenItem item = items.get(position);
        holder.checkBox.setText(item.text);
        holder.checkBox.setChecked(item.checked);

        holder.checkBox.setOnCheckedChangeListener((buttonView, isChecked) -> {
            item.checked = isChecked;
        });
    }

    @Override
    public int getItemCount() {
        return items.size();
    }
}
