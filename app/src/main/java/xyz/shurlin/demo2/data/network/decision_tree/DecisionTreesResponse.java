package xyz.shurlin.demo2.data.network.decision_tree;

import java.util.Map;

public class DecisionTreesResponse {
    private Map<Long, String> trees;

    public DecisionTreesResponse(Map<Long, String> trees) {
        this.trees = trees;
    }

    public Map<Long, String> getTrees() {
        return trees;
    }

}
