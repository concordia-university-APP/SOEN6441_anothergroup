package services;

import models.Video;

import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

import static services.YoutubeService.searchResults;

/**
 * Author : Tanveer Reza
 * Version : 1
 * A service class for handling word-stats of a video
 */
public class StatisticsService {
    /**
     * @author Tanveer Reza
     * @param query the search terms for the video
     * @return frequency of all unique words from top 50 videos based on search query
     */
    public CompletableFuture<Map<String, Long>> getWordFrequency(String query) {
        return searchResults(query, 50L).thenApply(videos -> {
            List<String> titles = videos.getVideoList().stream()
                    .map(Video::getTitle) // Extract each title
                    .collect(Collectors.toList());

            List<String> listOfWordsFromTitles = extractAndNormalizeWords(titles);

            return countAndSortWordFrequencies(listOfWordsFromTitles)
                    .stream()
                    .collect(Collectors.toMap(
                            Map.Entry::getKey,
                            Map.Entry::getValue,
                            (e1, e2) -> e1, // In case of a key collision, keep the existing entry
                            LinkedHashMap::new // Use LinkedHashMap to preserve the order
                    ));
        });
    }

    /**
     * @author : Tanveer Reza
     * @param titles list of video titles
     * @return all words from a list of titles, normalize them and convert to lowercase for case handling
     */
    public List<String> extractAndNormalizeWords(List<String> titles) {
        return titles.stream()
                .flatMap(title -> Arrays.stream(title.split("\\W+"))) // Split titles into words
                .filter(word -> !word.isEmpty()) // Filter out empty words
                .map(String::toLowerCase)
                .collect(Collectors.toList()); // Collect to list
    }

    /**
     * @author : Tanveer Reza
     * @param words list of words gathered from titles
     * @return all unique words with their frequency, sorted by frequency and then alphabets
     */
    public List<Map.Entry<String, Long>> countAndSortWordFrequencies(List<String> words) {
        return getWordOccurences(words).entrySet().stream()
                .sorted(Map.Entry.<String, Long>comparingByValue().reversed()
                        .thenComparing(Map.Entry.comparingByKey()))
                .collect(Collectors.toList());
    }

    /**
     * @author : Tanveer Reza
     * @param words list of words gathered from titles
     * @return frequency of each word from a list of Words
     */
    public Map<String, Long> getWordOccurences(List<String> words) {
        return words.stream()
                .collect(Collectors.groupingBy(word -> word, Collectors.counting())); // Count occurrences
    }
}