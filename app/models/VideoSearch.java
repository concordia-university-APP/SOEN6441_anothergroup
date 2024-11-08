package models;

public class VideoSearch {
    private final String searchTerms;
    private final VideoList results;
    private final String sentiment;

    public VideoSearch(String searchTerms, VideoList results, String sentiment) {
        this.searchTerms = searchTerms;
        this.results = results;
        this.sentiment = sentiment;
    }

    public String getSearchTerms() { return searchTerms;}
    public VideoList getResults() { return results;}
    public String getSentiment() { return sentiment;}



    public double getFleschEaseScoreAverage() {
        return this.results.getVideoList().stream()
                .map(Video::getFleschReadingEaseScore)
                .mapToDouble(FleschReadingEaseScore::getReadingEaseScore)
                .average()
                .orElse(-1.0f);
    }

    public double getFleschGradeLevelAverage() {
        return this.results.getVideoList().stream()
                .map(Video::getFleschReadingEaseScore)
                .mapToDouble(FleschReadingEaseScore::getGradeLevel)
                .average()
                .orElse(-1.0f);
    }
}

