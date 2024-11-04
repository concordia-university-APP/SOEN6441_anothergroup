package models;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class FleschReadingEaseScore {

    private final String vowels = "aeiouy";
    private final double readingEaseScore;
    private final double gradeLevel;

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


        this.readingEaseScore = Math.round((206.835 - 1.015 * ((double) words.size() / totalSentences)  - 84.6 * ( (double) syllablesCount / words.size() )) * 10) / 10.0;
        this.gradeLevel = Math.round((0.39 * ((double) words.size() / totalSentences ) + 11.8 *  ((double) syllablesCount / words.size()) - 15.59) * 10) / 10.0;
    }

    public double getReadingEaseScore() {
        return readingEaseScore;
    }

    public double getGradeLevel() {
        return gradeLevel;
    }

    private boolean isVowel(char letter) {
        return vowels.contains(Character.toString(letter));
    }

    private int countSentenceSyllables(String sentence) {
        return Arrays.stream(sentence.split(" "))
                .map(this::countWordSyllables)
                .mapToInt(Integer::intValue)
                .sum();
    }

    private int countWordSyllables(String word) {
        // first remove last letter if it is e
        word = word.toLowerCase();

        // remove when E is at last position of a word since it doesn't count
        word = word.replaceAll("e$", "");

        String trimmedWord = Arrays.stream(word.split(""))
                .reduce("", (string, letter) ->  {
                    // if there are two adjacent vowels
                    if (!string.isEmpty() && isVowel(letter.charAt(0)) && isVowel(string.charAt(string.length() - 1))) {
                        return string;
                    }
                    return string.concat(letter);
                });

        int syllables = (int)Arrays.stream(trimmedWord.split(""))
                .filter((x) -> isVowel(x.charAt(0)))
                .count();


        return Math.max(syllables, 1);
    }

    private List<String> getSentences(String description)
    {
        return Arrays.stream(description.split("[.!?]"))
                .map(String::trim)
                .collect(Collectors.toList());
    }

    private List<String> getDescriptionWords(String description)
    {
        return Arrays.asList(description.replaceAll("[.!?]", "").split("\\s+"));
    }
}
