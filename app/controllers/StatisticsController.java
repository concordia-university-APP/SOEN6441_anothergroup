package controllers;

import play.mvc.Controller;
import play.mvc.Result;
import services.StatisticsService;
import views.html.statistics;

import java.util.Map;

/**
 * Author : Tanveer Reza
 * Version : 1
 * Controller that handles everything related to Statistics Service
 */
public class StatisticsController extends Controller {
    private final StatisticsService statisticsService;

    public StatisticsController() {
        this.statisticsService = new StatisticsService(); // Manually create an instance
    }

    public StatisticsController(StatisticsService statisticsService) {
        this.statisticsService = new StatisticsService(); // Manually create an instance
    }
    public Result getStatistics(String query) {
        // Generate or retrieve word frequency data based on the query.
        Map<String, Long> wordFrequency = statisticsService.getWordFrequency(query);

        // Pass the word frequency data to the view.
        return ok(statistics.render(wordFrequency, query));
    }
}
