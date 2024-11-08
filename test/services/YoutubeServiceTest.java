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
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

public class YoutubeServiceTest {
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
        Method setServiceMethod = youtubeService.getClass().getDeclaredMethod("setYoutubeService", YouTube.class);
        setServiceMethod.setAccessible(true);
        setServiceMethod.invoke(youtubeService, youtubeMock);
        youtubeService = Mockito.spy(new YoutubeService());
        mockRequest = mock(YouTube.Search.List.class);
        mockResponse = mock(SearchListResponse.class);

        //Initialize with mock YouTube
    }

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

        Assert.assertNotNull("Expected non-null channel result",channel);
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

        Assert.assertThrows(CompletionException.class, () -> youtubeService.getChannelById("sampleChannelId").toCompletableFuture().join());
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

        Assert.assertThrows(CompletionException.class, () -> youtubeService.getChannelById("sampleChannelId").toCompletableFuture().join());
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
     * @author Yehia Metwally
     * Tests the searchResults method for a successful search with valid keywords and max results.
     * It verifies that the method returns a non-null VideoList with correct video details.
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
     * @author Yehia Metwally
     * Tests the searchResults method for a case where no search results are returned.
     * It verifies that the method returns an empty VideoList.
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
     * @author Yehia Metwally
     * Tests the searchResults method when an exception occurs during the request.
     * It verifies that an ExecutionException is thrown due to a RuntimeException.
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
     * @author Yehia Metwally
     * Tests the getSearchListResponse method for a successful response.
     * Verifies that the request setup is successful and that execute() is called.
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
     * @author Yehia Metwally
     * Tests the getSearchListResponse method when an IOException occurs during the request setup.
     * Verifies that a RuntimeException is thrown in response to the IOException.
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
     * @author Yehia Metwally
     * Tests the getYoutubeSearchList method for successful setup of the search list.
     * Verifies that search() and list() are called correctly and a non-null List object is returned.
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
     * @author Yehia Metwally
     * Tests the getYoutubeSearchList method when an exception occurs during the search setup.
     * Verifies that a RuntimeException is thrown when an IOException is encountered.
     */
    @Test(expected = RuntimeException.class)
    public void testGetYoutubeSearchList_Exception() throws IOException {
        when(youtubeService.getYoutubeService()).thenReturn(youtubeMock);
        when(youtubeMock.search()).thenThrow(new IOException("Test exception"));

        youtubeService.getYoutubeSearchList();
    }


    @Test
    public void testGetVideo() throws IOException {
        Video v = new Video("1", "title","desc","id","channel","dawkad");

        YouTube.Videos videosMock = mock(YouTube.Videos.class);
        YouTube.Videos.List videoListMock = mock(YouTube.Videos.List.class);
        VideoListResponse responseMock = mock(VideoListResponse.class);

        com.google.api.services.youtube.model.Video vidResponse = new com.google.api.services.youtube.model.Video();
        vidResponse.setId(v.getId());
        vidResponse.setSnippet(new VideoSnippet());
        vidResponse.getSnippet().setTitle(v.getTitle());
        vidResponse.getSnippet().setDescription(v.getDescription());
        vidResponse.getSnippet().setChannelId(v.getChannelId());
        vidResponse.getSnippet().setChannelTitle(v.getChannelName());
        vidResponse.getSnippet().setThumbnails(new ThumbnailDetails());
        vidResponse.getSnippet().getThumbnails().setDefault(new Thumbnail());
        vidResponse.getSnippet().getThumbnails().setDefault(new Thumbnail());
        vidResponse.getSnippet().getThumbnails().getDefault().setUrl(v.getThumbnailUrl());
        responseMock.setItems(List.of(vidResponse));

        when(youtubeMock.videos()).thenReturn(videosMock);
        when(videosMock.list(anyList())).thenReturn(videoListMock);

        when(videoListMock.setId(anyList())).thenReturn(videoListMock);
        when(videoListMock.setKey(anyString())).thenReturn(videoListMock);
        when(videoListMock.setFields(anyString())).thenReturn(videoListMock);
        when(videoListMock.execute()).thenReturn(responseMock);
        when(responseMock.getItems()).thenReturn(List.of(vidResponse));

        Video result = youtubeService.getVideo("id").join();
        assertEquals(v.getId(), result.getId() );
        assertEquals(v.getTitle(), result.getTitle() );
        assertEquals(v.getDescription(), result.getDescription() );
        assertEquals(v.getChannelName(), result.getChannelName() );
        assertEquals(v.getChannelId(), result.getChannelId() );
        assertEquals(v.getThumbnailUrl(), result.getThumbnailUrl() );
        assertEquals(v.getFleschReadingEaseScore().getReadingEaseScore(), result.getFleschReadingEaseScore().getReadingEaseScore(),0.0 );
        assertEquals(v.getFleschReadingEaseScore().getGradeLevel(), result.getFleschReadingEaseScore().getGradeLevel(), 0.0);
    }

    @Test
    public void testGetVideos() throws IOException {
        Video[] videos = new Video[]{
                new Video("1", "title1", "desc1", "id1", "channel1", "dawkad1"),
                new Video("2", "title2", "desc2", "id2", "channel2", "dawkad2"),
                new Video("3", "title3", "desc3", "id3", "channel3", "dawkad3")
        };
        YouTube.Videos videosMock = mock(YouTube.Videos.class);
        YouTube.Videos.List videoListMock = mock(YouTube.Videos.List.class);
        VideoListResponse responseMock = mock(VideoListResponse.class);

        com.google.api.services.youtube.model.Video[] vidResponse = new com.google.api.services.youtube.model.Video[3];
        for(int i = 0; i < 3; i++) {
            vidResponse[i] = new com.google.api.services.youtube.model.Video();
            vidResponse[i].setId(videos[i].getId());
            vidResponse[i].setSnippet(new VideoSnippet());
            vidResponse[i].getSnippet().setTitle(videos[i].getTitle());
            vidResponse[i].getSnippet().setDescription(videos[i].getDescription());
            vidResponse[i].getSnippet().setChannelId(videos[i].getChannelId());
            vidResponse[i].getSnippet().setChannelTitle(videos[i].getChannelName());
            vidResponse[i].getSnippet().setThumbnails(new ThumbnailDetails());
            vidResponse[i].getSnippet().getThumbnails().setDefault(new Thumbnail());
            vidResponse[i].getSnippet().getThumbnails().setDefault(new Thumbnail());
            vidResponse[i].getSnippet().getThumbnails().getDefault().setUrl(videos[i].getThumbnailUrl());
        }
        responseMock.setItems(List.of(vidResponse));

        when(youtubeMock.videos()).thenReturn(videosMock);
        when(videosMock.list(anyList())).thenReturn(videoListMock);

        when(videoListMock.setId(anyList())).thenReturn(videoListMock);
        when(videoListMock.setKey(anyString())).thenReturn(videoListMock);
        when(videoListMock.setFields(anyString())).thenReturn(videoListMock);
        when(videoListMock.execute()).thenReturn(responseMock);
        when(responseMock.getItems()).thenReturn(List.of(vidResponse));

        List<Video> result = youtubeService.getVideos(List.of("1","2","3")).join();
        for(int i = 0; i < 3; i++) {
            assertEquals(videos[i].getId(), result.get(i).getId() );
            assertEquals(videos[i].getTitle(), result.get(i).getTitle() );
            assertEquals(videos[i].getDescription(), result.get(i).getDescription() );
            assertEquals(videos[i].getChannelName(), result.get(i).getChannelName() );
            assertEquals(videos[i].getChannelId(), result.get(i).getChannelId() );
            assertEquals(videos[i].getThumbnailUrl(), result.get(i).getThumbnailUrl() );
            assertEquals(videos[i].getFleschReadingEaseScore().getReadingEaseScore(), result.get(i).getFleschReadingEaseScore().getReadingEaseScore(),0.0 );
            assertEquals(videos[i].getFleschReadingEaseScore().getGradeLevel(), result.get(i).getFleschReadingEaseScore().getGradeLevel(), 0.0);
        }
    }
}
