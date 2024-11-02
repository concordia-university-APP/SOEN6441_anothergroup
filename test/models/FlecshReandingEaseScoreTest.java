package models;


import org.junit.Assert;
import org.junit.Test;

import java.lang.reflect.Method;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;


import static org.junit.Assert.fail;

public class FlecshReandingEaseScoreTest {

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
    public void testComputeFleschReadingEaseScore() throws NoSuchMethodException {
        final double expectedGradeLevel = 11.1;
        final double expectedReadingScore = 56.6;
        String description = "The Lord of the Rings is an epic high fantasy novel by the English author and scholar JRR Tolkien. Set in Middle-earth, the story began as a sequel to Tolkien's 1937 children's book The Hobbit, but eventually developed into a much larger work. Written in stages between 1937 and 1949, The Lord of the Rings is one of the best-selling books ever written, with over 150 million copies sold.";
        FleschReadingEaseScore fleschReadingEaseScore = new FleschReadingEaseScore(description);

        Stream<String> s = fleschReadingEaseScore.getSentences(description);
        List<Integer> count = s.flatMap(fleschReadingEaseScore::getSentenceWords)
                .map(fleschReadingEaseScore::countWordSyllables)
                .collect(Collectors.toList());
//        Stream<Integer> firstSentence = w.get(0).map(fleschReadingEaseScore::countWordSyllables);
//        int total = w.stream()
//                .map(x -> {
//                    return x.map(fleschReadingEaseScore::countWordSyllables)
//                            .mapToInt(Integer::intValue)
//                            .sum();
//                }).mapToInt(Integer::intValue).sum();

        Assert.assertEquals(expectedGradeLevel, fleschReadingEaseScore.getGradeLevel(), 0.0);
        Assert.assertEquals(expectedReadingScore, fleschReadingEaseScore.getReadingEaseScore(), 0.0);
    }
}
