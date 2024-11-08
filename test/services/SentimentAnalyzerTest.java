package services;

import models.Video;
import org.junit.jupiter.api.Test;
import java.util.Arrays;
import java.util.List;
import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for the SentimentAnalyzer class.
 * This test class verifies the behavior of the SentimentAnalyzer's sentiment analysis method
 * with different sets of video data. The analysis is expected to classify the sentiment
 * as happy, sad, or neutral based on the content of the provided video descriptions.
 *   <li>testAnalyzeSentiment_Happy: Tests when the overall sentiment is positive.</li>
 *   <li>testAnalyzeSentiment_Sad: Tests when the overall sentiment is negative.</li>
 *   <li>testAnalyzeSentiment_Neutral: Tests when the overall sentiment is neutral.</li>
 * @author Rumeysa Turkmen
 */
public class SentimentAnalyzerTest {

    @Test
    public void testAnalyzeSentiment_Happy() {
        // Mock data with more than 70% happy words
        Video video1 = new Video("id1", "Title 1", "This is an amazing, wonderful, happy day full of joy", "channelId1", "channelName1", "thumbnailUrl1");
        Video video2 = new Video("id2", "Title 2", "I love this fantastic and fun event :)", "channelId2", "channelName2", "thumbnailUrl2");

        List<Video> videos = Arrays.asList(video1, video2);

        String result = SentimentAnalyzer.analyzeSentiment(videos);
        assertEquals(":-)", result, "The sentiment should be happy (:-))");
    }

    @Test
    public void testAnalyzeSentiment_Sad() {
        // Mock data with more than 70% sad words
        Video video1 = new Video("id3", "Title 1", "It was a terrible and sad experience full of anger", "channelId3", "channelName3", "thumbnailUrl3");
        Video video2 = new Video("id4", "Title 2", "The day was gloomy, and I felt broken and distant", "channelId4", "channelName4", "thumbnailUrl4");

        List<Video> videos = Arrays.asList(video1, video2);

        String result = SentimentAnalyzer.analyzeSentiment(videos);
        assertEquals(":-(", result, "The sentiment should be sad (:-() ");
    }

    @Test
    public void testAnalyzeSentiment_Neutral() {
        // Mock data with mixed or neutral words
        Video video1 = new Video("id5", "Title 1", "This day was okay, just a mix of different feelings", "channelId5", "channelName5", "thumbnailUrl5");
        Video video2 = new Video("id6", "Title 2", "It was neither happy nor sad, just average", "channelId6", "channelName6", "thumbnailUrl6");

        List<Video> videos = Arrays.asList(video1, video2);

        String result = SentimentAnalyzer.analyzeSentiment(videos);
        assertEquals(":-|", result, "The sentiment should be neutral (:-|)");
    }
}
