package actors;

import akka.actor.AbstractActor;
import akka.actor.Props;
import services.SearchService;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class FleschReadingActor extends AbstractActor {
    private final String vowels = "aeiouy";

    /**
     * Factory method to create Props for SearchServiceActor.
     *
     * @return Props for creating FleschReadingActor
     * @author Laurent Voisard
     */
    public static Props props() {
        return Props.create(FleschReadingActor.class, FleschReadingActor::new);
    }

    @Override
    public Receive createReceive() {
        return receiveBuilder()
                .match(FleschReadingActor.GetReadingEaseScore.class, message -> {
                    ReadingEaseScoreResult result = this.calculateReadingEaseScore(message);
                    getSender().tell(result, self());
                })
                .build();
    }

    private ReadingEaseScoreResult calculateReadingEaseScore(GetReadingEaseScore message) {
        double readingEaseScore = 0.0;
        double gradeLevel = 0.0;

        if (message.description.isEmpty()) {
            readingEaseScore= 0;
            gradeLevel= 0;
            return new ReadingEaseScoreResult(readingEaseScore, gradeLevel);
        }
        String description = message.description.replaceAll("/[^A-Za-z,.!?]/", "");
        List<String> words = getDescriptionWords (description);
        List<String> sentences = getSentences(description);

        int totalSentences = Math.max(1, sentences.size());
        int syllablesCount = countSentenceSyllables(description);

        double score = Math.round((206.835 - 1.015 * ((double) words.size() / totalSentences)  - 84.6 * ( (double) syllablesCount / words.size() )) * 10) / 10.0;
        double grade = Math.round((0.39 * ((double) words.size() / totalSentences ) + 11.8 *  ((double) syllablesCount / words.size()) - 15.59) * 10) / 10.0;
        readingEaseScore = Math.min(100, Math.max(score, 0));
        gradeLevel = Math.max(grade, 0);
        return new ReadingEaseScoreResult(readingEaseScore, gradeLevel);
    }

    /**
     * @param sentence one sentence of the description
     * @return number of syllables in the sentence
     * @author Laurent Voisard
     */
    private int countSentenceSyllables(String sentence) {
        return Arrays.stream(sentence.split(" "))
                .map(this::countWordSyllables)
                .mapToInt(Integer::intValue)
                .sum();
    }

    /**
     * @param word one word of the description
     * @return number of syllables in the word
     * @author Laurent Voisard
     */
    private int countWordSyllables(String word) {

        word = word.toLowerCase();
        // first remove last letter if it is e
        word = word.replaceAll("e$", "");

        if (word.isEmpty()) return 1;

        // remove when E is at last position of a word since it doesn't count

        String trimmedWord = Arrays.stream(word.split(""))
                .reduce("", (string, letter) -> {
                    // if there are two adjacent vowels
                    if (!string.isEmpty() && isVowel(letter.charAt(0)) && isVowel(string.charAt(string.length() - 1))) {
                        return string;
                    }
                    return string.concat(letter);
                });

        int syllables = (int) Arrays.stream(trimmedWord.split(""))
                .filter((x) -> isVowel(x.charAt(0)))
                .count();


        return Math.max(syllables, 1);
    }

    /**
     * @param description description of a video
     * @return separated sentences from the description
     * @author Laurent Voisard
     */
    private List<String> getSentences(String description) {
        return Arrays.stream(description.split("[.!?]"))
                .map(String::trim)
                .collect(Collectors.toList());
    }

    /**
     * @param description get the description words
     * @return list of words contained in the description
     * @author Laurent Voisard
     */
    private List<String> getDescriptionWords(String description) {
        return Arrays.asList(description.replaceAll("[.!?]", "").split("\\s+"));
    }


    public interface Message {
    }

    public static class GetReadingEaseScore implements Message {
        public final String description;

        public GetReadingEaseScore(String description) {
            this.description = description;
        }
    }

    public static class ReadingEaseScoreResult {
        public final double easeScore;
        public final double gradeLevel;

        public ReadingEaseScoreResult(double easeScore, double gradeLevel) {
            this.easeScore = easeScore;
            this.gradeLevel = gradeLevel;
        }
    }

    /**
     * @author Laurent Voisard
     * Check if letter is a vowel
     * @param letter letter to check
     * @return if the letter is a vowel
     */
    private boolean isVowel(char letter) {
        return vowels.contains(Character.toString(letter));
    }
}
