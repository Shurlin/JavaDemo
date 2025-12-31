package xyz.shurlin.demo2.ui.list.simulator;

import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

import xyz.shurlin.demo2.data.TreeItem;

public class DecisionTreeListAdapter
        extends RecyclerView.Adapter<DecisionTreeListAdapter.VH> {

    private final List<TreeItem> data = new ArrayList<>();
    private final Consumer<TreeItem> onClick;

    public DecisionTreeListAdapter(Consumer<TreeItem> onClick) {
        this.onClick = onClick;
    }

    public void submitList(List<TreeItem> list) {
        data.clear();
        data.addAll(list);
        notifyDataSetChanged();
    }

    @Override
    public VH onCreateViewHolder(ViewGroup p, int v) {
        TextView tv = new TextView(p.getContext());
        tv.setPadding(40, 40, 40, 40);
        tv.setTextSize(16);
        return new VH(tv);
    }

    @Override
    public void onBindViewHolder(VH h, int i) {
        if (i == 0) {
            ((TextView) h.itemView).setText("历史决策树");
            ((TextView) h.itemView).setTextSize(18);
//            h.itemView.setOnClickListener(null);
            return;
        }
        if (i == data.size() + 1) {
            ((TextView) h.itemView).setText("新建决策树");
            ((TextView) h.itemView).setTextSize(14);
            h.itemView.setOnClickListener(v -> onClick.accept(null));
            return;
        }
        TreeItem item = data.get(i - 1);
        ((TextView) h.itemView).setText(item.title);
        ((TextView) h.itemView).setTextSize(14);
        h.itemView.setOnClickListener(v -> onClick.accept(item));
    }

    @Override
    public int getItemCount() { return data.size() + 2; }

    static class VH extends RecyclerView.ViewHolder {
        VH(View v) { super(v); }
    }
}
