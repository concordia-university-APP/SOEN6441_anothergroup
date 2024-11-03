package models;
import java.util.List;
public class Channel {
    private final String id;
    private final String title;
    private final String description;
    private final String thumbnailUrl;
    private final VideoList recentVideos;

    public Channel(String id, String title, String description, String thumbnailUrl, VideoList recentVideos) {
        this.id = id;
        this.title = title;
        this.description = description;
        this.thumbnailUrl = thumbnailUrl;
        this.recentVideos = recentVideos;
    }

    public String getId() { return id; }
    public String getTitle() { return title; }
    public String getDescription() { return description; }
    public String getThumbnailUrl() { return thumbnailUrl; }
    public VideoList getRecentVideos() { return recentVideos; }
}
