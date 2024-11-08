package models;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * @author Laurent Voisard
 * VideoList Model
 */
public class VideoList {
    private List<Video> videoList;

    /**
     * @author Laurent Voisard
     * Basic constructor
     * @param videos list of videos
     */
    public VideoList(List<Video> videos ) {
        this.videoList = new ArrayList<>(videos);
    }

    /**
     * @author Laurent Voisard
     * @return list of videos
     */
    public List<Video> getVideoList() {
        return videoList;
    }

}
