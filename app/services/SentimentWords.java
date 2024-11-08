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
            "blessed", "lively", "hopeful", "sunny", "bright", "positive", "yay", "adore", "celebrate", "like", "fun", "christmas", "relax", "sleep", "cat",
            "dog", "cozy", "cheer", "tranquil", "epic", "serene", "natural", "beauty", "peace", "charm", "fall",
            "autumn", "sky", "thrill", "lovers", "clouds", "cloud", "spring", "nature", "motivation", "motivates",
            "game", "gaming", "warm"
    ));

    public static final Set<String> SAD_WORDS = new HashSet<>(Arrays.asList(
            "sad", "bad", "hate", "terrible", "awful", "worst", "cry", ":-(", ":(", "T_T",
            "depressed", "angry", "frustrated", "heartbroken", "sorrow", "miserable", "tearful",
            "lonely", "painful", "tragic", "disappointed", "gloomy", "hopeless", "empty", "lost",
            "downcast", "grief", "regret", "devastated", "melancholy", "trouble", "distress", "upset", "dead", "death", "global warming",
            "stress", "anxiety", "fail", "sorrowful", "negative", "shocking", "climate change", "extreme", "cold", "season change", "shocked"
    ));
}
