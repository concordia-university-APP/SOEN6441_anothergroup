package services;

import models.Video;
import org.junit.Test;
import java.util.Arrays;
import java.util.List;
import java.util.Collections;
import java.util.concurrent.CompletableFuture;
import static org.junit.Assert.*;


/**
 * Unit tests for the SentimentAnalyzer class.
 * This test class verifies the behavior of the SentimentAnalyzer's sentiment analysis method
 * with different sets of video data. The analysis is expected to classify the sentiment
 * as happy, sad, or neutral based on the content of the provided video descriptions.
 *   testAnalyzeSentiment_Happy: Tests when the overall sentiment is positive.
 *   testAnalyzeSentiment_Sad: Tests when the overall sentiment is negative.
 *   testAnalyzeSentiment_Neutral: Tests when the overall sentiment is neutral.
 * @author Rumeysa Turkmen
 */
public class SentimentAnalyzerTest {


    /**
     * Test positive sentiment majority
     * @author Rumeysa Turkmen
     */
    @Test
    public void testAnalyzeSentiment_Happy() {
        // Mock data with more than 70% happy words
        Video video1 = new Video("id1", "Title 1", "This is an amazing, wonderful, happy day full of joy", "channelId1", "channelName1", "thumbnailUrl1");
        Video video2 = new Video("id2", "Title 2", "I love this fantastic and fun event :)", "channelId2", "channelName2", "thumbnailUrl2");

        List<Video> videos = Arrays.asList(video1, video2);

        String result = SentimentAnalyzer.analyzeSentiment(videos);
        assertEquals("The sentiment should be happy (:-))",":-)", result);
    }

    /**
     * Test negative sentiment majority
     * @author Rumeysa Turkmen
     */
    @Test
    public void testAnalyzeSentiment_Sad() {
        // Mock data with more than 70% sad words
        Video video1 = new Video("id3", "Title 1", "It was a terrible and sad experience full of anger", "channelId3", "channelName3", "thumbnailUrl3");
        Video video2 = new Video("id4", "Title 2", "The day was gloomy, and I felt broken and distant", "channelId4", "channelName4", "thumbnailUrl4");

        List<Video> videos = Arrays.asList(video1, video2);

        String result = SentimentAnalyzer.analyzeSentiment(videos);
        assertEquals("The sentiment should be sad (:-() ",":-(", result);
    }

    /**
     * Test neutral sentiment majority
     * @author Rumeysa Turkmen
     */
    @Test
    public void testAnalyzeSentiment_Neutral() {
        // Mock data with mixed or neutral words
        Video video1 = new Video("id5", "Title 1", "This day was okay, just a mix of different feelings", "channelId5", "channelName5", "thumbnailUrl5");
        Video video2 = new Video("id6", "Title 2", "It was neither happy nor sad, just average", "channelId6", "channelName6", "thumbnailUrl6");

        List<Video> videos = Arrays.asList(video1, video2);

        String result = SentimentAnalyzer.analyzeSentiment(videos);
        assertEquals("The sentiment should be neutral (:-|)",":-|", result);
    }

    /**
     * Test empty videos
     * @author Rumeysa Turkmen
     */
    @Test
    public void testAnalyzeSentiment_Empty() {
        // Create an empty list of videos
        List<Video> videos = Collections.emptyList();

        // Call the analyzeSentiment method
        String result = SentimentAnalyzer.analyzeSentiment(videos);

        // Assert that the result is neutral (:-|) when there are no videos
        assertEquals("The sentiment should be neutral when there are no videos.",":-|", result);
    }

    /**
     * No happy words
     * @author Rumeysa Turkmen
     */
    @Test
    public void testAnalyzeSentiment_NoHappySadWords() {
        // Mock data with no happy or sad words
        Video video1 = new Video("id7", "Title 1", "Just a regular video with no emotions expressed", "channelId7", "channelName7", "thumbnailUrl7");
        Video video2 = new Video("id8", "Title 2", "The content is just informative and neutral", "channelId8", "channelName8", "thumbnailUrl8");

        List<Video> videos = Arrays.asList(video1, video2);

        String result = SentimentAnalyzer.analyzeSentiment(videos);
        assertEquals("The sentiment should be neutral (:-|) when there are no happy or sad words.",":-|", result);
    }

    /**
     * No happy and sad words
     * @author Rumeysa Turkmen
     */
    @Test
    public void testAnalyzeSentiment_MixedSentiments() {
        // Mock data with mixed happy and sad words
        Video video1 = new Video("id9", "Title 1", "The experience was amazing but also challenging", "channelId9", "channelName9", "thumbnailUrl9");
        Video video2 = new Video("id10", "Title 2", "I felt a little sad but also excited", "channelId10", "channelName10", "thumbnailUrl10");
        Video video3 = new Video("id11", "Title 3", "A normal day with ups and downs", "channelId11", "channelName11", "thumbnailUrl11");

        List<Video> videos = Arrays.asList(video1, video2, video3);

        String result = SentimentAnalyzer.analyzeSentiment(videos);
        assertEquals("The sentiment should be neutral (:-|) when there is a mix of happy and sad words.",":-|", result);
    }

    /**
     * Sad majority
     * @author Rumeysa Turkmen
     */
    @Test
    public void testAnalyzeSentiment_SadMajority() {
        // Mock data where sad sentiments outnumber happy and neutral ones
        Video video1 = new Video("id7", "Title 1", "It was a terrible and sad experience", "channelId7", "channelName7", "thumbnailUrl7");
        Video video2 = new Video("id8", "Title 2", "The weather was dark and depressing", "channelId8", "channelName8", "thumbnailUrl8");
        Video video3 = new Video("id9", "Title 3", "I had a sad and gloomy day", "channelId9", "channelName9", "thumbnailUrl9");
        Video video4 = new Video("id10", "Title 4", "A fine day but nothing special bad sad", "channelId10", "channelName10", "thumbnailUrl10");  // Neutral video
        Video video5 = new Video("id11", "Title 5", "It was a and event horrible", "channelId11", "channelName11", "thumbnailUrl11"); // Happy video

        List<Video> videos = Arrays.asList(video1, video2, video3, video4, video5);

        String result = SentimentAnalyzer.analyzeSentiment(videos);

        // Since there are more sad videos than happy or neutral ones, the result should be sad (:-()
        assertEquals("The sentiment should be sad (:-() when there are more sad videos than happy or neutral ones.",":-(", result);
    }
}
