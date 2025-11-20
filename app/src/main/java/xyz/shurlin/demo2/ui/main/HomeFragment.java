package xyz.shurlin.demo2.ui.main;

import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
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
import xyz.shurlin.demo2.ui.list.DigitalLogic.RCA_Activity;
import xyz.shurlin.demo2.ui.list.EatShitActivity;
import xyz.shurlin.demo2.ui.list.GuideActivity;
import xyz.shurlin.demo2.ui.list.ImageShowActivity;
import xyz.shurlin.demo2.ui.list.LouderActivity;
import xyz.shurlin.demo2.ui.list.SpeedTestActivity;
import xyz.shurlin.demo2.ui.list.TestActivity;
import xyz.shurlin.demo2.ui.list.wall.WallActivity;

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

        SwipeRefreshLayout swipeRefreshLayout = requireActivity().findViewById(R.id.swipeHome);
        swipeRefreshLayout.setOnChildScrollUpCallback((parent, child) ->
                recycler.canScrollVertically(1) || recycler.canScrollVertically(-1));


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
        menuList.add(new ToolMenuItem("item0", "使用指南", "初次使用请阅读指南", R.drawable.book1, GuideActivity.class));
        menuList.add(new ToolMenuItem("item1", "cdc", "进来帮cd来c", R.drawable.feedback, TestActivity.class));
        menuList.add(new ToolMenuItem("item2", "表白墙", "cd的表白墙", R.drawable.unlike, WallActivity.class));
//        menuList.add(new ToolMenuItem("item3", "小吃街", "小吃街导航与评论", R.drawable.lollipop, SnackStreetActivity.class));
        menuList.add(new ToolMenuItem("item4", "看看谁更吵", "分贝测试器——klf不要再吵了", R.drawable.speak, LouderActivity.class));
//        menuList.add(new ToolMenuItem("item5", "暗黑阅读器", "手指不放在屏幕上屏幕就会变黑", R.drawable.book1, DarkReaderActivity.class));
        menuList.add(new ToolMenuItem("item6", "DL", "Digital Logics", R.drawable.book1, RCA_Activity.class));
        menuList.add(new ToolMenuItem("item7", "吃什么食", "434每日不知道吃什么", R.drawable.hotpot, EatShitActivity.class));
        menuList.add(new ToolMenuItem("item8", "校卡展示", "存储校卡图片方便进出校门", R.drawable.file_cb, ImageShowActivity.class));
        menuList.add(new ToolMenuItem("item9", "手速测试器", "看谁更快", R.drawable.file_cb, SpeedTestActivity.class));

    }

}