package services;

import models.Video;
import models.VideoList;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

/**
 * Unit tests for the TagService class, functionality for extracting tags
 * from video descriptions and retrieving videos based on specific tags.
 */
public class TagServiceTest {

    private TagService tagService;
    private YoutubeService mockYoutubeService;

    /**
     * Sets up the necessary mock objects
     * @author Ryane
     */
    @Before
    public void setUp() {
        mockYoutubeService = mock(YoutubeService.class);
        tagService = new TagService(mockYoutubeService);
    }

    /**
     * Tests the getTagFromDescription method when the description contains multiple hashtags.
     * @author Ryane
     */
    @Test
    public void testGetTagsFromDescriptionWithMultipleHashtags() {
        Video video = new Video("videoId1", "Title", "This is a test #tag1 and #tag2", "channelId", "Channel Title", "thumbnailUrl");
        List<String> tags = TagService.getTagsFromDescription(video);
        assertEquals(2, tags.size());
        assertTrue(tags.contains("#tag1"));
        assertTrue(tags.contains("#tag2"));
    }

    /**
     * Tests the getTagFromDescription method when the description contains no hashtags.
     * @author Ryane
     */
    @Test
    public void testGetTagsFromDescriptionWithNoHashtags() {
        Video video = new Video("videoId2", "Title", "This is a test without hashtags", "channelId", "Channel Title", "thumbnailUrl");
        List<String> tags = TagService.getTagsFromDescription(video);
        assertTrue(tags.isEmpty());
    }

    /**
     * Tests the getTagFromDescription method when the description contains only one hashtag.
     * @author Ryane
     */
    @Test
    public void testGetTagsFromDescriptionWithOneHashtag() {
        Video video = new Video("videoId3", "Title", "This description #hashtag1 is cool", "channelId", "Channel Title", "thumbnailUrl");
        List<String> tags = TagService.getTagsFromDescription(video);
        assertEquals(1, tags.size());
        assertTrue(tags.contains("#hashtag1"));
    }

    /**
     * Tests the method when the description contains symbols but no hashtags.
     * @author Ryane
     */
    @Test
    public void testGetTagsFromDescriptionWithSymbolsNoHashtags() {
        Video video = new Video("videoId4", "Title", "This is a #description! test #tag123", "channelId", "Channel Title", "thumbnailUrl");
        List<String> tags = TagService.getTagsFromDescription(video);
        assertEquals(2, tags.size());
        assertTrue(tags.contains("#description"));
        assertTrue(tags.contains("#tag123"));
    }
    /**
     * Tests the method when the description is empty.
     * @author Ryane
     */
    @Test
    public void testGetTagsFromDescriptionWithEmptyDescription() {
        Video video = new Video("videoId5", "Title", "", "channelId", "Channel Title", "thumbnailUrl");
        List<String> tags = TagService.getTagsFromDescription(video);
        assertTrue(tags.isEmpty());
    }

    /**
     * Tests the method when the youtubeService call is successful.
     * Verifies that the method returns the expected list of videos.
     * @author Ryane
     */
    @Test
    public void testGetVideoWithTagsSuccess() {
        List<Video> mockVideoList = new ArrayList<>();
        mockVideoList.add(new Video("videoId1", "Title1", "Description1", "channelId1", "Channel Title1", "thumbnailUrl"));
        VideoList mockVideoListWrapper = new VideoList(mockVideoList);

        when(mockYoutubeService.searchResults("tag1", 10L)).thenReturn(CompletableFuture.completedFuture(mockVideoListWrapper));

        CompletableFuture<VideoList> result = tagService.getVideoWithTags("tag1", 10L, "tag1");

        result.thenAccept(videoList -> {
            assertNotNull(videoList);
            assertEquals(1, videoList.getVideoList().size());
            assertEquals("Title1", videoList.getVideoList().get(0).getTitle());
            assertEquals("videoId1", videoList.getVideoList().get(0).getId());
        });
    }

    /**
     * Tests the method when the youtubeService call fails.
     * Verifies that the method correctly handles errors and propagates the exception.
     * @author Ryane
     */
    @Test
    public void testGetVideoWithTagsFailure() {
        when(mockYoutubeService.searchResults("tag2", 10L)).thenReturn(CompletableFuture.failedFuture(new RuntimeException("Failed")));

        CompletableFuture<VideoList> failedResult = tagService.getVideoWithTags("tag2", 10L, "tag2");

        failedResult.exceptionally(ex -> {
            assertEquals("Failed", ex.getMessage());
            return null;
        });
    }
}
