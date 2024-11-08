package models;

/**
 * @author Laurent Voisard
 * Instance of a search model
 */
public class VideoSearch {
    private final String searchTerms;
    private final VideoList results;
    private final String sentiment;

    /**
     * @author Laurent Voisard
     * Basic Constructor
     * @param searchTerms keywords of the search
     * @param results list of videos of the search
     * @param sentiment overall sentiment of the search videos
     */
    public VideoSearch(String searchTerms, VideoList results, String sentiment) {
        this.searchTerms = searchTerms;
        this.results = results;
        this.sentiment = sentiment;
    }

    /**
     * @author Laurent Voisard
     * @return keywords
     */
    public String getSearchTerms() { return searchTerms;}
    /**
     * @author Laurent Voisard
     * @return list of videos
     */
    public VideoList getResults() { return results;}

    /**
     * @author Rumeysa Turkmen
     * @return sentiment of the search
     */
    public String getSentiment() { return sentiment;}



    /**
     * @author Laurent Voisard
     * @return the average reading ease score of all videos in the search
     */
    public double getFleschEaseScoreAverage() {
        return this.results.getVideoList().stream()
                .map(Video::getFleschReadingEaseScore)
                .mapToDouble(FleschReadingEaseScore::getReadingEaseScore)
                .average()
                .orElse(0.0f);
    }

    /**
     * @author Laurent Voisard
     * @return the average grade level of all videos in the search
     */
    public double getFleschGradeLevelAverage() {
        return this.results.getVideoList().stream()
                .map(Video::getFleschReadingEaseScore)
                .mapToDouble(FleschReadingEaseScore::getGradeLevel)
                .average()
                .orElse(0.0f);
    }

}

