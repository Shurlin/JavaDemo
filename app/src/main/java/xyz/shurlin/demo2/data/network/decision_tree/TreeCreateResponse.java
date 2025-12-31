package xyz.shurlin.demo2.data.network.decision_tree;

public class TreeCreateResponse {
    private Long treeId;
    private String title;

    public TreeCreateResponse(Long treeId, String title) {
        this.treeId = treeId;
        this.title = title;
    }

    public Long getTreeId() {
        return treeId;
    }

    public String getTitle() {
        return title;
    }
}
