package services;

import models.Video;
import models.VideoList;

import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static services.YoutubeService.searchResults;

/**
 * Author : Tanveer Reza
 * Version : 1
 * A service class for handling word-stats of a video
 */
public class StatisticsService {
    /**
     * @Author: Tanveer Reza
     * @param query
     * @return frequency of all unique words from top 50 videos based on search query
     */
    public Map<String, Long> getWordFrequency(String query) {
        VideoList videos = searchResults(query, 50L);

        List<String> titles = videos.getVideoList().stream()
                .map(Video::getTitle) // Extract each title
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
    }

    /**
     * @Author: Tanveer Reza
     * @param titles
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
     * @Author: Tanveer Reza
     * @param words
     * @return all unique words with their frequency, sorted by frequency and then alphabets
     */
    public List<Map.Entry<String, Long>> countAndSortWordFrequencies(List<String> words) {
        return getWordOccurences(words).entrySet().stream()
                .sorted(Map.Entry.<String, Long>comparingByValue().reversed()
                        .thenComparing(Map.Entry.comparingByKey()))
                .collect(Collectors.toList());
    }

    /**
     * @Author: Tanveer Reza
     * @param words
     * @return frequency of each word from a list of Words
     */
    public Map<String, Long> getWordOccurences(List<String> words) {
        return words.stream()
                .collect(Collectors.groupingBy(word -> word, Collectors.counting())); // Count occurrences
    }
}
