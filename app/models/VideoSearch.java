package models;

import actors.FleschReadingActor;
import services.SentimentAnalyzer;

/**
 *  Represents an instance of a search model containing search terms,
 *  a list of video results, and an overall sentiment analysis.
 * @author Laurent Voisard, Rumeysa Turkmen
 */
public class VideoSearch {
    private final String searchTerms;
    private final VideoList results;
    private String sentiment;
    private double fleschEaseScoreAverage;
    private double fleschGradeLevelAverage;

    /**
     * Constructs a VideoSearch object with the specified search terms,
     * results, and sentiment analysis.
     *
     * @author Laurent Voisard, Rumeysa Turkmen
     * @param searchTerms The keywords used for the search.
     * @param results The list of videos returned from the search.
     * @param sentiment The overall sentiment of the video results.
     */
    public VideoSearch(String searchTerms, VideoList results, String sentiment) {
        this.searchTerms = searchTerms;
        this.results = results;
        this.sentiment = sentiment;
        this.fleschEaseScoreAverage = getFleschEaseScoreAverage();
        this.fleschGradeLevelAverage = getFleschGradeLevelAverage();
    }

    public void updateScoresAndSentiment() {
        this.fleschEaseScoreAverage = getFleschEaseScoreAverage();
        this.fleschGradeLevelAverage = getFleschGradeLevelAverage();
        this.sentiment = SentimentAnalyzer.analyzeSentiment(this.getResults().getVideoList());
    }

    /**
     * Retrieves the search terms associated with this video search
     * @author Laurent Voisard
     * @return The search terms used.
     */
    public String getSearchTerms() { return searchTerms;}
    /**
     * Retrieves the list of video results from the search
     * @author Laurent Voisard
     * @return The list of videos in the search result.
     */
    public VideoList getResults() { return results;}

    /**
     * Retrieves the overall sentiment analysis of the video search.
     * @author Rumeysa Turkmen
     * @return The sentiment as a string (e.g., happy, sad, neutral).
     */
    public String getSentiment() { return sentiment;}


    /**
     * Calculates the average Flesch Reading Ease score of all videos in the search result.
     * @author Laurent Voisard
     * @return The average reading ease score, or 0.0 if no videos are present.
     */
    public double getFleschEaseScoreAverage() {
        return this.results.getVideoList().stream()
                .map(Video::getReadingEaseScore)
                .mapToDouble(Double::doubleValue)
                .average()
                .orElse(0.0f);
    }

    /**
     * Calculates the average Flesch grade level of all videos in the search result.
     * @author Laurent Voisard
     * @return The average grade level, or 0.0 if no videos are present.
     */
    public double getFleschGradeLevelAverage() {
        return this.results.getVideoList().stream()
                .map(Video::getReadingGradeLevel)
                .mapToDouble(Double::doubleValue)
                .average()
                .orElse(0.0f);
    }

}