package actors;

import akka.actor.ActorRef;
import akka.actor.ActorSystem;
import akka.testkit.javadsl.TestKit;
import models.Video;
import models.VideoList;
import models.YoutubeChannel;
import org.checkerframework.checker.units.qual.A;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import services.YoutubeService;

import java.io.IOException;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;

import static org.junit.Assert.assertEquals;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class YoutubeServiceActorTest {
    ActorSystem system;
    YoutubeService youtubeService;

    @Before
    public void setup() {
        system = ActorSystem.create();
        youtubeService = mock(YoutubeService.class);
    }

    @After
    public void teardown() {
        TestKit.shutdownActorSystem(system);
        system = null;
    }

    @Test
    public void testYoutubeServiceActor_getVideo() {
        Video response = new Video("test", "test video", "", "", "", "", List.of(""));
        when(youtubeService.getVideo("test")).thenReturn(CompletableFuture.completedFuture(response));

        final ActorRef subject = system.actorOf(YoutubeServiceActor.props(youtubeService));

        final TestKit probe = new TestKit(system);

        YoutubeServiceActor.GetVideo message = new YoutubeServiceActor.GetVideo("test");
        subject.tell(message, probe.getRef());

        Video actual = probe.expectMsgClass(Duration.ofSeconds(5), Video.class);

        assertEquals(response, actual);
    }

    @Test
    public void testYoutubeServiceActor_getVideo_exception() {
        when(youtubeService.getVideo("test")).thenReturn(CompletableFuture.failedFuture(new RuntimeException("Test Exception")));

        final ActorRef subject = system.actorOf(YoutubeServiceActor.props(youtubeService));

        final TestKit probe = new TestKit(system);

        YoutubeServiceActor.GetVideo message = new YoutubeServiceActor.GetVideo("test");
        subject.tell(message, probe.getRef());

        probe.expectMsgClass(Duration.ofSeconds(5), akka.actor.Status.Failure.class);

    }

    @Test
    public void testYoutubeServiceActor_searchVideos() {
        VideoList response = mock(VideoList.class);
        when(youtubeService.searchResults(anyString(), anyLong())).thenReturn(CompletableFuture.completedFuture(response));

        final ActorRef subject = system.actorOf(YoutubeServiceActor.props(youtubeService));

        final TestKit probe = new TestKit(system);

        YoutubeServiceActor.SearchVideos message = new YoutubeServiceActor.SearchVideos("test", 10L);
        subject.tell(message, probe.getRef());

        VideoList actual = probe.expectMsgClass(Duration.ofSeconds(5), VideoList.class);

    }

    @Test
    public void testYoutubeServiceActor_searchVideos_exception() {
        when(youtubeService.searchResults(anyString(), anyLong()))
                .thenReturn(CompletableFuture.failedFuture(new RuntimeException("Error occurred")));

        final ActorRef subject = system.actorOf(YoutubeServiceActor.props(youtubeService));

        final TestKit probe = new TestKit(system);

        YoutubeServiceActor.SearchVideos message = new YoutubeServiceActor.SearchVideos("test", 10L);
        subject.tell(message, probe.getRef());

        probe.expectMsgClass(Duration.ofSeconds(5), akka.actor.Status.Failure.class);

    }

    @Test
    public void testYoutubeServiceActor_getChannelVideos() throws IOException {
        List<Video> response = new ArrayList<>() {
            {
                add(new Video("test", "test video", "", "", "", "", List.of("")));
            }
        };
        when(youtubeService.getChannelVideos(anyString())).thenReturn(CompletableFuture.completedFuture(response));

        final ActorRef subject = system.actorOf(YoutubeServiceActor.props(youtubeService));

        final TestKit probe = new TestKit(system);

        YoutubeServiceActor.GetChannelVideos message = new YoutubeServiceActor.GetChannelVideos("test-channel");
        subject.tell(message, probe.getRef());

        List<Video> actual = probe.expectMsg(Duration.ofSeconds(10), response);

    }

    @Test
    public void testYoutubeServiceActor_getChannelVideos_exception() throws IOException {

        when(youtubeService.getChannelVideos(anyString())).thenReturn(CompletableFuture.failedFuture(new RuntimeException("Test Exception")));

        final ActorRef subject = system.actorOf(YoutubeServiceActor.props(youtubeService));

        final TestKit probe = new TestKit(system);

        YoutubeServiceActor.GetChannelVideos message = new YoutubeServiceActor.GetChannelVideos("test-channel");
        subject.tell(message, probe.getRef());

        probe.expectMsgClass(Duration.ofSeconds(10), akka.actor.Status.Failure.class);

    }

    @Test
    public void testYoutubeServiceActor_getChannelById() throws IOException {
        YoutubeChannel response = mock(YoutubeChannel.class);
        when(youtubeService.getChannelById(anyString())).thenReturn(CompletableFuture.completedFuture(response));

        final ActorRef subject = system.actorOf(YoutubeServiceActor.props(youtubeService));

        final TestKit probe = new TestKit(system);

        YoutubeServiceActor.GetChannelById message = new YoutubeServiceActor.GetChannelById("test-channel");
        subject.tell(message, probe.getRef());

        YoutubeChannel actual = probe.expectMsgClass(Duration.ofSeconds(5), YoutubeChannel.class);
    }

    @Test
    public void testYoutubeServiceActor_getChannelById_exception() throws IOException {
        when(youtubeService.getChannelById(anyString())).thenReturn(CompletableFuture.failedFuture(new RuntimeException("Test Exception")));

        final ActorRef subject = system.actorOf(YoutubeServiceActor.props(youtubeService));

        final TestKit probe = new TestKit(system);

        YoutubeServiceActor.GetChannelById message = new YoutubeServiceActor.GetChannelById("test-channel");
        subject.tell(message, probe.getRef());

        probe.expectMsgClass(Duration.ofSeconds(5), akka.actor.Status.Failure.class);
    }



}
