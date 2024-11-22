package services;

import com.google.api.services.youtube.YouTube;
import com.google.api.services.youtube.model.*;
import models.VideoList;
import models.YoutubeChannel;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import models.Video;

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

import static org.junit.Assert.*;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThrows;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * Test class for YoutubeService
 * @author Laurent Voisard, Tanveer Reza, Yehia
 */
public class YoutubeServiceTest {
    private YouTube youtubeMock;

    private YoutubeService youtubeService;

    YouTube.Search mockSearch;
    YouTube.Search.List mockRequest;
    SearchListResponse mockResponse;

    /**
     * Setup function to prepare tests
     * @throws NoSuchMethodException if the method does not exist
     * @throws InvocationTargetException if the method cannot be invoked
     * @throws IllegalAccessException if the method cannot be accessed
     * @throws GeneralSecurityException if there is a security exception
     * @throws IOException if there is an IO exception
     * @author Laurent Voisard, Tanveer Reza, Yehia
     */
    @Before
    public void setUp() throws NoSuchMethodException, InvocationTargetException, IllegalAccessException, GeneralSecurityException, IOException {
        youtubeMock = mock(YouTube.class);
        youtubeService = new YoutubeService();
        Method setServiceMethod = youtubeService.getClass().getDeclaredMethod("setYoutubeService", YouTube.class);
        setServiceMethod.setAccessible(true);
        setServiceMethod.invoke(youtubeService, youtubeMock);


        mockSearch = mock(YouTube.Search.class);
        mockRequest = mock(YouTube.Search.List.class);
        mockResponse = mock(SearchListResponse.class);

        when(youtubeMock.search()).thenReturn(mockSearch);
        when(mockSearch.list(anyList())).thenReturn(mockRequest);

        when(mockRequest.setKey(anyString())).thenReturn(mockRequest);
        when(mockRequest.setQ(anyString())).thenReturn(mockRequest);
        when(mockRequest.setFields(anyString())).thenReturn(mockRequest);
        when(mockRequest.setChannelId(anyString())).thenReturn(mockRequest);
        when(mockRequest.setMaxResults(anyLong())).thenReturn(mockRequest);
        when(mockRequest.setOrder(anyString())).thenReturn(mockRequest);
        when(mockRequest.setType(anyList())).thenReturn(mockRequest);
        when(mockRequest.execute()).thenReturn(mockResponse);
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

        assertNotNull("Expected non-null channel result",channel);
        assertEquals("sampleChannelId", channel.getId());
        assertEquals("Sample Channel", channel.getTitle());
        assertEquals("Sample Channel Description", channel.getDescription());
        assertEquals("http://example.com/channel_image.jpg", channel.getThumbnailUrl());
    }

    /**
     * Test the getChannelById method handles IOException and throws a CompletionException
     * @throws Exception IOException
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

        Assert.assertThrows(CompletionException.class, () -> youtubeService.getChannelById("sampleChannelId").toCompletableFuture().join());
    }

    /**
     * Test the getChannelVideos method handles IOException and throws a CompletionException
     * @throws Exception IOException
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

        Assert.assertThrows(CompletionException.class, () -> youtubeService.getChannelVideos("sampleChannelId").toCompletableFuture().join());
    }

    /**
     * Test the getChannelById method returns null when the channel is empty
     * @throws Exception IOException
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
    /**
     * Tests the searchResults method for a successful search with valid keywords and max results.
     * It verifies that the method returns a non-null VideoList with correct video details.
     * @throws Exception error handling
     * @author Yehia Metwally
     */
    @Test
    public void testSearchResults_Success() throws Exception {
        String keywords = "sample";
        Long maxResults = 5L;


        com.google.api.services.youtube.model.SearchResult searchResponse = new com.google.api.services.youtube.model.SearchResult();
        searchResponse.setId(new ResourceId());
        searchResponse.getId().setVideoId("videoId1");

        when(mockSearch.list(anyList())).thenReturn(mockRequest);
        when(mockRequest.execute()).thenReturn(mockResponse);
        when(mockResponse.getItems()).thenReturn(List.of(searchResponse));

        Video[] videos = new Video[]{
                new Video("videoId1", "Sample Video", "Sample Description", "id1", "channel1", "dawkad1", Arrays.asList("sample", "tutorial")),
                new Video("videoId2", "title2", "desc2", "id2", "channel2", "dawkad2", Arrays.asList("sample")),
                new Video("videoId3", "title3", "desc3", "id3", "channel3", "dawkad3", Arrays.asList("description"))
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

        when(youtubeMock.videos()).thenReturn(videosMock);
        when(videosMock.list(anyList())).thenReturn(videoListMock);

        when(videoListMock.setId(anyList())).thenReturn(videoListMock);
        when(videoListMock.setKey(anyString())).thenReturn(videoListMock);
        when(videoListMock.setFields(anyString())).thenReturn(videoListMock);
        when(videoListMock.execute()).thenReturn(responseMock);
        when(responseMock.getItems()).thenReturn(List.of(vidResponse));


        VideoList videoList = youtubeService.searchResults(keywords, maxResults).join();
        assertNotNull("Expected non-null VideoList", videoList);

        // Assertions
        assertEquals("Expected video ID to match", "videoId1", videoList.getVideoList().get(0).getId());
        assertEquals("Expected video title to match", "Sample Video", videoList.getVideoList().get(0).getTitle());
    }

    /**
     * Tests the searchResults method for a case where no search results are returned.
     * It verifies that the method returns an empty VideoList.
     * @throws Exception error handling
     * @author Yehia Metwally
     */
    @Test
    public void testSearchResults_NoResults() throws Exception {
        String keywords = "noResults";
        Long maxResults = 50L;

        mockResponse.setItems(List.of());
        when(mockSearch.list(anyList())).thenReturn(mockRequest);
        when(mockRequest.execute()).thenReturn(mockResponse);


        VideoList videoList = youtubeService.searchResults(keywords, maxResults).join();
        assertNotNull("Expected non-null VideoList",videoList);

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

        Assert.assertThrows("Expected ExecutionException due to RuntimeException", RuntimeException.class, () -> youtubeService.searchResults(keywords, maxResults).join());
    }

    /**
     * Tests the getSearchListResponse method for a successful response.
     * Verifies that the request setup is successful and that execute() is called.
     * @throws IOException error handling
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
     * @throws IOException error handling
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
     * @throws IOException error handling
     * @throws GeneralSecurityException error handling
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

        assertNotNull("Expected non-null result from getYoutubeSearchList", result);
    }

    /**
     * Tests the getYoutubeSearchList method when an exception occurs during the search setup.
     * Verifies that a RuntimeException is thrown when an IOException is encountered.
     * @throws IOException error handling
     * @author Yehia Metwally
     */
    @Test()
    public void testGetYoutubeSearchList_Exception() throws IOException {
        when(mockSearch.list(anyList())).thenThrow(new IOException("Test exception"));

        Assert.assertThrows(RuntimeException.class, () -> youtubeService.getYoutubeSearchList());
    }

    /**
     * Validate that we can get a video
     * @throws IOException error handling
     * @author Laurent Voisard
     */
    @Test
    public void testGetVideo() throws IOException {
        Video v = new Video("1", "title","desc","id","channel","dawkad", Arrays.asList("java", "programming"));

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

    /**
     * Validate that we cannot get more that 1 vid with get video
     * @throws IOException error handling
     * @author Laurent Voisard
     */
    @Test
    public void testGetVideoMultipleFor1Id() throws IOException {
        YouTube.Videos videosMock = mock(YouTube.Videos.class);
        YouTube.Videos.List videoListMock = mock(YouTube.Videos.List.class);
        VideoListResponse responseMock = mock(VideoListResponse.class);

        com.google.api.services.youtube.model.Video[] vidResponse = new com.google.api.services.youtube.model.Video[] {
                new com.google.api.services.youtube.model.Video(),
                new com.google.api.services.youtube.model.Video(),
        };

        when(youtubeMock.videos()).thenReturn(videosMock);
        when(videosMock.list(anyList())).thenReturn(videoListMock);

        when(videoListMock.setId(anyList())).thenReturn(videoListMock);
        when(videoListMock.setKey(anyString())).thenReturn(videoListMock);
        when(videoListMock.setFields(anyString())).thenReturn(videoListMock);
        when(videoListMock.execute()).thenReturn(responseMock);
        when(responseMock.getItems()).thenReturn(List.of(vidResponse));

        assertNull(youtubeService.getVideo("id").join());
    }

    /**
     * Validate that we handle when we can't find a video by id
     * @throws IOException error handling
     * @author Laurent Voisard
     */
    @Test
    public void testGetVideoNoResults() throws IOException {
        YouTube.Videos videosMock = mock(YouTube.Videos.class);
        YouTube.Videos.List videoListMock = mock(YouTube.Videos.List.class);
        VideoListResponse responseMock = mock(VideoListResponse.class);

        com.google.api.services.youtube.model.Video[] vidResponse = new com.google.api.services.youtube.model.Video[] {
        };

        when(youtubeMock.videos()).thenReturn(videosMock);
        when(videosMock.list(anyList())).thenReturn(videoListMock);

        when(videoListMock.setId(anyList())).thenReturn(videoListMock);
        when(videoListMock.setKey(anyString())).thenReturn(videoListMock);
        when(videoListMock.setFields(anyString())).thenReturn(videoListMock);
        when(videoListMock.execute()).thenReturn(responseMock);
        when(responseMock.getItems()).thenReturn(List.of(vidResponse));

        assertNull(youtubeService.getVideo("id").join());
    }

    /**
     * Test functionality of get videos
     * @throws IOException error handling
     * @author Laurent Voisard
     */
    @Test
    public void testGetVideos() throws IOException {
        Video[] videos = new Video[]{
                new Video("1", "title1", "desc1", "id1", "channel1", "dawkad1", Arrays.asList("java", "programming")),
                new Video("2", "title2", "desc2", "id2", "channel2", "dawkad2", Arrays.asList("coding", "tutorial")),
                new Video("3", "title3", "desc3", "id3", "channel3", "dawkad3", Arrays.asList("java", "development"))
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

    /**
     * Validate error handling in function
     * @throws IOException error handling
     * @author Laurent Voisard
     */
    @Test
    public void testGetYoutubeVideosListException() throws IOException {

        when(youtubeMock.videos()).thenReturn(mock(YouTube.Videos.class));
        when(youtubeMock.videos().list(anyList())).thenThrow(new IOException("Test exception"));

        assertThrows(RuntimeException.class, () -> youtubeService.getYoutubeVideosList());
    }

    /**
     * Validate error handling case in function
     * @throws IOException error handling
     * @author Laurent Voisard
     */
    @Test
    public void testGetVideoListResponseException() throws IOException {

        when(mockRequest.execute()).thenThrow(new IOException("Test exception"));
        YouTube.Videos.List request = mock(YouTube.Videos.List.class);
        when(request.setKey(anyString())).thenReturn(request);
        when(request.setFields(anyString())).thenReturn(request);
        when(request.setId(anyList())).thenReturn(request);
        when(request.execute()).thenThrow(new IOException("Test exception"));

        assertThrows(RuntimeException.class, () -> youtubeService.getVideoListResponse(List.of(""), request));
    }
}
