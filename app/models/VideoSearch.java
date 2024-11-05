package models;

public class VideoSearch {
    private final String searchTerms;
    private final VideoList results;

    public VideoSearch(String searchTerms, VideoList results) {
        this.searchTerms = searchTerms;
        this.results = results;
    }

    public String getSearchTerms() { return searchTerms;}
    public VideoList getResults() { return results;}

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

