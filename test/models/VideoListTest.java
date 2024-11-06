package models;

import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.assertEquals;

/**
 * @author Laurent
 * Test class to verify the behavior of VideoList model
 */
public class VideoListTest {
    @Test
    public void TestVideoList() {
        List<Video> videos = new ArrayList<Video>();

        VideoList videoList = new VideoList(videos);
        assertEquals(videos, videoList.getVideoList());
    }
}
