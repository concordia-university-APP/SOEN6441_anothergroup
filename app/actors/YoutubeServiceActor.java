package actors;

import akka.actor.AbstractActor;
import akka.actor.Props;
import services.YoutubeService;
import models.VideoList;
import models.Video;
import models.YoutubeChannel;

import javax.inject.Inject;
import java.io.IOException;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;

/**
 * Actor class to handle YouTube service requests.
 * @author Tanveer Reza
 */
public class YoutubeServiceActor extends AbstractActor {
    private final YoutubeService youtubeService;

    /**
     * Constructor to initialize the YoutubeServiceActor.
     *
     * @param youtubeService the service to handle YouTube operations
     * @author Tanveer Reza
     */
    @Inject
    public YoutubeServiceActor(YoutubeService youtubeService) {
        this.youtubeService = youtubeService;
    }

    /**
     * Factory method to create Props for YoutubeServiceActor.
     *
     * @param youtubeService the service to handle YouTube operations
     * @return Props for creating YoutubeServiceActor
     * @author Tanveer Reza
     */
    public static Props props(YoutubeService youtubeService) {
        return Props.create(YoutubeServiceActor.class, () -> new YoutubeServiceActor(youtubeService));
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
                .match(SearchVideos.class, this::handleSearchVideos)
                .match(GetVideo.class, this::handleGetVideo)
                .match(GetChannelVideos.class, this::handleGetChannelVideos)
                .match(GetChannelById.class, this::handleGetChannelById)
                .build();
    }

    /**
     * Handles the SearchVideos message by calling the YoutubeService to fetch search results.
     * @param message
     * @author Tanveer Reza
     */
    private void handleSearchVideos(SearchVideos message) {
        CompletableFuture<VideoList> searchResults = youtubeService.searchResults(message.keywords, message.maxResults);
        searchResults.thenAccept(results -> getSender().tell(results, getSelf())).exceptionally(ex -> {
            getSender().tell(new akka.actor.Status.Failure(ex), getSelf());
            return null;
        });
    }

    /**
     * Handles the GetVideo message by calling the YoutubeService to fetch video by ID.
     * @param message
     * @author Tanveer Reza
     */
    private void handleGetVideo(GetVideo message) {
        CompletableFuture<Video> video = youtubeService.getVideo(message.videoId);
        video.thenAccept(result -> getSender().tell(result, getSelf())).exceptionally(ex -> {
            getSender().tell(new akka.actor.Status.Failure(ex), getSelf());
            return null;
        });
    }

    /**
     * Handles the GetChannelVideos message by calling the YoutubeService to fetch channel videos.
     * @param message
     * @throws IOException
     * @author Tanveer Reza
     */
    private void handleGetChannelVideos(GetChannelVideos message) throws IOException {
        CompletionStage<List<Video>> videos = youtubeService.getChannelVideos(message.channelId);
        videos.thenAccept(result -> getSender().tell(result, getSelf())).exceptionally(ex -> {
            getSender().tell(new akka.actor.Status.Failure(ex), getSelf());
            return null;
        });
    }

    /**
     * Handles the GetChannelById message by calling the YoutubeService to fetch channel by ID.
     * @param message
     * @author Tanveer Reza
     */
    private void handleGetChannelById(GetChannelById message) {
        CompletionStage<YoutubeChannel> channel = youtubeService.getChannelById(message.channelId);
        channel.thenAccept(result -> getSender().tell(result, getSelf())).exceptionally(ex -> {
            getSender().tell(new akka.actor.Status.Failure(ex), getSelf());
            return null;
        });
    }

    public interface Message {}

    /**
     * Message class for search video requests.
     * @author Tanveer Reza
     */
    public static class SearchVideos implements Message {
        public final String keywords;
        public final Long maxResults;

        /**
         * Constructor for SearchVideos message.
         *
         * @param keywords the search keywords
         * @param maxResults the maximum number of results
         * @author Tanveer Reza
         */
        public SearchVideos(String keywords, Long maxResults) {
            this.keywords = keywords;
            this.maxResults = maxResults;
        }
    }

    /**
     * Message class for video requests by ID.
     * @author Tanveer Reza
     */
    public static class GetVideo implements Message {
        public final String videoId;

        /**
         * Constructor for GetVideo message.
         *
         * @param videoId the video ID
         * @author Tanveer Reza
         */
        public GetVideo(String videoId) {
            this.videoId = videoId;
        }
    }

    /**
     * Message class for channel videos requests.
     * @author Tanveer Reza
     */
    public static class GetChannelVideos implements Message {
        public final String channelId;

        /**
         * Constructor for GetChannelVideos message.
         *
         * @param channelId the channel ID
         * @author Tanveer Reza
         */
        public GetChannelVideos(String channelId) {
            this.channelId = channelId;
        }
    }

    /**
     * Message class for channel profile requests by ID.
     * @author Tanveer Reza
     */
    public static class GetChannelById implements Message{
        public final String channelId;

        /**
         * Constructor for GetChannelById message.
         *
         * @param channelId the channel ID
         * @author Tanveer Reza
         */
        public GetChannelById(String channelId) {
            this.channelId = channelId;
        }
    }
}