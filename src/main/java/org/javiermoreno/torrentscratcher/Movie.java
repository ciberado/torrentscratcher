package org.javiermoreno.torrentscratcher;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.HashMap;
import java.util.Map;

/**
 *
 * @author ciberado
 */
public class Movie {

    private String type = "movie";
    @JsonProperty("imdb_id")
    private String imdbId;
    private String filmAffinityId;
    private String title;
    private String originalTitle;
    private String year;
    private String genre;
    private double rating;
    private String image;
    private String description;
    private String url;
    private Map<String /* type: 720p */, Torrent> torrents = new HashMap<>();

    public Movie() {
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getImdbId() {
        return imdbId;
    }

    public void setImdbId(String imdbId) {
        this.imdbId = imdbId;
    }

    public String getFilmAffinityId() {
        return filmAffinityId;
    }

    public void setFilmAffinityId(String filmAffinityId) {
        this.filmAffinityId = filmAffinityId;
    }
    
    public String getOriginalTitle() {
        return originalTitle;
    }

    public void setOriginalTitle(String originalTitle) {
        this.originalTitle = originalTitle;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getYear() {
        return year;
    }

    public void setYear(String year) {
        this.year = year;
    }

    public String getGenre() {
        return genre;
    }

    public void setGenre(String genre) {
        this.genre = genre;
    }

    public double getRating() {
        return rating;
    }

    public void setRating(double rating) {
        this.rating = rating;
    }

    public String getImage() {
        return image;
    }

    public void setImage(String image) {
        this.image = image;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public Map<String, Torrent> getTorrents() {
        return torrents;
    }

    public void setTorrents(Map<String, Torrent> torrents) {
        this.torrents = torrents;
    }

    
    
    
        
}
