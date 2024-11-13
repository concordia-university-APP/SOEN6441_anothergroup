package services;

import models.Video;
import models.VideoList;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;

import java.util.Arrays;
import java.util.List;
import java.util.concurrent.CompletableFuture;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

public class TagServiceTest {

    private TagService tagService;
    private YoutubeService mockYoutubeService;

    @Before
    public void setUp() {
        // Initialize the mock YoutubeService and TagService instances
        mockYoutubeService = mock(YoutubeService.class);
        tagService = new TagService(mockYoutubeService);
    }

    // Unit tests for the getTagsFromDescription method

    @Test
    public void testGetTagsFromDescriptionWithMultipleHashtags() {
        // Arrange
        Video video = new Video("videoId1", "Title", "This is a test #tag1 and #tag2", "channelId", "Channel Title", "thumbnailUrl");

        // Act
        List<String> tags = TagService.getTagsFromDescription(video);

        // Assert
        assertEquals(2, tags.size());
        assertTrue(tags.contains("#tag1"));
        assertTrue(tags.contains("#tag2"));
    }

    @Test
    public void testGetTagsFromDescriptionWithNoHashtags() {
        // Arrange
        Video video = new Video("videoId2", "Title", "This is a test without hashtags", "channelId", "Channel Title", "thumbnailUrl");

        // Act
        List<String> tags = TagService.getTagsFromDescription(video);

        // Assert
        assertTrue(tags.isEmpty());
    }

    @Test
    public void testGetTagsFromDescriptionWithOneHashtag() {
        // Arrange
        Video video = new Video("videoId3", "Title", "This description #hashtag1 is cool", "channelId", "Channel Title", "thumbnailUrl");

        // Act
        List<String> tags = TagService.getTagsFromDescription(video);

        // Assert
        assertEquals(1, tags.size());
        assertTrue(tags.contains("#hashtag1"));
    }

    @Test
    public void testGetTagsFromDescriptionWithSymbolsNoHashtags() {
        // Arrange
        Video video = new Video("videoId4", "Title", "This is a #description! test #tag123", "channelId", "Channel Title", "thumbnailUrl");

        // Act
        List<String> tags = TagService.getTagsFromDescription(video);

        // Assert
        assertEquals(2, tags.size());
        assertTrue(tags.contains("#description"));
        assertTrue(tags.contains("#tag123"));
    }

    @Test
    public void testGetTagsFromDescriptionWithEmptyDescription() {
        // Arrange
        Video video = new Video("videoId5", "Title", "", "channelId", "Channel Title", "thumbnailUrl");

        // Act
        List<String> tags = TagService.getTagsFromDescription(video);

        // Assert
        assertTrue(tags.isEmpty());
    }

    // Unit tests for the getVideoWithTags method

    @Test
    public void testGetVideoWithTags_FilteredByTag() {
        // Arrange
        String keywords = "sampleKeywords";
        String tagToCheck = "#targetTag";
        Video videoWithTag = new Video("1", "Title1", "Description with #targetTag", "channelId", "Channel1", "thumbnailUrl1");
        Video videoWithoutTag = new Video("2", "Title2", "Description without tag", "channelId", "Channel2", "thumbnailUrl2");

        VideoList videoList = mock(VideoList.class);
        when(videoList.getVideoList()).thenReturn(Arrays.asList(videoWithTag, videoWithoutTag));
        CompletableFuture<VideoList> futureVideos = CompletableFuture.completedFuture(videoList);

        when(mockYoutubeService.searchResults(keywords, 10L)).thenReturn(futureVideos);

        // Act
        List<Video> result = tagService.getVideoWithTags(keywords, 10L, tagToCheck).toCompletableFuture().join();

        // Assert
        assertEquals(1, result.size());
        assertEquals(videoWithTag, result.get(0));
    }

    @Test
    public void testGetVideoWithTags_NoMatchingTag() {
        // Arrange
        String keywords = "sampleKeywords";
        String tagToCheck = "#nonexistentTag";
        Video videoWithoutTag = new Video("1", "Title", "Description without tag", "channelId", "Channel", "thumbnailUrl");

        VideoList videoList = mock(VideoList.class);
        when(videoList.getVideoList()).thenReturn(Arrays.asList(videoWithoutTag));
        CompletableFuture<VideoList> futureVideos = CompletableFuture.completedFuture(videoList);

        when(mockYoutubeService.searchResults(keywords, 10L)).thenReturn(futureVideos);

        // Act
        List<Video> result = tagService.getVideoWithTags(keywords, 10L, tagToCheck).toCompletableFuture().join();

        // Assert
        assertTrue(result.isEmpty());
    }

    @Test
    public void testGetVideoWithTags_EmptyResults() {
        // Arrange
        String keywords = "sampleKeywords";
        String tagToCheck = "#targetTag";

        VideoList emptyVideoList = mock(VideoList.class);
        when(emptyVideoList.getVideoList()).thenReturn(Arrays.asList());
        CompletableFuture<VideoList> futureVideos = CompletableFuture.completedFuture(emptyVideoList);

        when(mockYoutubeService.searchResults(keywords, 10L)).thenReturn(futureVideos);

        // Act
        List<Video> result = tagService.getVideoWithTags(keywords, 10L, tagToCheck).toCompletableFuture().join();

        // Assert
        assertTrue(result.isEmpty());
    }
}
