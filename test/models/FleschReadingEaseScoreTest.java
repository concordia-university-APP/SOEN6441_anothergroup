package models;


import org.junit.Assert;
import org.junit.Test;

import java.util.List;


import static org.junit.Assert.fail;

public class FleschReadingEaseScoreTest {

    @Test
    public void testCountSyllablesInAWord1() {
        final int expected = 4;
        String word = "Constitution";
        FleschReadingEaseScore fleschReadingEaseScore = new FleschReadingEaseScore("a a");
        int result = fleschReadingEaseScore.countWordSyllables(word);

        Assert.assertEquals(expected, result);
    }

    @Test
    public void testCountSyllablesInAWord2() {
        final int expected = 1;
        String word = "Joe";
        FleschReadingEaseScore fleschReadingEaseScore = new FleschReadingEaseScore("a a");
        int result = fleschReadingEaseScore.countWordSyllables(word);

        Assert.assertEquals(expected, result);
    }

    @Test
    public void testCountSyllablesInAWord3() {
        final int expected = 2;
        String word = "Barbie";
        FleschReadingEaseScore fleschReadingEaseScore = new FleschReadingEaseScore("a a");
        int result = fleschReadingEaseScore.countWordSyllables(word);

        Assert.assertEquals(expected, result);
    }

    @Test
    public void testCountSyllablesInAWord4() {
        final int expected = 3;
        String word = "Beautiful";
        FleschReadingEaseScore fleschReadingEaseScore = new FleschReadingEaseScore("a a");
        int result = fleschReadingEaseScore.countWordSyllables(word);

        Assert.assertEquals(expected, result);
    }

    @Test
    public void testComputeFleschReadingEaseScore() {
        final double expectedGradeLevel = 11.1;
        final double expectedReadingScore = 56.6;
        String description = "The Lord of the Rings is an epic high fantasy novel by the English author and scholar JRR Tolkien. Set in Middle-earth, the story began as a sequel to Tolkien's 1937 children's book The Hobbit, but eventually developed into a much larger work. Written in stages between 1937 and 1949, The Lord of the Rings is one of the best-selling books ever written, with over 150 million copies sold.";
        FleschReadingEaseScore fleschReadingEaseScore = new FleschReadingEaseScore(description);


        Assert.assertEquals(expectedGradeLevel, fleschReadingEaseScore.getGradeLevel(), 0.2);
        Assert.assertEquals(expectedReadingScore, fleschReadingEaseScore.getReadingEaseScore(), 1.0);
    }

    @Test
    public void testComputeFleschReadingEaseScore2() {
        String s = "My name is Jeff .";
        FleschReadingEaseScore fleschReadingEaseScore = new FleschReadingEaseScore(s);

        fleschReadingEaseScore.getSentences(s);
        Assert.assertEquals(1, fleschReadingEaseScore.getSentences(s).size());
        Assert.assertEquals(4, fleschReadingEaseScore.getSentenceWords(s).size());

    }

    @Test
    public void testComputeFleschReadingEaseScore3() {
        String s = "My name is Jeff, but call me Jeffery? Hoes are bad. truly";
        FleschReadingEaseScore fleschReadingEaseScore = new FleschReadingEaseScore(s);

        List<String> sentences = fleschReadingEaseScore.getSentences(s);
        List<String> words = fleschReadingEaseScore.getSentenceWords(s);
        Assert.assertEquals(3, sentences.size());
        Assert.assertEquals(12, words.size());

        int syllableCount = words.stream()
                .map(fleschReadingEaseScore::countWordSyllables)
                .mapToInt(Integer::intValue)
                .sum();
        Assert.assertEquals(15, syllableCount);

    }

    @Test
    public void testComputeFleschReadingEaseScore5() {
        final double expectedGradeLevel = 2.5;
        final double expectedReadingScore = 95.2;
        String s = "My pants were falling down so I added a belt.";
        FleschReadingEaseScore fleschReadingEaseScore = new FleschReadingEaseScore(s);

        Assert.assertEquals(expectedGradeLevel, fleschReadingEaseScore.getGradeLevel(), 0.2);
        Assert.assertEquals(expectedReadingScore, fleschReadingEaseScore.getReadingEaseScore(), 1.0);

    }
}
