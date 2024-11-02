package services;

import models.Video;
import models.VideoList;
import models.VideoSearch;

import java.util.ArrayList;
import java.util.List;

public class SearchService {
    private static final int MAX_RESULTS = 10;
    private static List<VideoSearch> videoSearchList = new ArrayList<VideoSearch>();
    private static class SearchServiceInstanceHolder {
        private static final SearchService INSTANCE = new SearchService();
    }

    public static SearchService getInstance() {
        return SearchServiceInstanceHolder.INSTANCE;
    }

    private SearchService() {
    }

    public List<VideoSearch> searchKeywords(String keywords) {
        VideoList results = YoutubeService.searchResults(keywords);

        VideoSearch search = new VideoSearch(keywords, results);
        videoSearchList.add(0, search);

        if (videoSearchList.size() > MAX_RESULTS) {
            videoSearchList.remove(MAX_RESULTS);
        }

        return videoSearchList;
    }

    public Video getVideoById(String id) {
        return YoutubeService.getVideo(id);
    }
}
