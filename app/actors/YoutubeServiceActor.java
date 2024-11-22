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

public class YoutubeServiceActor extends AbstractActor {
    private final YoutubeService youtubeService;

    @Inject
    public YoutubeServiceActor(YoutubeService youtubeService) {
        this.youtubeService = youtubeService;
    }

    public static Props props(YoutubeService youtubeService) {
        return Props.create(YoutubeServiceActor.class, () -> new YoutubeServiceActor(youtubeService));
    }

    @Override
    public Receive createReceive() {
        return receiveBuilder()
                .match(SearchVideos.class, this::handleSearchVideos)
                .match(GetVideo.class, this::handleGetVideo)
                .match(GetChannelVideos.class, this::handleGetChannelVideos)
                .match(GetChannelById.class, this::handleGetChannelById)
                .build();
    }

    private void handleSearchVideos(SearchVideos message) {
        CompletableFuture<VideoList> searchResults = youtubeService.searchResults(message.keywords, message.maxResults);
        searchResults.thenAccept(results -> getSender().tell(results, getSelf())).exceptionally(ex -> {
            getSender().tell(new akka.actor.Status.Failure(ex), getSelf());
            return null;
        });
    }

    private void handleGetVideo(GetVideo message) {
        CompletableFuture<Video> video = youtubeService.getVideo(message.videoId);
        video.thenAccept(result -> getSender().tell(result, getSelf())).exceptionally(ex -> {
            getSender().tell(new akka.actor.Status.Failure(ex), getSelf());
            return null;
        });
    }

    private void handleGetChannelVideos(GetChannelVideos message) throws IOException {
        CompletionStage<List<Video>> videos = youtubeService.getChannelVideos(message.channelId);
        videos.thenAccept(result -> getSender().tell(result, getSelf())).exceptionally(ex -> {
            getSender().tell(new akka.actor.Status.Failure(ex), getSelf());
            return null;
        });
    }

    private void handleGetChannelById(GetChannelById message) {
        CompletionStage<YoutubeChannel> channel = youtubeService.getChannelById(message.channelId);
        channel.thenAccept(result -> getSender().tell(result, getSelf())).exceptionally(ex -> {
            getSender().tell(new akka.actor.Status.Failure(ex), getSelf());
            return null;
        });
    }

    // Messages
    public static class SearchVideos {
        public final String keywords;
        public final Long maxResults;

        public SearchVideos(String keywords, Long maxResults) {
            this.keywords = keywords;
            this.maxResults = maxResults;
        }
    }

    public static class GetVideo {
        public final String videoId;

        public GetVideo(String videoId) {
            this.videoId = videoId;
        }
    }

    public static class GetChannelVideos {
        public final String channelId;

        public GetChannelVideos(String channelId) {
            this.channelId = channelId;
        }
    }

    public static class GetChannelById {
        public final String channelId;

        public GetChannelById(String channelId) {
            this.channelId = channelId;
        }
    }
}
