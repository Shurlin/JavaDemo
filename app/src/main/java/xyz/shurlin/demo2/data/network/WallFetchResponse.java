package xyz.shurlin.demo2.data.network;


import java.time.LocalDateTime;

public class WallFetchResponse {
    private Long id;

    private String title;

    private String content;

    private String createdAt;

    private Long likes;

    public WallFetchResponse(Long id, String title, String content, String createdAt, Long likes) {
        this.id = id;
        this.title = title;
        this.content = content;
        this.createdAt = createdAt;
        this.likes = likes;
    }

    public Long getId() {
        return id;
    }

    public String getTitle() {
        return title;
    }

    public String getContent() {
        return content;
    }

    public String getCreatedAt() {
        return createdAt;
    }

    public Long getLikes() {
        return likes;
    }
}
