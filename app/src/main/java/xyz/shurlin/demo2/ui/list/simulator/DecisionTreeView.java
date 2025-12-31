package xyz.shurlin.demo2.ui.list.simulator;

import static android.view.MotionEvent.*;

import android.animation.ValueAnimator;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.LinearGradient;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.RectF;
import android.graphics.Shader;
import android.text.Layout;
import android.text.StaticLayout;
import android.text.TextPaint;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

import xyz.shurlin.demo2.data.network.decision_tree.DecisionNode;
import xyz.shurlin.demo2.data.network.decision_tree.DecisionTreeResponse;

public class DecisionTreeView extends View {

    // 颜色定义 - ChatGPT风格
    private static final int COLOR_BACKGROUND = Color.WHITE;
    private static final int COLOR_ROOT_BG = Color.parseColor("#F0F7FF"); // 浅蓝色背景
    private static final int COLOR_NODE_BG = Color.parseColor("#F7F0FF");
    private static final int COLOR_SELECTED_BG = Color.parseColor("#FFF4E5"); // 选中橙色
    private static final int COLOR_PRIMARY = Color.parseColor("#7fA310");
    private static final int COLOR_PRIMARY_LIGHT = Color.rgb(255, 250, 174);
    private static final int COLOR_TEXT_PRIMARY = Color.parseColor("#202123");
    private static final int COLOR_TEXT_SECONDARY = Color.parseColor("#565869");
    private static final int COLOR_LINE = Color.parseColor("#ECECF1");
    private static final int COLOR_SHADOW = Color.parseColor("#1A000000");

    private final Paint nodePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint selectedPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint textPaint = new TextPaint(Paint.ANTI_ALIAS_FLAG);
    private final Paint linePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint addButtonPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint shadowPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint gradientPaint = new Paint(Paint.ANTI_ALIAS_FLAG);

    // 尺寸定义
    private static final float TEXT_SIZE = 40f;
    private static final float LONG_TEXT_SIZE = 35f;
    // 节点宽度（单位：像素）
    private static final float NODE_WIDTH = 800f;
    // 节点高度（单位：像素）
    private static final float NODE_HEIGHT = 400f;
    // 节点圆角半径（单位：像素），用于创建圆角矩形
    private static final float NODE_CORNER_RADIUS = 16f;
    // 水平间距（单位：像素）：节点之间的水平距离
    private static final float H_GAP = 200f;
    // 垂直间距（单位：像素）：同一层级节点之间的垂直距离
    private static final float V_GAP = 100f;
    // 触摸容差（单位：像素）：用于判断是点击还是拖动的阈值距离
    private static final float TOUCH_SLOP = 20f;
    // 添加按钮大小（直径，单位：像素）
    private static final float ADD_BUTTON_SIZE = 66f;
    // 阴影半径（单位：像素）：节点阴影的模糊半径
    private static final float SHADOW_RADIUS = 8f;
    // 连接线宽度（单位：像素）：决策树节点之间连接线的粗细
    private static final float LINE_WIDTH = 2f;

    private static final float BIAS = 50f;

    // 手势控制
    private float offsetX = 0;
    private float offsetY = 0;
    private float lastX, lastY;
    private float downX, downY;
    private boolean isDragging;

    // 数据
    private DecisionTreeResponse tree;
    private DecisionNode selectedNode;
    private float viewCenterX, viewCenterY;

    // 回调
    private Runnable onAddRootClick;
    private Consumer<Long> onAddNodeClick;
    private Runnable onEditBackground;
    private Runnable closeInput;
    private boolean inputOpen = false;

    // 点击区域
    private final List<HitArea> hitAreas = new ArrayList<>();

    // 动画相关
    private float addButtonPulse = 0f;
    private ValueAnimator pulseAnimator;

    // 新增：长文本显示相关
    private DecisionNode expandedNode;  // 当前展开的节点
    private boolean showFullSimulation = false; // 是否显示完整模拟文本
    private final RectF expandedTextArea = new RectF(); // 展开文本显示区域
    private final Paint expandedTextBgPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final TextPaint expandedTextPaint = new TextPaint(Paint.ANTI_ALIAS_FLAG);

    public DecisionTreeView(Context context, AttributeSet attrs) {
        super(context, attrs);
        initPaints();
        startPulseAnimation();
        initExpandedTextPaint();
    }

    private void initPaints() {
        // 阴影画笔
        shadowPaint.setColor(COLOR_SHADOW);
        shadowPaint.setStyle(Paint.Style.FILL);

        // 节点背景画笔
        nodePaint.setStyle(Paint.Style.FILL);

        // 选中状态画笔
        selectedPaint.setColor(COLOR_SELECTED_BG);
        selectedPaint.setStyle(Paint.Style.FILL);

        // 文字画笔
        textPaint.setColor(COLOR_TEXT_PRIMARY);
        textPaint.setTextSize(TEXT_SIZE);
        textPaint.setTextAlign(Paint.Align.CENTER);

        // 连接线画笔
        linePaint.setColor(COLOR_LINE);
        linePaint.setStrokeWidth(LINE_WIDTH);
        linePaint.setStyle(Paint.Style.STROKE);
        linePaint.setStrokeCap(Paint.Cap.ROUND);

        // 添加按钮画笔
        addButtonPaint.setColor(COLOR_PRIMARY);
        addButtonPaint.setStyle(Paint.Style.FILL);

        // 渐变画笔
        gradientPaint.setStyle(Paint.Style.FILL);
    }

    private void startPulseAnimation() {
        pulseAnimator = ValueAnimator.ofFloat(0f, 1f);
        pulseAnimator.setDuration(2000);
        pulseAnimator.setRepeatCount(ValueAnimator.INFINITE);
        pulseAnimator.setRepeatMode(ValueAnimator.REVERSE);
        pulseAnimator.addUpdateListener(animation -> {
            addButtonPulse = (float) animation.getAnimatedValue();
            invalidate();
        });
        pulseAnimator.start();
    }

    private void initExpandedTextPaint() {
        expandedTextBgPaint.setColor(Color.WHITE);
        expandedTextBgPaint.setStyle(Paint.Style.FILL);

        expandedTextPaint.setTextSize(TEXT_SIZE);
        expandedTextPaint.setColor(Color.parseColor("#202123"));
        expandedTextPaint.setAntiAlias(true);
    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        if (pulseAnimator != null) {
            pulseAnimator.cancel();
        }
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        viewCenterX = w / 2f;
        viewCenterY = h / 2f;
    }

    public void showCreateRootHint() {
        tree = null;
        invalidate();
    }

    public void loadTree(DecisionTreeResponse tree) {
        this.tree = tree;
        offsetX = 0;
        offsetY = 0;
        invalidate();
    }

    public void addNode(DecisionNode node) {
        if (tree != null) {
            tree.addNode(node);
            invalidate();
        }
    }

    public void updateBackground(String background) {
        if (tree != null) {
            tree.setBackground(background);
            invalidate();
        }
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        // 绘制背景
        canvas.drawColor(COLOR_BACKGROUND);

        if (tree == null) {
            drawCreateRoot(canvas);
        } else {
            drawTree(canvas);
        }

        // 绘制展开的simulation文本（在最上层）
        if (showFullSimulation && expandedNode != null) {
            drawExpandedSimulation(canvas);
        }
    }

    private void drawCreateRoot(Canvas canvas) {
        hitAreas.clear();

        // 绘制提示文字
        textPaint.setTextSize(TEXT_SIZE);
        textPaint.setColor(COLOR_TEXT_SECONDARY);
        textPaint.setTextAlign(Paint.Align.CENTER);
        canvas.drawText("创建人生决策树", viewCenterX, viewCenterY - 120, textPaint);

        // 绘制提示文字
        textPaint.setTextSize(TEXT_SIZE);
        textPaint.setColor(COLOR_TEXT_SECONDARY);
        canvas.drawText("点击上方按钮开始", viewCenterX, viewCenterY + 160, textPaint);

        // 绘制圆形添加按钮
        float radius = 80f + 10f * addButtonPulse; // 脉冲效果
        float cx = viewCenterX;
        float cy = viewCenterY;

        // 按钮阴影
        shadowPaint.setAlpha(20);
        canvas.drawCircle(cx, cy, radius + SHADOW_RADIUS, shadowPaint);

        // 渐变背景
        gradientPaint.setShader(new LinearGradient(cx - radius, cy - radius, cx + radius, cy + radius, Color.parseColor("#10A37F"), Color.parseColor("#0D8C6F"), Shader.TileMode.CLAMP));
        canvas.drawCircle(cx, cy, radius, gradientPaint);

        // 加号
        addButtonPaint.setColor(Color.WHITE);
        float plusSize = 40f;
        float strokeWidth = 8f;
        addButtonPaint.setStrokeWidth(strokeWidth);
        addButtonPaint.setStrokeCap(Paint.Cap.ROUND);
        addButtonPaint.setStyle(Paint.Style.STROKE);

        canvas.drawLine(cx - plusSize / 2, cy, cx + plusSize / 2, cy, addButtonPaint);
        canvas.drawLine(cx, cy - plusSize / 2, cx, cy + plusSize / 2, addButtonPaint);

        // 恢复填充模式
        addButtonPaint.setStyle(Paint.Style.FILL);

        // 添加点击区域
        RectF rect = new RectF(cx - radius, cy - radius, cx + radius, cy + radius);
        hitAreas.add(new HitArea() {{
            this.curRect = rect;
            this.curNode = null;
            this.isAddButton = true;
            this.depth = -1;
        }});
    }

    private void drawTree(Canvas canvas) {
        hitAreas.clear();

        canvas.save();
        canvas.translate(offsetX, offsetY);

        Map<Long, List<DecisionNode>> childrenMap = buildChildrenMap();

        // 计算根节点位置（稍微偏左，为子节点留出空间）
        float rootX = getWidth() / 4f;
        float rootY = viewCenterY;

        // 绘制连接线
        drawConnections(canvas, childrenMap, rootX, rootY);

        // 绘制根节点
        drawRoot(canvas, rootX, rootY);

        // 绘制子节点
        List<DecisionNode> roots = childrenMap.get(-1L);
        if (roots != null && !roots.isEmpty()) {
            drawChildren(canvas, roots, rootX + NODE_WIDTH + H_GAP, rootY, childrenMap);
        }

        canvas.restore();
    }

    // 在drawConnections方法中修改：
    private void drawConnections(Canvas canvas, Map<Long, List<DecisionNode>> childrenMap, float parentX, float parentY) {
        linePaint.setColor(COLOR_LINE);
        linePaint.setStrokeWidth(LINE_WIDTH);

        // 绘制从根节点到一级子节点的连接
        List<DecisionNode> firstLevel = childrenMap.get(-1L);
        if (firstLevel != null) {
            for (int i = 0; i < firstLevel.size(); i++) {
                DecisionNode child = firstLevel.get(i);
                float childY = calculateChildY(parentY, i, firstLevel.size());
                float descTextLength = textPaint.measureText(child.getDecisionText());
                // 传递decisionText作为最后一个参数
                drawConnection(canvas, parentX, parentY, parentX + NODE_WIDTH + H_GAP, childY, 0, child.getDecisionText());

                // 递归绘制子节点的连接
                drawChildConnections(canvas, child.getNodeId(), parentX + NODE_WIDTH + H_GAP + descTextLength, childY, childrenMap, 1);
            }
        }
    }

    // 在drawChildConnections方法中修改：
    private void drawChildConnections(Canvas canvas, long parentId, float parentX, float parentY, Map<Long, List<DecisionNode>> childrenMap, int depth) {
        List<DecisionNode> children = childrenMap.get(parentId);
        if (children != null) {
            for (int i = 0; i < children.size(); i++) {
                DecisionNode child = children.get(i);
                float childY = calculateChildY(parentY, i, children.size());
                float descTextLength = textPaint.measureText(child.getDecisionText());
                // 传递decisionText作为最后一个参数
                drawConnection(canvas, parentX, parentY, parentX + NODE_WIDTH + H_GAP, childY, depth, child.getDecisionText());

                drawChildConnections(canvas, child.getNodeId(), parentX + NODE_WIDTH + H_GAP + descTextLength, childY, childrenMap, depth + 1);
            }
        }
    }

    private void drawConnection(Canvas canvas, float startX, float startY,
                                float endX, float endY, int depth, String decisionText) {
        // 使用贝塞尔曲线绘制连接线
        Path path = new Path();
        path.moveTo(startX + NODE_WIDTH/2, startY);

        float controlX1 = startX + NODE_WIDTH/2 + H_GAP/3;
        float controlY1 = startY;
        float controlX2 = endX - NODE_WIDTH/2 - H_GAP/3;
        float controlY2 = endY;

        path.cubicTo(controlX1, controlY1, controlX2, controlY2, endX - NODE_WIDTH/2, endY);

        // 根据深度设置不同的颜色
        float alpha = 1f - depth * 0.1f;
        if (alpha < 0.5f) alpha = 0.5f;
        linePaint.setAlpha((int)(alpha * 255));

        canvas.drawPath(path, linePaint);
        linePaint.setAlpha(255);

        // 绘制连线末端的水平线段（思维导图样式）
        float lineStartX = endX - NODE_WIDTH/2;
        float horizontalLineLength = textPaint.measureText(decisionText);
        float lineEndX = lineStartX + horizontalLineLength;

        // 绘制水平线段
        linePaint.setColor(COLOR_LINE);
        linePaint.setStrokeWidth(LINE_WIDTH);
        canvas.drawLine(lineStartX, endY, lineEndX, endY, linePaint);

        // 绘制决策文本在水平线段正上方
        if (decisionText != null && !decisionText.trim().isEmpty()) {
            drawDecisionTextOnLine(canvas, decisionText, lineStartX, lineEndX, endY);
        }
    }

    private float calculateChildY(float parentY, int index, int total) {
        if (total == 1) return parentY;
        float totalHeight = (total - 1) * (NODE_HEIGHT + V_GAP);
        return parentY - totalHeight / 2 + index * (NODE_HEIGHT + V_GAP);
    }

    private void drawRoot(Canvas canvas, float x, float y) {
        RectF rect = new RectF(x - NODE_WIDTH / 2, y - NODE_HEIGHT / 2, x + NODE_WIDTH / 2, y + NODE_HEIGHT / 2);

        // 绘制阴影
        drawShadow(canvas, rect);

        // 绘制根节点背景
        nodePaint.setColor(COLOR_ROOT_BG);
        canvas.drawRoundRect(rect, NODE_CORNER_RADIUS, NODE_CORNER_RADIUS, nodePaint);

        // 选中状态边框
        if (selectedNode == null) {
            selectedPaint.setColor(COLOR_PRIMARY);
            selectedPaint.setStyle(Paint.Style.STROKE);
            selectedPaint.setStrokeWidth(3f);
            canvas.drawRoundRect(rect, NODE_CORNER_RADIUS, NODE_CORNER_RADIUS, selectedPaint);
            selectedPaint.setStyle(Paint.Style.FILL);
        }

//        // 绘制标题
//        textPaint.setColor(COLOR_TEXT_PRIMARY);
//        textPaint.setTextSize(28f);
//        textPaint.setTextAlign(Paint.Align.CENTER);
//        canvas.drawText("背景", x, y - NODE_HEIGHT/2 + 40, textPaint);

        // 绘制背景内容
        textPaint.setColor(COLOR_TEXT_SECONDARY);
        textPaint.setTextSize(TEXT_SIZE);
        drawMultilineText(canvas, tree.getBackground(), rect, x, y, 6);

        hitAreas.add(new HitArea() {{
            this.curRect = rect;
            this.curNode = null;
            this.isAddButton = false;
            this.depth = 0;
        }});

        // 绘制添加按钮
        drawAddButton(canvas, x + NODE_WIDTH / 2 + 30, y, null);
    }

    private void drawChildren(Canvas canvas, List<DecisionNode> nodes, float x, float centerY, Map<Long, List<DecisionNode>> childrenMap) {
        if (nodes.isEmpty()) return;

        for (int i = 0; i < nodes.size(); i++) {
            DecisionNode node = nodes.get(i);
            float y = calculateChildY(centerY, i, nodes.size());
            float descTextLength = textPaint.measureText(node.getDecisionText());

            drawNode(canvas, node, x + descTextLength, y);

            List<DecisionNode> children = childrenMap.get(node.getNodeId());
            if (children != null && !children.isEmpty()) {
                drawChildren(canvas, children, x + NODE_WIDTH + H_GAP + descTextLength, y, childrenMap);
            }
        }
    }

    private void drawNode(Canvas canvas, DecisionNode node, float x, float y) {
        RectF rect = new RectF(x - NODE_WIDTH / 2, y - NODE_HEIGHT / 2, x + NODE_WIDTH / 2, y + NODE_HEIGHT / 2);

        // 绘制阴影
        drawShadow(canvas, rect);

        // 绘制节点背景
        if (node == selectedNode) {
            selectedPaint.setColor(COLOR_SELECTED_BG);
            canvas.drawRoundRect(rect, NODE_CORNER_RADIUS, NODE_CORNER_RADIUS, selectedPaint);
        } else {
            nodePaint.setColor(COLOR_NODE_BG);
            canvas.drawRoundRect(rect, NODE_CORNER_RADIUS, NODE_CORNER_RADIUS, nodePaint);
        }

        // 绘制决策文本
        textPaint.setColor(COLOR_TEXT_PRIMARY);
        textPaint.setTextSize(TEXT_SIZE);
        drawMultilineText(canvas, node.getSimulation(), rect, x, y, 6);

//        // 如果节点有模拟结果，显示提示
//        if (node.getSimulation() != null && !node.getSimulation().isEmpty()) {
//            textPaint.setColor(COLOR_PRIMARY);
//            textPaint.setTextSize(22f);
//            canvas.drawText("已模拟", x, y + NODE_HEIGHT/2 - 25, textPaint);
//        }

        hitAreas.add(new HitArea() {{
            this.curRect = rect;
            this.curNode = node;
            this.isAddButton = false;
            this.depth = node.getDepth();
        }});

        // 绘制添加按钮
        drawAddButton(canvas, x + NODE_WIDTH / 2 + 30, y, node);
    }

    private void drawAddButton(Canvas canvas, float x, float y, DecisionNode parentNode) {
        // 按钮阴影
        shadowPaint.setAlpha(20);
        canvas.drawCircle(x, y, ADD_BUTTON_SIZE / 2 + 4, shadowPaint);

        // 脉冲效果
        float pulseScale = 1f + 0.2f * addButtonPulse;
        float scaledSize = ADD_BUTTON_SIZE * pulseScale;

        // 按钮背景
        addButtonPaint.setColor(COLOR_PRIMARY_LIGHT);
        canvas.drawCircle(x, y, scaledSize / 2, addButtonPaint);

        // 加号图标
        addButtonPaint.setColor(COLOR_PRIMARY);
        float plusSize = 26f;
        float strokeWidth = 5f;
        addButtonPaint.setStrokeWidth(strokeWidth);
        addButtonPaint.setStrokeCap(Paint.Cap.ROUND);
        addButtonPaint.setStyle(Paint.Style.STROKE);

        canvas.drawLine(x - plusSize / 2, y, x + plusSize / 2, y, addButtonPaint);
        canvas.drawLine(x, y - plusSize / 2, x, y + plusSize / 2, addButtonPaint);

        // 恢复填充模式
        addButtonPaint.setStyle(Paint.Style.FILL);

        // 添加点击区域
        RectF plusRect = new RectF(x - scaledSize / 2, y - scaledSize / 2, x + scaledSize / 2, y + scaledSize / 2);

        hitAreas.add(new HitArea() {{
            this.curRect = plusRect;
            this.curNode = parentNode;
            this.isAddButton = true;
            this.depth = parentNode == null ? 1 : parentNode.getDepth() + 1;
        }});
    }

    private void drawShadow(Canvas canvas, RectF rect) {
        shadowPaint.setAlpha(15);
        canvas.drawRoundRect(rect.left + SHADOW_RADIUS, rect.top + SHADOW_RADIUS, rect.right + SHADOW_RADIUS, rect.bottom + SHADOW_RADIUS, NODE_CORNER_RADIUS, NODE_CORNER_RADIUS, shadowPaint);
    }

    private Map<Long, List<DecisionNode>> buildChildrenMap() {
        Map<Long, List<DecisionNode>> map = new HashMap<>();
        if (tree == null || tree.getNodes() == null) return map;

        // 添加根节点的子节点
        map.put(-1L, new ArrayList<>());

        for (DecisionNode node : tree.getNodes()) {
            Long parentId = node.getParentNodeId() != null ? node.getParentNodeId() : -1L;
            map.computeIfAbsent(parentId, k -> new ArrayList<>()).add(node);
        }

        return map;
    }

    private void drawMultilineText(Canvas canvas, String text, RectF rect, float centerX, float centerY, int maxLines) {
        if (text == null || text.isEmpty()) return;

        // 创建TextPaint
        TextPaint tp = new TextPaint(Paint.ANTI_ALIAS_FLAG);
        tp.setTextSize(textPaint.getTextSize());
        tp.setColor(textPaint.getColor());
        tp.setTextAlign(Paint.Align.LEFT); // 关键：设置为左对齐，让StaticLayout处理对齐

        // 计算文本区域宽度（减去左右边距）
        int width = (int) (rect.width() - 32); // 左右各16边距

        // 创建StaticLayout
        StaticLayout layout = StaticLayout.Builder
                .obtain(text, 0, Math.min(text.length(), 150), tp, width)
                .setAlignment(Layout.Alignment.ALIGN_CENTER)  // 关键：设置布局居中对齐
                .setLineSpacing(4f, 1f)
                .setMaxLines(maxLines)
                .setEllipsize(android.text.TextUtils.TruncateAt.END)
                .build();

        // 计算文本高度
        float textHeight = layout.getHeight();

        // 计算绘制起始位置，确保居中
        float textTop = centerY - textHeight / 2;

        canvas.save();
        // 将画布平移到矩形左侧 + 边距，这样StaticLayout的居中才会正确
        canvas.translate(rect.left + 16, textTop);
        layout.draw(canvas);
        canvas.restore();
    }

    // 新增：在水平线段上方绘制文本
    private void drawDecisionTextOnLine(Canvas canvas, String decisionText,
                                        float lineStartX, float lineEndX, float lineY) {
        // 限制文本长度
        String displayText = decisionText.trim();
        if (displayText.length() > 15) {
            displayText = displayText.substring(0, 12) + "...";
        }

//        // 创建文本画笔
//        TextPaint textPaint = new TextPaint(Paint.ANTI_ALIAS_FLAG);
//        textPaint.setTextSize(22f); // 比节点文字小一些
//        textPaint.setColor(COLOR_TEXT_PRIMARY);
//        textPaint.setTextAlign(Paint.Align.CENTER);

        // 计算文本宽度
        float textWidth = textPaint.measureText(displayText);
        float lineCenterX = (lineStartX + lineEndX) / 2;

        // 计算文本背景区域
        float padding = 8f;
        float textHeight = textPaint.getTextSize();
        float textTop = lineY - textHeight - padding - 5; // 在水平线段上方
        float textBottom = lineY - 5; // 距离线段5px

        RectF textRect = new RectF(
                lineCenterX - textWidth/2 - padding,
                textTop,
                lineCenterX + textWidth/2 + padding,
                textBottom
        );

//        // 绘制文本背景（圆角矩形）
//        Paint bgPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
//        bgPaint.setColor(Color.WHITE); // 白色背景
//        bgPaint.setStyle(Paint.Style.FILL);
//        canvas.drawRoundRect(textRect, 6, 6, bgPaint);
//
//        // 绘制文本边框（可选）
//        Paint borderPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
//        borderPaint.setColor(COLOR_LINE);
//        borderPaint.setStyle(Paint.Style.STROKE);
//        borderPaint.setStrokeWidth(1f);
//        canvas.drawRoundRect(textRect, 6, 6, borderPaint);

        // 绘制文本
        textPaint.setColor(COLOR_TEXT_PRIMARY);
        canvas.drawText(displayText, lineCenterX - 5f, textRect.bottom - padding, textPaint);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        switch (event.getAction()) {
            case ACTION_DOWN:
                if (inputOpen || showFullSimulation) return true;
                downX = lastX = event.getX();
                downY = lastY = event.getY();
                isDragging = false;

                // 如果已经展开，检查是否点击了关闭按钮
                if (showFullSimulation && expandedTextArea.contains(event.getX(), event.getY())) {
                    return true;
                }
                return true;

            case ACTION_MOVE:
                if (inputOpen || showFullSimulation) return true;
                float dx = event.getX() - lastX;
                float dy = event.getY() - lastY;

                if (!isDragging) {
                    float distance = (float) Math.hypot(event.getX() - downX, event.getY() - downY);
                    if (distance > TOUCH_SLOP) {
                        isDragging = true;
                    }
                }

                if (isDragging) {
                    offsetX += dx;
                    offsetY += dy;
                    invalidate();
                }

                lastX = event.getX();
                lastY = event.getY();
                return true;

            case ACTION_UP:
                if (inputOpen && event.getY() < getHeight() - 300) {
                    inputOpen = false;
                    if (closeInput != null) closeInput.run();
                    return true;
                }
                if (!isDragging) {
                    handleClick(event.getX(), event.getY());
                    return true;
                }
                isDragging = false;
                return true;
        }
        return super.onTouchEvent(event);
    }

    private void handleClick(float rawX, float rawY) {
        float x = rawX - offsetX;
        float y = rawY - offsetY;

        // 检查是否点击了关闭按钮
        if (showFullSimulation) {
            for (HitArea area : hitAreas) {
                if (area.depth == -2 && area.curRect.contains(rawX, rawY)) {
                    expandedNode = null;
                    showFullSimulation = false;
                    invalidate();
                    return ;
                }
            }

            // 如果点击了展开区域外部，也关闭
            if (!expandedTextArea.contains(rawX, rawY)) {
                expandedNode = null;
                showFullSimulation = false;
                invalidate();
                return ;
            }
            return ;
        }

        for (HitArea area : hitAreas) {
            if (area.curRect.contains(x, y)) {
                if (area.depth == -1 && area.isAddButton) {
                    if (onAddRootClick != null) {
                        onAddRootClick.run();
                        inputOpen = true;
                    }
                } else if (area.isAddButton) {
                    if (onAddNodeClick != null) {
                        onAddNodeClick.accept(area.curNode == null ? -1L : area.curNode.getNodeId());
                        inputOpen = true;
                    }
                } else {
                    selectedNode = area.curNode;
                    // 点击节点时，如果节点有simulation且内容较长，展开显示
                    if (area.curNode != null && area.curNode.getSimulation() != null) {
                        String simulation = area.curNode.getSimulation();
                        // 如果simulation超过一定长度（比如100字符），则展开显示
                        if (simulation.length() > 100) {
                            toggleNodeExpansion(area.curNode);
                        } else {
                            // 否则触发普通点击事件
                            if (onNodeClickListener != null) {
                                onNodeClickListener.onNodeClick(area.curNode);
                            }
                        }
                    } else if (area.curNode == null && onEditBackground != null) {
                        onEditBackground.run();
                    } else {
                        if (onNodeClickListener != null) {
                            onNodeClickListener.onNodeClick(area.curNode);
                        }
                    }
                    invalidate();
                }
                break;
            }
        }
    }

    // callback setters
    public void setOnAddRootClick(Runnable r) {
        this.onAddRootClick = r;
    }

    public void setOnAddNodeClick(Consumer<Long> c) {
        this.onAddNodeClick = c;
    }

    public void setOnEditBackground(Runnable r) {
        this.onEditBackground = r;
    }

    public void setCloseInput(Runnable r) {
        this.closeInput = r;
    }

    public DecisionNode getSelectedNode() {
        return selectedNode;
    }

    public void clearSelection() {
        selectedNode = null;
        invalidate();
    }

    // 新增回调接口
    public interface OnNodeClickListener {
        void onNodeClick(DecisionNode node);
        void onNodeLongClick(DecisionNode node);
    }
    private OnNodeClickListener onNodeClickListener;

    // 新增：设置点击监听器
    public void setOnNodeClickListener(OnNodeClickListener listener) {
        this.onNodeClickListener = listener;
    }

    // 新增：展开/收起节点详情
    public void toggleNodeExpansion(DecisionNode node) {
        if (expandedNode == node) {
            // 如果点击的是已展开的节点，则收起
            expandedNode = null;
            showFullSimulation = false;
        } else {
            // 展开新节点
            expandedNode = node;
            showFullSimulation = true;
            // 计算展开区域
            calculateExpandedArea();
        }
        invalidate();
    }

    private void calculateExpandedArea() {
        // 在屏幕底部创建一个区域显示完整文本
        float padding = 50f;
        expandedTextArea.left = padding;
        expandedTextArea.top = getHeight() * 0.6f; // 占据屏幕下方40%区域
        expandedTextArea.right = getWidth() - padding;
        expandedTextArea.bottom = getHeight() - padding;
    }

    private void drawExpandedSimulation(Canvas canvas) {
        if (expandedNode == null || !showFullSimulation || expandedNode.getSimulation() == null) {
            return;
        }

        // 绘制半透明背景遮罩
        Paint overlayPaint = new Paint();
        overlayPaint.setColor(Color.parseColor("#80000000")); // 半透明黑色
        canvas.drawRect(0, 0, getWidth(), getHeight(), overlayPaint);

        // 计算对话框位置（在屏幕中央）
        float dialogWidth = getWidth() * 0.8f;  // 对话框宽度为屏幕的80%
        float dialogHeight = getHeight() * 0.7f; // 对话框高度为屏幕的70%
        float dialogLeft = (getWidth() - dialogWidth) / 2;
        float dialogTop = (getHeight() - dialogHeight) / 2;

        expandedTextArea.set(dialogLeft, dialogTop, dialogLeft + dialogWidth, dialogTop + dialogHeight);

        // 绘制对话框背景（带阴影效果）
        // 先绘制阴影
        shadowPaint.setColor(Color.parseColor("#40000000"));
        canvas.drawRoundRect(
                expandedTextArea.left + 10,
                expandedTextArea.top + 10,
                expandedTextArea.right + 10,
                expandedTextArea.bottom + 10,
                20, 20, shadowPaint
        );

        // 绘制对话框背景
        expandedTextBgPaint.setColor(Color.WHITE);
        canvas.drawRoundRect(expandedTextArea, 20, 20, expandedTextBgPaint);

        // 绘制标题栏
        Paint titlePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        titlePaint.setColor(Color.parseColor("#10A37F"));
        titlePaint.setStyle(Paint.Style.FILL);
        RectF titleRect = new RectF(
                expandedTextArea.left,
                expandedTextArea.top,
                expandedTextArea.right,
                expandedTextArea.top + 80
        );
        canvas.drawRect(titleRect, titlePaint);

        // 绘制标题文字
        TextPaint titleTextPaint = new TextPaint(Paint.ANTI_ALIAS_FLAG);
        titleTextPaint.setColor(Color.WHITE);
        titleTextPaint.setTextSize(TEXT_SIZE);
        titleTextPaint.setTextAlign(Paint.Align.CENTER);
        canvas.drawText("模拟结果详情",
                expandedTextArea.centerX(),
                expandedTextArea.top + 48,
                titleTextPaint);

        // 绘制关闭按钮
        Paint closePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        closePaint.setColor(Color.WHITE);
        closePaint.setStrokeWidth(4f);
        closePaint.setStyle(Paint.Style.STROKE);
        float closeSize = 24f;
        float closeX = expandedTextArea.right - 50;
        float closeY = expandedTextArea.top + 40;

        // 绘制圆形背景
        Paint closeBgPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        closeBgPaint.setColor(Color.parseColor("#40000000"));
        canvas.drawCircle(closeX, closeY, 18, closeBgPaint);

        // 绘制X图标
        canvas.drawLine(
                closeX - closeSize/2, closeY - closeSize/2,
                closeX + closeSize/2, closeY + closeSize/2,
                closePaint
        );
        canvas.drawLine(
                closeX + closeSize/2, closeY - closeSize/2,
                closeX - closeSize/2, closeY + closeSize/2,
                closePaint
        );

        // 添加关闭按钮点击区域（稍微扩大一些）
        hitAreas.add(new HitArea() {{
            this.curRect = new RectF(
                    closeX - 30, closeY - 30,
                    closeX + 30, closeY + 30
            );
            this.curNode = null;
            this.isAddButton = false;
            this.depth = -2; // 特殊值表示关闭按钮
        }});

        // 绘制完整simulation文本（带滚动区域模拟）
        String fullText = expandedNode.getSimulation();
        if (fullText != null && !fullText.isEmpty()) {
            // 计算文本区域（减去标题和边距）
            float textLeft = expandedTextArea.left + 25;
            float textTop = expandedTextArea.top + 100;
            float textRight = expandedTextArea.right - 25;
            float textBottom = expandedTextArea.bottom - 25;
            float textAreaHeight = textBottom - textTop;

            // 绘制文本区域的背景（模拟ScrollView的内容区域）
            Paint textAreaBgPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
            textAreaBgPaint.setColor(Color.parseColor("#F9F9F9"));
            canvas.drawRoundRect(
                    textLeft, textTop, textRight, textBottom,
                    12, 12, textAreaBgPaint
            );

            // 计算文本高度，如果需要滚动则显示滚动条
            expandedTextPaint.setTextSize(LONG_TEXT_SIZE);
            expandedTextPaint.setColor(Color.parseColor("#202123"));

            float availableWidth = textRight - textLeft - 30; // 留出滚动条空间
            float lineHeight = expandedTextPaint.getTextSize() * 1.2f;

            // 计算总行数
            int maxCharsPerLine = (int) (availableWidth / expandedTextPaint.getTextSize() * 1.5f);
            int totalLines = (int) Math.ceil((double) fullText.length() / maxCharsPerLine);
            float totalTextHeight = totalLines * lineHeight;

            // 如果需要滚动，显示滚动条
            boolean needsScroll = totalTextHeight > textAreaHeight;

            if (needsScroll) {
                // 绘制滚动条背景
                Paint scrollBgPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
                scrollBgPaint.setColor(Color.parseColor("#E0E0E0"));
                float scrollBarWidth = 8f;
                float scrollBarX = textRight - 15;
                canvas.drawRect(
                        scrollBarX - scrollBarWidth/2, textTop + 10,
                        scrollBarX + scrollBarWidth/2, textBottom - 10,
                        scrollBgPaint
                );

                // 绘制滚动条滑块
                Paint scrollThumbPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
                scrollThumbPaint.setColor(Color.parseColor("#10A37F"));
                float scrollableHeight = (textBottom - 10) - (textTop + 10);
                float thumbHeight = Math.max(40, scrollableHeight * (textAreaHeight / totalTextHeight));

                // 计算滑块位置（暂时固定在顶部）
                float thumbTop = textTop + 10;
                canvas.drawRoundRect(
                        scrollBarX - scrollBarWidth/2, thumbTop,
                        scrollBarX + scrollBarWidth/2, thumbTop + thumbHeight,
                        4, 4, scrollThumbPaint
                );

                // 添加提示文字
                Paint hintPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
                hintPaint.setColor(Color.parseColor("#666666"));
                hintPaint.setTextSize(22f);
                hintPaint.setTextAlign(Paint.Align.CENTER);
                canvas.drawText("内容较长，可滚动查看",
                        expandedTextArea.centerX(),
                        textBottom + 30,
                        hintPaint);
            }

            // 实际绘制文本（只显示可见部分）
            float visibleTextHeight = Math.min(totalTextHeight, textAreaHeight);

            // 创建StaticLayout进行多行文本绘制
            StaticLayout layout = StaticLayout.Builder
                    .obtain(fullText, 0, fullText.length(), expandedTextPaint, (int)availableWidth)
                    .setAlignment(Layout.Alignment.ALIGN_NORMAL)
                    .setLineSpacing(8f, 1f)
                    .setIncludePad(true)
                    .setMaxLines((int)(textAreaHeight / lineHeight)) // 限制显示行数
                    .setEllipsize(TextUtils.TruncateAt.END)
                    .build();

            canvas.save();
            // 添加裁剪区域，模拟ScrollView的裁剪效果
            canvas.clipRect(textLeft, textTop, textRight, textBottom);
            canvas.translate(textLeft, textTop);
            layout.draw(canvas);
            canvas.restore();
        }
    }

    private static class HitArea {
        RectF curRect;
        DecisionNode curNode;   // null 表示 root
        boolean isAddButton;
        int depth;
    }

    public void setInputClose(){
        this.inputOpen = false;
    }
}