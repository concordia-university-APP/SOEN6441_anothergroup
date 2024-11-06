package services;


import models.Video;
import models.VideoList;
import org.junit.Before;
import org.junit.Test;
import org.mockito.MockedStatic;
import org.mockito.Mockito;

import java.util.*;
import java.util.concurrent.CompletableFuture;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.*;

/**
 * @author Tanveer Reza
 * Version : 1
 * Unit tests for statistic service class
 */
public class StatisticsServiceTest {

    private StatisticsService statisticsService;
    private YoutubeService youtubeService;
    private VideoList sampleVideoList;
    private Map<String, Long> expectedWordFrequency;

    @Before
    public void setUp() {
        youtubeService = Mockito.mock(YoutubeService.class);
        statisticsService = new StatisticsService(youtubeService);

            // Create mock Video objects with complete data
            Video video1 = new Video("1", "Java programming tutorial", "Learn Java programming.", "101", "Java Channel", "http://example.com/thumb1.jpg");
            Video video2 = new Video("2", "Java and Python comparison", "Comparing Java and Python.", "102", "Comparison Channel", "http://example.com/thumb2.jpg");
            Video video3 = new Video("3", "Introduction to Java programming", "An intro to Java programming.", "103", "Intro Channel", "http://example.com/thumb3.jpg");

            when(youtubeService.getVideo("Java programming tutorial"))
                    .thenReturn(CompletableFuture.completedFuture(video1));
            when(youtubeService.getVideo("Java and Python comparison"))
                    .thenReturn(CompletableFuture.completedFuture(video2));
            when(youtubeService.getVideo("Introduction to Java programming"))
                    .thenReturn(CompletableFuture.completedFuture(video3));

            // Create VideoList with mocked videos
            sampleVideoList = new VideoList(Arrays.asList(video1, video2, video3));

            // Prepare expected word frequency
            expectedWordFrequency = new LinkedHashMap<>();
            expectedWordFrequency.put("java", 3L);
            expectedWordFrequency.put("programming", 2L);
            expectedWordFrequency.put("tutorial", 1L);
            expectedWordFrequency.put("and", 1L);
            expectedWordFrequency.put("python", 1L);
            expectedWordFrequency.put("comparison", 1L);
            expectedWordFrequency.put("introduction", 1L);
            expectedWordFrequency.put("to", 1L);

    }

    @Test
    public void testGetWordFrequency_basicCase() {
            when(youtubeService.searchResults(eq("Java"), anyLong()))
                    .thenReturn(CompletableFuture.completedFuture(sampleVideoList));

            CompletableFuture<Map<String, Long>> wordFrequencyFuture = statisticsService.getWordFrequency("Java");
            Map<String, Long> wordFrequency = wordFrequencyFuture.join();

            assertEquals(expectedWordFrequency, wordFrequency);

    }

    @Test
    public void testGetWordFrequency_emptyQueryResults() {
            VideoList emptyVideoList = new VideoList(Collections.emptyList());
            when( youtubeService.searchResults(eq("NonexistentQuery"), anyLong()))
                    .thenReturn(CompletableFuture.completedFuture(emptyVideoList));

            CompletableFuture<Map<String, Long>> wordFrequencyFuture = statisticsService.getWordFrequency("NonexistentQuery");
            Map<String, Long> wordFrequency = wordFrequencyFuture.join();

            assertTrue(wordFrequency.isEmpty());

    }

    @Test
    public void testExtractAndNormalizeWords_basicCase() {
        List<String> titles = Arrays.asList("Java Programming", "Python Tutorial");
        List<String> words = statisticsService.extractAndNormalizeWords(titles);

        List<String> expected = Arrays.asList("java", "programming", "python", "tutorial");

        assertEquals(expected, words);
    }

    @Test
    public void testExtractAndNormalizeWords_handlesEmptyTitles() {
        List<String> titles = Arrays.asList("", "   ");
        List<String> words = statisticsService.extractAndNormalizeWords(titles);

        assertTrue(words.isEmpty());
    }

    @Test
    public void testExtractAndNormalizeWords_specialCharacters() {
        List<String> titles = Arrays.asList("Java & Python!", "Programming: Tips & Tricks");
        List<String> words = statisticsService.extractAndNormalizeWords(titles);

        List<String> expected = Arrays.asList("java", "python", "programming", "tips", "tricks");

        assertEquals(expected, words);
    }

    @Test
    public void testCountAndSortWordFrequencies_basicCase() {
        List<String> words = Arrays.asList("java", "programming", "java", "tutorial");
        List<Map.Entry<String, Long>> sortedFrequencies = statisticsService.countAndSortWordFrequencies(words);

        List<Map.Entry<String, Long>> expected = new ArrayList<>();
        expected.add(new AbstractMap.SimpleEntry<>("java", 2L));
        expected.add(new AbstractMap.SimpleEntry<>("programming", 1L));
        expected.add(new AbstractMap.SimpleEntry<>("tutorial", 1L));

        assertEquals(expected, sortedFrequencies);
    }

    @Test
    public void testCountAndSortWordFrequencies_tieBreakerAlphabetical() {
        List<String> words = Arrays.asList("java", "python", "java", "python");
        List<Map.Entry<String, Long>> sortedFrequencies = statisticsService.countAndSortWordFrequencies(words);

        List<Map.Entry<String, Long>> expected = new ArrayList<>();
        expected.add(new AbstractMap.SimpleEntry<>("java", 2L));
        expected.add(new AbstractMap.SimpleEntry<>("python", 2L));

        assertEquals(expected, sortedFrequencies);
    }

    @Test
    public void testCountAndSortWordFrequencies_emptyList() {
        List<Map.Entry<String, Long>> sortedFrequencies = statisticsService.countAndSortWordFrequencies(Collections.emptyList());

        assertTrue(sortedFrequencies.isEmpty());
    }

    @Test
    public void testGetWordOccurrences_basicCase() {
        List<String> words = Arrays.asList("java", "programming", "java", "tutorial");
        Map<String, Long> wordOccurrences = statisticsService.getWordOccurences(words);

        Map<String, Long> expected = new HashMap<>();
        expected.put("java", 2L);
        expected.put("programming", 1L);
        expected.put("tutorial", 1L);

        assertEquals(expected, wordOccurrences);
    }

    @Test
    public void testGetWordOccurrences_emptyList() {
        Map<String, Long> wordOccurrences = statisticsService.getWordOccurences(Collections.emptyList());

        assertTrue(wordOccurrences.isEmpty());
    }

    @Test
    public void testGetWordOccurrences_caseSensitivity() {
        List<String> words = Arrays.asList("Java", "java", "JAVA");
        Map<String, Long> wordOccurrences = statisticsService.getWordOccurences(words);

        Map<String, Long> expected = new HashMap<>();
        expected.put("java", 1L);
        expected.put("JAVA", 1L);
        expected.put("Java", 1L);

        assertEquals(expected, wordOccurrences);
    }

}
