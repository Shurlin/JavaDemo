package xyz.shurlin.demo2.data.network;

import java.util.List;

/**
 * 通用分页响应（兼容后端返回 content/number/size/totalElements/totalPages）
 * JSON 示例:
 * {
 *   "content": [ ... ],
 *   "number": 0,
 *   "size": 20,
 *   "totalElements": 123,
 *   "totalPages": 7
 * }
 */
public class PageResponse<T> {
    private List<T> content;
    private int number;         // 0-based page index
    private int size;
    private long totalElements;
    private int totalPages;

    public List<T> getContent() { return content; }
    public void setContent(List<T> content) { this.content = content; }

    public int getNumber() { return number; }
    public void setNumber(int number) { this.number = number; }

    public int getSize() { return size; }
    public void setSize(int size) { this.size = size; }

    public long getTotalElements() { return totalElements; }
    public void setTotalElements(long totalElements) { this.totalElements = totalElements; }

    public int getTotalPages() { return totalPages; }
    public void setTotalPages(int totalPages) { this.totalPages = totalPages; }
}
