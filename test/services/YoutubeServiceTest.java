package services;

import com.google.api.services.youtube.YouTube;
import com.google.api.services.youtube.model.*;
import models.YoutubeChannel;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import models.Video;
import org.junit.rules.ExpectedException;
import org.mockito.Mockito;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.security.GeneralSecurityException;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;
import java.util.concurrent.CompletionStage;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThrows;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * Test class for YoutubeService
 * @author Laurent Voisard, Tanveer Reza, Yehia
 */
public class YoutubeServiceTest {
    @Rule
    public ExpectedException exceptionRule = ExpectedException.none();
    private YouTube youtubeMock;

    private YoutubeService youtubeService;

    /**
     * Setup function to prepare tests
     * @throws NoSuchMethodException
     * @throws InvocationTargetException
     * @throws IllegalAccessException
     * @throws GeneralSecurityException
     * @throws IOException
     * @author Laurent Voisard, Tanveer Reza, Yehia
     */
    @Before
    public void setUp() throws NoSuchMethodException, InvocationTargetException, IllegalAccessException, GeneralSecurityException, IOException {
        youtubeMock = mock(YouTube.class);

        youtubeService = Mockito.mock(YoutubeService.class, Mockito.CALLS_REAL_METHODS);
        Method setServiceMethod = youtubeService.getClass().getDeclaredMethod("setYoutubeService", YouTube.class);
        setServiceMethod.setAccessible(true);
        setServiceMethod.invoke(youtubeService, youtubeMock);
        //Initialize with mock YouTube
    }

    /**
     * Test the getChannelVideos method with a basic case
     * @author Yehia Metwally
     */
    @Test
    public void testGetChannelVideos_Success() throws Exception {

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

        when(searchMock.list(Collections.singletonList("id,snippet"))).thenReturn(mockSearchList);
        when(mockSearchList.setKey(anyString())).thenReturn(mockSearchList);
        when(mockSearchList.setChannelId(anyString())).thenReturn(mockSearchList);
        when(mockSearchList.setMaxResults(anyLong())).thenReturn(mockSearchList);
        when(mockSearchList.setOrder(anyString())).thenReturn(mockSearchList);
        when(mockSearchList.setType(anyList())).thenReturn(mockSearchList);
        when(mockSearchList.execute()).thenReturn(mockResponse);

        CompletionStage<List<Video>> videosFuture = youtubeService.getChannelVideos("sampleChannelId");
        List<Video> videos = videosFuture.toCompletableFuture().join();

        assertEquals(1, videos.size());
        Video video = videos.get(0);
        assertEquals("sampleVideoId", video.getId());
        assertEquals("Sample Title", video.getTitle());
        assertEquals("Sample Description", video.getDescription());
    }

    /**
     * Test the getChannelVideos method with an empty query result list
     * The method should return an empty list in this case
     * @author Yehia Metwally
     */
    @Test
    public void testGetChannelById_Success() throws Exception {
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

        Assert.assertNotNull("Expected non-null channel result", channel);
        assertEquals("sampleChannelId", channel.getId());
        assertEquals("Sample Channel", channel.getTitle());
        assertEquals("Sample Channel Description", channel.getDescription());
        assertEquals("http://example.com/channel_image.jpg", channel.getThumbnailUrl());
    }

    /**
     * Test the getChannelById method handles IOException and throws a CompletionException
     * @throws Exception
     * @author Tanveer Reza
     */
    @Test
    public void testGetChannelById_handlesIOException() throws Exception {
        YouTube.Channels.List mockChannelsList = mock(YouTube.Channels.List.class);
        when(youtubeMock.channels()).thenReturn(mock(YouTube.Channels.class));
        when(youtubeMock.channels().list(anyList())).thenReturn(mockChannelsList);
        when(mockChannelsList.setId(anyList())).thenReturn(mockChannelsList);
        when(mockChannelsList.setKey(anyString())).thenReturn(mockChannelsList);
        when(mockChannelsList.execute()).thenThrow(new IOException("Test Exception"));

        assertThrows(CompletionException.class, () -> youtubeService.getChannelById("sampleChannelId").toCompletableFuture().join());
    }

    /**
     * Test the getChannelVideos method handles IOException and throws a CompletionException
     * @throws Exception
     * @author Tanveer Reza
     */
    @Test
    public void testGetChannelVideos_handlesIOException() throws Exception {
        YouTube.Search.List mockSearchList = mock(YouTube.Search.List.class);
        when(youtubeMock.search()).thenReturn(mock(YouTube.Search.class));
        when(youtubeMock.search().list(anyList())).thenReturn(mockSearchList);
        when(mockSearchList.setKey(anyString())).thenReturn(mockSearchList);
        when(mockSearchList.setChannelId(anyString())).thenReturn(mockSearchList);
        when(mockSearchList.setMaxResults(anyLong())).thenReturn(mockSearchList);
        when(mockSearchList.setOrder(anyString())).thenReturn(mockSearchList);
        when(mockSearchList.setType(anyList())).thenReturn(mockSearchList);
        when(mockSearchList.execute()).thenThrow(new IOException("Test Exception"));

        assertThrows(CompletionException.class, () -> youtubeService.getChannelVideos("sampleChannelId").toCompletableFuture().join());


    }

    /**
     * Test the getChannelById method returns null when the channel is empty
     * @throws Exception
     * @author Tanveer Reza
     */
    @Test
    public void testGetChannelById_ReturnsNullWhenChannelEmpty() throws Exception {
        ChannelListResponse mockResponse = new ChannelListResponse();
        mockResponse.setItems(Collections.emptyList());

        YouTube.Channels.List mockChannelsList = mock(YouTube.Channels.List.class);
        when(youtubeMock.channels()).thenReturn(mock(YouTube.Channels.class));
        when(youtubeMock.channels().list(anyList())).thenReturn(mockChannelsList);
        when(mockChannelsList.setId(anyList())).thenReturn(mockChannelsList);
        when(mockChannelsList.setKey(anyString())).thenReturn(mockChannelsList);
        when(mockChannelsList.execute()).thenReturn(mockResponse);

        CompletableFuture<YoutubeChannel> channelFuture = (CompletableFuture<YoutubeChannel>) youtubeService.getChannelById("sampleChannelId");
        YoutubeChannel channel = channelFuture.join();

        Assert.assertNull("Expected null channel result", channel);
    }
}
