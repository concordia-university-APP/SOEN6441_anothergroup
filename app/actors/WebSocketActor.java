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
import services.SearchService;
import services.StatisticsService;
import services.YoutubeService;

import javax.inject.Inject;
import java.util.List;
import java.util.Map;

public class WebSocketActor extends AbstractActor {
    private final ActorRef searchServiceActor;
    private final ActorRef youtubeServiceActor;
    private final ActorRef statisticsServiceActor;
    private final String sessionId;
    private final ActorRef out;

    @Inject
    public WebSocketActor(SearchService searchService, YoutubeService youtubeService, StatisticsService statisticsService,  String sessionId, ActorRef out) {
        this.sessionId = sessionId;
        this.out = out;
        searchServiceActor = getContext().actorOf(SearchServiceActor.props(searchService), "searchServiceActor");
        youtubeServiceActor = getContext().actorOf(YoutubeServiceActor.props(youtubeService), "youtubeServiceActor");
        statisticsServiceActor = getContext().actorOf(StatisticsServiceActor.props(statisticsService), "statisticsServiceActor");
    }

    public static Props props(SearchService searchService, YoutubeService youtubeService, StatisticsService statisticsService,  String sessionId, ActorRef out) {
        return Props.create(WebSocketActor.class, () -> new WebSocketActor(searchService, youtubeService, statisticsService, sessionId, out));
    }

    @Override
    public Receive createReceive() {
        return receiveBuilder()
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
                            SearchServiceActor.SearchKeywords searchKeywords = new SearchServiceActor.SearchKeywords(query, sessionId, out);
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
                    }else {
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
                    System.out.println("Received unknown message: " + message);
                })
                .build();
    }

    @Override
    public SupervisorStrategy supervisorStrategy() {
        return new OneForOneStrategy(
                10,
                Duration.create("1 minute"),
                DeciderBuilder.match(Exception.class, e -> SupervisorStrategy.restart()).build()
        );
    }

    public static class Search {
        public final String query;
        public final String sessionId;

        public Search(String query, String sessionId) {
            this.query = query;
            this.sessionId = sessionId;
        }
    }

    public static class GetVideo {
        public final String id;

        public GetVideo(String id) {
            this.id = id;
        }
    }

    public static class ShowChannelProfile {
        public final String channelId;

        public ShowChannelProfile(String channelId) {
            this.channelId = channelId;
        }
    }

    public static class GetStatistics {
        public final String query;
        public final String sessionId;

        public GetStatistics(String query, String sessionId) {
            this.query = query;
            this.sessionId = sessionId;
        }
    }

    public static class VideosByTag {
        public final String tag;

        public VideosByTag(String tag) {
            this.tag = tag;
        }
    }

    public static class SearchResult {
        private final List<VideoSearch> videoSearchList;

        public SearchResult(List<VideoSearch> videoSearchList) {
            this.videoSearchList = videoSearchList;
        }

        public List<VideoSearch> getVideoSearchList() {
            return videoSearchList;
        }
    }

    @Override
    public void preRestart(Throwable reason, scala.Option<Object> message) throws Exception {
        super.preRestart(reason, message);
        System.out.println("WebSocketActor is restarting due to: " + reason.getMessage());
    }

    @Override
    public void postRestart(Throwable reason) throws Exception {
        super.postRestart(reason);
        System.out.println("WebSocketActor has restarted.");
    }

    @Override
    public void postStop() {
        System.out.println("WebSocketActor has stopped.");
    }
}

