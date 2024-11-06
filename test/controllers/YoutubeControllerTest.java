package controllers;


import models.Video;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.mockito.Mockito;
import play.Application;
import play.inject.guice.GuiceApplicationBuilder;
import play.libs.concurrent.HttpExecutionContext;
import play.mvc.Http;
import play.mvc.Result;
import play.test.Helpers;
import play.test.WithApplication;
import services.SearchService;
import services.StatisticsService;
import services.YoutubeService;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;
import static play.mvc.Http.Status.OK;
import static play.mvc.Http.Status.SEE_OTHER;
import static play.test.Helpers.*;

public class YoutubeControllerTest extends WithApplication {
    private static YoutubeController youtubeController;
    private static StatisticsService statisticsService;
    private static SearchService searchService;
    private static YoutubeService mockYoutubeService;
    /**
     * @author Tanveer Reza
     * Setup the YoutubeController, StatisticsService, HttpExecutionContext, and Http.Session
     */
    @BeforeClass
    public static void setUp() {
        mockYoutubeService = Mockito.mock(YoutubeService.class);
        statisticsService = Mockito.mock(StatisticsService.class);
        searchService = Mockito.mock(SearchService.class);
        HttpExecutionContext ec = Mockito.mock(HttpExecutionContext.class);
        youtubeController = new YoutubeController(statisticsService, searchService, ec , mockYoutubeService);

        // Mock the HttpExecutionContext to return a direct executor
        when(ec.current()).thenReturn(Runnable::run);
    }

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

    @Test
    public void testSearchWithUserSession() {
        Http.RequestBuilder request = Helpers.fakeRequest()
                .method(GET)
                .uri("/search?query=test")
                .session("user", "1");

        when(searchService.searchKeywords(anyString(), anyString())).thenReturn(CompletableFuture.completedFuture(null));
        // Ensure the application is properly initialized
        Result res = youtubeController.search("test", request.build()).toCompletableFuture().join();
        assertEquals(OK, res.status());
    }

    @Test
    public void testVideo() {
        Video v = new Video("test","test","test","test","test","test");
        when(searchService.getVideoById(Mockito.anyString())).thenReturn(CompletableFuture.completedFuture(v));
        Result res = youtubeController.video("id").toCompletableFuture().join();
        assertEquals(OK, res.status());
    }

    /**
     * @author Tanveer Reza
     * Test the getStatistics method of YoutubeController
     */
    @Test
    public void testGetStatistics() {
        // Arrange
        String query = "Java";
        Map<String, Long> mockWordFrequency = new LinkedHashMap<>();
        mockWordFrequency.put("java", 3L);
        mockWordFrequency.put("programming", 2L);
        mockWordFrequency.put("tutorial", 1L);

        // Mock the behavior of statisticsService
        when(statisticsService.getWordFrequency(anyString(), anyString())).thenReturn(CompletableFuture.completedFuture(mockWordFrequency));

        // Create a mock request with a session
        Http.RequestBuilder requestBuilder = new Http.RequestBuilder()
                .method(GET)
                .uri("/statistics?query=" + query)
                .session("user", "1");

        Http.Request request = requestBuilder.build();

        // Act
        CompletionStage<Result> resultStage = youtubeController.getStatistics(query, request);
        Result result = resultStage.toCompletableFuture().join();

        // Assert
        assertEquals(OK, result.status());
    }
}