package controllers;

import models.VideoList;
import scala.Option;
import com.google.api.services.youtube.model.SearchResult;
import models.Video;
import play.mvc.Controller;
import play.mvc.Result;
import services.YoutubeService;
import scala.jdk.javaapi.OptionConverters;
import views.html.search;
import java.io.IOException;
import java.security.GeneralSecurityException;
import java.util.*;

public class YoutubeController extends Controller {

    private static VideoList combinedResults = new VideoList(Collections.emptyList());

    public Result search(String query) {
        try {
            // Fetch new search results
            VideoList newResults = YoutubeService.searchResults(query);

            // Add new results to the combined list
            combinedResults.getVideoList().addAll(newResults.getVideoList());

            // Convert combined results to Scala Option for rendering
            Option<VideoList> scalaResults = OptionConverters.toScala(Optional.of(combinedResults));
            return ok(views.html.search.render(scalaResults));

        } catch (IOException e) {
            return internalServerError("Error occurred while searching YouTube: " + e.getMessage());
        } catch (GeneralSecurityException e) {
            throw new RuntimeException(e);
        }
    }


    public Result video(String id) {
        Video video = YoutubeService.getVideo(id);
        return ok(views.html.video.render(video));
    }

    public Result searchForm() {
        Option<VideoList> results = Option.empty();
        return ok(views.html.search.render(results));
    }

}
