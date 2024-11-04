package controllers;

import models.VideoSearch;
import play.libs.concurrent.HttpExecutionContext;
import play.mvc.Controller;
import play.mvc.Http;
import play.mvc.Result;
import scala.Option;
import services.SearchService;
import services.StatisticsService;
import views.html.statistics;

import javax.inject.Inject;
import java.util.Collection;
import java.util.concurrent.CompletionStage;

public class YoutubeController extends Controller {
    private final StatisticsService statisticsService;
    private final HttpExecutionContext ec;
    private final Http.Session session;

    @Inject
    public YoutubeController(StatisticsService statisticsService, HttpExecutionContext ec, Http.Session session) {
        this.statisticsService = statisticsService;
        this.ec = ec;
        this.session = session;
    }

    public CompletionStage<Result> search(String query) {
        // Store the search query in the session
        session.adding("lastSearchQuery", query);

        return SearchService.getInstance().searchKeywords(query)
                .thenApplyAsync(searches -> ok(views.html.search.render(Option.apply(searches))), ec.current());
    }

    public CompletionStage<Result> video(String id) {
        return SearchService.getInstance().getVideoById(id)
                .thenApplyAsync(video -> ok(views.html.video.render(video)), ec.current());
    }

    public Result searchForm() {
        Option<Collection<VideoSearch>> results = Option.empty();
        return ok(views.html.search.render(results));
    }


    /**
     * Author : Tanveer Reza
     * Get the word frequency statistics for the given query
     * @param query The search query
     * @return The word frequency statistics
     */
    public CompletionStage<Result> getStatistics(String query) {
        // Retrieve the last search query from the session
        session.adding("lastSearchQuery", query);
        return statisticsService.getWordFrequency(query)
                .thenApplyAsync(wordFrequency -> ok(statistics.render(wordFrequency, query)), ec.current());
    }
}