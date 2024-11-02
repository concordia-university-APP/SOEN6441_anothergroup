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
import models.VideoId;
import models.VideoIdList;

import java.io.IOException;
import java.security.GeneralSecurityException;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

public class YoutubeService {
    private static final Config config = ConfigFactory.load();
    private static final String API_KEY = config.getString("youtube.apiKey");
    private static final String APPLICATION_NAME = config.getString("youtube.applicationName");
    private static final JsonFactory JSON_FACTORY = JacksonFactory.getDefaultInstance();


    public static YouTube getService() throws GeneralSecurityException, IOException {
        return new YouTube.Builder(GoogleNetHttpTransport.newTrustedTransport(), JSON_FACTORY, null)
                .setApplicationName(APPLICATION_NAME)
                .build();
    }

    public static VideoIdList searchResults(String keywords) throws GeneralSecurityException, IOException {
        YouTube.Search.List request = getService().search().list(Collections.singletonList("id, snippet"));
        try {
            SearchListResponse response = request
                    .setKey(API_KEY)
                    .setQ(keywords)
                    .setType(Collections.singletonList("video"))
                    .setOrder("date")
                    .setFields("items(id/kind,id/videoId,snippet/title,snippet/description,snippet/channelId, snippet/channelTitle,snippet/thumbnails/default/url)")
                    .setMaxResults(10L)
                    .execute();
            List<SearchResult> items = response.getItems();
            List<VideoId>  ids = items.stream()
                            .map(x -> new VideoId(x.getId().getVideoId()))
                            .collect(Collectors.toList());
            VideoIdList list = new VideoIdList(ids);
            return list;
        } catch (IOException e) {
            throw new IOException("Error occurred while executing YouTube search: " + e.getMessage(), e);
        }
    }



    public static Video getVideo(String videoId) {
        YouTube.Videos.List request = null;
        try {
            request = getService().videos().list(Collections.singletonList("snippet"));
        } catch (IOException | GeneralSecurityException e) {
            throw new RuntimeException(e);
        }

        VideoListResponse response = null;
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
    }



}
