package services;

import models.Video;
import models.VideoSearch;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;

public class SearchService {
    private static final int MAX_RESULTS = 10;
    private static final List<VideoSearch> videoSearchList = new ArrayList<>();
    private static class SearchServiceInstanceHolder {
        private static final SearchService INSTANCE = new SearchService();
    }

    public static SearchService getInstance() {
        return SearchServiceInstanceHolder.INSTANCE;
    }

    private SearchService() {
    }

    public CompletableFuture<List<VideoSearch>> searchKeywords(String keywords) {
        return YoutubeService.searchResults(keywords, 10L).thenApply(results -> {
            VideoSearch search = new VideoSearch(keywords, results);
            videoSearchList.add(0, search);

            if (videoSearchList.size() > MAX_RESULTS) {
                videoSearchList.remove(MAX_RESULTS);
            }

            return videoSearchList;
        });
    }

    public CompletableFuture<Video> getVideoById(String id) {
        return YoutubeService.getVideo(id);
    }
}
