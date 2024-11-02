package controllers;

import models.Video;
import play.mvc.Controller;
import play.mvc.Result;
import services.YoutubeService;
import views.html.search;
import java.io.IOException;
import java.security.GeneralSecurityException;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import views.html.statistics;

public class YoutubeController extends Controller {

    public Result search(String query) {
        try {
            List<Video> results = YoutubeService.searchResults(query, 10L);
            return ok(search.render(results, query));
        } catch (IOException e) {
            return internalServerError("Error occurred while searching YouTube: " + e.getMessage());
        } catch (GeneralSecurityException e) {
            throw new RuntimeException(e);
        }
    }

    public Result searchForm() {
        List<Video> results = Collections.emptyList();
        return ok(search.render(results, ""));
    }
}
