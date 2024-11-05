package controllers;

import com.google.api.services.youtube.YouTube;
import com.google.api.services.youtube.model.Channel;
import com.google.api.services.youtube.model.SearchResult;
import com.google.inject.Inject;
import models.*;
import play.core.j.HttpExecutionContext;
import play.mvc.Controller;
import play.mvc.Result;
import scala.compat.java8.OptionConverters;
import services.YoutubeService;
import views.html.search;
import java.io.IOException;
import java.security.GeneralSecurityException;
import java.util.*;
import views.html.statistics;
import scala.Option;
import models.Video;
import play.mvc.Controller;
import play.mvc.Result;
import services.SearchService;
import com.google.api.services.youtube.model.VideoSnippet;


import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;
import java.util.concurrent.CompletionStage;

public class YoutubeController extends Controller {
    public Result search(String query) {
        List<VideoSearch> searches = SearchService.getInstance().searchKeywords(query);
        return ok(views.html.search.render(Option.apply(searches)));
    }

    public Result video(String id) {
        Video video = SearchService.getInstance().getVideoById(id);
        return ok(views.html.video.render(video));
    }

    public Result searchForm() {
        Option<Collection<VideoSearch>> results = Option.empty();
        return ok(views.html.search.render(results));
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
