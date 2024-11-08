package services;

import models.Video;
import org.junit.Test;
import static org.junit.Assert.*;
import java.util.Arrays;
import java.util.List;

/**
 * Unit tests for analyzing the sentiment words using the SentimentAnalyzer class.
 *
 * <p>
 * This class verifies the functionality of sentiment analysis by testing various cases
 * with different sentiment compositions in the video descriptions.
 * </p>
 *
 * <p>
 * The following tests are included:
 * <ul>
 *   <li>testHappySentiment: Verifies analysis when the content is predominantly happy.</li>
 *   <li>testSadSentiment: Verifies analysis when the content is predominantly sad.</li>
 *   <li>testNeutralSentiment: Verifies analysis when the content is mixed, leading to neutral sentiment.</li>
 *   <li>testMixedSentiment: Verifies analysis with a balance of positive and negative sentiments.</li>
 *   <li>testEdgeCaseNoSentiment: [Placeholder for testing cases with no sentiment, if applicable].</li>
 * </ul>
 * </p>
 *
 * @author Rumeysa Turkmen
 */
public class SentimentWordsTest {

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

    @Test
    public void testEdgeCaseNoSentimentWords() {
        // Prepare a list of videos with no sentiment words
        Video video1 = new Video("1", "Video 1", "Just a regular video description.", "123", "Channel 1", "url1");

        List<Video> videos = Arrays.asList(video1);

        String result = SentimentAnalyzer.analyzeSentiment(videos);

        // Expecting neutral sentiment as no sentiment words are present
        assertEquals(":-|", result);
    }
}
