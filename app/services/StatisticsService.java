package services;

import models.Video;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

/**
 * Author : Tanveer Reza
 * Version : 1
 * A service class for handling word-stats of a video
 */
@Singleton
public class StatisticsService {
    private final SearchService searchService;

    @Inject
    public StatisticsService(SearchService searchService) {
        this.searchService = searchService;
    }
    /**
     * @param query the search terms for the video
     * @return frequency of all unique words from top 50 videos based on search query
     * @author Tanveer Reza
     */
    public CompletableFuture<Map<String, Long>> getWordFrequency(String query, String sessionId) {
        return searchService.searchKeywords(query, sessionId,50L).thenApply(videos -> {
            List<String> titles = videos.stream()
                    .flatMap(videoSearch -> videoSearch.getResults().getVideoList().stream())
                    .map(Video::getTitle) // Extract each title
                    .collect(Collectors.toList());

            List<String> listOfWordsFromTitles = extractAndNormalizeWords(titles);

            return countAndSortWordFrequencies(listOfWordsFromTitles);
        });
    }

    /**
     * @param titles list of video titles
     * @return all words from a list of titles, normalize them and convert to lowercase for case handling
     * @author : Tanveer Reza
     */
    public List<String> extractAndNormalizeWords(List<String> titles) {
        return titles.stream()
                .flatMap(title -> Arrays.stream(title.split("\\W+"))) // Split titles into words
                .filter(word -> !word.isEmpty()) // Filter out empty words
                .map(String::toLowerCase)
                .collect(Collectors.toList()); // Collect to list
    }

    /**
     * @param words list of words gathered from titles
     * @return all unique words with their frequency, sorted by frequency and then alphabets
     * @author : Tanveer Reza
     */
    public Map<String, Long> countAndSortWordFrequencies(List<String> words) {
        return getWordOccurences(words).entrySet().stream()
                .sorted(Map.Entry.<String, Long>comparingByValue().reversed()
                        .thenComparing(Map.Entry.comparingByKey()))
                .collect(Collectors.collectingAndThen(
                        Collectors.toList(),
                        list -> {
                            Map<String, Long> result = new LinkedHashMap<>();
                            list.forEach(entry -> result.put(entry.getKey(), entry.getValue()));
                            return result;
                        }
                ));
    }

    /**
     * @param words list of words gathered from titles
     * @return frequency of each word from a list of Words
     * @author : Tanveer Reza
     */
    public Map<String, Long> getWordOccurences(List<String> words) {
        return words.stream()
                .collect(Collectors.groupingBy(word -> word, Collectors.counting())); // Count occurrences
    }
}
