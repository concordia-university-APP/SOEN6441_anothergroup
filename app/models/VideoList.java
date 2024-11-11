package models;

import scala.Option;

import java.util.ArrayList;
import java.util.List;

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
     * @return list of videos
     * @author Laurent Voisard
     */
    public List<Video> getVideoList() {return videoList;}

}
