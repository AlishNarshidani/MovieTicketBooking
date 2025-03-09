package com.example.majorprojectticketbookingsystem;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class HallAdapter extends RecyclerView.Adapter<HallAdapter.HallViewHolder> {

    private List<Hall> hallList;
    private Context context;
    private Movie movie;
    private String caller;
    private Theatre theatre;

    public HallAdapter(List<Hall> hallList, Context context, Movie movie, String caller) {
        this.hallList = hallList;
        this.context = context;
        this.movie = movie;
        this.caller = caller;
    }
    public HallAdapter(List<Hall> hallList, Context context, Movie movie, String caller, Theatre theatre) {
        this.hallList = hallList;
        this.context = context;
        this.movie = movie;
        this.caller = caller;
        this.theatre = theatre;
    }

    @NonNull
    @Override
    public HallViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_hall, parent, false);
        return new HallAdapter.HallViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull HallViewHolder holder, int position) {
        Hall hall = hallList.get(position);
        holder.hallName.setText("Hall Name: " + hall.getHallName());
        holder.totalSeats.setText("Total Seats: " + hall.getTotalSeats());
        holder.screenType.setText("Screen Type: " + hall.getScreenType());
        holder.soundSystem.setText("Sound System: " + hall.getSoundSystem());

        // Redirect to another activity when a hall is selected
        holder.itemView.setOnClickListener(v -> {
            Intent intent;

            if ("customer".equalsIgnoreCase(caller)) {
                //redirect to activity that shows available shows for booking
                intent = new Intent(context, CustomerSideChooseAvailableShowsActivity.class);
                intent.putExtra("theatre",theatre);
            } else if ("employee".equalsIgnoreCase(caller)) {
                intent = new Intent(context, EmployeeSideAddMovieToHallActivity.class);
            } else {
                intent = new Intent(context, CustomerSideChooseAvailableShowsActivity.class); // Fallback page
                intent.putExtra("theatre",theatre);
            }

            intent.putExtra("hall", hall);
            intent.putExtra("movie", movie);
            context.startActivity(intent);
        });
    }

    @Override
    public int getItemCount() {
        return hallList.size();
    }

    public static class HallViewHolder extends RecyclerView.ViewHolder {
        TextView hallName, totalSeats, screenType, soundSystem;

        public HallViewHolder(@NonNull View itemView) {
            super(itemView);
            hallName = itemView.findViewById(R.id.hallName);
            totalSeats = itemView.findViewById(R.id.totalSeats);
            screenType = itemView.findViewById(R.id.screenType);
            soundSystem = itemView.findViewById(R.id.soundSystem);
        }
    }
}
