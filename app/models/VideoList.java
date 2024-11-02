package models;

import java.util.ArrayList;
import java.util.List;

public class VideoList {
    private List<Video> videoList;

    public VideoList(List<Video> videos) {
        this.videoList = new ArrayList<>(videos);
    }

    public List<Video> getVideoList() {
        return videoList;
    }
}
