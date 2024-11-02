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
}

