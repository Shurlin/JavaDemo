package xyz.shurlin.demo2.data.network.decision_tree;

import java.util.Date;

public class DecisionNode {
    private Long nodeId;
    private Long treeId;
    private Long parentNodeId; // -1代表父节点是根节点
    private String decisionText; // 决策文本
    private String simulation; // 模型输出的模拟结果
    private int depth; // 节点深度
    private Date createTime;

    public DecisionNode() {
    }

    public DecisionNode(Long nodeId, Long treeId, Long parentNodeId, String decisionText, String simulation, Integer depth) {
        this.nodeId = nodeId;
        this.treeId = treeId;
        this.parentNodeId = parentNodeId;
        this.decisionText = decisionText;
        this.simulation = simulation;
        this.depth = depth;
        this.createTime = null;
    }

    public void setNodeId(Long nodeId) {
        this.nodeId = nodeId;
    }

    public void setTreeId(Long treeId) {
        this.treeId = treeId;
    }

    public void setParentNodeId(Long parentNodeId) {
        this.parentNodeId = parentNodeId;
    }

    public void setDecisionText(String decisionText) {
        this.decisionText = decisionText;
    }

    public void setSimulation(String simulation) {
        this.simulation = simulation;
    }

    public void setDepth(Integer depth) {
        this.depth = depth;
    }

    public void setCreateTime(Date createTime) {
        this.createTime = createTime;
    }

    public Long getNodeId() {
        return nodeId;
    }

    public Long getTreeId() {
        return treeId;
    }

    public Long getParentNodeId() {
        return parentNodeId;
    }

    public String getDecisionText() {
        return decisionText;
    }

    public String getSimulation() {
        return simulation;
    }

    public int getDepth() {
        return depth;
    }

    public Date getCreateTime() {
        return createTime;
    }
}
