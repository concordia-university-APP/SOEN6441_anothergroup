package services;

import com.google.api.services.youtube.YouTube;
import com.google.api.services.youtube.model.ChannelListResponse;
import com.google.api.services.youtube.model.SearchListResponse;
import com.google.api.services.youtube.model.SearchResult;
import models.Channel;
import models.Video;
import models.VideoList;

import java.io.IOException;
import java.security.GeneralSecurityException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class ChannelService {
    public static Channel getChannelProfile(String channelId) throws GeneralSecurityException, IOException {
        YouTube youtubeService = YoutubeService.getService();

        // Fetch channel details
        YouTube.Channels.List channelRequest = youtubeService.channels().list(Collections.singletonList("snippet"));
        ChannelListResponse channelResponse = channelRequest
                .setId(Collections.singletonList(channelId))
                .setFields("items(id,snippet/title,snippet/description,snippet/thumbnails/default/url)")
                .execute();

        var channelItem = channelResponse.getItems().get(0);
        String title = channelItem.getSnippet().getTitle();
        String description = channelItem.getSnippet().getDescription();
        String thumbnailUrl = channelItem.getSnippet().getThumbnails().getDefault().getUrl();

        // Fetch recent videos from the channel
        YouTube.Search.List searchRequest = youtubeService.search().list(Collections.singletonList("id,snippet"));
        SearchListResponse searchResponse = searchRequest
                .setChannelId(channelId)
                .setType(Collections.singletonList("video"))
                .setOrder("date")
                .setMaxResults(10L)
                .execute();

        List<Video> videos = new ArrayList<>();
        for (SearchResult result : searchResponse.getItems()) {
            String videoId = result.getId().getVideoId();
            String videoTitle = result.getSnippet().getTitle();
            String videoDescription = result.getSnippet().getDescription();
            String videoThumbnailUrl = result.getSnippet().getThumbnails().getDefault().getUrl();
            String videoChannelId = result.getSnippet().getChannelId();
            String videoChannelName = result.getSnippet().getChannelTitle();

            videos.add(new Video(videoId, videoTitle, videoDescription, videoChannelId, videoChannelName, videoThumbnailUrl));
        }

        return new Channel(channelId, title, description, thumbnailUrl, new VideoList(videos));
    }
}
