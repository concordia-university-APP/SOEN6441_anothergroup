package models;

public class Video {
    private final String id;
    private final String title;
    private final String description;
    private final String channelId;
    private final String channelName;
    private final String thumbnailUrl;
    private final FleschReadingEaseScore fleschReadingEaseScore;

    public Video(String id, String title, String description, String channelId, String channelName, String thumbnailUrl) {
        this.id = id;
        this.title = title;
        this.description = description;
        this.channelId = channelId;
        this.channelName = channelName;
        this.thumbnailUrl = thumbnailUrl;
        this.fleschReadingEaseScore = new FleschReadingEaseScore(this.description);
    }

    public String getId() { return id;}
    public String getTitle() { return title;}
    public String getDescription() { return description;}
    public String getChannelId() { return channelId;}
    public String getChannelName() { return channelName;}
    public String getThumbnailUrl() { return thumbnailUrl;}
    public FleschReadingEaseScore getFleschReadingEaseScore() { return fleschReadingEaseScore;}
}
