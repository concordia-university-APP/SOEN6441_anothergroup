package services;

import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.api.services.youtube.YouTube;
import com.google.api.services.youtube.model.SearchListResponse;
import com.google.api.services.youtube.model.SearchResult;
import com.google.api.services.youtube.model.VideoListResponse;
import com.typesafe.config.Config;
import com.typesafe.config.ConfigFactory;
import models.Video;
import models.VideoList;

import java.io.IOException;
import java.security.GeneralSecurityException;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

public class YoutubeService {
    private final Config config = ConfigFactory.load();
    private final String API_KEY = config.getString("youtube.apiKey");
    private final String APPLICATION_NAME = config.getString("youtube.applicationName");
    private final JsonFactory JSON_FACTORY = JacksonFactory.getDefaultInstance();
    private final YouTube youtube;

    public YoutubeService() {
        try {
            youtube = new YouTube.Builder(GoogleNetHttpTransport.newTrustedTransport(), JSON_FACTORY, null)
                    .setApplicationName(APPLICATION_NAME)
                    .build();
        } catch (GeneralSecurityException e) {
            throw new RuntimeException(e);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
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
                YouTube.Search.List request = youtube.search().list(Collections.singletonList("id, snippet"));
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
                request = youtube.videos().list(Collections.singletonList("snippet"));
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
                request = youtube.videos().list(Collections.singletonList("snippet"));
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
}