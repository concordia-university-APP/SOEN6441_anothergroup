package actors;

import akka.actor.ActorRef;
import akka.actor.ActorSystem;
import akka.actor.Status;
import akka.testkit.javadsl.TestKit;
import models.Video;
import models.VideoList;
import models.VideoSearch;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;
import services.SearchService;
import services.YoutubeService;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;

import static org.junit.Assert.assertEquals;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class SearchServiceActorTest {
    ActorSystem system;
    SearchService searchService;

    @Before
    public void setup() {
        system = ActorSystem.create();
        searchService = mock(SearchService.class);
    }

    @After
    public void teardown() {
        TestKit.shutdownActorSystem(system);
        system = null;
    }

    @Test
    public void searchServiceActorTest_searchKeywords() {
        VideoList vl = new VideoList(new ArrayList<Video>() {
            {
                add(new Video("test", "test", "Description Test", "", "", "", List.of("")));
            }
        });
        List<VideoSearch> response = List.of(new VideoSearch("joe", vl,":-)"));

        when(searchService.searchKeywords(anyString(), anyString())).thenReturn(CompletableFuture.completedFuture(response));

        final ActorRef subject = system.actorOf(SearchServiceActor.props(searchService));

        final TestKit probe = new TestKit(system);

        SearchServiceActor.SearchKeywords message = new SearchServiceActor.SearchKeywords("test", "0");
        subject.tell(message, probe.getRef());

        List<VideoSearch> actual = probe.expectMsg(Duration.ofSeconds(15), response);

    }

    @Test
    public void searchServiceActorTest_searchKeywords_exception() {
        when(searchService.searchKeywords(anyString(), anyString())).thenReturn(CompletableFuture.failedFuture(new RuntimeException("Test Exception")));

        final ActorRef subject = system.actorOf(SearchServiceActor.props(searchService));

        final TestKit probe = new TestKit(system);

        SearchServiceActor.SearchKeywords message = new SearchServiceActor.SearchKeywords("test", "0");
        subject.tell(message, probe.getRef());

        probe.expectMsgClass(Duration.ofSeconds(15), Status.Failure.class);
    }

    @Test
    public void searchServiceActorTest_getVideoById() {
        Video response = mock(Video.class);
        when(searchService.getVideoById(anyString())).thenReturn(CompletableFuture.completedFuture(response));

        final ActorRef subject = system.actorOf(SearchServiceActor.props(searchService));

        final TestKit probe = new TestKit(system);

        SearchServiceActor.GetVideoById message = new SearchServiceActor.GetVideoById("test");
        subject.tell(message, probe.getRef());

        Video actual = probe.expectMsg(Duration.ofSeconds(5), response);

    }

    @Test
    public void searchServiceActorTest_getVideoById_exception() {
        when(searchService.getVideoById(anyString())).thenReturn(CompletableFuture.failedFuture(new RuntimeException("Test Exception")));

        final ActorRef subject = system.actorOf(SearchServiceActor.props(searchService));

        final TestKit probe = new TestKit(system);

        SearchServiceActor.GetVideoById message = new SearchServiceActor.GetVideoById("test");
        subject.tell(message, probe.getRef());

        probe.expectMsgClass(Duration.ofSeconds(5), Status.Failure.class);

    }

    @Test
    public void searchServiceActorTest_getVideoBySearchTerm() {
        VideoList response = mock(VideoList.class);
        when(searchService.getVideosBySearchTerm(anyString(), anyString())).thenReturn(CompletableFuture.completedFuture(response));

        final ActorRef subject = system.actorOf(SearchServiceActor.props(searchService));

        final TestKit probe = new TestKit(system);

        SearchServiceActor.GetVideosBySearchTerm message = new SearchServiceActor.GetVideosBySearchTerm("test", "0");
        subject.tell(message, probe.getRef());

        VideoList actual = probe.expectMsg(Duration.ofSeconds(5), response);

    }

    @Test
    public void searchServiceActorTest_getVideoBySearchTerm_exception() {
        when(searchService.getVideosBySearchTerm(anyString(), anyString())).thenReturn(CompletableFuture.failedFuture(new RuntimeException("Test Exception")));

        final ActorRef subject = system.actorOf(SearchServiceActor.props(searchService));

        final TestKit probe = new TestKit(system);

        SearchServiceActor.GetVideosBySearchTerm message = new SearchServiceActor.GetVideosBySearchTerm("test", "0");
        subject.tell(message, probe.getRef());

        probe.expectMsgClass(Duration.ofSeconds(5), Status.Failure.class);

    }

    @Test
    public void searchServiceActorTest_getUserSearchList() {
        VideoList vl = new VideoList(new ArrayList<Video>() {
            {
                add(new Video("test", "test", "Description Test", "", "", "", List.of("")));
            }
        });
        List<VideoSearch> response = List.of(new VideoSearch("joe", vl,":-)"));

        when(searchService.getSessionSearchList(anyString())).thenReturn(response);

        final ActorRef subject = system.actorOf(SearchServiceActor.props(searchService));

        final TestKit probe = new TestKit(system);

        SearchServiceActor.GetUserSearchList message = new SearchServiceActor.GetUserSearchList("0");
        subject.tell(message, probe.getRef());

        List<VideoSearch> actual = probe.expectMsg(Duration.ofSeconds(5), response);

    }

    @Test
    public void searchServiceActorTest_updateUserSearchList() {
        VideoList vl = new VideoList(new ArrayList<Video>() {
            {
                add(new Video("test", "test", "Description Test", "", "", "", List.of("")));
            }
        });
        List<VideoSearch> response = List.of(new VideoSearch("joe", vl,":-)"));

        when(searchService.updateSearches(anyString())).thenReturn(CompletableFuture.completedFuture(response));

        final ActorRef subject = system.actorOf(SearchServiceActor.props(searchService));

        final TestKit probe = new TestKit(system);

        SearchServiceActor.UpdateUserSearchList message = new SearchServiceActor.UpdateUserSearchList("0");
        subject.tell(message, probe.getRef());

        List<VideoSearch> actual = probe.expectMsg(Duration.ofSeconds(5), response);

    }

    @Test
    public void searchServiceActorTest_updateUserSearchList_exception() {
        when(searchService.updateSearches(anyString())).thenReturn(CompletableFuture.failedFuture(new RuntimeException("Test Exception")));

        final ActorRef subject = system.actorOf(SearchServiceActor.props(searchService));

        final TestKit probe = new TestKit(system);

        SearchServiceActor.UpdateUserSearchList message = new SearchServiceActor.UpdateUserSearchList("0");
        subject.tell(message, probe.getRef());

        probe.expectMsgClass(Duration.ofSeconds(5), Status.Failure.class);
    }


}
