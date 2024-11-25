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
 * Test class for SearchService
 * @author Laurent Voisard
 */
public class SearchServiceTest {

    private SearchService searchService;
    private YoutubeService youtubeService;

    /**
     * Setup function to prepare tests
     * @author Laurent Voisard
     */
    @Before
    public void setUpBeforeClass() {

        youtubeService = Mockito.mock(YoutubeService.class);
        searchService = new SearchService(youtubeService);
        searchService.createSessionSearchList();

        VideoList videoList = new VideoList(List.of( new Video[] {
                new Video("1","1","1","1","1","1", List.of("#tag1", "#tag2")),
                new Video("2","2","1","1","1","1", List.of("#tag3", "#tag4")),
                new Video("3","3","1","1","1","1", List.of("#tag5")),
                new Video("4","4","1","1","1","1", List.of("#tag6")),
                new Video("5","5","1","1","1","1", List.of("#tag7")),
                new Video("6","6","1","1","1","1", List.of("#tag8")),
                new Video("7","7","1","1","1","1", List.of("#tag9")),
                new Video("8","8","1","1","1","1", List.of("#tag10")),
                new Video("9","9","1","1","1","1", List.of("#tag11")),
                new Video("10","10","1","1","1","1", List.of("#tag12")),
        }));

        VideoSearch search = new VideoSearch("test", videoList, "");


        when(youtubeService.searchResults(anyString(), anyLong()))
                    .thenReturn(CompletableFuture.completedFuture(videoList));

    }

    /**
     * validate that querying a new search returns a result
     * @author Laurent Voisard
     */
    @Test
    public void testGetVideosForSearchTermNoExistingEqualSearchTerm() {
        searchService.searchKeywords("test", "0").join();
        assertEquals(1, searchService.getSessionSearchList("0").size());
    }

    /**
     * validate that querying an existing search returns the same result first
     * @author Laurent Voisard
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
     * validate that when more than 10 searches are made, the first one gets erased
     * @author Laurent Voisard
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
     * tests getting a video by id from the youtube service
     * @author Laurent Voisard
     */
    @Test
    public void testGetVideoById() {
        Video vid = new Video("id", "", "","", "","", List.of("#exampleTag1", "#exampleTag2"));
        when(youtubeService.getVideo("id")).thenReturn(CompletableFuture.completedFuture(vid));
        Video v = searchService.getVideoById("id").join();
        assertEquals(vid, v);
    }

    /**
     * Tests the `getVideosBySearchTerm` method when an existing search term is used.
     * It verifies that the cached results are returned instead of making a new request.
     * @author Tanveer Reza
     */
    @Test
    public void testGetVideosBySearchTerm_existingSearch() {
        Video video1 = new Video("1","1","1","1","1","1", List.of("#tag1", "#tag2"));
        VideoList videoList = new VideoList(List.of(video1));
        VideoSearch videoSearch = new VideoSearch("test", videoList, "");
        searchService.getSessionSearchList("1").add(videoSearch);

        CompletableFuture<VideoList> result = searchService.getVideosBySearchTerm("test", "1");
        assertEquals(videoList, result.join());
    }

    /**
     * Tests the `getVideosBySearchTerm` method when a new search term is used.
     * It verifies that the correct number of videos are returned and that the titles match the expected values.
     * @author Tanveer Reza
     */
    @Test
    public void testGetVideosBySearchTerm_newSearch() {
        CompletableFuture<VideoList> result = searchService.getVideosBySearchTerm("newSearch", "1");
        VideoList videoList = result.join();

        assertEquals(10, videoList.getVideoList().size());
        assertEquals("1", videoList.getVideoList().get(0).getTitle());
        assertEquals("2", videoList.getVideoList().get(1).getTitle());
        assertEquals("3", videoList.getVideoList().get(2).getTitle());
        assertEquals("4", videoList.getVideoList().get(3).getTitle());
        assertEquals("5", videoList.getVideoList().get(4).getTitle());
        assertEquals("6", videoList.getVideoList().get(5).getTitle());
        assertEquals("7", videoList.getVideoList().get(6).getTitle());
        assertEquals("8", videoList.getVideoList().get(7).getTitle());
        assertEquals("9", videoList.getVideoList().get(8).getTitle());
        assertEquals("10", videoList.getVideoList().get(9).getTitle());
    }

    /**
     * validate that querying a new search returns a result
     * @author Laurent Voisard
     */
    @Test
    public void testRefreshSearch_noExistingSearches() {
        List<VideoSearch> res = searchService.updateSearches( "1").join();
        assertEquals(res.size(), 0);
    }

    /**
     * validate that querying a new search returns a result
     * @author Laurent Voisard
     */
    @Test
    public void testRefreshSearch_withExistingSearches() {
        searchService.searchKeywords("test", "0").join();
        List<VideoSearch> res = searchService.updateSearches( "0").join();

        assertNotEquals(res.size(), 0);
    }

    @Test
    public void testRefreshSearch_withExistingSearches_newResults() {
        searchService.searchKeywords("test", "0").join();

        VideoList newVideoList = new VideoList(List.of( new Video[] {

                new Video("11","11","1","1","1","1", List.of("#tag11")),
                new Video("12","12","1","1","1","1", List.of("#tag12")),
                new Video("13","13","1","1","1","1", List.of("#tag13")),
                new Video("1","1","1","1","1","1", List.of("#tag1", "#tag2")),
                new Video("2","2","1","1","1","1", List.of("#tag3", "#tag4")),
                new Video("3","3","1","1","1","1", List.of("#tag5")),
                new Video("4","4","1","1","1","1", List.of("#tag6")),
                new Video("5","5","1","1","1","1", List.of("#tag7")),
                new Video("6","6","1","1","1","1", List.of("#tag8")),
                new Video("7","7","1","1","1","1", List.of("#tag9")),
        }));

        when(youtubeService.searchResults(anyString(), anyLong())).thenReturn(CompletableFuture.completedFuture(newVideoList));
        List<VideoSearch> res = searchService.updateSearches( "0").join();

        assertNotEquals(res.size(), 0);
    }
}
