package controllers;

import play.libs.concurrent.HttpExecutionContext;
import com.google.inject.Inject;
import models.*;
import play.mvc.Controller;
import play.mvc.Http;
import play.mvc.Result;
import services.YoutubeService;
import java.io.IOException;

import java.security.GeneralSecurityException;
import java.util.*;
import scala.Option;
import services.SearchService;
import services.StatisticsService;

import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;

import java.util.List;

/**
 * <p>
 * This controller provides endpoints for searching YouTube videos, retrieving a video by ID, showing channel profiles,
 * and displaying statistics about search queries. It uses the {@link SearchService}, {@link StatisticsService}, and
 * {@link YoutubeService} to interact with YouTube data.
 * </p>
 *
 * @author Yehia, Laurent, Tanveer
 */
public class YoutubeController extends Controller {
    private final StatisticsService statisticsService;
    private final SearchService searchService;
    private final HttpExecutionContext ec;
    private final int DISPLAY_COUNT = 10;
    private final YoutubeService youtubeService;

    @Inject
    public YoutubeController(StatisticsService statisticsService, SearchService searchService, HttpExecutionContext ec, YoutubeService youtubeService) {
        this.searchService = searchService;
        this.statisticsService = statisticsService;
        this.ec = ec;
        this.youtubeService = youtubeService;
    }

    /**
     *  Search for videos with keywords
     * creates a new session if one doesn't exist
     * @author Laurent, Yehia
     * @param query string query to send to youtube api
     * @param request Http request of the browser
     * @return returns the search page populated with the last 10 or less requests made
     */
    public CompletionStage<Result> search(String query, Http.Request request) {

        Optional<String> user = request.session().get("user");

        if(user.isEmpty()) {
            return CompletableFuture.supplyAsync(()
                    -> redirect("/").addingToSession(request,"user", searchService.createSessionSearchList()));
        }

        return searchService.searchKeywords(query, user.get())
                .thenApplyAsync(searches -> ok(views.html.search.render(
                                Option.apply(searches),
                                DISPLAY_COUNT)),
                        ec.current());
    }

    /**
     * Creates a user session or retrieves an existing one
     * @author Laurent, Yehia
     * @param request browser http request
     * @return redirects to search page only the form is visible unless a user session exists
     */
    public CompletionStage<Result> searchForm(Http.Request request) {
        Optional<String> user = request.session().get("user");

        return user.map(sessionId -> CompletableFuture.supplyAsync(() ->
                ok(views.html.search.render(
                        Option.apply(searchService.getSessionSearchList(sessionId)),
                        DISPLAY_COUNT))
        )).orElseGet(() -> CompletableFuture.supplyAsync(() ->
                redirect("/").addingToSession(request, "user", searchService.createSessionSearchList())));

    }

    /**
     * Get a video with the specified id from youtube api
     * @author Laurent Voisard
     * @param id video Id
     * @return video model
     */
    public CompletionStage<Result> video(String id) {
        return searchService.getVideoById(id)
                .thenApplyAsync(video -> ok(views.html.video.render(video)), ec.current());
    }

    /**
     * Redirect to the profile page
     * @param channelId id of the channel in youtube api
     * @return channel page
     * @throws GeneralSecurityException errors
     * @throws IOException errors
     * @author Yehia
     */
    public CompletionStage<Result> showChannelProfile(String channelId) throws GeneralSecurityException, IOException {
        CompletionStage<YoutubeChannel> channelFuture = youtubeService.getChannelById(channelId);
        CompletionStage<List<Video>> videosFuture = youtubeService.getChannelVideos(channelId);

        // Debugging logs to check if the futures are not null
        System.out.println("Channel Future: " + channelFuture);
        System.out.println("Videos Future: " + videosFuture);

        return channelFuture
                .thenCombine(videosFuture, (channel, videos) -> {
                    // Check if the channel is null
                    if (channel == null) {
                        return badRequest("Channel not found");
                    }

                    // Check if the videos list is null or empty
                    if (videos == null || videos.isEmpty()) {
                        return badRequest("No videos found for this channel");
                    }

                    // Prepare the response if no issues were found
                    return ok(views.html.channelProfile.render(Option.apply(channel), Option.apply(videos)));
                })
                .exceptionally(e -> {
                    // Enhanced error logging
                    System.err.println("Error retrieving channel profile: " + e.getMessage());
                    e.printStackTrace();
                    return internalServerError("Error occurred while retrieving channel profile: " + e.getMessage());
                });
    }

    /**
     * Get the word frequency statistics for the given query
     * @author Tanveer Reza
     * @param query The search query
     * @param request Incoming http request from the web page
     * @return The word frequency statistics
     */
    public CompletionStage<Result> getStatistics(String query, Http.Request request) {
        Optional<String> user = request.session().get("user");

        if(user.isEmpty()) {
            return CompletableFuture.supplyAsync(()
                    -> redirect(request.uri()).addingToSession(request,"user", searchService.createSessionSearchList()));
        } else {
            return statisticsService.getWordFrequency(query, user.get())
                    .thenApplyAsync(wordFrequency -> ok(views.html.statistics.render(wordFrequency, query)), ec.current());
        }
        // Retrieve the last search query from the session
    }
}