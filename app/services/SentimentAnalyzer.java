package services;

import models.Video;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.*;

public class SentimentAnalyzer {
    public static String analyzeSentiment(List<Video> videos) {
        int happyCount = 0;
        int sadCount = 0;

        for (Video video : videos) {
            String description = video.getDescription();
            String[] words = description.toLowerCase().split("\\W+");

            for (String word : words) {
                if (SentimentWords.HAPPY_WORDS.contains(word)) {
                    happyCount++;
                } else if (SentimentWords.SAD_WORDS.contains(word)) {
                    sadCount++;
                }
            }

            // Debugging: Print counts for each video
            System.out.println("Video: " + video.getTitle());
            System.out.println("Happy Count: " + happyCount + ", Sad Count: " + sadCount);
        }

        // Calculate percentages
        double totalSentimentWords = happyCount + sadCount;
        double happyPercentage = 0;
        double sadPercentage = 0;

        if (totalSentimentWords > 0) {
            happyPercentage = happyCount / totalSentimentWords;
            sadPercentage = sadCount / totalSentimentWords;
        }

        // Debugging: Print percentages
        System.out.println("Happy Percentage: " + happyPercentage + ", Sad Percentage: " + sadPercentage);

        // Determine overall sentiment
        if (happyPercentage > 0.7) {
            return ":-)";
        } else if (sadPercentage > 0.7) {
            return ":-(";
        } else {
            return ":-|";
        }
    }

}