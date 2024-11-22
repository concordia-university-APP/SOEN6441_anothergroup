package actors;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import akka.actor.ActorSystem;
import akka.actor.ActorRef;
import akka.testkit.javadsl.TestKit;
import org.mockito.Mockito;
import services.StatisticsService;

import java.util.concurrent.CompletableFuture;
import java.util.Map;

import static org.mockito.Mockito.when;

/**
 * This class tests the StatisticsServiceActor class.
 * It mocks the StatisticsService and tests the handling of the WordFrequency message.
 * @author Tanveer Reza
 */
public class StatisticsServiceActorTest {

    private ActorSystem system;
    private TestKit testKit;
    private StatisticsService mockStatisticsService;

    /**
     * Sets up the test environment by creating the ActorSystem and TestKit,
     * and mocking the StatisticsService.
     * @author Tanveer Reza
     */
    @Before
    public void setUp() {
        // Disable logging to avoid FileAppender issue
        disableLogging();

        // Create the ActorSystem and TestKit
        system = ActorSystem.create("TestSystem");
        testKit = new TestKit(system);

        // Mock the StatisticsService
        mockStatisticsService = Mockito.mock(StatisticsService.class);
    }

    /**
     * Tears down the test environment by shutting down the ActorSystem.
     * @author Tanveer Reza
     */
    @After
    public void tearDown() {
        // Terminate the ActorSystem after the test
        TestKit.shutdownActorSystem(system);
        system = null;
    }

    /**
     * Tests the successful retrieval of word frequency statistics.
     * Mocks the StatisticsService to return a predefined result and verifies
     * that the actor responds with the expected result.
     *  @author Tanveer Reza
     */
    @Test
    public void testWordFrequencySuccess() {
        // Prepare the mock service to return a predefined result
        when(mockStatisticsService.getWordFrequency(Mockito.anyString(), Mockito.anyString()))
                .thenReturn(CompletableFuture.completedFuture(Map.of("word1", 10L, "word2", 5L)));

        // Create the actor
        ActorRef statisticsServiceActor = system.actorOf(StatisticsServiceActor.props(mockStatisticsService));

        // Send the WordFrequency message to the actor
        statisticsServiceActor.tell(new StatisticsServiceActor.WordFrequency("test", "session"), testKit.getRef());

        // Expect the response from the actor
        Map<String, Long> expectedResult = Map.of("word1", 10L, "word2", 5L);
        testKit.expectMsg(expectedResult);
    }

    /**
     * Tests the failure scenario when retrieving word frequency statistics.
     * Mocks the StatisticsService to throw an exception and verifies that the
     * actor responds with a failure message.
     * @author Tanveer Reza
     */
    @Test
    public void testWordFrequencyFailure() {
        // Prepare the mock service to throw an exception
        when(mockStatisticsService.getWordFrequency(Mockito.anyString(), Mockito.anyString()))
                .thenReturn(CompletableFuture.failedFuture(new RuntimeException("Error occurred")));

        // Create the actor
        ActorRef statisticsServiceActor = system.actorOf(StatisticsServiceActor.props(mockStatisticsService));

        // Send the WordFrequency message to the actor
        statisticsServiceActor.tell(new StatisticsServiceActor.WordFrequency("test", "session"), testKit.getRef());

        // Expect a failure message
        testKit.expectMsgClass(akka.actor.Status.Failure.class);
    }

    /**
     * Tests the constructor of the StatisticsResponse class.
     * Verifies that the dataType is set correctly and the frequency map is correct.
     * @author Tanveer Reza
     */
    @Test
    public void testStatisticsResponseConstructor() {
        // Test the StatisticsResponse constructor
        Map<String, Long> frequencyMap = Map.of("word1", 10L, "word2", 5L);
        StatisticsServiceActor.StatisticsResponse response = new StatisticsServiceActor.StatisticsResponse(frequencyMap);

        // Verify the dataType is set correctly
        assert "statistics".equals(response.getDataType());

        // Verify the frequency map is correct
        assert frequencyMap.equals(response.getFrequency());
    }

    /**
     * Tests the handling of the WordFrequency message by the actor.
     * Mocks the StatisticsService to return a predefined result and verifies
     * that the actor responds with the expected result.
     * @author Tanveer Reza
     */
    @Test
    public void testHandleGetWordFrequency() {
        // Prepare the mock service to return a predefined result
        when(mockStatisticsService.getWordFrequency("test", "session"))
                .thenReturn(CompletableFuture.completedFuture(Map.of("word1", 10L, "word2", 5L)));

        // Create the actor
        ActorRef statisticsServiceActor = system.actorOf(StatisticsServiceActor.props(mockStatisticsService));

        // Create the WordFrequency message
        StatisticsServiceActor.WordFrequency message = new StatisticsServiceActor.WordFrequency("test", "session");

        // Send the message to the actor
        statisticsServiceActor.tell(message, testKit.getRef());

        // Expect the result message from the actor
        testKit.expectMsg(Map.of("word1", 10L, "word2", 5L));
    }

    /**
     * Disables logging during tests to avoid FileAppender issues.
     * @author Tanveer Reza
     */
    private void disableLogging() {
        System.setProperty("org.slf4j.simpleLogger.defaultLogLevel", "off");
        // Optional: Disable specific loggers to prevent FileAppender errors
        System.setProperty("logback.configurationFile", "src/test/resources/logback-test.xml");
    }
}