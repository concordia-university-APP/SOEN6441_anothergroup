package models;

import scala.Option;

import java.util.ArrayList;
import java.util.List;

public class VideoIdList {
    private List<VideoId> videoIdList = null;

    public VideoIdList(List<VideoId> videoIdList) {
        this.videoIdList = videoIdList;
    }

    public List<VideoId> getVideoIdList() {
        return videoIdList;
    }
}
