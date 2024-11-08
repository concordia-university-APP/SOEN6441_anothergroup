package models;

/**
 * Represents a YouTube channel, including its unique identifier, title, description,
 * thumbnail URL, and a list of its recent videos.
 * @author Yehia metwally
 */

/** The unique identifier for the YouTube channel.
 * The title of the YouTube channel.
 * The description of the YouTube channel.
 * The URL of the channel's thumbnail image.
 * The list of recent videos posted by the channel.
 */
public class YoutubeChannel {
    private final String id;
    private final String title;
    private final String description;
    private final String thumbnailUrl;
    private final VideoList recentVideos;

    /**
     * Constructs a new YoutubeChannel instance with the specified details.
     *
     * @param id The unique identifier of the YouTube channel.
     * @param title The title of the YouTube channel.
     * @param description The description of the YouTube channel.
     * @param thumbnailUrl The URL of the channel's thumbnail image.
     * @param recentVideos A list of recent videos posted by the channel.
     */
    public YoutubeChannel(String id, String title, String description, String thumbnailUrl, VideoList recentVideos) {
        this.id = id;
        this.title = title;
        this.description = description;
        this.thumbnailUrl = thumbnailUrl;
        this.recentVideos = recentVideos;
    }

    /**
     * Returns the unique identifier of the YouTube channel.
     *
     * @return The channel ID.
     */
    public String getId() { return id; }

    /**
     * Returns the title of the YouTube channel.
     *
     * @return The channel title.
     */
    public String getTitle() { return title; }

    /**
     * Returns the description of the YouTube channel.
     *
     * @return The channel description.
     */
    public String getDescription() { return description; }

    /**
     * Returns the URL of the channel's thumbnail image.
     *
     * @return The thumbnail URL.
     */
    public String getThumbnailUrl() { return thumbnailUrl; }

    /**
     * Returns the list of recent videos posted by the channel.
     *
     * @return A VideoList containing recent videos.
     */
    public VideoList getRecentVideos() { return recentVideos; }
}
