package com.example.majorprojectticketbookingsystem;

import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class BookingAdapter extends RecyclerView.Adapter<BookingAdapter.BookingViewHolder>{

    private List<Booking> bookingList;
    private Context context;

    public BookingAdapter(List<Booking> bookingList, Context context) {
        this.bookingList = bookingList;
        this.context = context;
    }

    @NonNull
    @Override
    public BookingAdapter.BookingViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_booking,parent,false);

        return new BookingAdapter.BookingViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull BookingAdapter.BookingViewHolder holder, int position) {
        Booking booking = bookingList.get(position);
        holder.txtMovieName.setText("Movie: "+booking.getMovieName());
        holder.txtTheatreName.setText("Theatre: "+booking.getTheatreName());
        holder.txtTheatreLocation.setText("Address: "+booking.getTheatreLocation());
        holder.txtShowStartTime.setText("Start: "+booking.getShowStartTime());
        holder.txtShowEndTime.setText("End: "+booking.getShowEndTime());
        holder.txtSelectedSeats.setText("Seats: "+booking.getSelectedSeats());
        holder.txtTotalPrice.setText("Total Amount: ₹"+booking.getTotalPrice());
        String bookingStatus = booking.getBookingStatus();

        if(bookingStatus != null && !bookingStatus.isEmpty()) {
            holder.txtBookingStatus.setText("Booking: "+bookingStatus);
            if (bookingStatus.equalsIgnoreCase("confirmed")) {
                holder.txtBookingStatus.setTextColor(Color.GREEN);
            } else if (bookingStatus.equalsIgnoreCase("cancelled")) {
                holder.txtBookingStatus.setTextColor(Color.RED);
            }
        } else {
            holder.txtBookingStatus.setText("Booking Pending");
        }
    }

    @Override
    public int getItemCount() {
        return bookingList.size();
    }


    public static class BookingViewHolder extends RecyclerView.ViewHolder
    {
        TextView txtMovieName, txtTheatreName, txtTheatreLocation, txtShowStartTime, txtShowEndTime, txtSelectedSeats, txtTotalPrice, txtBookingStatus;

        public BookingViewHolder(@NonNull View itemView) {
            super(itemView);
            txtMovieName = itemView.findViewById(R.id.txtMovieName);
            txtTheatreName = itemView.findViewById(R.id.txtTheatreName);
            txtTheatreLocation = itemView.findViewById(R.id.txtTheatreLocation);
            txtShowStartTime = itemView.findViewById(R.id.txtShowStartTime);
            txtShowEndTime = itemView.findViewById(R.id.txtShowEndTime);
            txtSelectedSeats = itemView.findViewById(R.id.txtSelectedSeats);
            txtTotalPrice = itemView.findViewById(R.id.txtTotalPrice);
            txtBookingStatus = itemView.findViewById(R.id.txtBookingStatus);
        }
    }
}
