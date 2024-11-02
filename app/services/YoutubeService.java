package services;

import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.api.services.youtube.YouTube;
import com.google.api.services.youtube.model.SearchListResponse;
import com.google.api.services.youtube.model.SearchResult;
import com.typesafe.config.Config;
import com.typesafe.config.ConfigFactory;

import java.io.IOException;
import java.security.GeneralSecurityException;
import java.util.Collections;
import java.util.List;

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

    public static List<SearchResult> searchResults(String keywords) throws GeneralSecurityException, IOException{
        YouTube youtubeService = getService();
        YouTube.Search.List request = youtubeService.search().list("id, snippet");
        try {
            SearchListResponse response = request.setKey(API_KEY)
                    .setQ(keywords)
                    .setType("video")
                    .setOrder("date")
                    .setFields("items(id/kind,id/videoId,snippet/title,snippet/thumbnails/default/url)")
                    .setMaxResults(10L)
                    .execute();
        List<SearchResult> items = response.getItems();
        return items != null ? items : Collections.emptyList();
        } catch (IOException e) {
            throw new IOException("Error occurred while executing YouTube search: " + e.getMessage(), e);
        }
    }
}
