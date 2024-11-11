package services;

import models.Video;
import models.VideoList;

import javax.inject.Inject;
import java.util.ArrayList;
import java.util.Arrays;
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
     * Extracts the tags from the video description
     * @author Ryane
     * @param video The video from which to extract tags.
     * @return A list of hashtags found in the video's description.
     */
    public static List<String> getTagsFromDescription(Video video) {
        return Arrays.stream(video.getDescription().split("\\s+"))
                .filter(word -> word.startsWith("#"))
                .map(word -> word.replaceAll("[^#\\w]", ""))
                .collect(Collectors.toList());
    }

    /**
     * get videos by the video
     * @author Ryane
     * @param keywords   to use for the video search.
     * @param maxResults The maximum number of results to retrieve.
     * @param tagToCheck The specific tag to check within the video descriptions (optional).
     * @return A CompletableFuture that will contain a VideoList of search results.
     */
    public CompletableFuture<VideoList> getVideoWithTags(String keywords, Long maxResults, String tagToCheck) {
        return youtubeService.searchResults(keywords, maxResults)
                .thenApply(videoList -> {
                    // Filter the list to include only videos with the specific tag
                    List<Video> filteredVideos = videoList.getVideoList().stream()
                            .filter(video -> getTagsFromDescription(video).contains(tagToCheck))
                            .collect(Collectors.toList());
                    return new VideoList(filteredVideos);
                });
    }

}

