package models;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * @author Laurent Voisard
 *
 * Class to compute Flesch reading score
 */
public class FleschReadingEaseScore {

    private final String vowels = "aeiouy";
    private final double readingEaseScore;
    private final double gradeLevel;

    /**
     * @author Laurent Voisard
     * Constructor for the Flesh reading score
     * @param description the description of the video
     */
    public FleschReadingEaseScore(String description) {

        if (description.isEmpty()) {
            readingEaseScore= 0;
            gradeLevel= 0;
            return;
        }
        description = description.replaceAll("/[^A-Za-z,.!?]/", "");
        List<String> words = getDescriptionWords (description);
        List<String> sentences = getSentences(description);

        int totalSentences = Math.max(1, sentences.size());
        int syllablesCount = countSentenceSyllables(description);

        double score = Math.round((206.835 - 1.015 * ((double) words.size() / totalSentences)  - 84.6 * ( (double) syllablesCount / words.size() )) * 10) / 10.0;
        double grade = Math.round((0.39 * ((double) words.size() / totalSentences ) + 11.8 *  ((double) syllablesCount / words.size()) - 15.59) * 10) / 10.0;
        this.readingEaseScore = Math.min(100, Math.max(score, 0));
        this.gradeLevel = Math.max(grade, 0);
    }

    /**
     * @author Laurent Voisard
     * Getter for reading ease score
     * @return reading ease score
     */
    public double getReadingEaseScore() {
        return readingEaseScore;
    }

    /**
     * @author Laurent Voisard
     * Getter for grade level
     * @return grade level
     */
    public double getGradeLevel() {
        return gradeLevel;
    }

    /**
     * @author Laurent Voisard
     * Check if letter is a vowel
     * @return if the letter is a vowel
     */
    private boolean isVowel(char letter) {
        return vowels.contains(Character.toString(letter));
    }

    /**
     * @author Laurent Voisard
     * @param sentence one sentence of the description
     * @return number of syllables in the sentence
     */
    private int countSentenceSyllables(String sentence) {
        return Arrays.stream(sentence.split(" "))
                .map(this::countWordSyllables)
                .mapToInt(Integer::intValue)
                .sum();
    }

    /**
     * @author Laurent Voisard
     * @param word one word of the description
     * @return number of syllables in the word
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
     * @author Laurent Voisard
     * @param description description of a video
     * @return separated sentences from the description
     */
    private List<String> getSentences(String description)
    {
        return Arrays.stream(description.split("[.!?]"))
                .map(String::trim)
                .collect(Collectors.toList());
    }

    /**
     * @author Laurent Voisard
     * @param description get the description words
     * @return list of words contained in the description
     */
    private List<String> getDescriptionWords(String description)
    {
        return Arrays.asList(description.replaceAll("[.!?]", "").split("\\s+"));
    }
}
