package actors;

import akka.actor.*;
import akka.japi.pf.DeciderBuilder;
import akka.pattern.Patterns;
import akka.util.Timeout;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import models.VideoSearch;
import scala.concurrent.Future;
import scala.concurrent.duration.Duration;
import scala.concurrent.duration.FiniteDuration;
import services.SearchService;
import services.StatisticsService;
import services.YoutubeService;

import javax.inject.Inject;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * Actor class to handle WebSocket connections and messages.
 * @author Tanveer Reza
 */
public class WebSocketActor extends AbstractActorWithTimers {
    private final ActorRef searchServiceActor;
    private final ActorRef youtubeServiceActor;
    private final ActorRef statisticsServiceActor;
    private final String sessionId;
    private final ActorRef out;
    private final FiniteDuration refreshDuration;

    /**
     * Constructor to initialize the WebSocketActor.
     *
     * @param searchService the service to handle search operations
     * @param youtubeService the service to handle YouTube operations
     * @param statisticsService the service to handle statistics operations
     * @param sessionId the session ID for the WebSocket connection
     * @param out the ActorRef to send messages to the WebSocket client
     * @author Tanveer Reza
     */
    @Inject
    public WebSocketActor(SearchService searchService, YoutubeService youtubeService, StatisticsService statisticsService, String sessionId, ActorRef out) {
        this.sessionId = sessionId;
        this.out = out;
        searchServiceActor = getContext().actorOf(SearchServiceActor.props(searchService), "searchServiceActor");
        youtubeServiceActor = getContext().actorOf(YoutubeServiceActor.props(youtubeService), "youtubeServiceActor");
        statisticsServiceActor = getContext().actorOf(StatisticsServiceActor.props(statisticsService), "statisticsServiceActor");
        this.refreshDuration = Duration.create(5, TimeUnit.SECONDS);

    }

    private static final class Tick {}

    @Override
    public void preStart() {
        // getTimers().startPeriodicTimer("Refresh",new Tick(), refreshDuration);
    }

    /**
     * Factory method to create Props for WebSocketActor.
     *
     * @param searchService the service to handle search operations
     * @param youtubeService the service to handle YouTube operations
     * @param statisticsService the service to handle statistics operations
     * @param sessionId the session ID for the WebSocket connection
     * @param out the ActorRef to send messages to the WebSocket client
     * @return Props for creating WebSocketActor
     * @author Tanveer Reza
     */
    public static Props props(SearchService searchService, YoutubeService youtubeService, StatisticsService statisticsService, String sessionId, ActorRef out) {
        return Props.create(WebSocketActor.class, () -> new WebSocketActor(searchService, youtubeService, statisticsService, sessionId, out));
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
                .match(Tick.class, tick -> {
                    // change this to update
                    getContext().getSelf().forward(new SearchServiceActor.UpdateUserSearchList(sessionId), getContext());
                    // searchServiceActor.tell(, out);
                })
                .match(String.class, message -> {
                    // Check if the message is of type "search"
                    if (message.startsWith("{\"type\":\"search\"")) {
                        try {
                            // Assuming the message is a JSON string
                            ObjectMapper objectMapper = new ObjectMapper();
                            JsonNode jsonNode = objectMapper.readTree(message);
                            String query = jsonNode.get("query").asText();
                            System.out.println("Received Search message: " + query + " for session: " + sessionId);
                            // Forward to SearchServiceActor
                            SearchServiceActor.SearchKeywords searchKeywords = new SearchServiceActor.SearchKeywords(query, sessionId);
                            getContext().getSelf().forward(searchKeywords, getContext());
                        } catch (Exception e) {
                            e.printStackTrace();
                            System.out.println("Error parsing message: " + message);
                        }
                    } else if(message.startsWith("{\"type\":\"getStatistics\"")) {
                        try {
                            // Assuming the message is a JSON string
                            ObjectMapper objectMapper = new ObjectMapper();
                            JsonNode jsonNode = objectMapper.readTree(message);
                            String searchTerm = jsonNode.get("searchTerm").asText();
                            System.out.println("Received Search message: " + searchTerm + " for session: " + sessionId);
                            // Forward to SearchServiceActor
                            StatisticsServiceActor.WordFrequency wordFrequency = new StatisticsServiceActor.WordFrequency(searchTerm, sessionId);
                            getContext().getSelf().forward(wordFrequency, getContext());
                        } catch (Exception e) {
                            e.printStackTrace();
                            System.out.println("Error parsing message: " + message);
                        }
                    } else if (message.startsWith("{\"type\":\"getUserSearchList\"")) {
                        SearchServiceActor.GetUserSearchList getUserSearchList = new SearchServiceActor.GetUserSearchList(sessionId);
                        getContext().getSelf().forward(getUserSearchList, getContext());
                    } else {
                        System.out.println("Received unknown message: " + message);
                    }
                })
                .match(SearchServiceActor.SearchKeywords.class, message -> {
                    System.out.println("sender: " + getSender().toString());
                    System.out.println("out: " + out.toString());
                    System.out.println("Forwarding search message to SearchServiceActor.");
                    Timeout timeout = Timeout.create(java.time.Duration.ofSeconds(20));
                    Future<Object> futureResult = Patterns.ask(searchServiceActor, message, timeout);
                    futureResult.onComplete(result -> {
                        try {
                            if (result.isSuccess()) {
                                // Send the result back to the WebSocket actor
                                List<VideoSearch> searchResults = (List<VideoSearch>) result.get();
                                System.out.println("Search completed, sending results to WebSocket.");
                                String jsonResults = new ObjectMapper().writeValueAsString(searchResults);
                                out.tell(jsonResults, getSelf());
                            } else {
                                // If there is an error, send failure response to the WebSocket actor
                                Throwable failure = result.failed().get();
                                System.out.println("Search failed: " + failure.getMessage());
                                out.tell(new Status.Failure(failure), getSelf());
                            }
                        } catch (Exception e) {
                            System.out.println("Exception while processing search result: " + e.getMessage());
                            out.tell(new Status.Failure(e), getSelf());
                        }
                        return null;
                    }, context().dispatcher());
                })
                .match(SearchServiceActor.GetUserSearchList.class, message -> {
                    System.out.println("sender: " + getSender().toString());
                    System.out.println("out: " + out.toString());
                    System.out.println("Forwarding search message to SearchServiceActor.");
                    Timeout timeout = Timeout.create(java.time.Duration.ofSeconds(20));
                    Future<Object> futureResult = Patterns.ask(searchServiceActor, message, timeout);
                    futureResult.onComplete(result -> {
                        try {
                            if (result.isSuccess()) {
                                // Send the result back to the WebSocket actor
                                List<VideoSearch> searchResults = (List<VideoSearch>) result.get();
                                System.out.println("Search completed, sending results to WebSocket.");
                                String jsonResults = new ObjectMapper().writeValueAsString(searchResults);
                                out.tell(jsonResults, getSelf());
                            } else {
                                // If there is an error, send failure response to the WebSocket actor
                                Throwable failure = result.failed().get();
                                System.out.println("Search failed: " + failure.getMessage());
                                out.tell(new Status.Failure(failure), getSelf());
                            }
                        } catch (Exception e) {
                            System.out.println("Exception while processing search result: " + e.getMessage());
                            out.tell(new Status.Failure(e), getSelf());
                        }
                        return null;
                    }, context().dispatcher());
                })
                .match(SearchServiceActor.UpdateUserSearchList.class, message -> {
                    System.out.println("sender: " + getSender().toString());
                    System.out.println("out: " + out.toString());
                    System.out.println("Forwarding updated search message to SearchServiceActor.");
                    Timeout timeout = Timeout.create(java.time.Duration.ofSeconds(20));
                    Future<Object> futureResult = Patterns.ask(searchServiceActor, message, timeout);
                    futureResult.onComplete(result -> {
                        try {
                            if (result.isSuccess()) {
                                // Send the result back to the WebSocket actor
                                List<VideoSearch> searchResults = (List<VideoSearch>) result.get();
                                System.out.println("Update completed, sending results to WebSocket.");
                                String jsonResults = new ObjectMapper().writeValueAsString(searchResults);
                                out.tell(jsonResults, getSelf());
                            } else {
                                // If there is an error, send failure response to the WebSocket actor
                                Throwable failure = result.failed().get();
                                System.out.println("Search failed: " + failure.getMessage());
                                out.tell(new Status.Failure(failure), getSelf());
                            }
                        } catch (Exception e) {
                            System.out.println("Exception while processing search result: " + e.getMessage());
                            out.tell(new Status.Failure(e), getSelf());
                        }
                        return null;
                    }, context().dispatcher());
                })
                .match(StatisticsServiceActor.WordFrequency.class, message -> {
                    System.out.println("sender: " + getSender().toString());
                    System.out.println("out: " + out.toString());
                    System.out.println("Forwarding search message to SearchServiceActor.");
                    Timeout timeout = Timeout.create(java.time.Duration.ofSeconds(20));
                    Future<Object> futureResult = Patterns.ask(statisticsServiceActor, message, timeout);
                    futureResult.onComplete(result -> {
                        try {
                            if (result.isSuccess()) {
                                // Send the result back to the WebSocket actor
                                Map<String, Long> wordFrequencyMap = (Map<String, Long>) result.get();
                                System.out.println("Sending word Frequency results to WebSocket.");
                                StatisticsServiceActor.StatisticsResponse statisticsResponse = new StatisticsServiceActor.StatisticsResponse(wordFrequencyMap);
                                String jsonResults = new ObjectMapper().writeValueAsString(statisticsResponse);
                                out.tell(jsonResults, getSelf());
                            } else {
                                // If there is an error, send failure response to the WebSocket actor
                                Throwable failure = result.failed().get();
                                System.out.println("Search failed: " + failure.getMessage());
                                out.tell(new Status.Failure(failure), getSelf());
                            }
                        } catch (Exception e) {
                            System.out.println("Exception while processing search result: " + e.getMessage());
                            out.tell(new Status.Failure(e), getSelf());
                        }
                        return null;
                    }, context().dispatcher());
                })
                .match(SearchServiceActor.GetVideoById.class, message -> searchServiceActor.forward(message, getContext()))
                .match(SearchServiceActor.GetVideosBySearchTerm.class, message -> searchServiceActor.forward(message, getContext()))
                .match(YoutubeServiceActor.SearchVideos.class, message -> youtubeServiceActor.forward(message, getContext()))
                .match(YoutubeServiceActor.GetVideo.class, message -> youtubeServiceActor.forward(message, getContext()))
                .match(YoutubeServiceActor.GetChannelVideos.class, message -> youtubeServiceActor.forward(message, getContext()))
                .match(YoutubeServiceActor.GetChannelById.class, message -> youtubeServiceActor.forward(message, getContext()))
                .matchAny(message -> {
                    System.out.println("Any Received unknown message: " + message);
                })
                .build();
    }

    /**
     * Defines the supervisor strategy for this actor.
     *
     * @return the SupervisorStrategy object defining the strategy
     * @author Tanveer Reza
     */
    @Override
    public SupervisorStrategy supervisorStrategy() {
        return new OneForOneStrategy(
                10,
                Duration.create("1 minute"),
                DeciderBuilder.match(Exception.class, e -> SupervisorStrategy.restart()).build()
        );
    }

    /**
     * Message class for search requests.
     * @author Tanveer Reza
     */
    public static class Search {
        public final String query;
        public final String sessionId;

        /**
         * Constructor for Search message.
         *
         * @param query the search query
         * @param sessionId the session ID
         * @author Tanveer Reza
         */
        public Search(String query, String sessionId) {
            this.query = query;
            this.sessionId = sessionId;
        }
    }

    /**
     * Message class for video requests by ID.
     * @author Tanveer Reza
     */
    public static class GetVideo {
        public final String id;

        /**
         * Constructor for GetVideo message.
         *
         * @param id the video ID
         * @author Tanveer Reza
         */
        public GetVideo(String id) {
            this.id = id;
        }
    }

    /**
     * Message class for channel profile requests.
     * @author Tanveer Reza
     */
    public static class ShowChannelProfile {
        public final String channelId;

        /**
         * Constructor for ShowChannelProfile message.
         *
         * @param channelId the channel ID
         * @author Tanveer Reza
         */
        public ShowChannelProfile(String channelId) {
            this.channelId = channelId;
        }
    }

    /**
     * Message class for statistics requests.
     * @author Tanveer Reza
     */
    public static class GetStatistics {
        public final String query;
        public final String sessionId;

        /**
         * Constructor for GetStatistics message.
         *
         * @param query the search query
         * @param sessionId the session ID
         * @author Tanveer Reza
         */
        public GetStatistics(String query, String sessionId) {
            this.query = query;
            this.sessionId = sessionId;
        }
    }

    /**
     * Message class for video requests by tag.
     */
    public static class VideosByTag {
        public final String tag;

        /**
         * Constructor for VideosByTag message.
         *
         * @param tag the video tag
         * @author Tanveer Reza
         */
        public VideosByTag(String tag) {
            this.tag = tag;
        }
    }

    /**
     * Message class for search results.
     * @author Tanveer Reza
     */
    public static class SearchResult {
        private final List<VideoSearch> videoSearchList;

        /**
         * Constructor for SearchResult message.
         *
         * @param videoSearchList the list of video search results
         * @author Tanveer Reza
         */
        public SearchResult(List<VideoSearch> videoSearchList) {
            this.videoSearchList = videoSearchList;
        }

        /**
         * Gets the list of video search results.
         *
         * @return the list of video search results
         * @author Tanveer Reza
         */
        public List<VideoSearch> getVideoSearchList() {
            return videoSearchList;
        }
    }

    /**
     * Called before the actor is restarted.
     *
     * @param reason the reason for the restart
     * @param message the message that caused the restart
     * @throws Exception if an error occurs
     * @author Tanveer Reza
     */
    @Override
    public void preRestart(Throwable reason, scala.Option<Object> message) throws Exception {
        super.preRestart(reason, message);
        System.out.println("WebSocketActor is restarting due to: " + reason.getMessage());
    }

    /**
     * Called after the actor is restarted.
     *
     * @param reason the reason for the restart
     * @throws Exception if an error occurs
     * @author Tanveer Reza
     */
    @Override
    public void postRestart(Throwable reason) throws Exception {
        super.postRestart(reason);
        System.out.println("WebSocketActor has restarted.");
    }

    /**
     * Called when the actor is stopped.
     * @author Tanveer Reza
     */
    @Override
    public void postStop() {
        System.out.println("WebSocketActor has stopped.");
    }
}