package com.example.majorprojectticketbookingsystem;

import java.io.Serializable;

public class ShowDayWise implements Serializable {
    private String showId, hall_id, theatreId, movieId, movieName, showStartTime, showEndTime;
    Hall hall;
    Movie movie;
    Theatre theatre;

    // ✅ Empty constructor for Firestore
    public ShowDayWise() {}

    // ✅ Constructor
    public ShowDayWise(String showId, String hall_id, String theatreId, String movieId,
                       String movieName, String  showStartTime, String showEndTime, Hall hall, Movie movie, Theatre theatre) {
        this.showId = showId;
        this.hall_id = hall_id;
        this.theatreId = theatreId;
        this.movieId = movieId;
        this.movieName = movieName;
        this.showStartTime = showStartTime;
        this.showEndTime = showEndTime;
        this.hall = hall;
        this.movie = movie;
        this.theatre = theatre;
    }

    // ✅ Getters
    public String getShowId() { return showId; }
    public String getHall_id() { return hall_id; }
    public String getTheatreId() { return theatreId; }
    public String getMovieId() { return movieId; }
    public String getMovieName() { return movieName; }
    public String getShowStartTime() { return showStartTime; }
    public String getShowEndTime() { return showEndTime; }
    public Hall getHallObj() { return hall; }
    public Movie getMovieObj() { return movie; }
    public Theatre getTheareObj() { return theatre; }
}
