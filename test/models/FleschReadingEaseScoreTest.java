package models;



import org.junit.Test;

import static org.junit.Assert.assertEquals;

/**
 * @author Laurent Voisard
 * Unit test class for flesch reading score feature
 * all tests have about a 1% precision fault
 */
public class FleschReadingEaseScoreTest {

    /**
     * @author Laurent Voisard
     *
     * Test that the scores are accurate, multi sentences
     */
    @Test
    public void testComputeFleschReadingEaseScore1() {
        final double expectedGradeLevel = 11.1;
        final double expectedReadingScore = 56.6;
        String description = "The Lord of the Rings is an epic high fantasy novel by the English author and scholar JRR Tolkien. Set in Middle-earth, the story began as a sequel to Tolkien's 1937 children's book The Hobbit, but eventually developed into a much larger work. Written in stages between 1937 and 1949, The Lord of the Rings is one of the best-selling books ever written, with over 150 million copies sold.";
        FleschReadingEaseScore fleschReadingEaseScore = new FleschReadingEaseScore(description);


        assertEquals(expectedGradeLevel, fleschReadingEaseScore.getGradeLevel(), 0.2);
        assertEquals(expectedReadingScore, fleschReadingEaseScore.getReadingEaseScore(), 1.0);
    }

    /**
     * @author Laurent Voisard
     *
     * Test that the scores are accurate, on sentence
     */
    @Test
    public void testComputeFleschReadingEaseScore2() {
        final double expectedGradeLevel = 2.5;
        final double expectedReadingScore = 95.2;
        String s = "My pants were falling down so I added a belt.";
        FleschReadingEaseScore fleschReadingEaseScore = new FleschReadingEaseScore(s);

        assertEquals(expectedGradeLevel, fleschReadingEaseScore.getGradeLevel(), 0.2);
        assertEquals(expectedReadingScore, fleschReadingEaseScore.getReadingEaseScore(), 1.0);

    }

    /**
     * @author Laurent Voisard
     *
     * Test that the scores are 0 when the description is empty
     */
    @Test
    public void testComputeFleschReadingEaseEmpty() {
        final double expectedGradeLevel = 0;
        final double expectedReadingScore = 0;
        String s = "";
        FleschReadingEaseScore fleschReadingEaseScore = new FleschReadingEaseScore(s);

        assertEquals(expectedGradeLevel, fleschReadingEaseScore.getGradeLevel(), 0.2);
        assertEquals(expectedReadingScore, fleschReadingEaseScore.getReadingEaseScore(), 1.0);

    }

    /**
     * @author Laurent Voisard
     *
     * Test that the scores are 100 when a description is e
     */
    @Test
    public void testComputeFleschReadingEaseOnlyE() {
        final double expectedGradeLevel = 0;
        final double expectedReadingScore = 100;
        String s = "e";
        FleschReadingEaseScore fleschReadingEaseScore = new FleschReadingEaseScore(s);

        assertEquals(expectedGradeLevel, fleschReadingEaseScore.getGradeLevel(), 0.2);
        assertEquals(expectedReadingScore, fleschReadingEaseScore.getReadingEaseScore(), 1.0);

    }
}
