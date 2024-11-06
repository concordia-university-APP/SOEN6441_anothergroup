package models;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;

public class YoutubeChannelTest {
    private YoutubeChannel youtubeChannel;
    private final String TEST_ID = "sampleId";
    private final String TEST_TITLE = "Sample Channel";
    private final String TEST_DESCRIPTION = "Sample Description";
    private final String TEST_THUMBNAIL_URL = "http://example.com/thumbnail.jpg";
    private VideoList testVideoList;

    @BeforeEach
    void setUp() {
        // Assume testVideoList is initialized if VideoList is a complex object
        testVideoList = new VideoList(List.of(new Video[]{
                new Video("1", "1", "My pants were falling down so I added a belt.", "1", "1", "1"),
                new Video("2", "2", "The Lord of the Rings is an epic high fantasy novel by the English author and scholar JRR Tolkien. Set in Middle-earth, the story began as a sequel to Tolkien's 1937 children's book The Hobbit, but eventually developed into a much larger work. Written in stages between 1937 and 1949, The Lord of the Rings is one of the best-selling books ever written, with over 150 million copies sold.", "2", "2", "2"),
        }));
        youtubeChannel = new YoutubeChannel(TEST_ID, TEST_TITLE, TEST_DESCRIPTION, TEST_THUMBNAIL_URL, testVideoList);
    }

    @Test
    void testGetId() {
        assertEquals(TEST_ID, youtubeChannel.getId(), "Expected ID to match the initialized value");
    }

    @Test
    void testGetTitle() {
        assertEquals(TEST_TITLE, youtubeChannel.getTitle(), "Expected title to match the initialized value");
    }

    @Test
    void testGetDescription() {
        assertEquals(TEST_DESCRIPTION, youtubeChannel.getDescription(), "Expected description to match the initialized value");
    }

    @Test
    void testGetThumbnailUrl() {
        assertEquals(TEST_THUMBNAIL_URL, youtubeChannel.getThumbnailUrl(), "Expected thumbnail URL to match the initialized value");
    }

    @Test
    void testGetRecentVideos() {
        assertSame(testVideoList, youtubeChannel.getRecentVideos(), "Expected recent videos to match the initialized VideoList");
    }

    @Test
    void testConstructor() {
        // Test that all fields are correctly initialized
        YoutubeChannel channel = new YoutubeChannel(TEST_ID, TEST_TITLE, TEST_DESCRIPTION, TEST_THUMBNAIL_URL, testVideoList);

        assertEquals(TEST_ID, channel.getId(), "ID should match the input");
        assertEquals(TEST_TITLE, channel.getTitle(), "Title should match the input");
        assertEquals(TEST_DESCRIPTION, channel.getDescription(), "Description should match the input");
        assertEquals(TEST_THUMBNAIL_URL, channel.getThumbnailUrl(), "Thumbnail URL should match the input");
        assertSame(testVideoList, channel.getRecentVideos(), "VideoList should match the input");
    }
}
