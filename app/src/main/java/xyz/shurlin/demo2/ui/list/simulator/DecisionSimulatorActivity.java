package xyz.shurlin.demo2.ui.list.simulator;


import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.method.ScrollingMovementMethod;
import android.util.Log;
import android.widget.ImageButton;
import android.widget.ScrollView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.Map;
import java.util.stream.Collectors;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import xyz.shurlin.demo2.R;
import xyz.shurlin.demo2.data.TreeItem;
import xyz.shurlin.demo2.data.network.decision_tree.DecisionNode;
import xyz.shurlin.demo2.data.network.decision_tree.DecisionTreeResponse;
import xyz.shurlin.demo2.data.network.decision_tree.DecisionTreesResponse;
import xyz.shurlin.demo2.data.network.decision_tree.NodeCreateRequest;
import xyz.shurlin.demo2.data.network.decision_tree.NodeCreateResponse;
import xyz.shurlin.demo2.data.network.decision_tree.TreeCreateRequest;
import xyz.shurlin.demo2.data.network.decision_tree.TreeCreateResponse;
import xyz.shurlin.demo2.network.ApiClient;
import xyz.shurlin.demo2.network.ApiService;

public class DecisionSimulatorActivity extends AppCompatActivity {
    private DecisionTreeView treeView;
    private InputOverlayView inputOverlayView;
    private DrawerLayout drawerLayout;
    private RecyclerView treeListRecycler;
    private ImageButton btnBack;
    private ImageButton btnTreeList;
    private TextView tvGenerating;

    private Long currentTreeId = null;
    private String username; // 从登录态获取
    private boolean generating;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_decision_simulator);

        SharedPreferences prefs = getSharedPreferences("profile", MODE_PRIVATE);
        username = prefs.getString("username", null);

        drawerLayout = findViewById(R.id.drawer_layout);
        treeView = findViewById(R.id.tree_view);
        inputOverlayView = findViewById(R.id.input_overlay);
        treeListRecycler = findViewById(R.id.recycler_tree_list);
        btnBack = findViewById(R.id.btnBack);
        btnTreeList = findViewById(R.id.btnTreeList);
        tvGenerating = findViewById(R.id.tvGenerating);

        // 初始化侧边栏
        treeListRecycler.setLayoutManager(new LinearLayoutManager(this));
        DecisionTreeListAdapter adapter = new DecisionTreeListAdapter(tree -> {
            drawerLayout.closeDrawer(GravityCompat.END);
            if (tree == null) {
                // 新建决策树
                treeView.showCreateRootHint();
                return;
            }
            loadTree(tree.treeId);
            Log.d("DecisionSimulator", "Selected tree: " + tree.treeId);
        });
        treeListRecycler.setAdapter(adapter);

//        SharedPreferences sharedPreferences = getSharedPreferences("profile", MODE_PRIVATE);
//        username = sharedPreferences.getString("username", null);

        ApiService api = ApiClient.getApiService();
        Call<DecisionTreesResponse> call = api.getAllTree(username);
        call.enqueue(new Callback<>() {
            @Override
            public void onResponse(retrofit2.Call<DecisionTreesResponse> call, retrofit2.Response<DecisionTreesResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    Map<Long, String> trees = response.body().getTrees();
                    Log.d("DecisionSimulator", "Loaded tree list: " + trees);
                    adapter.submitList(response.body().getTrees().entrySet().stream().map(e -> new TreeItem(e.getKey(), e.getValue())).collect(Collectors.toList()));
                } else {
                    Log.e("DecisionSimulator", "Failed to load tree list: " + response.code());
                }
            }

            @Override
            public void onFailure(retrofit2.Call<DecisionTreesResponse> call, Throwable t) {
                Log.e("DecisionSimulator", "Failed to load tree list", t);
            }
        });

        initTreeView();

        // 默认：新建决策树模式
        treeView.showCreateRootHint();

        btnBack.setOnClickListener(v -> finish());
        btnTreeList.setOnClickListener(v -> drawerLayout.openDrawer(GravityCompat.END));
    }

    private void initTreeView() {
        treeView.setOnAddRootClick(() -> {
            inputOverlayView.show("请形容本次人生决策的背景", text -> {
                tvGenerating.setVisibility(ScrollView.VISIBLE);
                treeView.setInputClose();
                ApiService api = ApiClient.getApiService();
                api.createTree(new TreeCreateRequest(username, text)).enqueue(new Callback<TreeCreateResponse>() {
                    @Override
                    public void onResponse(Call<TreeCreateResponse> call, Response<TreeCreateResponse> resp) {
                        if (resp.isSuccessful()) {
                            runOnUiThread(() -> {
//                                resp.body().getTreeId();
                                loadTree(resp.body().getTreeId());
                                inputOverlayView.setEnabled(true);
                                tvGenerating.setVisibility(ScrollView.GONE);

                            });
                        }
                    }

                    @Override
                    public void onFailure(Call<TreeCreateResponse> c, Throwable t) {
                        tvGenerating.setVisibility(ScrollView.GONE);
                    }
                });
            });
            inputOverlayView.setEnabled(false);
        });
        treeView.setOnAddNodeClick(parentNodeId -> {
            inputOverlayView.show("请输入你的决策 / 遭遇 / 选择", text -> {
                tvGenerating.setVisibility(ScrollView.VISIBLE);
                treeView.setInputClose();
                ApiService api = ApiClient.getApiService();
                api.createNode(new NodeCreateRequest(currentTreeId, parentNodeId, text)).enqueue(new Callback<>() {
                    @Override
                    public void onResponse(Call<NodeCreateResponse> call, Response<NodeCreateResponse> resp) {
                        if (resp.isSuccessful()) {
                            NodeCreateResponse body = resp.body();
                            runOnUiThread(() -> {
                                treeView.addNode(new DecisionNode(body.getNodeId(), currentTreeId, parentNodeId, text, body.getSimulation(), body.getDepth()));
                                inputOverlayView.setEnabled(true);
                                tvGenerating.setVisibility(ScrollView.GONE);
                            });
                        }
                    }

                    @Override
                    public void onFailure(Call<NodeCreateResponse> c, Throwable t) {
                        tvGenerating.setVisibility(ScrollView.GONE);
                    }
                });
            });
            inputOverlayView.setEnabled(false);
        });
        treeView.setCloseInput(() -> inputOverlayView.setVisibility(InputOverlayView.GONE));
//        treeView.setOnNodeClickListener(new DecisionTreeView.OnNodeClickListener() {
//            @Override
//            public void onNodeClick(DecisionNode node) {
//                if (node != null && node.getSimulation() != null && !node.getSimulation().isEmpty()) {
//                    Log.d("DecisionSimulator", "Showing simulation for node: " + node.getNodeId());
//                    showSimulationDialog(node);
//                }
//            }
//
//            @Override
//            public void onNodeLongClick(DecisionNode node) {
//                // 可选：长按功能
//                Log.d("DecisionSimulator", "Long clicked node: " + node.getNodeId());
//            }
//        });

    }

    private void loadTree(Long treeId) {
        Log.d("DecisionSimulator", "Loading tree: " + treeId);
        ApiService api = ApiClient.getApiService();
        Call<DecisionTreeResponse> call = api.getDecisionTree(treeId);
        call.enqueue(new retrofit2.Callback<>() {
            @Override
            public void onResponse(retrofit2.Call<DecisionTreeResponse> call, retrofit2.Response<DecisionTreeResponse> response) {
                Log.d("DecisionSimulator", "Loaded tree: " + response.body());
                if (response.isSuccessful() && response.body() != null) {
                    DecisionTreeResponse tree = response.body();
//                    Log.d("DecisionSimulator", "Loaded tree desc: " + tree.getBackground());
//                    Log.d("DecisionSimulator", "Loaded tree node desc: " + tree.getNodes().get(0).getSimulation());
                    currentTreeId = tree.getTreeId();
                    runOnUiThread(() -> treeView.loadTree(tree));
                } else {
                    Log.e("DecisionSimulator", "Failed to load tree: " + response.code());
                }
            }

            @Override
            public void onFailure(retrofit2.Call<DecisionTreeResponse> call, Throwable t) {
                Log.e("DecisionSimulator", "Failed to load tree", t);
            }
        });
    }
}
