package services;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

public class SentimentWords {

    public static final Set<String> HAPPY_WORDS = new HashSet<>(Arrays.asList(
            "happy", "joy", "love", "great", "wonderful", "amazing", "fantastic", ":)", ":D", ":-)"
    ));

    public static final Set<String> SAD_WORDS = new HashSet<>(Arrays.asList(
            "sad", "bad", "hate", "terrible", "awful", "worst", "cry", ":-(", ":(", "T_T", "dead", "death"
    ));
}
