package services;

import models.Video;
import models.VideoList;
import models.VideoSearch;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;


/**
 * Service for searching videos on YouTube and storing the search results for a user session.
 * @author Laurent Voisard
 */
@Singleton
public class SearchService {
    private int userSessionCounter = 0;
    private final long MAX_VIDEO_COUNT = 50;
    private final long MAX_SEARCHES_PER_SESSION = 10;
    private final HashMap<String, List<VideoSearch>> sessionVideoSearchList = new HashMap<>();
    private final YoutubeService youtubeService;

    /**
     * Constructor for the SearchService class.
     * author Laurent Voisard
     * @param youtubeService the YouTube service to use for fetching videos
     */
    @Inject
    public SearchService(YoutubeService youtubeService) {
        this.youtubeService = youtubeService;
    }

    /**
     * Indirection level to youtube service search, we store the search results in the correct user session search result list
     * We also handle if a search term has already been made, if so put it back to the top of the list without making a request
     * to the api. Otherwise request from the youtube api.
     * This method also performs sentiment analysis on the list of videos returned by the YouTube
     *  search. The sentiment is analyzed to determine the overall mood (happy, sad, or neutral)
     * based on the descriptions of the videos in the result. The result, including the sentiment
     *  analysis, is encapsulated in a {@link VideoSearch} object and added to the session's search list
     * to the api. Otherwise request from the youtube api
     * @author Laurent Voisard, Rumeysa Turkmen
     * @param keywords keywords of the search
     * @param sessionId user session id
     * @return list of the last 10 or less searches made
     */
    public CompletableFuture<List<VideoSearch>> searchKeywords(String keywords, String sessionId) {

        Optional<VideoSearch> existingSearch = getSessionSearchList(sessionId).stream()
                .filter(x -> x.getSearchTerms().equals(keywords))
                .findFirst();

        if (existingSearch.isPresent()) {
            System.out.println("Search already made");
            getSessionSearchList(sessionId).remove(existingSearch.get());
            getSessionSearchList(sessionId).add(0, existingSearch.get());
            return CompletableFuture.completedFuture(getSessionSearchList(sessionId));
        }

        return youtubeService.searchResults(keywords, MAX_VIDEO_COUNT).thenApply(results -> {


       // Analyze sentiment
       String overallSentiment = SentimentAnalyzer.analyzeSentiment(results.getVideoList());
       VideoSearch search = new VideoSearch(keywords, results, overallSentiment);
            getSessionSearchList(sessionId).add(0, search);

            if (getSessionSearchList(sessionId).size() > MAX_SEARCHES_PER_SESSION) {
                getSessionSearchList(sessionId).remove((int)MAX_SEARCHES_PER_SESSION);
            }

            return getSessionSearchList(sessionId);
        });
    }

    public CompletableFuture<List<VideoSearch>> updateSearches(String sessionId) {
        if (getSessionSearchList(sessionId).isEmpty()) return CompletableFuture.supplyAsync(() -> getSessionSearchList(sessionId));

        for(VideoSearch videoSearch: getSessionSearchList(sessionId)) {
            youtubeService.searchResults(videoSearch.getSearchTerms(), 10L).thenApplyAsync(results -> {
                List<Video> newVideos = results.getVideoList().stream()
                        .filter(x -> videoSearch.getResults().getVideoList().stream()
                                .map(Video::getId)
                                .noneMatch(y -> y.equals(x.getId())))
                        .collect(Collectors.toList());
                for(int i = newVideos.size() - 1; i > 0; i--) {
                    videoSearch.getResults().getVideoList().add(0, newVideos.get(i));
                    videoSearch.getResults().getVideoList().remove(videoSearch.getResults().getVideoList().size() - 1);
                }
                return videoSearch;
            });
        }
        return CompletableFuture.supplyAsync(() -> getSessionSearchList(sessionId));
    }

    /**
     * Get the search list for a user session, if it doesn't exist create a new one
     * @author Laurent Voisard
     * @param sessionId user session id
     * @return the video search list for this user
     */
    public List<VideoSearch> getSessionSearchList(String sessionId) {
        if (!sessionVideoSearchList.containsKey(sessionId)) {
            createSessionSearchList(sessionId);
        }
        return sessionVideoSearchList.get(sessionId);
    }

    /**
     * Create a new session search list
     * @author Laurent Voisard
     * @return created ID
     */
    public String createSessionSearchList() {
        int sessionId = userSessionCounter++;
        sessionVideoSearchList.put(String.valueOf(sessionId), new ArrayList<>());
        return String.valueOf(sessionId);
    }

    /**
     * Internal method to create a new session if a browser already had one
     * @author Laurent Voisard
     * @param sessionId user session id
     */
    private void createSessionSearchList(String sessionId) {

        userSessionCounter = Integer.parseInt(sessionId) + 1;
        sessionVideoSearchList.put(sessionId, new ArrayList<>());
    }

    /**
     * Retrieves a video by its ID.
     * @author Laurent Voisard
     * @param id the ID of the video to retrieve
     * @return a CompletableFuture containing the video with the specified ID
     */
    public CompletableFuture<Video> getVideoById(String id) {
        return youtubeService.getVideo(id);
    }

    /**
     * Retrieves a list of videos based on the search term. If the search term has already been made in the current session,
     * it returns the cached results. Otherwise, it makes a request to the YouTube API to get the results.
     * @author Tanveer Reza
     * @param keywords the search terms for the video
     * @param sessionId the user session id
     * @return a CompletableFuture containing the list of videos based on the search term
     */
    public CompletableFuture<VideoList> getVideosBySearchTerm(String keywords, String sessionId) {
        Optional<VideoSearch> existingSearch = getSessionSearchList(sessionId).stream()
                .filter(x -> x.getSearchTerms().equals(keywords))
                .findFirst();
        return existingSearch.map(videoSearch -> CompletableFuture.completedFuture(videoSearch.getResults()))
                .orElseGet(() -> youtubeService.searchResults(keywords, MAX_VIDEO_COUNT).thenApply(videoList -> videoList));
    }
}
