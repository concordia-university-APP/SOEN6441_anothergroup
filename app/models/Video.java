package models;

import services.TagService;

import java.util.Collections;
import java.util.List;

/**
 * @author Laurent Voisard
 *
 * Video Model
 */
public class Video {
    private final String id;
    private final String title;
    private final String description;
    private final String channelId;
    private final String channelName;
    private final String thumbnailUrl;
    private final FleschReadingEaseScore fleschReadingEaseScore;
    private List<String> tags;

    /**
     * @author Laurent Voisard
     * Basic Constructor
     * @param id video id
     * @param title video title
     * @param description video description
     * @param channelId video channel
     * @param channelName video channel name
     * @param thumbnailUrl video thumbnail url
     */
    public Video(String id, String title, String description, String channelId, String channelName, String thumbnailUrl, List<String> tags) {
        this.id = id;
        this.title = title;
        this.description = description;
        this.channelId = channelId;
        this.channelName = channelName;
        this.thumbnailUrl = thumbnailUrl;
        this.tags = tags;
        this.fleschReadingEaseScore = new FleschReadingEaseScore(this.description);
    }


    /**
     * @author Laurent Voisard
     * Id getter
     * @return id
     */
    public String getId() { return id;}

    /**
     * @author Laurent Voisard
     * Title getter
     * @return title
     */
    public String getTitle() { return title;}

    /**
     * @author Laurent Voisard
     * Description getter
     * @return description
     */
    public String getDescription() { return description;}
    /**
     * @author Laurent Voisard
     * channel id getter
     * @return channel id
     */
    public String getChannelId() { return channelId;}

    /**
     * @author Laurent Voisard
     * channel name getter
     * @return channel name
     */
    public String getChannelName() { return channelName;}

    /**
     * @author Laurent Voisard
     * thumbnailUrl getter
     * @return thumbnailUrl
     */
    public String getThumbnailUrl() { return thumbnailUrl;}

    /**
     * @author Laurent Voisard
     * fleschScore getter
     * @return flesch score
     */
    public FleschReadingEaseScore getFleschReadingEaseScore() { return fleschReadingEaseScore;}
    /**
     * Retrieves the list of tags associated with this video.
     * @author Ryane
     * @return a {@code List<String>} containing the tags for this video,
     *         or a singleton list with "No tags for this video" if no tags are available.
     */
    public List<String> getTags() {
        return tags != null ? tags : Collections.singletonList("No tags for this video");
    }

}
