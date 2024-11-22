package controllers;

import actors.SearchServiceActor;
import actors.WebSocketActor;
import akka.actor.ActorRef;
import akka.actor.ActorSystem;
import akka.pattern.Patterns;
import akka.stream.Materializer;
import akka.util.Timeout;
import play.libs.concurrent.HttpExecutionContext;
import com.google.inject.Inject;
import models.*;
import play.libs.streams.ActorFlow;
import play.mvc.Controller;
import play.mvc.Http;
import play.mvc.Result;
import play.mvc.WebSocket;
import scala.compat.java8.FutureConverters;
import scala.concurrent.duration.Duration;
import services.TagService;
import services.YoutubeService;
import java.io.IOException;

import java.security.GeneralSecurityException;

import scala.Option;
import services.SearchService;
import services.StatisticsService;

import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;

import java.util.List;
import java.util.concurrent.TimeUnit;

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
    private final TagService tagService;
    private final ActorSystem actorSystem;
    private final Materializer materializer;

    @Inject
    public YoutubeController(StatisticsService statisticsService, SearchService searchService, HttpExecutionContext ec, YoutubeService youtubeService, TagService tagService, ActorSystem actorSystem, Materializer materializer) {
        this.statisticsService = statisticsService;
        this.searchService = searchService;
        this.ec = ec;
        this.youtubeService = youtubeService;
        this.tagService = tagService;
        this.actorSystem = actorSystem;
        this.materializer = materializer;
    }

    /**
     * Establishes a WebSocket connection for the user session.
     * If no user session exists, a new session is created.
     *
     * @return WebSocket connection
     * @author Tanveer Reza
     */
    public WebSocket webSocket() {
        System.out.println("WebSocket connection initiated.");

        return WebSocket.Text.accept(request -> {
            Optional<String> user = request.session().get("user");

            // If no user session exists, create a new session and start WebSocket
            String sessionId;
            sessionId = user.orElseGet(searchService::createSessionSearchList);

            // Now pass sessionId to the WebSocket actor
            return ActorFlow.actorRef(out -> WebSocketActor.props(searchService, youtubeService, statisticsService, sessionId, out), actorSystem, materializer);
        });
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

        ActorRef webSocketActor = actorSystem.actorOf(WebSocketActor.props(searchService, youtubeService, statisticsService,"", null));

        return FutureConverters.toJava(
                Patterns.ask(webSocketActor, new SearchServiceActor.SearchKeywords(query, user.get()), Timeout.durationToTimeout(Duration.create(5, TimeUnit.SECONDS)))
        ).thenApply(response -> {
            if (response instanceof List) {
                List<VideoSearch> searches = (List<VideoSearch>) response;
                return ok(views.html.search.render(Option.apply(searches), DISPLAY_COUNT));
            } else {
                return internalServerError("Unexpected response");
            }
        });
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
        return CompletableFuture.supplyAsync(() -> {
            ActorRef webSocketActor = actorSystem.actorOf(WebSocketActor.props(searchService, youtubeService, statisticsService,"", null));
            webSocketActor.tell(new WebSocketActor.GetVideo(id), ActorRef.noSender());
            return ok("Get video request sent via WebSocket");
        });
//        return searchService.getVideoById(id)
//                .thenApplyAsync(video -> ok(views.html.video.render(video)), ec.current());
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
     * @param query The search query
     * @return Forward to statistics page
     * @author Tanveer Reza
     */
    public CompletionStage<Result> getStatistics(String query) {
        return CompletableFuture.supplyAsync(() -> ok(views.html.statistics.render(query)));
    }

    /**
     * Handles the request to display a list of videos associated with a specific tag.
     * @author Ryane
     * @param videoID the ID of the video used to fetch related videos based on tags.
     * @return a {@link CompletionStage} that completes with an HTTP {@link Result} displaying the video list.
     */
    public CompletionStage<Result> videosByTag(String videoID) {
        return tagService.getVideoWithTags(videoID, 10L, videoID)
                .thenApplyAsync(searches -> ok(views.html.videoList.render(
                                searches,
                                DISPLAY_COUNT)),
                        ec.current());
    }

    /**
     * Handles the request to display a video and its associated tags.
     * @author Ryane
     * @param videoID the ID of the video to fetch from the search service.
     * @return a {@link CompletionStage} that completes with an HTTP {@link Result} displaying the video and its tags.
     */
    public CompletionStage<Result> showVideoWithTags(String videoID) {
        return searchService.getVideoById(videoID).thenApplyAsync(video -> {
            if (video == null) {
                return badRequest("Video not found.");
            }
            List<String> tags = video.getTags();
            return ok(views.html.videoTags.render(video, tags));
        }, ec.current());
    }
}