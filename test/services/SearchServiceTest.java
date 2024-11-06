package services;

import models.Video;
import models.VideoList;
import models.VideoSearch;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;

import java.util.List;
import java.util.concurrent.CompletableFuture;

import static org.junit.Assert.*;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

/**
 * @author Laurent Voisard
 *
 * Test class for SearchService
 */
public class SearchServiceTest {

    private SearchService searchService;
    private YoutubeService youtubeService;

    /**
     * @author Laurent Voisard
     *
     * Setup function to prepare tests
     */
    @Before
    public void setUpBeforeClass() {

        youtubeService = Mockito.mock(YoutubeService.class);
        searchService = new SearchService(youtubeService);
        searchService.createSessionSearchList();

        VideoList videoList = new VideoList(List.of( new Video[] {
                new Video("1","1","1","1","1","1"),
                new Video("2","1","1","1","1","1"),
                new Video("3","1","1","1","1","1"),
                new Video("4","1","1","1","1","1"),
                new Video("5","1","1","1","1","1"),
                new Video("6","1","1","1","1","1"),
                new Video("7","1","1","1","1","1"),
                new Video("8","1","1","1","1","1"),
                new Video("9","1","1","1","1","1"),
                new Video("10","1","1","1","1","1"),
        }));

        VideoSearch search = new VideoSearch("test", videoList);


        when(youtubeService.searchResults(anyString(), anyLong()))
                    .thenReturn(CompletableFuture.completedFuture(videoList));

    }

    /**
     * @author Laurent Voisard
     *
     * validate that querying a new search returns a result
     */
    @Test
    public void testGetVideosForSearchTermNoExistingEqualSearchTerm() {
        searchService.searchKeywords("test", "0").join();
        assertEquals(1, searchService.getSessionSearchList("0").size());
    }

    /**
     * @author Laurent Voisard
     *
     * validate that querying an existing search returns the same result first
     */
    @Test
    public void testGetVideosForSearchTermWithExistingEqualSearchTerm() {
        VideoSearch first = searchService.searchKeywords("test", "0").join().get(0);
        searchService.searchKeywords("test-2", "0").join();
        int before = searchService.getSessionSearchList("0").size();
        searchService.searchKeywords("test", "0").join();
        assertEquals(before, searchService.getSessionSearchList("0").size());
        assertEquals(first, searchService.getSessionSearchList("0").get(0));
    }

    /**
     * @author Laurent Voisard
     *
     * validate that when more than 10 searches are made, the first one gets erased
     */
    @Test
    public void testGetVideosForSearchTermMaximumCapacity() {
        searchService.searchKeywords(String.format("test-%d", 0), "0").join();
        for (int i = 1; i < 11; i++) {
            searchService.searchKeywords(String.format("test-%d", i), "0").join();
        }
        List<VideoSearch> v = searchService.getSessionSearchList("0");
        assertTrue(searchService.getSessionSearchList("0").stream().noneMatch(x -> x.getSearchTerms().equals("test-0")));
    }

    /**
     * @author Laurent Voisard
     *
     * tests getting a video by id
     */
    @Test
    public void testGetVideoById() {
        Video vid = new Video("id", "", "","", "","");
        when(youtubeService.getVideo("id")).thenReturn(CompletableFuture.completedFuture(vid));
        Video v = searchService.getVideoById("id").join();
        assertEquals(vid, v);
    }
}
