package services;

import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.api.services.youtube.YouTube;
import com.google.api.services.youtube.model.SearchListResponse;
import com.google.api.services.youtube.model.SearchResult;
import com.typesafe.config.Config;
import com.typesafe.config.ConfigFactory;

import java.io.IOException;
import java.security.GeneralSecurityException;
import java.util.*;
import java.util.stream.Collectors;

public class YoutubeService {
    private static final Config config = ConfigFactory.load();
    private static final String API_KEY = config.getString("youtube.apiKey");
    private static final String APPLICATION_NAME = config.getString("youtube.applicationName");
    private static final JsonFactory JSON_FACTORY = JacksonFactory.getDefaultInstance();


    public static YouTube getService() throws GeneralSecurityException, IOException {
        return new YouTube.Builder(GoogleNetHttpTransport.newTrustedTransport(), JSON_FACTORY, null)
                .setApplicationName(APPLICATION_NAME)
                .build();
    }

    public static List<SearchResult> searchResults(String keywords, long limit) throws GeneralSecurityException, IOException{
        YouTube youtubeService = getService();
        YouTube.Search.List request = youtubeService.search().list("id, snippet");
        try {
            SearchListResponse response = request.setKey(API_KEY)
                    .setQ(keywords)
                    .setType("video")
                    .setOrder("date")
                    .setFields("items(id/kind,id/videoId,snippet/title,snippet/thumbnails/default/url)")
                    .setMaxResults(limit)
                    .execute();
        List<SearchResult> items = response.getItems();
        return items != null ? items : Collections.emptyList();
        } catch (IOException e) {
            throw new IOException("Error occurred while executing YouTube search: " + e.getMessage(), e);
        }
    }

    public static Map<String, Long> getWordFrequency(String query) {
        try {
            List<SearchResult> response = searchResults(query, 50L);

            List<String> titles = response.stream()
                    .map(item -> item.getSnippet().getTitle()) // Extract each title
                    .collect(Collectors.toList());

            List<String> listOfWordsFromTiles = extractAndNormalizeWords(titles);

            return countAndSortWordFrequencies(listOfWordsFromTiles)
                    .stream()
                    .collect(Collectors.toMap(
                            Map.Entry::getKey,
                            Map.Entry::getValue,
                            (e1, e2) -> e1, // In case of a key collision, keep the existing entry
                            LinkedHashMap::new // Use LinkedHashMap to preserve the order
                    ));
        } catch (GeneralSecurityException | IOException e) {
            e.printStackTrace();
            return Collections.emptyMap();
        }
    }

    private static List<String> extractAndNormalizeWords(List<String> titles) {
        return titles.stream()
                .flatMap(title -> Arrays.stream(title.split("\\W+"))) // Split titles into words
                .filter(word -> !word.isEmpty()) // Filter out empty words
                .map(String::toLowerCase)
                .collect(Collectors.toList()); // Collect to list
    }

    private static List<Map.Entry<String, Long>> countAndSortWordFrequencies(List<String> words) {
        return getWordOccurences(words).entrySet().stream()
                .sorted(Map.Entry.<String, Long>comparingByValue().reversed())
                .collect(Collectors.toList());
    }

    private static Map<String, Long> getWordOccurences(List<String> words) {
        return words.stream()
                .collect(Collectors.groupingBy(word -> word, Collectors.counting())); // Count occurrences
    }
}
