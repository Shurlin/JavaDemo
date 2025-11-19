package xyz.shurlin.demo2.data.network;

import java.time.LocalDateTime;

public class WallCreateRequest {
    private String title;

    private String content;

    private String createdBy; // username

    public WallCreateRequest(String title, String content, String createdBy) {
        this.title = title;
        this.content = content;
        this.createdBy = createdBy;
    }

    public String getTitle() {
        return title;
    }

    public String getContent() {
        return content;
    }

    public String getCreatedBy() {
        return createdBy;
    }
}
