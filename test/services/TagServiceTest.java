package services;

import models.Video;
import models.VideoList;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import services.TagService;
import services.YoutubeService;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.CompletableFuture;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.when;

public class TagServiceTest {

    @Mock
    private YoutubeService youtubeService;

    private TagService tagService;

    @BeforeEach
    public void setUp() {
        MockitoAnnotations.openMocks(this);
        tagService = new TagService(youtubeService);
    }

    @Test
    public void testGetVideoWithTags_WithMatchingTags() {
        // Mock a list of videos where tags include "tag1"
        Video video1 = new Video("id1", "Title 1", "Description 1", "channel1", "Channel Name 1", "http://thumbnail.url", Arrays.asList("tag1", "tag2"));
        Video video2 = new Video("id2", "Title 2", "Description 2", "channel2", "Channel Name 2", "http://thumbnail2.url", Arrays.asList("tag3"));
        VideoList videoList = new VideoList(Arrays.asList(video1, video2));

        when(youtubeService.searchResults(any(), anyLong())).thenReturn(CompletableFuture.completedFuture(videoList));

        CompletableFuture<List<Video>> result = tagService.getVideoWithTags("test", 10L, "tag1");
        List<Video> filteredVideos = result.join();

        assertEquals(1, filteredVideos.size());
        assertEquals("id1", filteredVideos.get(0).getId());
    }

    @Test
    public void testGetVideoWithTags_NoMatchingTags() {
        // Mock a list of videos where no tags match "tag1"
        Video video1 = new Video("id1", "Title 1", "Description 1", "channel1", "Channel Name 1", "http://thumbnail.url", Arrays.asList("tag2"));
        Video video2 = new Video("id2", "Title 2", "Description 2", "channel2", "Channel Name 2", "http://thumbnail2.url", Arrays.asList("tag3"));
        VideoList videoList = new VideoList(Arrays.asList(video1, video2));

        when(youtubeService.searchResults(any(), anyLong())).thenReturn(CompletableFuture.completedFuture(videoList));

        CompletableFuture<List<Video>> result = tagService.getVideoWithTags("test", 10L, "tag1");
        List<Video> filteredVideos = result.join();

        assertEquals(0, filteredVideos.size());
    }

    @Test
    public void testGetVideoWithTags_EmptyVideoList() {
        // Mock an empty list of videos
        VideoList videoList = new VideoList(Collections.emptyList());

        when(youtubeService.searchResults(any(), anyLong())).thenReturn(CompletableFuture.completedFuture(videoList));

        CompletableFuture<List<Video>> result = tagService.getVideoWithTags("test", 10L, "tag1");
        List<Video> filteredVideos = result.join();

        assertEquals(0, filteredVideos.size());
    }

    @Test
    public void testGetVideoWithTags_NullVideoList() {
        // Mock a null video list
        when(youtubeService.searchResults(any(), anyLong())).thenReturn(CompletableFuture.completedFuture(null));

        CompletableFuture<List<Video>> result = tagService.getVideoWithTags("test", 10L, "tag1");

        List<Video> filteredVideos = result.join();

        // Ensure the filtered list is empty
        assertEquals(0, filteredVideos.size());
    }
}
