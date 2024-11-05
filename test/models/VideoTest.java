package models;

import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

public class VideoTest {
    @Test
    public void testVideoGetters() {
        final String id = "1";
        final String title = "2";
        final String description = "3";
        final String channelId = "4";
        final String channelName = "5";
        final String thumbnailUrl = "6";
        Video video = new Video(id, title, description, channelId, channelName, thumbnailUrl);

        assertEquals(id, video.getId());
        assertEquals(title, video.getTitle());
        assertEquals(description, video.getDescription());
        assertEquals(channelId, video.getChannelId());
        assertEquals(channelName, video.getChannelName());
        assertEquals(thumbnailUrl, video.getThumbnailUrl());
        assertNotNull(video.getFleschReadingEaseScore());
    }
}
