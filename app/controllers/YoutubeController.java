package controllers;

import play.libs.concurrent.HttpExecutionContext;
import com.google.api.services.youtube.YouTube;
import com.google.api.services.youtube.model.Channel;
import com.google.api.services.youtube.model.SearchResult;
import com.google.inject.Inject;
import models.*;
import play.mvc.Controller;
import play.mvc.Http;
import play.mvc.Result;
import scala.compat.java8.OptionConverters;
import services.YoutubeService;
import views.html.search;
import java.io.IOException;
import java.security.GeneralSecurityException;
import java.util.*;
import views.html.statistics;
import scala.Option;
import services.SearchService;
import services.StatisticsService;
import views.html.statistics;
import com.google.api.services.youtube.model.VideoSnippet;

import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;

import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;
import java.util.concurrent.CompletionStage;

/**
 *
 */
public class YoutubeController extends Controller {
    private final StatisticsService statisticsService;
    private final SearchService searchService;
    private final HttpExecutionContext ec;
    private final int DISPLAY_COUNT = 10;

    @Inject
    public YoutubeController(StatisticsService statisticsService, SearchService searchService, HttpExecutionContext ec) {
        this.searchService = searchService;
        this.statisticsService = statisticsService;
        this.ec = ec;
    }

    /**
     * @author Laurent & Yehia
     * Search for videos with keywords
     * creates a new session if one doesn't exist
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

        return searchService.searchKeywords(query, user.get(), DISPLAY_COUNT)
                .thenApplyAsync(searches -> ok(views.html.search.render(
                        Option.apply(searches),
                        DISPLAY_COUNT)),
                        ec.current());
    }

    /**
     * @author Laurent & Yehia
     * Creates a user session or retrieves an existing one
     * @param request browser http request
     * @return redirects to search page only the form is visible unless a user session exists
     */
    public CompletionStage<Result> searchForm(Http.Request request) {
        Optional<String> user = request.session().get("user");

        return user.map(s -> CompletableFuture.supplyAsync(() ->
                ok(views.html.search.render(
                        Option.apply(searchService.getSessionSearchList(s)),
                        DISPLAY_COUNT
                ))
        )).orElseGet(() -> CompletableFuture.supplyAsync(() ->
                redirect("/").addingToSession(request, "user", searchService.createSessionSearchList())));

    }

    /**
     * @author Laurent Voisard
     * Get the youtube video from youtube api
     * @param id video Id
     * @return video model
     */
    public CompletionStage<Result> video(String id) {
        return searchService.getVideoById(id)
                .thenApplyAsync(video -> ok(views.html.video.render(video)), ec.current());
    }

    public CompletionStage<Result> showChannelProfile(String channelId) throws GeneralSecurityException, IOException {
        CompletionStage<YoutubeChannel> channelFuture = YoutubeService.getChannelById(channelId);
        CompletionStage<List<Video>> videosFuture = YoutubeService.getChannelVideos(channelId);

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
                    Option<YoutubeChannel> scalaChannel = Option.apply(channel);
                    Option<List<Video>> scalaVideos = Option.apply(videos);
                    return ok(views.html.channelProfile.render(scalaChannel, scalaVideos));
                })
                .exceptionally(e -> {
                    // Enhanced error logging
                    System.err.println("Error retrieving channel profile: " + e.getMessage());
                    e.printStackTrace();
                    return internalServerError("Error occurred while retrieving channel profile: " + e.getMessage());
                });
    }
}

    /**
     * @author : Tanveer Reza
     * Get the word frequency statistics for the given query
     * @param query The search query
     * @return The word frequency statistics
     */
    public CompletionStage<Result> getStatistics(String query) {
        // Retrieve the last search query from the session
        return statisticsService.getWordFrequency(query)
                .thenApplyAsync(wordFrequency -> ok(statistics.render(wordFrequency, query)), ec.current());
    }
}