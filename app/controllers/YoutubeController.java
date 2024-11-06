package controllers;

import play.libs.concurrent.HttpExecutionContext;
import play.mvc.Controller;
import play.mvc.Http;
import play.mvc.Result;
import scala.Option;
import services.SearchService;
import services.StatisticsService;
import views.html.statistics;

import javax.inject.Inject;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
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

    /**
     * @author : Tanveer Reza
     * Get the word frequency statistics for the given query
     * @param query The search query
     * @return The word frequency statistics
     */
    public CompletionStage<Result> getStatistics(String query, Http.Request request) {
        Optional<String> user = request.session().get("user");

        if(user.isEmpty()) {
            return CompletableFuture.supplyAsync(()
                    -> redirect(request.uri()).addingToSession(request,"user", searchService.createSessionSearchList()));
        } else {
            return statisticsService.getWordFrequency(query, user.get())
                    .thenApplyAsync(wordFrequency -> ok(statistics.render(wordFrequency, query)), ec.current());
        }
        // Retrieve the last search query from the session
    }
}