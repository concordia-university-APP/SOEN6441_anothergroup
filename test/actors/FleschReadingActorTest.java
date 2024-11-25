package actors;

import akka.actor.ActorRef;
import akka.actor.ActorSystem;
import akka.actor.Props;
import akka.testkit.javadsl.TestKit;
import org.junit.*;

import java.time.Duration;
import java.util.List;

import static org.junit.Assert.assertEquals;

public class FleschReadingActorTest {
    ActorSystem system;

    @Before
    public void setup() {
        system = ActorSystem.create();
    }

    @After
    public void teardown() {
        TestKit.shutdownActorSystem(system);
        system = null;
    }

    @Test
    public void testFleschReadingActor() {
        final ActorRef subject = system.actorOf(FleschReadingActor.props());

        final TestKit probe = new TestKit(system);
        FleschReadingActor.GetReadingEaseScore message = new FleschReadingActor.GetReadingEaseScore("I like to wear my hat backwards.");
        FleschReadingActor.ReadingEaseScoreResult result = new FleschReadingActor.ReadingEaseScoreResult(100, 0.1);
        subject.tell(message, probe.getRef());
        FleschReadingActor.ReadingEaseScoreResult actual = probe.expectMsgClass(Duration.ofSeconds(5), FleschReadingActor.ReadingEaseScoreResult.class);

        assertEquals(result.easeScore, actual.easeScore, 2.0);
        assertEquals(result.gradeLevel, actual.gradeLevel, 0.5);
    }

    @Test
    public void testFleschReadingActorEmptyDescription() {
        final ActorRef subject = system.actorOf(FleschReadingActor.props());

        final TestKit probe = new TestKit(system);
        FleschReadingActor.GetReadingEaseScore message = new FleschReadingActor.GetReadingEaseScore("");
        FleschReadingActor.ReadingEaseScoreResult result = new FleschReadingActor.ReadingEaseScoreResult(0, 0);
        subject.tell(message, probe.getRef());
        FleschReadingActor.ReadingEaseScoreResult actual = probe.expectMsgClass(Duration.ofSeconds(5), FleschReadingActor.ReadingEaseScoreResult.class);

        assertEquals(result.easeScore, actual.easeScore, 2.0);
        assertEquals(result.gradeLevel, actual.gradeLevel, 0.5);
    }

    @Test
    public void testFleschReadingActorE() {
        final ActorRef subject = system.actorOf(FleschReadingActor.props());

        final TestKit probe = new TestKit(system);
        FleschReadingActor.GetReadingEaseScore message = new FleschReadingActor.GetReadingEaseScore("e");
        FleschReadingActor.ReadingEaseScoreResult result = new FleschReadingActor.ReadingEaseScoreResult(100, 0);
        subject.tell(message, probe.getRef());
        FleschReadingActor.ReadingEaseScoreResult actual = probe.expectMsgClass(Duration.ofSeconds(5), FleschReadingActor.ReadingEaseScoreResult.class);

        assertEquals(result.easeScore, actual.easeScore, 2.0);
        assertEquals(result.gradeLevel, actual.gradeLevel, 0.5);
    }

}
