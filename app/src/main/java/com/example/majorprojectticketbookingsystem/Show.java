package com.example.majorprojectticketbookingsystem;

import com.google.firebase.Timestamp;

import java.io.Serializable;

public class Show implements Serializable {
    private String showId, hall_id, theatreId, movieId, movieName, showStartTime, showEndTime;

    // ✅ Empty constructor for Firestore
    public Show() {}

    // ✅ Constructor
    public Show(String showId, String hall_id, String theatreId, String movieId,
                String movieName, String  showStartTime, String showEndTime) {
        this.showId = showId;
        this.hall_id = hall_id;
        this.theatreId = theatreId;
        this.movieId = movieId;
        this.movieName = movieName;
        this.showStartTime = showStartTime;
        this.showEndTime = showEndTime;
    }

    // ✅ Getters
    public String getShowId() { return showId; }
    public String getHall_id() { return hall_id; }
    public String getTheatreId() { return theatreId; }
    public String getMovieId() { return movieId; }
    public String getMovieName() { return movieName; }
    public String getShowStartTime() { return showStartTime; }
    public String getShowEndTime() { return showEndTime; }
}

