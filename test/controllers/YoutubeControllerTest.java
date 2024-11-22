package controllers;

import akka.actor.ActorSystem;
import akka.stream.Materializer;
import models.Video;
import models.YoutubeChannel;
import org.junit.BeforeClass;
import org.junit.Test;
import play.libs.concurrent.HttpExecutionContext;
import play.mvc.Http;
import play.mvc.Result;
import play.test.Helpers;
import play.test.WithApplication;
import services.SearchService;
import services.StatisticsService;
import services.TagService;
import services.YoutubeService;

import java.util.*;
import java.util.concurrent.CompletableFuture;

import static org.junit.Assert.*;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static play.mvc.Http.Status.OK;
import static play.mvc.Http.Status.SEE_OTHER;
import static play.test.Helpers.GET;

/**
 * Test class for the Youtube Controller
 *
 * author Laurent Voisard,
 * Yehia, Tanveer Reza
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
    private static ActorSystem actorSystem;
    private static Materializer materializer;

    /**
     * Setup the YoutubeController, StatisticsService, HttpExecutionContext, and
     * Http.Session
     *
     * author Tanveer Reza
     */
    @BeforeClass
    public static void setUp() {
        mockYoutubeService = mock(YoutubeService.class);
        statisticsService = mock(StatisticsService.class);
        searchService = mock(SearchService.class);
        HttpExecutionContext ec = mock(HttpExecutionContext.class);
        tagService = mock(TagService.class);
        actorSystem = mock(ActorSystem.class);
        materializer = mock(Materializer.class);
        youtubeController = new YoutubeController(statisticsService, searchService, ec, mockYoutubeService, tagService, actorSystem, materializer);
        testChannel = new YoutubeChannel(TEST_CHANNEL_ID, "Test Channel", "Test Description", "http://thumbnail.url", null);
        testVideos = Collections.singletonList(new Video("videoId1", "Video Title 1", "Description 1", "channelId", "Channel Title", "http://thumbnail1.url"));

        // Mock the HttpExecutionContext to return a direct executor
        when(ec.current()).thenReturn(Runnable::run);
    }


    /**
     * Test the getStatistics method of YoutubeController with user session
     *
     * author Tanveer Reza
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

        Result result = youtubeController.getStatistics(query).toCompletableFuture().join();

        assertEquals(OK, result.status());
    }

    /**
     * Test the searchForm method of YoutubeController with no user session
     *
     * throws Exception
     */
    @Test
    public void testSearchFormWithoutUserSession() {
        Http.RequestBuilder requestBuilder = Helpers.fakeRequest()
                .method(GET)
                .uri("/searchForm");

        when(searchService.createSessionSearchList()).thenReturn("sessionList");

        Result result = youtubeController.searchForm(requestBuilder.build()).toCompletableFuture().join();

        assertEquals(SEE_OTHER, result.status());
        assertEquals("/", result.redirectLocation().orElse(null));
        assertEquals("sessionList", result.session().get("user").orElse(null));
    }

    /**
     * Test the searchForm method of YoutubeController with user session
     *
     * throws Exception
     */
    @Test
    public void testSearchFormWithUserSession() {
        Http.RequestBuilder requestBuilder = Helpers.fakeRequest()
                .method(GET)
                .uri("/searchForm")
                .session("user", "1");

        when(searchService.getSessionSearchList(anyString())).thenReturn(List.of());

        Result result = youtubeController.searchForm(requestBuilder.build()).toCompletableFuture().join();

        assertEquals(OK, result.status());
    }
}