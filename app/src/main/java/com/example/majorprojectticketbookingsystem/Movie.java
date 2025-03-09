package com.example.majorprojectticketbookingsystem;

import java.io.Serializable;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Movie implements Serializable {
    private String id;
    private String title;
    private String originalTitle;
    private String releaseDate;
    private String overview;
    private double popularity;
    private String language;
    private double voteAverage;
    private int voteCount;
    private boolean isAdult;
    private boolean hasVideo;
    private String posterPath;
    private String backdropPath;
    private String genreNames;

    private static final Map<Integer, String> GENRE_MAP = new HashMap<>();
    static {
        GENRE_MAP.put(28, "Action");
        GENRE_MAP.put(12, "Adventure");
        GENRE_MAP.put(16, "Animation");
        GENRE_MAP.put(35, "Comedy");
        GENRE_MAP.put(80, "Crime");
        GENRE_MAP.put(99, "Documentary");
        GENRE_MAP.put(18, "Drama");
        GENRE_MAP.put(10751, "Family");
        GENRE_MAP.put(14, "Fantasy");
        GENRE_MAP.put(36, "History");
        GENRE_MAP.put(27, "Horror");
        GENRE_MAP.put(10402, "Music");
        GENRE_MAP.put(9648, "Mystery");
        GENRE_MAP.put(10749, "Romance");
        GENRE_MAP.put(878, "Science Fiction");
        GENRE_MAP.put(10770, "TV Movie");
        GENRE_MAP.put(53, "Thriller");
        GENRE_MAP.put(10752, "War");
        GENRE_MAP.put(37, "Western");
    }

    public Movie(String id, String title, String originalTitle, String releaseDate, String overview, double popularity, String language,
                 double voteAverage, int voteCount, boolean isAdult, boolean hasVideo, String posterPath,
                 String backdropPath, List<Integer> genreIds) {

        this.id = id;
        this.title = title;
        this.originalTitle = originalTitle;
        this.releaseDate = releaseDate;
        this.overview = overview;
        this.popularity = popularity;
        this.language = language;
        this.voteAverage = voteAverage;
        this.voteCount = voteCount;
        this.isAdult = isAdult;
        this.hasVideo = hasVideo;
        this.posterPath = posterPath;
        this.backdropPath = backdropPath;
        this.genreNames = convertGenres(genreIds);
    }
    public Movie(String id, String title, String originalTitle, String releaseDate, String overview, double popularity, String language,
                 double voteAverage, int voteCount, boolean isAdult, boolean hasVideo, String posterPath,
                 String backdropPath, String genreNames) {

        this.id = id;
        this.title = title;
        this.originalTitle = originalTitle;
        this.releaseDate = releaseDate;
        this.overview = overview;
        this.popularity = popularity;
        this.language = language;
        this.voteAverage = voteAverage;
        this.voteCount = voteCount;
        this.isAdult = isAdult;
        this.hasVideo = hasVideo;
        this.posterPath = posterPath;
        this.backdropPath = backdropPath;
        this.genreNames = genreNames;
    }

    private String convertGenres(List<Integer> genreIds) {
        StringBuilder genres = new StringBuilder();
        for (int id : genreIds) {
            if (GENRE_MAP.containsKey(id)) {
                if (genres.length() > 0) genres.append(", ");
                genres.append(GENRE_MAP.get(id));
            }
        }
        return genres.toString();
    }

    public String getId() { return id; }
    public String getTitle() { return title; }
    public String getOriginalTitle() { return originalTitle; }
    public String getReleaseDate() { return releaseDate; }
    public String getOverview() { return overview; }
    public double getPopularity() { return popularity; }
    public String getLanguage() { return language; }
    public double getVoteAverage() { return voteAverage; }
    public int getVoteCount() { return voteCount; }
    public boolean isAdult() { return isAdult; }
    public boolean hasVideo() { return hasVideo; }
    public String getPosterPath() { return posterPath; }
    public String getBackdropPath() { return backdropPath; }
    public String getGenreNames() { return genreNames; }
}
