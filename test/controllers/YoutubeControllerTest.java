package controllers;


import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;
import play.Application;
import play.inject.guice.GuiceApplicationBuilder;
import play.libs.concurrent.HttpExecutionContext;
import play.mvc.Http;
import play.mvc.Result;
import play.test.WithApplication;
import services.SearchService;
import services.StatisticsService;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;
import static play.mvc.Http.Status.OK;
import static play.mvc.Http.Status.SEE_OTHER;
import static play.test.Helpers.GET;
import static play.test.Helpers.route;

public class YoutubeControllerTest extends WithApplication {
    private YoutubeController youtubeController;
    private StatisticsService statisticsService;
    private SearchService searchService;

    @Override
    protected Application provideApplication() {
        return new GuiceApplicationBuilder().build();
    }

    /**
     * Author : Tanveer Reza
     * Setup the YoutubeController, StatisticsService, HttpExecutionContext, and Http.Session
     */
    @Before
    public void setUp() {
        statisticsService = Mockito.mock(StatisticsService.class);
        searchService = Mockito.mock(SearchService.class);
        HttpExecutionContext ec = Mockito.mock(HttpExecutionContext.class);
        youtubeController = new YoutubeController(statisticsService, searchService, ec);

        // Mock the HttpExecutionContext to return a direct executor
        when(ec.current()).thenReturn(Runnable::run);
    }

    @Test
    public void testIndexRedirectWhenNoUserSession() {
        Http.RequestBuilder request = new Http.RequestBuilder()
                .method(GET)
                .uri("/");

        // Ensure the application is properly initialized
        Application app = provideApplication();
        Result result = route(app, request);
        assertEquals(SEE_OTHER, result.status());
    }

    @Test
    public void testIndexWithUserSession() {
        Http.RequestBuilder request = new Http.RequestBuilder()
                .method(GET)
                .uri("/")
                .session("user", "1");


        // Ensure the application is properly initialized
        Application app = provideApplication();
        Result result = route(app, request);
        assertEquals(OK, result.status());
    }

    /**
     * Author : Tanveer Reza
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