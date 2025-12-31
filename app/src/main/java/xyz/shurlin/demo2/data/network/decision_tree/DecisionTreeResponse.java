package xyz.shurlin.demo2.data.network.decision_tree;

import java.util.ArrayList;
import java.util.List;

public class DecisionTreeResponse {
    private Long treeId;
    private String title;
    private String background;
    private List<DecisionNode> nodes;

    public DecisionTreeResponse() {
    }

    public void setTreeId(Long treeId) {
        this.treeId = treeId;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public void setBackground(String background) {
        this.background = background;
    }

    public void setNodes(List<DecisionNode> nodes) {
        this.nodes = nodes;
    }

    public Long getTreeId() {
        return treeId;
    }

    public String getTitle() {
        return title;
    }

    public String getBackground() {
        return background;
    }

    public List<DecisionNode> getNodes() {
        return nodes;
    }


    public void addNode(DecisionNode node) {
        if (nodes == null) nodes = new ArrayList<>();
        nodes.add(node);
    }

}
