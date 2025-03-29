package com.example.majorprojectticketbookingsystem;

import java.io.Serializable;

public class BookedSeat implements Serializable {
    String seatId, userId, price, bookingType;

    public BookedSeat(String seatId, String userId, String price, String bookingType) {
        this.seatId = seatId;
        this.userId = userId;
        this.price = price;
        this.bookingType = bookingType;
    }

    public String getSeatId() {
        return seatId;
    }

    public String getUserId() {
        return userId;
    }

    public String getPrice() {
        return price;
    }

    public String getBookingType() {
        return bookingType;
    }
}
