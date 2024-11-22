package services;

import models.Video;
import models.VideoList;
import org.junit.Before;
import org.junit.Test;

import java.util.*;
import java.util.concurrent.CompletableFuture;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.*;

/**
 * Unit tests for StatisticsService class
 * @author Tanveer Reza
 */
public class StatisticsServiceTest {

    private StatisticsService statisticsService;
    private SearchService searchService;
    private VideoList sampleVideoList;
    private Map<String, Long> expectedWordFrequency;

    /**
     * Set up the mock search service and sample video list
     *
     * @author Tanveer Reza
     */
    @Before
    public void setUp() {
        searchService = mock(SearchService.class);
        statisticsService = new StatisticsService(searchService);

        Video video1 = new Video("1", "Java programming tutorial", "Java programming tutorial", "101", "Java Channel", "http://example.com/thumb1.jpg", Arrays.asList("Java", "tutorial", "programming"));
        Video video2 = new Video("2", "Java and Python comparison", "Java and Python comparison", "102", "Comparison Channel", "http://example.com/thumb2.jpg", Arrays.asList("Java", "Python", "comparison"));
        Video video3 = new Video("3", "Introduction to Java programming", "Introduction to Java programming", "103", "Intro Channel", "http://example.com/thumb3.jpg", Arrays.asList("Java", "introduction", "programming"));

        sampleVideoList = new VideoList(Arrays.asList(video1, video2, video3));

        when(searchService.getVideosBySearchTerm(eq("Java"), anyString()))
                .thenReturn(CompletableFuture.completedFuture(sampleVideoList));

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

    /**
     * Test the getWordFrequency method with a basic case
     *
     * @author Tanveer Reza
     */
    @Test
    public void testGetWordFrequency_basicCase() {
        CompletableFuture<Map<String, Long>> wordFrequencyFuture = statisticsService.getWordFrequency("Java", "1");
        Map<String, Long> wordFrequency = wordFrequencyFuture.join();

        assertEquals(expectedWordFrequency, wordFrequency);
    }

    /**
     * Test the getWordFrequency method with an empty query result list
     * The method should return an empty map in this case
     *
     * @author Tanveer Reza
     */
    @Test
    public void testGetWordFrequency_emptyQueryResults() {
        when(searchService.getVideosBySearchTerm(eq("NonexistentQuery"), anyString()))
                .thenReturn(CompletableFuture.completedFuture(new VideoList(Collections.emptyList())));

        CompletableFuture<Map<String, Long>> wordFrequencyFuture = statisticsService.getWordFrequency("NonexistentQuery", "1");
        Map<String, Long> wordFrequency = wordFrequencyFuture.join();

        assertTrue(wordFrequency.isEmpty());
    }

    /**
     * Test the getWordFrequency method with an empty query
     * The method should return an empty map in this case
     *
     * @author Tanveer Reza
     */
    @Test
    public void testExtractAndNormalizeWords_basicCase() {
        List<String> titles = Arrays.asList("Java Programming", "Python Tutorial");
        List<String> words = statisticsService.extractAndNormalizeWords(titles);

        List<String> expected = Arrays.asList("java", "programming", "python", "tutorial");

        assertEquals(expected, words);
    }

    /**
     * Test the extractAndNormalizeWords method with empty titles
     * The method should return an empty list in this case
     *
     * @author Tanveer Reza
     */
    @Test
    public void testExtractAndNormalizeWords_handlesEmptyTitles() {
        List<String> titles = Arrays.asList("", "   ");
        List<String> words = statisticsService.extractAndNormalizeWords(titles);

        assertTrue(words.isEmpty());
    }

    /**
     * Test the extractAndNormalizeWords method with special characters
     * The method should remove special characters and convert words to lowercase
     *
     * @author Tanveer Reza
     */
    @Test
    public void testExtractAndNormalizeWords_specialCharacters() {
        List<String> titles = Arrays.asList("Java & Python!", "Programming: Tips & Tricks");
        List<String> words = statisticsService.extractAndNormalizeWords(titles);

        List<String> expected = Arrays.asList("java", "python", "programming", "tips", "tricks");

        assertEquals(expected, words);
    }

    /**
     * Test the countAndSortWordFrequencies method with a basic case
     * The method should return a map of word frequencies sorted by frequency in descending order
     *
     * @author Tanveer Reza
     */
    @Test
    public void testCountAndSortWordFrequencies_basicCase() {
        List<String> words = Arrays.asList("java", "programming", "java", "tutorial");
        Map<String, Long> sortedFrequencies = statisticsService.countAndSortWordFrequencies(words);

        Map<String, Long> expected = new LinkedHashMap<>();
        expected.put("java", 2L);
        expected.put("programming", 1L);
        expected.put("tutorial", 1L);

        assertEquals(expected, sortedFrequencies);
    }

    /**
     * Test the countAndSortWordFrequencies method with a tiebreaker
     * The method should sort words alphabetically if they have the same frequency count
     *
     * @author Tanveer Reza
     */
    @Test
    public void testCountAndSortWordFrequencies_tieBreakerAlphabetical() {
        List<String> words = Arrays.asList("java", "python", "java", "python");
        Map<String, Long> sortedFrequencies = statisticsService.countAndSortWordFrequencies(words);

        Map<String, Long> expected = new LinkedHashMap<>();
        expected.put("java", 2L);
        expected.put("python", 2L);

        assertEquals(expected, sortedFrequencies);
    }

    /**
     * Test the countAndSortWordFrequencies method with an empty list
     * The method should return an empty map in this case
     *
     * @author Tanveer Reza
     */
    @Test
    public void testCountAndSortWordFrequencies_emptyList() {
        Map<String, Long> sortedFrequencies = statisticsService.countAndSortWordFrequencies(Collections.emptyList());

        assertTrue(sortedFrequencies.isEmpty());
    }

    /**
     * Test the countAndSortWordFrequencies method with case sensitivity
     * The method should treat words with different cases as different words and count them separately
     *
     * @author Tanveer Reza
     */
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

    /**
     * Test the getWordOccurrences method with an empty list
     * The method should return an empty map in this case
     *
     * @author Tanveer Reza
     */
    @Test
    public void testGetWordOccurrences_emptyList() {
        Map<String, Long> wordOccurrences = statisticsService.getWordOccurences(Collections.emptyList());

        assertTrue(wordOccurrences.isEmpty());
    }

    /**
     * Test the getWordOccurrences method with case sensitivity
     * The method should treat words with different cases as different words and count them separately
     *
     * @author Tanveer Reza
     */
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

    /**
     * Test the getWordFrequency method with a key collision
     * The method should handle key collisions by summing the frequencies of the colliding keys together
     *
     * @author Tanveer Reza
     */
    @Test
    public void testGetWordFrequency_keyCollision() {
        Video video1 = new Video("1", "Java programming", "Java programming", "101", "Channel A", "http://example.com/thumb1.jpg", Arrays.asList("Java", "programming"));
        Video video2 = new Video("2", "Java Java", "Java Java", "102", "Channel B", "http://example.com/thumb2.jpg", Arrays.asList("Java"));

        VideoList videoList = new VideoList(Arrays.asList(video1, video2));

        when(searchService.getVideosBySearchTerm(eq("Java"), anyString()))
                .thenReturn(CompletableFuture.completedFuture(videoList));

        CompletableFuture<Map<String, Long>> wordFrequencyFuture = statisticsService.getWordFrequency("Java", "1");
        Map<String, Long> wordFrequency = wordFrequencyFuture.join();

        Map<String, Long> expected = new LinkedHashMap<>();
        expected.put("java", 3L);
        expected.put("programming", 1L);

        assertEquals(expected, wordFrequency);
    }
}