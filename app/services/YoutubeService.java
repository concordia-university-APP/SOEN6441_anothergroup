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
import java.util.stream.Collectors;

public class YoutubeService {
    private static final Config config = ConfigFactory.load();
    private static final String API_KEY = config.getString("youtube.apiKey");
    private static final String APPLICATION_NAME = config.getString("youtube.applicationName");
    private static final JsonFactory JSON_FACTORY = JacksonFactory.getDefaultInstance();

    public static YouTube getService() {
        try {
            return new YouTube.Builder(GoogleNetHttpTransport.newTrustedTransport(), JSON_FACTORY, null)
                    .setApplicationName(APPLICATION_NAME)
                    .build();
        } catch (GeneralSecurityException | IOException e) {
            throw new RuntimeException(e);
        }
    }

    public static VideoList searchResults(String keywords, Long maxResults) {
        try {
        YouTube.Search.List request = getService().search().list(Collections.singletonList("id, snippet"));
            SearchListResponse response = request
                    .setKey(API_KEY)
                    .setQ(keywords)
                    .setType(Collections.singletonList("video"))
                    .setOrder("date")
                    .setFields("items(id/videoId)")
                    .setMaxResults(maxResults)
                    .execute();
            List<SearchResult> items = response.getItems();
            List<Video> videos = items.stream()
                            .map(x -> getVideo(x.getId().getVideoId()))
                            .collect(Collectors.toList());
            return new VideoList(videos);
        } catch (IOException e) {
            throw new RuntimeException("Error occurred while executing YouTube search: " + e.getMessage(), e);
        }
    }



    public static Video getVideo(String videoId) {
        YouTube.Videos.List request = null;
        try {
            request = getService().videos().list(Collections.singletonList("snippet"));
        } catch (IOException e) {
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
    public static Map<String, Long> getWordFrequency(String query) {
        VideoList videos = searchResults(query, 50L);

        List<String> titles = videos.getVideoList().stream()
                .map(Video::getTitle) // Extract each title
                .collect(Collectors.toList());

        List<String> listOfWordsFromTiles = extractAndNormalizeWords(titles);

        return countAndSortWordFrequencies(listOfWordsFromTiles)
                .stream()
                .collect(Collectors.toMap(
                        Map.Entry::getKey,
                        Map.Entry::getValue,
                        (e1, e2) -> e1, // In case of a key collision, keep the existing entry
                        LinkedHashMap::new // Use LinkedHashMap to preserve the order
                ));
    }

    private static List<String> extractAndNormalizeWords(List<String> titles) {
        return titles.stream()
                .flatMap(title -> Arrays.stream(title.split("\\W+"))) // Split titles into words
                .filter(word -> !word.isEmpty()) // Filter out empty words
                .map(String::toLowerCase)
                .collect(Collectors.toList()); // Collect to list
    }

    private static List<Map.Entry<String, Long>> countAndSortWordFrequencies(List<String> words) {
        return getWordOccurences(words).entrySet().stream()
                .sorted(Map.Entry.<String, Long>comparingByValue().reversed())
                .collect(Collectors.toList());
    }

    private static Map<String, Long> getWordOccurences(List<String> words) {
        return words.stream()
                .collect(Collectors.groupingBy(word -> word, Collectors.counting())); // Count occurrences
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

}
