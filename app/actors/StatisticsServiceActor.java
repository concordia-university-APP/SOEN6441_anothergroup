package actors;

import akka.actor.AbstractActor;
import akka.actor.ActorRef;
import akka.actor.Props;
import services.StatisticsService;

import javax.inject.Inject;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

/**
 * Actor class for handling statistics-related messages.
 * This actor interacts with the StatisticsService to fetch word frequency statistics.
 *
 * @author Tanveer Reza
 */
public class StatisticsServiceActor extends AbstractActor {

    private final StatisticsService statisticsService;

    /**
     * Constructor to initialize the StatisticsService.
     *
     * @param statisticsService the service to fetch statistics
     * @author Tanveer Reza
     */
    @Inject
    public StatisticsServiceActor(StatisticsService statisticsService) {
        this.statisticsService = statisticsService;
    }

    /**
     * Factory method for creating Props for this actor.
     *
     * @param statisticsService the service to fetch statistics
     * @return Props for creating this actor
     * @author Tanveer Reza
     */
    public static Props props(StatisticsService statisticsService) {
        return Props.create(StatisticsServiceActor.class, () -> new StatisticsServiceActor(statisticsService));
    }

    /**
     * Defines the message handling for this actor.
     *
     * @return the Receive object defining the message handling
     * @author Tanveer Reza
     */
    @Override
    public Receive createReceive() {
        return receiveBuilder()
                .match(WordFrequency.class, this::handleGetWordFrequency)
                .build();
    }

    /**
     * Handles the WordFrequency message by calling the StatisticsService to fetch word frequency statistics.
     *
     * @param message the WordFrequency message containing search terms and session ID
     * @author Tanveer Reza
     */
    private void handleGetWordFrequency(WordFrequency message) {
        System.out.println("Handling SearchKeywords message: " + message.searchTerms);
        ActorRef sender = getSender();
        CompletableFuture<Map<String, Long>> wordFrequencyFuture = statisticsService.getWordFrequency(message.searchTerms, message.sessionId);
        wordFrequencyFuture.thenAccept(wordFrequency -> {
            sender.tell(wordFrequency, getSelf());
        }).exceptionally(ex -> {
            sender.tell(new akka.actor.Status.Failure(ex), getSelf());
            return null;
        });
    }

    /**
     * Message class for requesting word frequency statistics.
     * @author Tanveer Reza
     */
    public static class WordFrequency {
        public final String searchTerms;
        public final String sessionId;

        /**
         * Constructor for WordFrequency message.
         *
         * @param searchTerms the search terms to fetch word frequency for
         * @param sessionId the session ID
         * @author Tanveer Reza
         */
        public WordFrequency(String searchTerms, String sessionId) {
            this.searchTerms = searchTerms;
            this.sessionId = sessionId;
        }
    }

    /**
     * Response class for word frequency statistics.
     * @author Tanveer Reza
     */
    public static class StatisticsResponse {
        private final String dataType;
        private final Map<String, Long> frequency;

        /**
         * Constructor for StatisticsResponse.
         *
         * @param frequency the word frequency map
         * @author Tanveer Reza
         */
        public StatisticsResponse(Map<String, Long> frequency) {
            this.dataType = "statistics";
            this.frequency = frequency;
        }

        /**
         * Gets the data type of the response.
         *
         * @return the data type
         * @author Tanveer Reza
         */
        public String getDataType() {
            return dataType;
        }

        /**
         * Gets the word frequency map.
         *
         * @return the word frequency map
         * @author Tanveer Reza
         */
        public Map<String, Long> getFrequency() {
            return frequency;
        }
    }
}