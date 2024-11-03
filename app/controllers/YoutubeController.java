package controllers;

import com.google.api.services.youtube.model.Channel;
import com.google.api.services.youtube.model.SearchResult;
import models.Video;
import play.mvc.Controller;
import play.mvc.Result;
import scala.compat.java8.OptionConverters;
import services.ChannelService;
import services.YoutubeService;
import views.html.search;
import java.io.IOException;
import java.security.GeneralSecurityException;
import java.util.*;
import views.html.statistics;
import models.VideoList;
import models.VideoSearch;
import scala.Option;
import models.Video;
import play.mvc.Controller;
import play.mvc.Result;
import services.SearchService;
import com.google.api.services.youtube.model.VideoSnippet;


import java.util.List;

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

    public Result showChannelProfile(String channelId) {
        try {
            // Get the channel details
            Channel channel = YoutubeService.getChannelById(channelId);
            // Get the last 10 videos for the channel
            List<Video> videos = YoutubeService.getChannelVideos(channelId);

            // Convert videos to an Option<List<Video>>
            Option<List<Video>> scalaVideos = Option.apply(videos);

            // Render the channel profile view with the channel details and the video list
            return ok(views.html.channelProfile.render(Option.apply(channel), scalaVideos));
        } catch (GeneralSecurityException | IOException e) {
            return internalServerError("Error occurred while retrieving channel profile: " + e.getMessage());
        }
    }
}
