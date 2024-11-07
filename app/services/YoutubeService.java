package services;

import akka.japi.Pair;
import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.api.services.youtube.YouTube;
import com.google.api.services.youtube.model.*;
import com.typesafe.config.Config;
import com.typesafe.config.ConfigFactory;
import models.Video;
import models.VideoList;
import models.YoutubeChannel;
import scala.util.Either;

import javax.inject.Inject;
import java.io.IOException;
import java.security.GeneralSecurityException;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;
import java.util.concurrent.CompletionStage;
import java.util.stream.Collectors;

public class YoutubeService {
    private final Config config = ConfigFactory.load();
    private final String API_KEY = config.getString("youtube.apiKey");
    private final String APPLICATION_NAME = config.getString("youtube.applicationName");
    private final JsonFactory JSON_FACTORY = JacksonFactory.getDefaultInstance();
    private YouTube youtube;

    @Inject
    public YoutubeService() throws GeneralSecurityException, IOException {

            setYoutubeService(new YouTube.Builder(GoogleNetHttpTransport.newTrustedTransport(), JSON_FACTORY, null)
                    .setApplicationName(APPLICATION_NAME)
                    .build());
    }

    public YouTube getYoutubeService() {
        return youtube;
    }

    private void setYoutubeService(YouTube youtube) {
        this.youtube = youtube;
    }

    /**
     * @author Tanveer Reza, Laurent Voisard, Yehia
     * @param keywords search query
     * @param maxResults number of results to return
     * @return a list of videos based on the search query
     */
    public CompletableFuture<VideoList> searchResults(String keywords, Long maxResults) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                YouTube.Search.List request = getYoutubeService().search().list(Collections.singletonList("id, snippet"));
                SearchListResponse response = request
                        .setKey(API_KEY)
                        .setQ(keywords)
                        .setType(Collections.singletonList("video"))
                        .setOrder("date")
                        .setFields("items(id/videoId)")
                        .setMaxResults(maxResults)
                        .execute();
                List<SearchResult> items = response.getItems();
                // TODO remove .join()
                CompletableFuture<List<Video>> videoFutures = getVideos(items.stream().map(item -> item.getId().getVideoId()).collect(Collectors.toList()));
                return new VideoList(videoFutures.join());
            } catch (IOException e) {
                throw new RuntimeException("Error occurred while executing YouTube search: " + e.getMessage(), e);
            }
        });
    }

    /**
     * @author Laurent Voisard
     * Get video by id
     * @param videoId video id
     * @return video model
     */
    public CompletableFuture<Video> getVideo(String videoId) {
        return CompletableFuture.supplyAsync(() -> {
            YouTube.Videos.List request;
            try {
                request = getYoutubeService().videos().list(Collections.singletonList("snippet"));
            } catch (IOException e) {
                throw new RuntimeException(e);
            }

            VideoListResponse response;
            try {
                response = request
                        .setKey(API_KEY)
                        .setId(Collections.singletonList(videoId))
                        .setFields("items(id,snippet/title,snippet/description,snippet/channelId, snippet/channelTitle,snippet/thumbnails/default/url)")
                        .execute();
            } catch (IOException e) {
                throw new RuntimeException(e);
            }

            // make sure we only have 1 video
            assert response.getItems().size() == 1;
            com.google.api.services.youtube.model.Video video = response.getItems().get(0);
            return new Video(
                    video.getId(),
                    video.getSnippet().getTitle(),
                    video.getSnippet().getDescription(),
                    video.getSnippet().getChannelId(),
                    video.getSnippet().getChannelTitle(),
                    video.getSnippet().getThumbnails().getDefault().getUrl());
        });
    }

    /**
     * @author Laurent Voisard
     * get a list of videos
     * @param videoIds a list of video ids
     * @return list of videos from ids
     */
    public CompletableFuture<List<Video>> getVideos(List<String> videoIds) {
        return CompletableFuture.supplyAsync(() -> {
            YouTube.Videos.List request;
            try {
                request = getYoutubeService().videos().list(Collections.singletonList("snippet"));
            } catch (IOException e) {
                throw new RuntimeException(e);
            }

            VideoListResponse response;
            try {
                response = request
                        .setKey(API_KEY)
                        .setId(videoIds)
                        .setFields("items(id,snippet/title,snippet/description,snippet/channelId, snippet/channelTitle,snippet/thumbnails/default/url)")
                        .execute();
            } catch (IOException e) {
                throw new RuntimeException(e);
            }

            return response.getItems().stream().map(video -> new Video(
                    video.getId(),
                    video.getSnippet().getTitle(),
                    video.getSnippet().getDescription(),
                    video.getSnippet().getChannelId(),
                    video.getSnippet().getChannelTitle(),
                    video.getSnippet().getThumbnails().getDefault().getUrl()))
                    .collect(Collectors.toList());
        });
    }

    public static List<Video> parseVideos(List<SearchResult> searchResults) throws IOException {
        List<Video> videos = new ArrayList<>();
        for (SearchResult result : searchResults) {
            Video video = new Video(
            result.getId().getVideoId(),
            result.getSnippet().getTitle(),
            result.getSnippet().getDescription(),
            result.getSnippet().getChannelId(),
            result.getSnippet().getChannelTitle(),
            result.getSnippet().getThumbnails().getDefault().getUrl());
            videos.add(video);
        }

        return videos;
    }

//    Channel-Page Task A
public CompletionStage<List<Video>> getChannelVideos(String channelId) throws IOException {
    return CompletableFuture.supplyAsync(() -> {
        try {
            YouTube.Search.List request = getYoutubeService().search().list(Collections.singletonList("id,snippet"));
            request.setKey(API_KEY);
            request.setChannelId(channelId);
            request.setMaxResults(10L);
            request.setOrder("date");
            request.setType(Collections.singletonList("video"));

            SearchListResponse response = request.execute();
            List<SearchResult> searchResults = response.getItems();

            // Convert SearchResult to Video objects
            return searchResults.stream()
                    .map(sr -> new Video(
                            sr.getId().getVideoId(),
                            sr.getSnippet().getTitle(),
                            sr.getSnippet().getDescription(),
                            sr.getSnippet().getChannelId(),
                            sr.getSnippet().getChannelTitle(),
                            sr.getSnippet().getThumbnails().getDefault().getUrl()
                    ))
                    .collect(Collectors.toList());
        } catch (IOException e) {
            throw new CompletionException(e);
        }
    });
}
    public CompletionStage<YoutubeChannel> getChannelById(String channelId) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                YouTube.Channels.List request = getYoutubeService().channels().list(Collections.singletonList("snippet"));
                request.setId(Collections.singletonList(channelId));
                request.setKey(API_KEY);

                ChannelListResponse response = request.execute();
                List<Channel> channelsFromApi = response.getItems();
                List<YoutubeChannel> channels = channelsFromApi.stream()
                        .map(channel -> new YoutubeChannel(
                                channel.getId(),
                                channel.getSnippet().getTitle(),
                                channel.getSnippet().getDescription(),
                                channel.getSnippet().getThumbnails().getDefault().getUrl(),
                                null))
                        .collect(Collectors.toList());

                return channels.isEmpty() ? null : channels.get(0);
            } catch (IOException e) {
                throw new CompletionException(e);
            }
        });
    }

}