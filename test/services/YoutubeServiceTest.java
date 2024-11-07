package services;

import com.google.api.services.youtube.YouTube;
import com.google.api.services.youtube.model.*;
import models.YoutubeChannel;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import models.Video;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;

import static org.hibernate.validator.internal.util.Contracts.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

public class YoutubeServiceTest {
    private YouTube youtubeMock;

    private YoutubeService youtubeService;

    @BeforeEach
    void setUp() throws NoSuchMethodException, InvocationTargetException, IllegalAccessException {
        youtubeMock = mock(YouTube.class);

        youtubeService = new YoutubeService();
        Method setServiceMethod = youtubeService.getClass().getDeclaredMethod("setYoutubeService", YouTube.class);
        setServiceMethod.setAccessible(true);
        setServiceMethod.invoke(youtubeService, youtubeMock);
        //Initialize with mock YouTube
    }

    @Test
    void testGetChannelVideos_Success() throws Exception {

        SearchListResponse mockResponse = new SearchListResponse();

        // Mock search result with sample data
        SearchResult mockResult = new SearchResult();
        ResourceId mockResourceId = new ResourceId();
        mockResourceId.setVideoId("sampleVideoId");
        mockResult.setId(mockResourceId);

        SearchResultSnippet snippet = new SearchResultSnippet();
        snippet.setTitle("Sample Title");
        snippet.setDescription("Sample Description");
        snippet.setChannelId("sampleChannelId");
        snippet.setChannelTitle("Sample Channel");
        Thumbnail thumbnail = new Thumbnail();
        thumbnail.setUrl("http://example.com/image.jpg");
        ThumbnailDetails thumbnailDetails = new ThumbnailDetails();
        thumbnailDetails.setDefault(thumbnail);
        snippet.setThumbnails(thumbnailDetails);
        mockResult.setSnippet(snippet);

        mockResponse.setItems(Collections.singletonList(mockResult));


        YouTube.Search searchMock = mock(YouTube.Search.class);
        when(youtubeMock.search()).thenReturn(searchMock);
        YouTube.Search.List mockSearchList = mock(YouTube.Search.List.class);
        // Mock `youtube.search().list`
        when(searchMock.list(Collections.singletonList("id,snippet"))).thenReturn(mockSearchList);
        when(mockSearchList.setKey(anyString())).thenReturn(mockSearchList);
        when(mockSearchList.setChannelId(anyString())).thenReturn(mockSearchList);
        when(mockSearchList.setMaxResults(anyLong())).thenReturn(mockSearchList);
        when(mockSearchList.setOrder(anyString())).thenReturn(mockSearchList);
        when(mockSearchList.setType(anyList())).thenReturn(mockSearchList);
        when(mockSearchList.execute()).thenReturn(mockResponse);

        CompletionStage<List<Video>> videosFuture = youtubeService.getChannelVideos("sampleChannelId");
        List<Video> videos = videosFuture.toCompletableFuture().join();

        assertEquals(1, videos.size(), "Expected one video in the result");
        Video video = videos.get(0);
        assertEquals("sampleVideoId", video.getId());
        assertEquals("Sample Title", video.getTitle());
        assertEquals("Sample Description", video.getDescription());
    }

    @Test
    void testGetChannelById_Success() throws Exception {
        YouTube.Channels.List mockChannelsList = mock(YouTube.Channels.List.class);
        ChannelListResponse mockResponse = new ChannelListResponse();

        // Mock channel result with sample data
        Channel mockChannel = new Channel();
        ChannelSnippet snippet = new ChannelSnippet();
        snippet.setTitle("Sample Channel");
        snippet.setDescription("Sample Channel Description");
        Thumbnail thumbnail = new Thumbnail();
        thumbnail.setUrl("http://example.com/channel_image.jpg");
        ThumbnailDetails thumbnailDetails = new ThumbnailDetails();
        thumbnailDetails.setDefault(thumbnail);
        snippet.setThumbnails(thumbnailDetails);
        mockChannel.setSnippet(snippet);
        mockChannel.setId("sampleChannelId");

        mockResponse.setItems(Collections.singletonList(mockChannel));

        // Mock `youtube.channels().list`
        YouTube.Channels channels = mock(YouTube.Channels.class);
        when(youtubeMock.channels()).thenReturn(channels);
        YouTube.Channels.List channelLists = mock(YouTube.Channels.List.class);
        when(channels.list(anyList())).thenReturn(channelLists);

        when(channelLists.setId(anyList())).thenReturn(channelLists);
        when(channelLists.setKey(anyString())).thenReturn(channelLists);
        when(channelLists.execute()).thenReturn(mockResponse);

        CompletableFuture<YoutubeChannel> channelFuture = (CompletableFuture<YoutubeChannel>) youtubeService.getChannelById("sampleChannelId");
        YoutubeChannel channel = channelFuture.join();

        assertNotNull(channel, "Expected non-null channel result");
        assertEquals("sampleChannelId", channel.getId());
        assertEquals("Sample Channel", channel.getTitle());
        assertEquals("Sample Channel Description", channel.getDescription());
        assertEquals("http://example.com/channel_image.jpg", channel.getThumbnailUrl());
    }
}
