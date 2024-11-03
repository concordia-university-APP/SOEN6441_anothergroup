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
import models.VideoList;
import models.VideoSearch;
import scala.Option;
import models.Video;
import play.mvc.Controller;
import play.mvc.Result;
import services.SearchService;

import java.util.Collection;
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
}
