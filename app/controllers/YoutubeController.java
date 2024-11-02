package controllers;

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
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class YoutubeController extends Controller {

    private static List<SearchResult> combinedResults = new ArrayList<>();

    public Result search(String query) {
        try {
            // Fetch new search results
            List<Video> newResults = YoutubeService.searchResults(query);

            // Add new results to the combined list
            combinedResults.addAll(newResults);

            // Convert combined results to Scala Option for rendering
            Option<List<SearchResult>> scalaResults = OptionConverters.toScala(Optional.of(combinedResults));
            return ok(views.html.search.render(scalaResults));

            VideoIdList results = YoutubeService.searchResults(query);
            return ok(views.html.search.render(OptionConverters.toScala(Optional.of(results))));
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
        Optional<List<SearchResult>> results = Optional.empty();
        return ok(search.render(OptionConverters.toScala(results)));
    }

}
