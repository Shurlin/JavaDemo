package xyz.shurlin.demo2.ui.main;

import android.content.Context;
import android.content.SharedPreferences;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.OptionalInt;
import java.util.stream.IntStream;

import xyz.shurlin.demo2.data.ToolMenuItem;
import xyz.shurlin.demo2.R;

public class MenuAdapter extends RecyclerView.Adapter<MenuAdapter.VH> {

    public interface OnItemClickListener {
        void onItemClick(ToolMenuItem item);
    }

    private final List<ToolMenuItem> originItems;
    private final List<ToolMenuItem> currentItems = new ArrayList<>();
    private final OnItemClickListener listener;
    private final Context context;
    private final List<String> menu_star;

    private final SharedPreferences preferences;

    private final int no_star = R.drawable.star;
    private final int stared = R.drawable.stared;

    public MenuAdapter(Context context, List<ToolMenuItem> items, OnItemClickListener listener) {
        this.context = context;
        this.originItems = items;
        this.listener = listener;
        preferences = context.getSharedPreferences("menu_star", Context.MODE_PRIVATE);
        String pref_info = preferences.getString("pref", null);
        if (pref_info != null) {
            menu_star = new ArrayList<>(List.of(pref_info.split(",")));
        } else {
            menu_star = new ArrayList<>();
        }
        this.updateItemsWithStars(true);
    }

    private void updateItemsWithStars(boolean first) {
        currentItems.clear();
        if (!menu_star.isEmpty()) {
            for (String id : menu_star) {
                Optional<ToolMenuItem> found = originItems.stream()
                        .filter(item -> item.getId().equals(id)).findFirst();
                found.ifPresent(currentItems::add);
            }
        }
        for (ToolMenuItem item : this.originItems) {
            if (!currentItems.contains(item))
                currentItems.add(item);
        }
        if (!first) {
            preferences.edit().putString("pref", menu_star.isEmpty() ? null : String.join(",", menu_star)).apply(); // 空的会不会有问题
        }
    }

    @NonNull
    @Override
    public VH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item, parent, false);
        return new VH(v);
    }

    @Override
    public void onBindViewHolder(@NonNull VH holder, int position) {
        final ToolMenuItem item = currentItems.get(position);
        holder.title.setText(item.getTitle());
        holder.desc.setText(item.getDesc());
        holder.icon.setImageResource(item.getIconRes());
        holder.itemView.setOnClickListener(v -> {
            if (listener != null) listener.onItemClick(item);
        });
        holder.star.setImageResource(menu_star.contains(item.getId()) ? stared : no_star);
        holder.star.setOnClickListener(v -> {
            int nowPos = currentItems.indexOf(item);
            if (menu_star.contains(item.getId())) {
                menu_star.remove(item.getId());
                holder.star.setImageResource(no_star);
            } else {
                menu_star.add(item.getId());
                holder.star.setImageResource(stared);
            }
            updateItemsWithStars(false);
            int newPos = currentItems.indexOf(item);
            notifyItemMoved(nowPos, newPos);
        });
    }

    @Override
    public int getItemCount() {
        return originItems.size();
    }

    static class VH extends RecyclerView.ViewHolder {
        ImageView icon;
        TextView title;
        TextView desc;
        ImageView star;

        VH(@NonNull View itemView) {
            super(itemView);
            icon = itemView.findViewById(R.id.icon);
            title = itemView.findViewById(R.id.title);
            desc = itemView.findViewById(R.id.desc);
            star = itemView.findViewById(R.id.star);
        }
    }
}
