package models;

import java.util.Arrays;
import java.util.stream.Stream;

public class FleschReadingEaseScore {

    private double readingEaseScore;
    private double gradeLevel;

    public FleschReadingEaseScore(String description) {
        int totalWords = description.split(" ").length;
        String[] sentences = description.split("\\. ");
        int totalSentences = sentences.length;
        int syllablesCount = 1;
        //int syllablesCount = countSentenceSyllables(description);


        this.readingEaseScore = 206.835 - 1.015 * ( totalWords / totalSentences ) - 84.6 * ( syllablesCount / totalWords );;
        this.gradeLevel = 0.39 * ( totalWords / totalSentences ) + 11.8 * ( syllablesCount / totalWords ) - 15.59;
    }

    public double getReadingEaseScore() {
        return readingEaseScore;
    }

    public double getGradeLevel() {
        return gradeLevel;
    }

    private int countSentenceSyllables(String sentence) {
        return Arrays.stream(sentence.split(" "))
                .map(this::countWordSyllables)
                .mapToInt(Integer::intValue)
                .sum();
    }

    public int countWordSyllables(String word) {
        // first remove last letter if it is e
        if (word.isEmpty()) return 0;
        word = word.toLowerCase();
        word = word.replaceAll("e$", "");

        String trimmedWord = Arrays.stream(word.split(""))
                .reduce("", (string, letter) ->  {

                    // if there are two adjacent vowels
                    if(string.isEmpty()) return string;
                    if (isVowel(letter.charAt(0)) && isVowel(string.charAt(string.length() - 1))) {
                        return string;
                    }
                    return string.concat(letter);
                });
        int syllables = (int)Arrays.stream(trimmedWord.split("")).filter((x) -> {
            return isVowel(x.charAt(0));
        }).count();


        return Math.max(syllables, 1);
    }

    private String vowels = "aeiouy";
    private boolean isVowel(char letter) {
        return vowels.contains(Character.toString(letter));
    }

    public Stream<String> getSentences(String description)
    {
        return Arrays.stream(description.split("\\. "));
    }

    public Stream<String> getSentenceWords(String sentence)
    {
        return Arrays.stream(sentence.split(" "));
    }
}
