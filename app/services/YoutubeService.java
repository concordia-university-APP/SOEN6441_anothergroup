package services;

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

import java.io.IOException;
import java.security.GeneralSecurityException;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;
import java.util.concurrent.CompletionStage;
import java.util.stream.Collectors;

/**
 * @author Yehia, Laurent, Tanveer Reza
 */
public class YoutubeService {
    private final Config config = ConfigFactory.load();
    private final String API_KEY = config.getString("youtube.apiKey");
    private final String APPLICATION_NAME = config.getString("youtube.applicationName");
    private final JsonFactory JSON_FACTORY = JacksonFactory.getDefaultInstance();
    private YouTube youtube;

    public YoutubeService() throws GeneralSecurityException, IOException {

            setYoutubeService(new YouTube.Builder(GoogleNetHttpTransport.newTrustedTransport(), JSON_FACTORY, null)
                    .setApplicationName(APPLICATION_NAME)
                    .build());
    }

    /**
     * @author Laurent Voisard
     * @return youtube service
     */
    public YouTube getYoutubeService() {
        return youtube;
    }

    /**
     * @author Laurent Voisard
     * private setter, only used in tests to mock behavior
     * @param youtube youtube service
     */
    void setYoutubeService(YouTube youtube) {
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
            YouTube.Search.List request = getYoutubeSearchList();
            SearchListResponse response = getSearchListResponse(keywords, maxResults, request);
            List<SearchResult> items = response.getItems();

            return items.stream()
                    .map(item -> item.getId().getVideoId())
                    .collect(Collectors.toList());
        }).thenCompose(videoIds -> {
            // Once we have the video IDs, asynchronously fetch the videos
            return getVideos(videoIds).thenApply(VideoList::new);
        });
    }

    SearchListResponse getSearchListResponse(String keywords, Long maxResults, YouTube.Search.List request) {
        SearchListResponse response;
        try {
            response = request
                    .setKey(API_KEY)
                    .setQ(keywords)
                    .setType(Collections.singletonList("video"))
                    .setOrder("date")
                    .setFields("items(id/videoId)")
                    .setMaxResults(maxResults)
                    .execute();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return response;
    }

    YouTube.Search.List getYoutubeSearchList() {
        YouTube.Search.List request;
        try {
            request = getYoutubeService().search().list(Collections.singletonList("id, snippet"));
        } catch (IOException e) {
            throw new RuntimeException("Error occurred while executing YouTube search: " + e.getMessage(), e);
        }
        return request;
    }

    /**
     * @author Laurent Voisard
     * Get video by id
     * @param videoId video id
     * @return video model
     */
    public CompletableFuture<Video> getVideo(String videoId) {
        return CompletableFuture.supplyAsync(() -> {
            YouTube.Videos.List request = getYoutubeVideosList();

            VideoListResponse response = getVideoListResponse(Collections.singletonList(videoId), request);

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

    private YouTube.Videos.List getYoutubeVideosList() {
        YouTube.Videos.List request;
        try {
            request = getYoutubeService().videos().list(Collections.singletonList("snippet"));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return request;
    }


    /**
     * @author Laurent Voisard
     * get a list of videos
     * @param videoIds a list of video ids
     * @return list of videos from ids
     */
    public CompletableFuture<List<Video>> getVideos(List<String> videoIds) {
        return CompletableFuture.supplyAsync(() -> {
            YouTube.Videos.List request = getYoutubeVideosList();

            VideoListResponse response = getVideoListResponse(videoIds, request);

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

    private VideoListResponse getVideoListResponse(List<String> videoIds, YouTube.Videos.List request) {
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
        return response;
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