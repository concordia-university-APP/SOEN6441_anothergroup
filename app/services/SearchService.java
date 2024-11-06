package services;

import models.Video;
import models.VideoSearch;

import javax.inject.Singleton;
import java.util.*;
import java.util.concurrent.CompletableFuture;


@Singleton
public class SearchService {
    private int userSessionCounter = 0;
    private final long MAX_RESULTS = 50;
    private final HashMap<String, List<VideoSearch>> sessionVideoSearchList = new HashMap<>();

    /**
     * Author: Laurent Voisard
     * Indirection level to youtube service search, we store the search results in the correct user session search result list
     * We also handle if a search term has already been made, if so put it back to the top of the list without making a request
     * to the api. Otherwise request from the youtube api
     * @param keywords keywords of the search
     * @param sessionId user session id
     * @return list of the last 10 or less searches made
     */
    public CompletableFuture<List<VideoSearch>> searchKeywords(String keywords, String sessionId, long displayCount) {
        Optional<VideoSearch> existingSearch = sessionVideoSearchList.get(sessionId).stream()
                .filter(x -> x.getSearchTerms().equals(keywords))
                .findFirst();

        if (existingSearch.isPresent()) {
            sessionVideoSearchList.get(sessionId).remove(existingSearch.get());
            sessionVideoSearchList.get(sessionId).add(0, existingSearch.get());
            return CompletableFuture.completedFuture(sessionVideoSearchList.get(sessionId));
        }

        return YoutubeService.searchResults(keywords, MAX_RESULTS).thenApply(results -> {

            VideoSearch search = new VideoSearch(keywords, results);
            sessionVideoSearchList.get(sessionId).add(0, search);

            if (sessionVideoSearchList.get(sessionId).size() > MAX_RESULTS) {
                sessionVideoSearchList.get(sessionId).remove((int)MAX_RESULTS);
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
        return YoutubeService.getVideo(id);
    }
}
