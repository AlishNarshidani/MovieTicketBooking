package com.example.majorprojectticketbookingsystem;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class BookedSeatsAdapter extends RecyclerView.Adapter<BookedSeatsAdapter.BookedSeatsViewHolder>{

    private Context context;
    private List<BookedSeat> bookedSeatsList;

    public BookedSeatsAdapter(Context context, List<BookedSeat> bookedSeatsList) {
        this.context = context;
        this.bookedSeatsList = bookedSeatsList;
    }

    @NonNull
    @Override
    public BookedSeatsAdapter.BookedSeatsViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_booked_seat, parent, false);
        return new BookedSeatsAdapter.BookedSeatsViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull BookedSeatsAdapter.BookedSeatsViewHolder holder, int position) {
        BookedSeat bookedSeat = bookedSeatsList.get(position);
        holder.seatId.setText("Seat ID: " + bookedSeat.getSeatId());
        holder.userId.setText("User ID: " + bookedSeat.getUserId());
        holder.price.setText("Price: " + bookedSeat.getPrice());
        holder.bookingType.setText("Booking Type: " + bookedSeat.getBookingType());
    }

    @Override
    public int getItemCount() {
        return bookedSeatsList.size();
    }

    public static class BookedSeatsViewHolder extends RecyclerView.ViewHolder {
        TextView seatId, userId, price, bookingType;

        public BookedSeatsViewHolder(@NonNull View itemView) {
            super(itemView);
            seatId = itemView.findViewById(R.id.tvSeatId);
            userId = itemView.findViewById(R.id.tvUserId);
            price = itemView.findViewById(R.id.tvPrice);
            bookingType = itemView.findViewById(R.id.tvBookingType);
        }
    }

}
