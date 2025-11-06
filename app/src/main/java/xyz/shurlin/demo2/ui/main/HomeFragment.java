package xyz.shurlin.demo2.ui.main;

import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import java.util.ArrayList;
import java.util.List;

import xyz.shurlin.demo2.R;
import xyz.shurlin.demo2.data.ToolMenuItem;
import xyz.shurlin.demo2.ui.list.TestActivity;
import xyz.shurlin.demo2.ui.list.WallActivity;

public class HomeFragment extends Fragment {

    private RecyclerView recycler;
    private MenuAdapter adapter;
    private List<ToolMenuItem> menuList = new ArrayList<>();

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);


        RecyclerView recycler = view.findViewById(R.id.recyclerTools);
        LinearLayoutManager layoutManager = new LinearLayoutManager(requireContext());
        recycler.setLayoutManager(layoutManager);

        prepareMenu();

        adapter = new MenuAdapter(requireContext(), menuList, item -> {
            Class<?> cls = item.getTarget();
            if (cls != null) {
                Intent it = new Intent(requireContext(), cls);
                it.putExtra("menu_id", item.getId());
                startActivity(it);
            }
        });
        recycler.setAdapter(adapter);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_home, container, false);
    }

    private void prepareMenu() {
        menuList.clear();
        // 各个栏目
        menuList.add(new ToolMenuItem("item1", "cdc", "进来帮cd来c", R.drawable.feedback, TestActivity.class));
        menuList.add(new ToolMenuItem("item2", "表白墙", "cd的表白墙", R.drawable.user, WallActivity.class));
    }
}