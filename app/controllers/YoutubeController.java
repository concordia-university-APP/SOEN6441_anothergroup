package controllers;

import com.google.api.services.youtube.model.SearchResult;
import models.Video;
import models.VideoIdList;
import play.mvc.Controller;
import play.mvc.Result;
import services.YoutubeService;
import scala.jdk.javaapi.OptionConverters;
import java.io.IOException;
import java.security.GeneralSecurityException;
import java.util.List;
import java.util.Optional;
import scala.Option;

public class YoutubeController extends Controller {

    public Result search(String query) {
        try {
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
        Optional<VideoIdList> results = Optional.empty();
        return ok(views.html.search.render(OptionConverters.toScala(results)));
    }

}
