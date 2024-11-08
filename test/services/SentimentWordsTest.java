package services;

import models.Video;
import org.junit.Test;
import static org.junit.Assert.*;
import java.util.Arrays;
import java.util.List;

/**
 * Unit tests for analyzing the sentiment words using the SentimentAnalyzer class.

 * This class verifies the functionality of sentiment analysis by testing various cases
 * with different sentiment compositions in the video descriptions.

 * The following tests are included
 *   testHappySentiment: Verifies analysis when the content is predominantly happy.
 *   testSadSentiment: Verifies analysis when the content is predominantly sad.
 *   testNeutralSentiment: Verifies analysis when the content is mixed, leading to neutral sentiment.
 *   testMixedSentiment: Verifies analysis with a balance of positive and negative sentiments.
 *   testEdgeCaseNoSentiment: [Placeholder for testing cases with no sentiment, if applicable].
 * @author Rumeysa Turkmen
 */
public class SentimentWordsTest {

    /**
     * Test positive sentiment majority
     * @author Rumeysa Turkmen
     */
    @Test
    public void testHappySentiment() {
        // Prepare a list of videos with happy descriptions
        Video video1 = new Video("1", "Video 1", "This is a happy and amazing video!", "123", "Channel 1", "url1");
        Video video2 = new Video("2", "Video 2", "I love this video, it's fantastic!", "124", "Channel 2", "url2");

        List<Video> videos = Arrays.asList(video1, video2);

        String result = SentimentAnalyzer.analyzeSentiment(videos);

        // Expecting happy sentiment since both videos are happy
        assertEquals(":-)", result);
    }

    /**
     * Test negative sentiment majority
     * @author Rumeysa Turkmen
     */
    @Test
    public void testSadSentiment() {
        // Prepare a list of videos with sad descriptions
        Video video1 = new Video("1", "Video 1", "This video makes me sad and terrible.", "123", "Channel 1", "url1");
        Video video2 = new Video("2", "Video 2", "I hate this video, it's awful!", "124", "Channel 2", "url2");

        List<Video> videos = Arrays.asList(video1, video2);

        String result = SentimentAnalyzer.analyzeSentiment(videos);

        // Expecting sad sentiment since both videos are sad
        assertEquals(":-(", result);
    }

    /**
     * Test neutral sentiment majority
     * @author Rumeysa Turkmen
     */
    @Test
    public void testNeutralSentiment() {
        // Prepare a list of videos with mixed sentiments
        Video video1 = new Video("1", "Video 1", "This is a fun video!", "123", "Channel 1", "url1");
        Video video2 = new Video("2", "Video 2", "This video is quite boring.", "124", "Channel 2", "url2");

        List<Video> videos = Arrays.asList(video1, video2);

        String result = SentimentAnalyzer.analyzeSentiment(videos);

        // Expecting neutral sentiment as the positive and negative counts are balanced
        assertEquals(":-|", result);
    }

    /**
     * Test mixed positive and negative sentiment majority
     * @author Rumeysa Turkmen
     */
    @Test
    public void testMixedSentiment() {
        // Prepare a list of videos with mixed happy and sad descriptions
        Video video1 = new Video("1", "Video 1", "This is a wonderful video!", "123", "Channel 1", "url1");
        Video video2 = new Video("2", "Video 2", "This video is a disaster.", "124", "Channel 2", "url2");

        List<Video> videos = Arrays.asList(video1, video2);

        String result = SentimentAnalyzer.analyzeSentiment(videos);

        // Expecting neutral sentiment as the positive and negative counts are close
        assertEquals(":-|", result);
    }

    /**
     * Test no videos
     * @author Rumeysa Turkmen
     */
    @Test
    public void testEdgeCaseNoSentimentWords() {
        // Prepare a list of videos with no sentiment words
        Video video1 = new Video("1", "Video 1", "Just a regular video description.", "123", "Channel 1", "url1");

        List<Video> videos = Arrays.asList(video1);

        String result = SentimentAnalyzer.analyzeSentiment(videos);

        // Expecting neutral sentiment as no sentiment words are present
        assertEquals(":-|", result);
    }

    /**
     * Required for 100% test coverage
     * @author Rumeysa Turkmen
     */
    @Test
    public void testInitializeSentimentWords() {
        SentimentWords words = new SentimentWords();

        assertNotNull(words);
    }
}
