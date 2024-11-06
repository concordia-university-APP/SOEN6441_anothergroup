package controllers;
import static org.mockito.Mockito.*;
import static org.junit.jupiter.api.Assertions.*;
import static play.test.Helpers.*;

import models.YoutubeChannel;
import models.Video;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import play.libs.concurrent.HttpExecutionContext;
import play.mvc.Result;
import play.mvc.Http;
import play.test.Helpers;
import services.SearchService;
import services.StatisticsService;
import services.YoutubeService;

import java.io.IOException;
import java.security.GeneralSecurityException;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;

public class ChannelProfileControllerTest {
    private YoutubeController controller;
    private static final String TEST_CHANNEL_ID = "none";
    private YoutubeChannel testChannel;
    private List<Video> testVideos;
    private YoutubeService mockYoutubeService;
    private static StatisticsService statisticsService;
    private static SearchService searchService;
    @BeforeEach
    void setUp() throws GeneralSecurityException, IOException {
        mockYoutubeService = Mockito.mock(YoutubeService.class);
        statisticsService = Mockito.mock(StatisticsService.class);
        searchService = Mockito.mock(SearchService.class);
        HttpExecutionContext ec = Mockito.mock(HttpExecutionContext.class);
        controller = new YoutubeController(statisticsService, searchService, ec, mockYoutubeService);
        testChannel = new YoutubeChannel(TEST_CHANNEL_ID, "Test Channel", "Test Description", "http://thumbnail.url", null);
        testVideos = List.of(new Video("videoId1", "Video Title 1", "Description 1", "channelId", "Channel Title", "http://thumbnail1.url"));
    }

    @Test
    void testShowChannelProfile_Success() throws GeneralSecurityException, IOException {
        when(mockYoutubeService.getChannelById(TEST_CHANNEL_ID))
                .thenReturn(CompletableFuture.completedFuture(testChannel));

        when(mockYoutubeService.getChannelVideos(TEST_CHANNEL_ID))
                .thenReturn(CompletableFuture.completedFuture(testVideos));

        CompletionStage<Result> resultStage = controller.showChannelProfile(TEST_CHANNEL_ID);
        Result result = resultStage.toCompletableFuture().join();

        assertEquals(Http.Status.OK, result.status(), "Expected status to be OK");
        assertNotNull(result.contentType(), "Content type should not be null");
        assertTrue(Helpers.contentAsString(result).contains("Test Channel"), "Response should contain channel name");
    }

    @Test
    void testShowChannelProfile_ChannelNotFound() throws GeneralSecurityException, IOException {
        when(mockYoutubeService.getChannelById(TEST_CHANNEL_ID))
                .thenReturn(CompletableFuture.completedFuture(null));

        CompletionStage<Result> resultStage = controller.showChannelProfile(TEST_CHANNEL_ID);
        Result result = resultStage.toCompletableFuture().join();

        assertEquals(Http.Status.BAD_REQUEST, result.status(), "Expected status to be BAD REQUEST");
        assertTrue(Helpers.contentAsString(result).contains("Channel not found"), "Response should indicate channel not found");
    }

    @Test
    void testShowChannelProfile_NoVideosFound() throws GeneralSecurityException, IOException {
        when(mockYoutubeService.getChannelById(TEST_CHANNEL_ID))
                .thenReturn(CompletableFuture.completedFuture(testChannel));

        when(mockYoutubeService.getChannelVideos(TEST_CHANNEL_ID))
                .thenReturn(CompletableFuture.completedFuture(List.of())); // Simulate no videos found

        CompletionStage<Result> resultStage = controller.showChannelProfile(TEST_CHANNEL_ID);
        Result result = resultStage.toCompletableFuture().join();

        assertEquals(Http.Status.BAD_REQUEST, result.status(), "Expected status to be BAD REQUEST when no videos found");
        assertTrue(Helpers.contentAsString(result).contains("No videos found for this channel"), "Response should indicate no videos found");
    }

    @Test
    void testShowChannelProfile_Exception() throws GeneralSecurityException, IOException {
        when(mockYoutubeService.getChannelById(TEST_CHANNEL_ID))
                .thenReturn(CompletableFuture.failedFuture(new RuntimeException("Test Exception")));

        when(mockYoutubeService.getChannelVideos(TEST_CHANNEL_ID))
                .thenReturn(CompletableFuture.completedFuture(Collections.emptyList()));

        CompletionStage<Result> resultStage = controller.showChannelProfile(TEST_CHANNEL_ID);
        Result result = resultStage.toCompletableFuture().join();

        assertEquals(Http.Status.INTERNAL_SERVER_ERROR, result.status(), "Expected status to be Internal Server Error");
        assertTrue(Helpers.contentAsString(result).contains("Error occurred while retrieving channel profile"),
                "Response should indicate an error");
    }
}
