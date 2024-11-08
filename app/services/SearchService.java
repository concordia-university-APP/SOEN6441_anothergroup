package services;

import models.Video;
import models.VideoList;
import models.VideoSearch;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.util.*;
import java.util.concurrent.CompletableFuture;


@Singleton
public class SearchService {
    private int userSessionCounter = 0;
    private final long MAX_VIDEO_COUNT = 50;
    private final long MAX_SEARCHES_PER_SESSION = 10;
    private final HashMap<String, List<VideoSearch>> sessionVideoSearchList = new HashMap<>();
    private final YoutubeService youtubeService;

    @Inject
    public SearchService(YoutubeService youtubeService) {
        this.youtubeService = youtubeService;
    }

    /**
     * @author Laurent Voisard & Rumeysa Turkmen
     * Indirection level to youtube service search, we store the search results in the correct user session search result list
     * We also handle if a search term has already been made, if so put it back to the top of the list without making a request
     * to the api. Otherwise request from the youtube api
     * @param keywords keywords of the search
     * @param sessionId user session id
     * @return list of the last 10 or less searches made
     */
    public CompletableFuture<List<VideoSearch>> searchKeywords(String keywords, String sessionId) {
        Optional<VideoSearch> existingSearch = getSessionSearchList(sessionId).stream()
                .filter(x -> x.getSearchTerms().equals(keywords))
                .findFirst();

        if (existingSearch.isPresent()) {
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

    /**
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
     * @author Laurent Voisard
     * Create a new session search list
     * @return created ID
     */
    public String createSessionSearchList() {
        int sessionId = userSessionCounter++;
        sessionVideoSearchList.put(String.valueOf(sessionId), new ArrayList<>());
        return String.valueOf(sessionId);
    }

    /**
     * @author Laurent Voisard
     * Internal method to create a new session if a browser already had one
     * @param sessionId
     */
    private void createSessionSearchList(String sessionId) {

        userSessionCounter = Integer.parseInt(sessionId) + 1;
        sessionVideoSearchList.put(sessionId, new ArrayList<>());
    }

    public CompletableFuture<Video> getVideoById(String id) {
        return youtubeService.getVideo(id);
    }
}
