package controllers;


import models.Video;
import models.VideoList;
import models.YoutubeChannel;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.mockito.Mockito;
import play.Application;
import play.libs.concurrent.HttpExecutionContext;
import play.mvc.Http;
import play.mvc.Result;
import play.test.Helpers;
import play.test.WithApplication;
import services.SearchService;
import services.StatisticsService;
import services.TagService;
import services.YoutubeService;

import java.io.IOException;
import java.security.GeneralSecurityException;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;

import static org.junit.Assert.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static play.mvc.Http.Status.OK;
import static play.mvc.Http.Status.SEE_OTHER;
import static play.test.Helpers.*;

/**
 * Test class for the Youtube Controller
 * @author Laurent Voisard, Yehia, Tanveer Reza
 */
public class YoutubeControllerTest extends WithApplication {
    private static YoutubeController youtubeController;
    private static StatisticsService statisticsService;
    private static SearchService searchService;
    private static YoutubeService mockYoutubeService;
    private static final String TEST_CHANNEL_ID = "none";
    private static YoutubeChannel testChannel;
    private static List<Video> testVideos;
    private static TagService tagService;
    /**
     * @author Tanveer Reza
     * Setup the YoutubeController, StatisticsService, HttpExecutionContext, and Http.Session
     */
    @BeforeClass
    public static void setUp() {
        mockYoutubeService = mock(YoutubeService.class);
        statisticsService = mock(StatisticsService.class);
        searchService = mock(SearchService.class);
        HttpExecutionContext ec = mock(HttpExecutionContext.class);
        tagService = mock(TagService.class);
        youtubeController = new YoutubeController(statisticsService, searchService, ec, mockYoutubeService, tagService);
        testChannel = new YoutubeChannel(TEST_CHANNEL_ID, "Test Channel", "Test Description", "http://thumbnail.url", null);
        testVideos = Collections.singletonList(new Video("videoId1", "Video Title 1", "Description 1", "channelId", "Channel Title", "http://thumbnail1.url"));


        // Mock the HttpExecutionContext to return a direct executor
        when(ec.current()).thenReturn(Runnable::run);
    }

    /**
     * @author Laurent Voisard
     * Test when a user has no session and opens the app
     */
    @Test
    public void testIndexRedirectWhenNoUserSession() {
        Http.RequestBuilder request = Helpers.fakeRequest()
                .method(GET)
                .uri("/");

        // Ensure the application is properly initialized
        Application app = fakeApplication();
        Result result = route(app, request);
        assertEquals(SEE_OTHER, result.status());
    }

    /**
     * @author Laurent Voisard
     * Test when a user has a session and opens the app
     */
    @Test
    public void testIndexWithUserSession() {
        Http.RequestBuilder request = Helpers.fakeRequest()
                .method(GET)
                .uri("/")
                .session("user", "1");


        // Ensure the application is properly initialized
        Application app = fakeApplication();
        Result result = route(app, request);
        assertEquals(OK, result.status());
    }

    /**
     * @author Laurent Voisard
     * Test when a user searches without a session
     */
    @Test
    public void testSearchWithoutUserSession() {
        Http.RequestBuilder request = Helpers.fakeRequest()
                .method(GET)
                .uri("/search?query=test");

        // Ensure the application is properly initialized
        Application app = fakeApplication();
        Result result = route(app, request);
        assertEquals(SEE_OTHER, result.status());
    }

    /**
     * @author Laurent Voisard
     * Test when a user searches without a session
     */
    @Test
    public void testSearchWithUserSession() {
        Http.RequestBuilder request = Helpers.fakeRequest()
                .method(GET)
                .uri("/search?query=test")
                .session("user", "1");

        when(searchService.searchKeywords(anyString(), anyString())).thenReturn(CompletableFuture.completedFuture(List.of()));
        // Ensure the application is properly initialized
        Result res = youtubeController.search("test", request.build()).toCompletableFuture().join();
        assertEquals(OK, res.status());
    }

    /**
     * @author Laurent Voisard
     * Test the video route
     */
    @Test
    public void testVideo() {
        Video v = new Video("test","test","test","test","test","test");
        when(searchService.getVideoById(anyString())).thenReturn(CompletableFuture.completedFuture(v));
        Result res = youtubeController.video("id").toCompletableFuture().join();
        assertEquals(OK, res.status());
    }

    /**
     * @author Tanveer Reza
     * Test the getStatistics method of YoutubeController with no user session
     */
    @Test
    public void testGetStatisticsWithoutUserSession() {
        String query = "Java";
        Http.RequestBuilder requestBuilder = Helpers.fakeRequest()
                .session("user", null) // No user session
                .uri("/statistics?query=" + query);

        when(searchService.createSessionSearchList()).thenReturn("sessionList");

        Result result = youtubeController.getStatistics(query, requestBuilder.build()).toCompletableFuture().join();

        assertEquals(SEE_OTHER, result.status()); // Expecting a redirect
        assertEquals(requestBuilder.build().uri(), result.redirectLocation().orElse(null)); // Redirects to the same URI
        assertEquals("sessionList", result.session().get("user").orElse(null));
    }

    /**
     * @author Tanveer Reza
     * Test the getStatistics method of YoutubeController with user session
     */
    @Test
    public void testGetStatisticsWithUserSession() {
        String query = "Java";
        Map<String, Long> mockWordFrequency = new LinkedHashMap<>();
        mockWordFrequency.put("java", 3L);
        mockWordFrequency.put("programming", 2L);
        mockWordFrequency.put("tutorial", 1L);

        when(statisticsService.getWordFrequency(anyString(), anyString())).thenReturn(CompletableFuture.completedFuture(mockWordFrequency));

        Http.RequestBuilder requestBuilder = new Http.RequestBuilder()
                .method(GET)
                .uri("/statistics?query=" + query)
                .session("user", "1");

        Http.Request request = requestBuilder.build();

        Result result = youtubeController.getStatistics(query, request).toCompletableFuture().join();

        assertEquals(OK, result.status());
    }

    /**
     * Tests that the showChannelProfile method in YoutubeController successfully returns
     * the expected result when a channel and its videos are found.
     *
     * @throws GeneralSecurityException if a security error occurs during the test.
     * @throws IOException if an I/O error occurs during the test.
     * @author yehia metwally
     */
    @Test
    public void testShowChannelProfile_Success() throws GeneralSecurityException, IOException {
        when(mockYoutubeService.getChannelById(TEST_CHANNEL_ID))
                .thenReturn(CompletableFuture.completedFuture(testChannel));

        when(mockYoutubeService.getChannelVideos(TEST_CHANNEL_ID))
                .thenReturn(CompletableFuture.completedFuture(testVideos));

        CompletionStage<Result> resultStage = youtubeController.showChannelProfile(TEST_CHANNEL_ID);
        Result result = resultStage.toCompletableFuture().join();

        assertEquals("Expected status to be OK", Http.Status.OK, result.status());
        assertNotNull("Content type should not be null", result.contentType());
        assertTrue("Response should contain channel name", Helpers.contentAsString(result).contains("Test Channel"));
    }

    /**
     * Tests that the showChannelProfile method in YoutubeController returns a BAD REQUEST status
     * when the specified channel is not found.
     *
     * @throws GeneralSecurityException if a security error occurs during the test.
     * @throws IOException if an I/O error occurs during the test.
     * @author yehia metwally
     */
    @Test
    public void testShowChannelProfile_ChannelNotFound() throws GeneralSecurityException, IOException {
        when(mockYoutubeService.getChannelById(TEST_CHANNEL_ID))
                .thenReturn(CompletableFuture.completedFuture(null));

        CompletionStage<Result> resultStage = youtubeController.showChannelProfile(TEST_CHANNEL_ID);
        Result result = resultStage.toCompletableFuture().join();

        assertEquals("Expected status to be BAD REQUEST", Http.Status.BAD_REQUEST, result.status());
        assertTrue("Response should indicate channel not found", Helpers.contentAsString(result).contains("Channel not found"));
    }

    /**
     * Tests that the showChannelProfile method in YoutubeController returns a BAD REQUEST status
     * when no videos are found for a specified channel.
     *
     * @throws GeneralSecurityException if a security error occurs during the test.
     * @throws IOException if an I/O error occurs during the test.
     * @author yehia metwally
     */
    @Test
    public void testShowChannelProfile_NoVideosFound() throws GeneralSecurityException, IOException {
        when(mockYoutubeService.getChannelById(TEST_CHANNEL_ID))
                .thenReturn(CompletableFuture.completedFuture(testChannel));

        when(mockYoutubeService.getChannelVideos(TEST_CHANNEL_ID))
                .thenReturn(CompletableFuture.completedFuture(List.of())); // Simulate no videos found

        CompletionStage<Result> resultStage = youtubeController.showChannelProfile(TEST_CHANNEL_ID);
        Result result = resultStage.toCompletableFuture().join();

        assertEquals("Expected status to be BAD REQUEST when no videos found", Http.Status.BAD_REQUEST, result.status());
        assertTrue("Response should indicate no videos found", Helpers.contentAsString(result).contains("No videos found for this channel"));
    }

    /**
     * Tests that the showChannelProfile method in YoutubeController returns an INTERNAL SERVER ERROR
     * status when an exception occurs while retrieving the channel profile.
     *
     * @throws GeneralSecurityException if a security error occurs during the test.
     * @throws IOException if an I/O error occurs during the test.
     * @author yehia metwally
     */
    @Test
    public void testShowChannelProfile_Exception() throws GeneralSecurityException, IOException {
        when(mockYoutubeService.getChannelById(TEST_CHANNEL_ID))
                .thenReturn(CompletableFuture.failedFuture(new RuntimeException("Test Exception")));

        when(mockYoutubeService.getChannelVideos(TEST_CHANNEL_ID))
                .thenReturn(CompletableFuture.completedFuture(Collections.emptyList()));

        CompletionStage<Result> resultStage = youtubeController.showChannelProfile(TEST_CHANNEL_ID);
        Result result = resultStage.toCompletableFuture().join();

        assertEquals("Expected status to be Internal Server Error", Http.Status.INTERNAL_SERVER_ERROR, result.status());
        assertTrue("Response should indicate an error", Helpers.contentAsString(result).contains("Error occurred while retrieving channel profile"));
    }

    /**
     * Tests that the showChannelProfile method in YoutubeController returns a BAD REQUEST status
     * when the channel is found but no video list is returned (null).
     *
     * @throws GeneralSecurityException if a security error occurs during the test.
     * @throws IOException if an I/O error occurs during the test.
     *@author yehia metwally
     */
    @Test
    public void testShowChannelProfile_NotFoundNull() throws GeneralSecurityException, IOException {
        when(mockYoutubeService.getChannelById(TEST_CHANNEL_ID))
                .thenReturn(CompletableFuture.completedFuture(testChannel));

        when(mockYoutubeService.getChannelVideos(TEST_CHANNEL_ID))
                .thenReturn(CompletableFuture.completedFuture(null));

        CompletionStage<Result> resultStage = youtubeController.showChannelProfile(TEST_CHANNEL_ID);
        Result result = resultStage.toCompletableFuture().join();

        assertEquals("Expected status to be BAD REQUEST when no videos found", Http.Status.BAD_REQUEST, result.status());
    }

    /**
     * Tests the `videosByTag` method when no videos are found for the given tag.
     * Verifies that a 404 Not Found response is returned with the appropriate message.
     * @author Ryane
     */
    @Test
    public void testVideosByTag_NotFound() {
        String tag = "nonexistentTag";

        when(tagService.getVideoWithTags(tag, 10L, tag))
                .thenReturn(CompletableFuture.completedFuture(new VideoList(List.of())));

        Result result = youtubeController.videosByTag(tag).toCompletableFuture().join();

        assertEquals(NOT_FOUND, result.status());
        assertEquals("No videos found for tag: " + tag, contentAsString(result));
    }

    /**
     * Tests the `videosByTag` method when an exception occurs during the API call.
     * Verifies that a 500 Internal Server Error response is returned with the appropriate message.
     * @author Ryane
     */
    @Test
    public void testVideosByTag_Exception() {
        String tag = "exceptionTag";

        when(tagService.getVideoWithTags(tag, 10L, tag))
                .thenReturn(CompletableFuture.failedFuture(new RuntimeException("API error")));

        Result result = youtubeController.videosByTag(tag).toCompletableFuture().join();

        assertEquals(INTERNAL_SERVER_ERROR, result.status());
        assertEquals("Error fetching videos for tag: " + tag, contentAsString(result));
    }
}