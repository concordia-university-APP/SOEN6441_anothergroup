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
 * @author Tanveer Reza
 * A service class for handling word-stats of a video
 */
@Singleton
public class StatisticsService {
    private final SearchService searchService;

    /**
     * @author Tanveer Reza
     * @param searchService the search service to use for fetching videos
     */
    @Inject
    public StatisticsService(SearchService searchService) {
        this.searchService = searchService;
    }

    /**
     * @param query the search terms for the video
     * @param sessionId current user session id
     * @return frequency of all unique words from description of top 50 videos based on search query
     * @author Tanveer Reza
     */
    public CompletableFuture<Map<String, Long>> getWordFrequency(String query, String sessionId) {
        return searchService.getVideosBySearchTerm(query, sessionId).thenApply(videos -> {
            List<String> description = videos.getVideoList().stream()
                    .map(Video::getDescription) // Extract each description
                    .collect(Collectors.toList());

            List<String> listOfWordsFromTitles = extractAndNormalizeWords(description);

            return countAndSortWordFrequencies(listOfWordsFromTitles);
        });
    }

    /**
     * @param description list of video titles
     * @return all words from a list of titles, normalize them and convert to lowercase for case handling
     * @author Tanveer Reza
     */
    public List<String> extractAndNormalizeWords(List<String> description) {
        return description.stream()
                .flatMap(title -> Arrays.stream(title.split("\\W+"))) // Split descriptions into words
                .filter(word -> !word.isEmpty()) // Filter out empty words
                .map(String::toLowerCase)
                .collect(Collectors.toList()); // Collect to list
    }

    /**
     * Counts the occurrences of each word in the provided list, sorts them by frequency in descending order,
     * and then by the word in alphabetical order. The sorted entries are then collected into a LinkedHashMap
     * to maintain the order.
     * @author Tanveer Reza
     * @param words the list of words to count and sort
     * @return a map of words and their frequencies, sorted by frequency and then alphabetically
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
     * @author Tanveer Reza
     */
    public Map<String, Long> getWordOccurences(List<String> words) {
        return words.stream()
                .collect(Collectors.groupingBy(word -> word, Collectors.counting())); // Count occurrences
    }
}
