package actors;

import akka.actor.AbstractActor;
import akka.actor.ActorRef;
import akka.actor.Props;
import akka.pattern.Patterns;
import scala.concurrent.Future;
import services.SearchService;
import models.VideoSearch;
import models.VideoList;
import models.Video;

import javax.inject.Inject;
import java.util.List;
import java.util.concurrent.CompletableFuture;

/**
 * Actor class for handling search-related messages.
 * This actor interacts with the SearchService to perform search operations.
 * @author Tanveer Reza
 */
public class SearchServiceActor extends AbstractActor {
    private final SearchService searchService;
    private final ActorRef fleschReadingActor;

    /**
     * Constructor to initialize the SearchServiceActor.
     *
     * @param searchService the service to handle search operations
     * @author Tanveer Reza
     */
    @Inject
    public SearchServiceActor(SearchService searchService) {
        this.searchService = searchService;
        this.fleschReadingActor = getContext().actorOf(FleschReadingActor.props(), "fleschReadingActor");
    }

    /**
     * Factory method to create Props for SearchServiceActor.
     *
     * @param searchService the service to handle search operations
     * @return Props for creating SearchServiceActor
     * @author Tanveer Reza
     */
    public static Props props(SearchService searchService) {
        return Props.create(SearchServiceActor.class, () -> new SearchServiceActor(searchService));
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
                .match(SearchKeywords.class, this::handleSearchKeywords)
                .match(GetVideoById.class, this::handleGetVideoById)
                .match(GetVideosBySearchTerm.class, this::handleGetVideosBySearchTerm)
                .match(GetUserSearchList.class, this::handleGetUserSearchList)
                .match(UpdateUserSearchList.class, this::handleUpdateUserSearchList)
                .match(FleschReadingActor.Message.class, message -> fleschReadingActor.forward(message, getContext()))
                .build();
    }

    /**
     * Handles the SearchKeywords message by calling the SearchService to perform a search.
     *
     * @param message the SearchKeywords message containing search keywords and session ID
     * @author Tanveer Reza
     */
    private void handleSearchKeywords(SearchKeywords message) {
        System.out.println("Handling SearchKeywords message: " + message.keywords);
        ActorRef sender = getSender();
        CompletableFuture<List<VideoSearch>> searchResults = searchService.searchKeywords(message.keywords, message.sessionId);
        searchResults.thenAcceptAsync(results -> {
            // Ensure the sender is correctly set here.
            System.out.println("Sending search results to sender: " + sender.toString());
            System.out.println("Search results: " + results.getClass().getSimpleName());
            System.out.println("GetSender: " + getSender());
            results.stream().forEach(
                    res -> res.getResults().getVideoList().stream().forEach( v -> {
                        FleschReadingActor.GetReadingEaseScore msg = new FleschReadingActor.GetReadingEaseScore(v.getDescription());
                        Future<Object> r = Patterns.ask(fleschReadingActor,msg,10);
                        r.onComplete(scores -> {
                            v.setFleschReadingScore((FleschReadingActor.ReadingEaseScoreResult) scores.get());
                            return null;
                        }, context().dispatcher());
                        res.updateScoresAndSentiment();
                    })
            );


            sender.tell(results, getSelf());  // Ensure this is the correct sender (WebSocket actor)
        }).exceptionally(ex -> {
            sender.tell(new akka.actor.Status.Failure(ex), getSelf());
            return null;
        });
    }

    private void handleGetUserSearchList(GetUserSearchList message) {
        System.out.println("Handling Get User Search List message: " + message.sessionId);
        ActorRef sender = getSender();
        List<VideoSearch> searchResults = searchService.getSessionSearchList(message.sessionId);

        searchResults.stream().forEach(
                res -> res.getResults().getVideoList().stream().forEach( v -> {
                    FleschReadingActor.GetReadingEaseScore msg = new FleschReadingActor.GetReadingEaseScore(v.getDescription());
                    Future<Object> r = Patterns.ask(fleschReadingActor,msg,10);
                    r.onComplete(scores -> {
                        v.setFleschReadingScore((FleschReadingActor.ReadingEaseScoreResult) scores.get());
                        return null;
                    }, context().dispatcher());
                    res.updateScoresAndSentiment();
                })
        );
            // Ensure the sender is correctly set here.
        System.out.println("Sending search results to sender: " + sender.toString());
        System.out.println("Search results: " + searchResults.getClass().getSimpleName());
        System.out.println("GetSender: " + getSender());
        sender.tell(searchResults, getSelf());  // Ensure this is the correct sender (WebSocket actor)

    }

    private void handleUpdateUserSearchList(UpdateUserSearchList message) {
        System.out.println("Handling SearchKeywords message: " + message.sessionId);
        ActorRef sender = getSender();
        searchService.updateSearches(message.sessionId).thenAcceptAsync(results -> {
            System.out.println("sending updated search results to sender: " + sender.toString());
            System.out.println("Search results: " + results.getClass().getSimpleName());
            System.out.println("GetSender: " + getSender());
            results.stream().forEach(
                    res -> res.getResults().getVideoList().stream().forEach( v -> {
                        FleschReadingActor.GetReadingEaseScore msg = new FleschReadingActor.GetReadingEaseScore(v.getDescription());
                        Future<Object> r = Patterns.ask(fleschReadingActor,msg,10);
                        r.onComplete(scores -> {
                            v.setFleschReadingScore((FleschReadingActor.ReadingEaseScoreResult) scores.get());
                            return null;
                        }, context().dispatcher());
                        res.updateScoresAndSentiment();
                    })
            );
            sender.tell(results, getSelf());

        }).exceptionally(ex -> {
            sender.tell(new akka.actor.Status.Failure(ex), getSelf());
            return null;
        });
    }

    /**
     * Handles the GetVideoById message by calling the SearchService to fetch a video by its ID.
     *
     * @param message the GetVideoById message containing the video ID
     * @author Tanveer Reza
     */
    private void handleGetVideoById(GetVideoById message) {
        CompletableFuture<Video> video = searchService.getVideoById(message.id);
        video.thenAccept(result -> getSender().tell(result, getSelf())).exceptionally(ex -> {
            getSender().tell(new akka.actor.Status.Failure(ex), getSelf());
            return null;
        });
    }

    /**
     * Handles the GetVideosBySearchTerm message by calling the SearchService to fetch videos by search term.
     *
     * @param message the GetVideosBySearchTerm message containing search keywords and session ID
     * @author Tanveer Reza
     */
    private void handleGetVideosBySearchTerm(GetVideosBySearchTerm message) {
        CompletableFuture<VideoList> videos = searchService.getVideosBySearchTerm(message.keywords, message.sessionId);
        videos.thenAccept(result -> getSender().tell(result, getSelf())).exceptionally(ex -> {
            getSender().tell(new akka.actor.Status.Failure(ex), getSelf());
            return null;
        });
    }

    public interface Message {}

    /**
     * Message class for search keyword requests.
     * @author Tanveer Reza
     */
    public static class SearchKeywords implements Message{
        public final String keywords;
        public final String sessionId;

        /**
         * Constructor for SearchKeywords message.
         *
         * @param keywords the search keywords
         * @param sessionId the session ID
         * @author Tanveer Reza
         */
        public SearchKeywords(String keywords, String sessionId) {
            this.keywords = keywords;
            this.sessionId = sessionId;
        }
    }

    /**
     * Message class for search keyword requests.
     * @author Laurent Voisard
     */
    public static class GetUserSearchList implements Message {
        public final String sessionId;

        /**
         * Constructor for SearchKeywords message.
         *
         * @param sessionId the session ID
         * @author Laurent Voisard
         */
        public GetUserSearchList(String sessionId) {
            this.sessionId = sessionId;
        }
    }

    /**
     * Message class for search keyword requests.
     * @author Laurent Voisard
     */
    public static class UpdateUserSearchList implements Message {
        public final String sessionId;

        /**
         * Constructor for SearchKeywords message.
         *
         * @param sessionId the session ID
         * @author Laurent Voisard
         */
        public UpdateUserSearchList(String sessionId) {
            this.sessionId = sessionId;
        }
    }

    /**
     * Message class for video requests by ID.
     * @author Tanveer Reza
     */
    public static class GetVideoById implements Message {
        public final String id;

        /**
         * Constructor for GetVideoById message.
         *
         * @param id the video ID
         * @author Tanveer Reza
         */
        public GetVideoById(String id) {
            this.id = id;
        }
    }

    /**
     * Message class for video requests by search term.
     * @author Tanveer Reza
     */
    public static class GetVideosBySearchTerm implements Message {
        public final String keywords;
        public final String sessionId;

        /**
         * Constructor for GetVideosBySearchTerm message.
         *
         * @param keywords the search keywords
         * @param sessionId the session ID
         * @author Tanveer Reza
         */
        public GetVideosBySearchTerm(String keywords, String sessionId) {
            this.keywords = keywords;
            this.sessionId = sessionId;
        }
    }
}