package com.example.majorprojectticketbookingsystem;

import java.io.Serializable;
import java.util.List;

public class Booking implements Serializable {
    private String bookingId;
    private String bookingStatus;
    private String movieName;
    private String theatreName;
    private String theatreLocation;
    private String hallName;
    private String showId;
    private String showStartTime;
    private String showEndTime;
    private String selectedSeats;
    private String totalPrice;
    private List<String> seatList;

    public Booking(String bookingId, String bookingStatus, String movieName, String theatreName, String theatreLocation, String hallName, String showId, String showStartTime, String showEndTime, String selectedSeats, String totalPrice, List<String> seatList) {
        this.bookingId = bookingId;
        this.bookingStatus = bookingStatus;
        this.movieName = movieName;
        this.theatreName = theatreName;
        this.theatreLocation = theatreLocation;
        this.hallName = hallName;
        this.showId = showId;
        this.showStartTime = showStartTime;
        this.showEndTime = showEndTime;
        this.selectedSeats = selectedSeats;
        this.totalPrice = totalPrice;
        this.seatList = seatList;
    }

    public String getBookingId() {
        return bookingId;
    }

    public String getBookingStatus() {
        return bookingStatus;
    }

    public String getMovieName() {
        return movieName;
    }

    public String getTotalPrice() {
        return totalPrice;
    }

    public String getSelectedSeats() {
        return selectedSeats;
    }

    public String getShowEndTime() {
        return showEndTime;
    }

    public String getShowStartTime() {
        return showStartTime;
    }

    public String getHallName() {
        return hallName;
    }

    public String getShowId() {
        return showId;
    }

    public String getTheatreLocation() {
        return theatreLocation;
    }

    public String getTheatreName() {
        return theatreName;
    }

    public List<String> getSeatList() {
        return seatList;
    }
}
