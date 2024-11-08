package models;

import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.assertEquals;

/**
 * @author Laurent Voisard
 * Test class to validate the behavior of VideoSearch modell
 */
public class VideoSearchTest {
    /**
     * @author Laurent Voisard
     *
     * Test constructor and getters
     */
    @Test
    public void testVideoSearchCtor() {
        String keywords = "Java Swing";
        VideoList videoList = new VideoList(
                List.of(new Video[]{
                        new Video("1", "1", "My pants were falling down so I added a belt.", "1", "1", "1"),
                        new Video("2", "2", "The Lord of the Rings is an epic high fantasy novel by the English author and scholar JRR Tolkien. Set in Middle-earth, the story began as a sequel to Tolkien's 1937 children's book The Hobbit, but eventually developed into a much larger work. Written in stages between 1937 and 1949, The Lord of the Rings is one of the best-selling books ever written, with over 150 million copies sold.", "2", "2", "2"),
                })
        );

        VideoSearch videoSearch = new VideoSearch(keywords, videoList, "");
        assertEquals(keywords, videoSearch.getSearchTerms());
        assertEquals(videoList, videoSearch.getResults());
    }

    /**
     * @author Laurent Voisard
     *
     * Test flesh average scores
     */
    @Test
    public void testVideoSearchFleshScoreAverages() {
        final String keywords = "Java Swing";
        final double expectedGradeAverage = 6.8;
        final double expectedReadingScoreAverage = 75.9;
        VideoList videoList = new VideoList(
                List.of(new Video[]{
                        new Video("1", "1", "My pants were falling down so I added a belt.", "1", "1", "1"),
                        new Video("2", "2", "The Lord of the Rings is an epic high fantasy novel by the English author and scholar JRR Tolkien. Set in Middle-earth, the story began as a sequel to Tolkien's 1937 children's book The Hobbit, but eventually developed into a much larger work. Written in stages between 1937 and 1949, The Lord of the Rings is one of the best-selling books ever written, with over 150 million copies sold.", "2", "2", "2"),
                })
        );

        VideoSearch videoSearch = new VideoSearch(keywords, videoList, "");
        assertEquals(expectedReadingScoreAverage, videoSearch.getFleschEaseScoreAverage(), 1.0);
        assertEquals(expectedGradeAverage, videoSearch.getFleschGradeLevelAverage(), 0.2);
    }

}
