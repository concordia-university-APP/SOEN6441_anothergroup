package controllers;

import play.mvc.Controller;
import play.mvc.Result;
import services.YoutubeService;
import views.html.statistics;

import java.util.Map;

public class StatisticsController extends Controller {
    public Result getStatistics(String query) {
        // Generate or retrieve word frequency data based on the query.
        Map<String, Long> wordFrequency = YoutubeService.getWordFrequency(query);

        // Pass the word frequency data to the view.
        return ok(statistics.render(wordFrequency, query));
    }
}
