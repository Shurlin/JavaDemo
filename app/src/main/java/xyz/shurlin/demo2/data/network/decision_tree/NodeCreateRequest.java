package xyz.shurlin.demo2.data.network.decision_tree;

public class NodeCreateRequest {
    private Long treeId;
    private Long parentNodeId;
    private String decisionText;

    public NodeCreateRequest(Long treeId, Long parentNodeId, String decisionText) {
        this.treeId = treeId;
        this.parentNodeId = parentNodeId;
        this.decisionText = decisionText;
    }
}
