package services;

import models.Video;
import models.VideoList;
import models.VideoSearch;

import javax.inject.Inject;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;
public class TagService {
    private final YoutubeService youtubeService;

    /**
     * @author Ryane
     * @param youtubeService The YoutubeService instance used to fetch videos.
     */
    @Inject
    public TagService(YoutubeService youtubeService) {
        this.youtubeService = youtubeService;
    }

    /**
     * get videos by the video
     * @author Ryane
     * @param keywords   to use for the video search.
     * @param maxResults The maximum number of results to retrieve.
     * @param tagToCheck The specific tag to check within the video descriptions (optional).
     * @return A CompletableFuture that will contain a VideoList of search results.
     */
    public CompletableFuture<List<Video>> getVideoWithTags(String keywords, Long maxResults, String tagToCheck) {
        return youtubeService.searchResults(keywords, maxResults)
                .thenApply(videoList -> {
                    if (videoList == null || videoList.getVideoList() == null) {
                        return Collections.emptyList(); // Handle null or empty video list gracefully
                    }
                    // Filter videos containing the specified tag
                    return videoList.getVideoList().stream()
                            .filter(video -> video.getTags() != null && video.getTags().contains(tagToCheck))
                            .collect(Collectors.toList());
                });
    }


}

