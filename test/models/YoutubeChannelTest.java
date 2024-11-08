package models;

import org.junit.Before;
import org.junit.Test;

import java.util.List;

import static org.junit.Assert.assertEquals;

/**
 * Unit tests for the YoutubeChannel class.
 * @author yehia metwally
 */
public class YoutubeChannelTest {
    private YoutubeChannel youtubeChannel;
    private final String TEST_ID = "sampleId";
    private final String TEST_TITLE = "Sample Channel";
    private final String TEST_DESCRIPTION = "Sample Description";
    private final String TEST_THUMBNAIL_URL = "http://example.com/thumbnail.jpg";
    private VideoList testVideoList;

    /**
     * Sets up the test environment before each test. Initializes the youtubeChannel
     * instance with predefined values.
     * @author yehia metwally,laurant
     */
    @Before
    public void setUp() {
        // Assume testVideoList is initialized if VideoList is a complex object
        testVideoList = new VideoList(List.of(new Video[]{
                new Video("1", "1", "My pants were falling down so I added a belt.", "1", "1", "1"),
                new Video("2", "2", "The Lord of the Rings is an epic high fantasy novel by the English author and scholar JRR Tolkien. Set in Middle-earth, the story began as a sequel to Tolkien's 1937 children's book The Hobbit, but eventually developed into a much larger work. Written in stages between 1937 and 1949, The Lord of the Rings is one of the best-selling books ever written, with over 150 million copies sold.", "2", "2", "2"),
        }));
        youtubeChannel = new YoutubeChannel(TEST_ID, TEST_TITLE, TEST_DESCRIPTION, TEST_THUMBNAIL_URL, testVideoList);
    }

    /**
     * Tests that getId() returns the correct ID value initialized in the setup.
     * @author laurant
     */
    @Test
    public void testGetId() {
        assertEquals("Expected ID to match the initialized value", TEST_ID, youtubeChannel.getId());
    }

    /**
     * Tests that getTitle() returns the correct title initialized in the setup.
     * @author laurant
     */
    @Test
    public void testGetTitle() {
        assertEquals("Expected title to match the initialized value", TEST_TITLE, youtubeChannel.getTitle());
    }

    /**
     * Tests that getDescription() returns the correct description initialized in the setup.
     * @author laurant
     */
    @Test
    public void testGetDescription() {
        assertEquals("Expected description to match the initialized value", TEST_DESCRIPTION, youtubeChannel.getDescription());
    }

    /**
     * Tests that getThumbnailUrl() returns the correct thumbnail URL initialized in the setup.
     * @author laurant
     */
    @Test
    public void testGetThumbnailUrl() {
        assertEquals("Expected thumbnail URL to match the initialized value", TEST_THUMBNAIL_URL, youtubeChannel.getThumbnailUrl());
    }

    /**
     * Tests that getRecentVideos() returns the VideoList initialized in the setup.
     * @author laurant
     */
    @Test
    public void testGetRecentVideos() {
        assertEquals("Expected recent videos to match the initialized VideoList", testVideoList, youtubeChannel.getRecentVideos());
    }

    /**
     * Tests that the YoutubeChannel constructor correctly initializes all fields
     * with the provided values.
     * @author yehia metwally, laurant
     */
    @Test
    public void testConstructor() {
        // Test that all fields are correctly initialized
        YoutubeChannel channel = new YoutubeChannel(TEST_ID, TEST_TITLE, TEST_DESCRIPTION, TEST_THUMBNAIL_URL, testVideoList);

        assertEquals("ID should match the input", TEST_ID, channel.getId());
        assertEquals("Title should match the input", TEST_TITLE, channel.getTitle());
        assertEquals("Description should match the input", TEST_DESCRIPTION, channel.getDescription());
        assertEquals("Thumbnail URL should match the input", TEST_THUMBNAIL_URL, channel.getThumbnailUrl());
        assertEquals("VideoList should match the input", testVideoList, channel.getRecentVideos());
    }
}
