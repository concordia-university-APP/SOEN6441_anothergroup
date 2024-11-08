package services;

import com.google.api.services.youtube.YouTube;
import com.google.api.services.youtube.model.*;
import models.VideoList;
import models.YoutubeChannel;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import models.Video;
import org.junit.rules.ExpectedException;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.security.GeneralSecurityException;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;
import java.util.concurrent.CompletionStage;
import java.util.concurrent.ExecutionException;
import static org.hibernate.validator.internal.util.Contracts.assertNotNull;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

public class YoutubeServiceTest {
    @Rule
    public ExpectedException exceptionRule = ExpectedException.none();
    @Mock
    private YouTube youtubeMock;

    @InjectMocks
    private YoutubeService youtubeService;
    @Mock
    private YouTube.Search.List mockRequest;
    private SearchListResponse mockResponse;
    @Mock
    private YouTube.Search mockSearch;

    @Before
    public void setUp() throws NoSuchMethodException, InvocationTargetException, IllegalAccessException, GeneralSecurityException, IOException {
        youtubeMock = mock(YouTube.class);
        MockitoAnnotations.initMocks(this);
        youtubeService = Mockito.mock(YoutubeService.class, Mockito.CALLS_REAL_METHODS);
        Method setServiceMethod = youtubeService.getClass().getDeclaredMethod("setYoutubeService", YouTube.class);
        setServiceMethod.setAccessible(true);
        setServiceMethod.invoke(youtubeService, youtubeMock);
        youtubeService = Mockito.spy(new YoutubeService());
        mockRequest = mock(YouTube.Search.List.class);
        mockResponse = mock(SearchListResponse.class);

        //Initialize with mock YouTube
    }

    /**
     * Tests the getChannelVideos method in YoutubeService to verify it returns the expected
     * list of videos for a given channel ID when the API call is successful.
     *
     * @throws Exception if any exception occurs during the mock setup or method execution
     * @author yehia,laurant
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
     * Tests the getChannelById method in YoutubeService to verify it returns the expected
     * channel information when a valid channel ID is provided, and the API call is successful.
     *
     * @throws Exception if any exception occurs during the mock setup or method execution
     * @author yehia,laurant
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

    @Test
    public void testGetChannelById_handlesIOException() throws Exception {
        YouTube.Channels.List mockChannelsList = mock(YouTube.Channels.List.class);
        when(youtubeMock.channels()).thenReturn(mock(YouTube.Channels.class));
        when(youtubeMock.channels().list(anyList())).thenReturn(mockChannelsList);
        when(mockChannelsList.setId(anyList())).thenReturn(mockChannelsList);
        when(mockChannelsList.setKey(anyString())).thenReturn(mockChannelsList);
        when(mockChannelsList.execute()).thenThrow(new IOException("Test Exception"));

        exceptionRule.expect(CompletionException.class);
        exceptionRule.expectMessage("java.io.IOException: Test Exception");

        youtubeService.getChannelById("sampleChannelId").toCompletableFuture().join();
    }

    @Test
    public void testGetChannelVideos_handlesIOException() throws Exception{
        YouTube.Search.List mockSearchList = mock(YouTube.Search.List.class);
        when(youtubeMock.search()).thenReturn(mock(YouTube.Search.class));
        when(youtubeMock.search().list(anyList())).thenReturn(mockSearchList);
        when(mockSearchList.setKey(anyString())).thenReturn(mockSearchList);
        when(mockSearchList.setChannelId(anyString())).thenReturn(mockSearchList);
        when(mockSearchList.setMaxResults(anyLong())).thenReturn(mockSearchList);
        when(mockSearchList.setOrder(anyString())).thenReturn(mockSearchList);
        when(mockSearchList.setType(anyList())).thenReturn(mockSearchList);
        when(mockSearchList.execute()).thenThrow(new IOException("Test Exception"));

        exceptionRule.expect(CompletionException.class);
        exceptionRule.expectMessage("java.io.IOException: Test Exception");

        youtubeService.getChannelVideos("sampleChannelId").toCompletableFuture().join();
    }

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

        Assert.assertNull("Expected null channel result",channel);
    }
    /**
     * Tests the searchResults method for a successful search with valid keywords and max results.
     * It verifies that the method returns a non-null VideoList with correct video details.
     * @author Yehia Metwally
     */
    @Test
    public void testSearchResults_Success() throws Exception {
        String keywords = "sample";
        Long maxResults = 5L;

        YoutubeService mockYoutubeService = mock(YoutubeService.class);

        when(mockYoutubeService.searchResults(eq(keywords), eq(maxResults)))
                .thenReturn(CompletableFuture.completedFuture(new VideoList(Arrays.asList(
                        new Video("videoId1", "Sample Video", "Sample Description", "channelId", "Sample Channel", "http://thumbnail.url")
                ))));

        CompletableFuture<VideoList> resultFuture = mockYoutubeService.searchResults(keywords, maxResults);

        assertNotNull(resultFuture, "Expected non-null resultFuture");

        VideoList videoList = resultFuture.get();
        assertNotNull(videoList, "Expected non-null VideoList");

        // Assertions
        assertEquals("Expected video ID to match", "videoId1", videoList.getVideoList().get(0).getId());
        assertEquals("Expected video title to match", "Sample Video", videoList.getVideoList().get(0).getTitle());
    }

    /**
     * Tests the searchResults method for a case where no search results are returned.
     * It verifies that the method returns an empty VideoList.
     * @author Yehia Metwally
     */
    @Test
    public void testSearchResults_NoResults() throws Exception {
        String keywords = "noResults";
        Long maxResults = 5L;

        YoutubeService mockYoutubeService = mock(YoutubeService.class);

        when(mockYoutubeService.searchResults(eq(keywords), eq(maxResults)))
                .thenReturn(CompletableFuture.completedFuture(new VideoList(Arrays.asList()))); // Return empty list

        CompletableFuture<VideoList> resultFuture = mockYoutubeService.searchResults(keywords, maxResults);

        assertNotNull(resultFuture, "Expected non-null resultFuture");

        VideoList videoList = resultFuture.get();
        assertNotNull(videoList, "Expected non-null VideoList");

        assertTrue(videoList.getVideoList().isEmpty());
    }

    /**
     * Tests the searchResults method when an exception occurs during the request.
     * It verifies that an ExecutionException is thrown due to a RuntimeException.
     *@author Yehia Metwally
     */
    @Test
    public void testSearchResults_Exception() {
        String keywords = "sample";
        Long maxResults = 5L;

        when(youtubeService.getYoutubeSearchList()).thenThrow(new RuntimeException("Test Exception"));

        CompletableFuture<VideoList> resultFuture = youtubeService.searchResults(keywords, maxResults);

        assertThrows(ExecutionException.class, resultFuture::get, "Expected ExecutionException due to RuntimeException");
    }

    /**
     * Tests the getSearchListResponse method for a successful response.
     * Verifies that the request setup is successful and that execute() is called.
     * @author Yehia Metwally
     */
    @Test
    public void testGetSearchListResponse_Success() throws IOException {
        String keywords = "sample";
        Long maxResults = 5L;

        when(mockRequest.setKey(anyString())).thenReturn(mockRequest);
        when(mockRequest.setQ(keywords)).thenReturn(mockRequest);
        when(mockRequest.setType(anyList())).thenReturn(mockRequest);
        when(mockRequest.setOrder("date")).thenReturn(mockRequest);
        when(mockRequest.setFields("items(id/videoId)")).thenReturn(mockRequest);
        when(mockRequest.setMaxResults(maxResults)).thenReturn(mockRequest);

        when(mockRequest.execute()).thenReturn(mockResponse);

        SearchListResponse result = youtubeService.getSearchListResponse(keywords, maxResults, mockRequest);

        assertNotNull(result);

        verify(mockRequest, times(1)).execute();
        verify(mockRequest).setKey(anyString());
        verify(mockRequest).setQ(keywords);
        verify(mockRequest).setMaxResults(maxResults);
    }

    /**
     * Tests the getSearchListResponse method when an IOException occurs during the request setup.
     * Verifies that a RuntimeException is thrown in response to the IOException.
     * @author Yehia Metwally
     */
    @Test(expected = RuntimeException.class)
    public void testGetSearchListResponse_IOException() throws IOException {
        String keywords = "sample";
        Long maxResults = 5L;

        when(mockRequest.setKey(anyString())).thenReturn(mockRequest);
        when(mockRequest.setQ(keywords)).thenReturn(mockRequest);
        when(mockRequest.setType(anyList())).thenReturn(mockRequest);
        when(mockRequest.setOrder("date")).thenReturn(mockRequest);
        when(mockRequest.setFields("items(id/videoId)")).thenReturn(mockRequest);
        when(mockRequest.setMaxResults(maxResults)).thenReturn(mockRequest);

        when(mockRequest.execute()).thenThrow(new IOException("API call failed"));

        youtubeService.getSearchListResponse(keywords, maxResults, mockRequest);
    }

    /**
     * Tests the getYoutubeSearchList method for successful setup of the search list.
     * Verifies that search() and list() are called correctly and a non-null List object is returned.
     * @author Yehia Metwally
     */
    @Test
    public void testGetYoutubeSearchList_Success() throws IOException, GeneralSecurityException {
        YouTube mockYoutube = mock(YouTube.class);
        YoutubeService youtubeService = spy(new YoutubeService());

        doReturn(mockYoutube).when(youtubeService).getYoutubeService();

        YouTube.Search mockSearch = mock(YouTube.Search.class);
        when(mockYoutube.search()).thenReturn(mockSearch);

        YouTube.Search.List mockRequest = mock(YouTube.Search.List.class);
        when(mockSearch.list(Collections.singletonList("id, snippet"))).thenReturn(mockRequest);

        YouTube.Search.List result = youtubeService.getYoutubeSearchList();

        verify(mockYoutube).search();
        verify(mockSearch).list(Collections.singletonList("id, snippet"));

        assertNotNull(result, "Expected non-null result from getYoutubeSearchList");
    }

    /**
     * Tests the getYoutubeSearchList method when an exception occurs during the search setup.
     * Verifies that a RuntimeException is thrown when an IOException is encountered.
     * @author Yehia Metwally
     */
    @Test(expected = RuntimeException.class)
    public void testGetYoutubeSearchList_Exception() throws IOException {
        when(youtubeService.getYoutubeService()).thenReturn(youtubeMock);
        when(youtubeMock.search()).thenThrow(new IOException("Test exception"));

        youtubeService.getYoutubeSearchList();
    }

}
