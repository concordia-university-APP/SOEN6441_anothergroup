package actors;

import akka.actor.AbstractActor;
import akka.actor.ActorRef;
import akka.actor.Props;
import services.SearchService;
import models.VideoSearch;
import models.VideoList;
import models.Video;

import javax.inject.Inject;
import java.util.List;
import java.util.concurrent.CompletableFuture;

public class SearchServiceActor extends AbstractActor {
    private final SearchService searchService;

    @Inject
    public SearchServiceActor(SearchService searchService) {
        this.searchService = searchService;
    }

    public static Props props(SearchService searchService) {
        return Props.create(SearchServiceActor.class, () -> new SearchServiceActor(searchService));
    }

    @Override
    public Receive createReceive() {
        return receiveBuilder()
                .match(SearchKeywords.class, this::handleSearchKeywords)
                .match(GetVideoById.class, this::handleGetVideoById)
                .match(GetVideosBySearchTerm.class, this::handleGetVideosBySearchTerm)
                .build();
    }

    private void handleSearchKeywords(SearchKeywords message) {
        System.out.println("Handling SearchKeywords message: " + message.keywords);
        ActorRef sender = getSender();
        CompletableFuture<List<VideoSearch>> searchResults = searchService.searchKeywords(message.keywords, message.sessionId);
        searchResults.thenAccept(results -> {
            // Ensure the sender is correctly set here.
            System.out.println("Sending search results to sender: " + sender.toString());
            System.out.println("Search results: " + results.getClass().getSimpleName());
            System.out.println("GetSender: " + getSender());
            sender.tell(results, getSelf());  // Ensure this is the correct sender (WebSocket actor)
        }).exceptionally(ex -> {
            sender.tell(new akka.actor.Status.Failure(ex), getSelf());
            return null;
        });
    }

    private void handleGetVideoById(GetVideoById message) {
        CompletableFuture<Video> video = searchService.getVideoById(message.id);
        video.thenAccept(result -> {
            getSender().tell(result, getSelf());
        }).exceptionally(ex -> {
            getSender().tell(new akka.actor.Status.Failure(ex), getSelf());
            return null;
        });
    }

    private void handleGetVideosBySearchTerm(GetVideosBySearchTerm message) {
        CompletableFuture<VideoList> videos = searchService.getVideosBySearchTerm(message.keywords, message.sessionId);
        videos.thenAccept(result -> {
            getSender().tell(result, getSelf());
        }).exceptionally(ex -> {
            getSender().tell(new akka.actor.Status.Failure(ex), getSelf());
            return null;
        });
    }

    // Messages
    public static class SearchKeywords {
        public final String keywords;
        public final String sessionId;

        public SearchKeywords(String keywords, String sessionId, ActorRef sender) {
            this.keywords = keywords;
            this.sessionId = sessionId;
        }
    }

    public static class GetVideoById {
        public final String id;

        public GetVideoById(String id) {
            this.id = id;
        }
    }

    public static class GetVideosBySearchTerm {
        public final String keywords;
        public final String sessionId;

        public GetVideosBySearchTerm(String keywords, String sessionId) {
            this.keywords = keywords;
            this.sessionId = sessionId;
        }
    }
}