package services;

import models.Video;
import java.util.List;
import java.util.stream.Collectors;
import java.util.Arrays;

/**
 * @author Rumeysa Turkmen
 * this class implements the logic for sentiment analysis.
 */
public class SentimentAnalyzer {
    public static String analyzeSentiment(List<Video> videos) {
        if (videos.isEmpty()) {
            return ":-|"; // Return neutral if no videos are provided
        }

        // Process each video to determine its sentiment
        List<String> videoSentiments = videos.stream().map(video -> {
            String description = video.getDescription();
            long happyCount = SentimentWords.HAPPY_WORDS.stream()
                    .filter(description.toLowerCase()::contains)
                    .count();
            long sadCount = SentimentWords.SAD_WORDS.stream()
                    .filter(description.toLowerCase()::contains)
                    .count();

            double totalSentimentWords = happyCount + sadCount;
            if (totalSentimentWords == 0) {
                return ":-|"; // Neutral if no happy/sad words found
            }

            double happyPercentage = (double) happyCount / totalSentimentWords;
            double sadPercentage = (double) sadCount / totalSentimentWords;

            if (happyPercentage > 0.7) {
                return ":-)";
            } else if (sadPercentage > 0.7) {
                return ":-(";
            } else {
                return ":-|";
            }
        }).collect(Collectors.toList());

        // Count how many videos have each sentiment
        long happyVideos = videoSentiments.stream().filter(":-)"::equals).count();
        long sadVideos = videoSentiments.stream().filter(":-("::equals).count();
        long neutralVideos = videoSentiments.stream().filter(":-|"::equals).count();

        // Determine overall sentiment based on majority
        if (happyVideos > sadVideos && happyVideos > neutralVideos) {
            return ":-)";
        } else if (sadVideos > happyVideos && sadVideos > neutralVideos) {
            return ":-(";
        } else {
            return ":-|";
        }
    }
}
