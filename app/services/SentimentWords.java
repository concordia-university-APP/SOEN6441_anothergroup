package services;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

/**
 * This class contains sets of words associated with different sentiments.
 * @author Rumeysa Turkmen
 */
public class SentimentWords {
    public static final Set<String> HAPPY_WORDS = new HashSet<>(Arrays.asList(
            "happy", "joy", "love", "great", "wonderful", "amazing", "fantastic", "awesome",
            ":)", ":D", ":-)", "joyful", "bliss", "delight", "ecstatic", "excited", "pleased",
            "satisfied", "content", "glad", "cheerful", "smile", "laugh", "funny", "hilarious",
            "blessed", "lively", "hopeful", "sunny", "bright", "positive", "yay", "adore", "celebrate"
    ));

    public static final Set<String> SAD_WORDS = new HashSet<>(Arrays.asList(
            "sad", "bad", "hate", "terrible", "awful", "worst", "cry", ":-(", ":(", "T_T",
            "depressed", "angry", "frustrated", "heartbroken", "sorrow", "miserable", "tearful",
            "lonely", "painful", "tragic", "disappointed", "gloomy", "hopeless", "empty", "lost",
            "downcast", "grief", "regret", "devastated", "melancholy", "trouble", "distress", "upset"
    ));


    // Method to check if a word is a happy word
    public static boolean isHappyWord(String word) {
        return HAPPY_WORDS.contains(word.toLowerCase());
    }

    // Method to check if a word is a sad word
    public static boolean isSadWord(String word) {
        return SAD_WORDS.contains(word.toLowerCase());
    }

    // Method to count the number of happy words in a description
    public static int countHappyWords(String description) {
        int count = 0;
        for (String word : description.split(" ")) {
            if (isHappyWord(word)) {
                count++;
            }
        }
        return count;
    }

    // Method to count the number of sad words in a description
    public static int countSadWords(String description) {
        int count = 0;
        for (String word : description.split(" ")) {
            if (isSadWord(word)) {
                count++;
            }
        }
        return count;
    }

    // Method to analyze the sentiment of a description
    public static String analyzeSentiment(String description) {
        int happyCount = countHappyWords(description);
        int sadCount = countSadWords(description);

        if (happyCount > sadCount) {
            return ":-)";
        } else if (sadCount > happyCount) {
            return ":-(";
        } else {
            return ":-|";
        }
    }
}
