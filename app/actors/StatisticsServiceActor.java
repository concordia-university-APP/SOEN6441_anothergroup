package actors;

import akka.actor.AbstractActor;
import akka.actor.ActorRef;
import akka.actor.Props;
import services.StatisticsService;

import javax.inject.Inject;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

public class StatisticsServiceActor extends AbstractActor {

    private final StatisticsService statisticsService;

    @Inject// Constructor to initialize the StatisticsService
    public StatisticsServiceActor(StatisticsService statisticsService) {
        this.statisticsService = statisticsService;
    }

    // Factory method for creating Props
    public static Props props(StatisticsService statisticsService) {
        return Props.create(StatisticsServiceActor.class, () -> new StatisticsServiceActor(statisticsService));
    }

    // Define message handling
    @Override
    public Receive createReceive() {
        return receiveBuilder()
                .match(WordFrequency.class, this::handleGetWordFrequency)
                .build();
    }

    private void handleGetWordFrequency(WordFrequency message) {
        // Call service method to calculate statistics

        System.out.println("Handling SearchKeywords message: " + message.searchTerms);
        ActorRef sender = getSender();
        CompletableFuture<Map<String, Long>> wordFrequencyFuture = statisticsService.getWordFrequency(message.searchTerms, message.sessionId);;
        wordFrequencyFuture.thenAccept(wordFrequency -> {
            // Ensure the sender is correctly set here.
            sender.tell(wordFrequency, getSelf());
        }).exceptionally(ex -> {
            sender.tell(new akka.actor.Status.Failure(ex), getSelf());
            return null;
        });
    }

    // Message Classes
    public static class WordFrequency {
        public final String searchTerms;
        public final String sessionId;

        public WordFrequency(String searchTerms, String sessionId) {
            this.searchTerms = searchTerms;
            this.sessionId = sessionId;
        }
    }

    public static class StatisticsResponse {
        private final String dataType;
        private final Map<String, Long> frequency;

        // Constructor
        public StatisticsResponse(Map<String, Long> frequency) {
            this.dataType = "statistics";
            this.frequency = frequency;
        }

        public String getDataType() {
            return dataType;
        }

        public Map<String, Long> getFrequency() {
            return frequency;
        }
    }
}
