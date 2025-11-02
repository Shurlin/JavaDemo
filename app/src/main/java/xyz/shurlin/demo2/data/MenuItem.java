package xyz.shurlin.demo2.data;

public class MenuItem {
    private final String id;           // 唯一 id（可用于统计）
    private final String title;
    private final String desc;
    private final int iconRes;         // drawable 资源 id
    private final Class<?> target;     // 目标 Activity 的 class

    public MenuItem(String id, String title, String desc, int iconRes, Class<?> target) {
        this.id = id;
        this.title = title;
        this.desc = desc;
        this.iconRes = iconRes;
        this.target = target;
    }

    public String getId() { return id; }
    public String getTitle() { return title; }
    public String getDesc() { return desc; }
    public int getIconRes() { return iconRes; }
    public Class<?> getTarget() { return target; }
}
