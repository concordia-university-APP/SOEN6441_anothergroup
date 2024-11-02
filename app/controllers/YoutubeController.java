package controllers;

import com.google.api.services.youtube.model.SearchResult;
import play.mvc.Controller;
import play.mvc.Result;
import services.YoutubeService;
import views.html.search;
import scala.jdk.javaapi.OptionConverters;
import java.io.IOException;
import java.security.GeneralSecurityException;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import scala.Option;
import views.html.statistics;

public class YoutubeController extends Controller {

    public Result search(String query) {
        try {
            List<SearchResult> results = YoutubeService.searchResults(query, 10L);
            Option<List<SearchResult>> scalaResults;
            if (results.isEmpty()) {
                scalaResults = Option.empty();
            } else {
                scalaResults = OptionConverters.toScala(Optional.of(results));
            }
            return ok(search.render(scalaResults, query));
        } catch (IOException e) {
            return internalServerError("Error occurred while searching YouTube: " + e.getMessage());
        } catch (GeneralSecurityException e) {
            throw new RuntimeException(e);
        }
    }

    public Result searchForm() {
        Optional<List<SearchResult>> results = Optional.empty();
        return ok(search.render(OptionConverters.toScala(results), ""));
    }

    public Result getStatistics(String query) {
        // Get word frequency data
        Map<String, Long> wordFrequency = YoutubeService.getWordFrequency(query);

        // Render the statistics page
        return ok(statistics.render(query, wordFrequency));
    }

}
