package com.example.majorprojectticketbookingsystem;

import java.io.Serializable;

public class Hall implements Serializable {
    private String hallId;
    private String theatreId;
    private String hallName;
    private long totalSeats;
    private String screenType;
    private String soundSystem;

    public Hall(String hallId, String theatreId, String hallName, long totalSeats, String screenType, String soundSystem) {
        this.hallId = hallId;
        this.theatreId = theatreId;
        this.hallName = hallName;
        this.totalSeats = totalSeats;
        this.screenType = screenType;
        this.soundSystem = soundSystem;
    }

    public String getHallId() {
        return hallId;
    }

    public String getTheatreId() {
        return theatreId;
    }

    public String getHallName() {
        return hallName;
    }

    public long getTotalSeats() {
        return totalSeats;
    }

    public String getScreenType() {
        return screenType;
    }

    public String getSoundSystem() {
        return soundSystem;
    }
}
