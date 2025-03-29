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

public class ShowDayWiseAdapter extends RecyclerView.Adapter<ShowDayWiseAdapter.ShowDayWiseViewHolder>{

    private List<ShowDayWise> showList;
    private Context context;
    private String caller;

    public ShowDayWiseAdapter(List<ShowDayWise> showList, Context context, String caller) {
        this.showList = showList;
        this.context = context;
        this.caller = caller;
    }

    @NonNull
    @Override
    public ShowDayWiseAdapter.ShowDayWiseViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_show_day_wise, parent, false);
        return new ShowDayWiseAdapter.ShowDayWiseViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ShowDayWiseAdapter.ShowDayWiseViewHolder holder, int position) {
        ShowDayWise showDayWise = showList.get(position);
        Movie movie = showDayWise.getMovieObj();
        Hall hall = showDayWise.getHallObj();
        Theatre theatre = showDayWise.getTheareObj();
        holder.movieName.setText("Movie: " + movie.getTitle());
        holder.hallName.setText("Hall: " + hall.getHallName());
        holder.totalSeats.setText("Total Seats: " + hall.getTotalSeats());
        holder.showStartTime.setText("Show Start : " + showDayWise.getShowStartTime());
        holder.showEndTime.setText("Show End : " + showDayWise.getShowEndTime());


        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                Intent intent;

                if (caller.equalsIgnoreCase("EmployeeViewUpcomingBookingsDayWise")) {
                    intent = new Intent(context, EmployeeSideSeatsSelectionForViewBookings.class);
                    intent.putExtra("caller",caller);
                } else if (caller.equalsIgnoreCase("EmployeeViewPastBookingsDayWise")){
                    intent = new Intent(context, EmployeeSideViewBookedSeatsDetails.class);
                    intent.putExtra("showId",showDayWise.getShowId());
                } else {
                    intent = new Intent(context, EmployeeSideSeatsSelectionForViewBookings.class);
                }

                intent.putExtra("theatre",theatre);
                intent.putExtra("showDayWise", showDayWise);
                intent.putExtra("hall", hall);
                intent.putExtra("movie", movie);
                context.startActivity(intent);
            }
        });

    }

    @Override
    public int getItemCount() {
        return showList.size();
    }

    public static class ShowDayWiseViewHolder extends RecyclerView.ViewHolder {
        TextView movieName, hallName, totalSeats, showStartTime, showEndTime;

        public ShowDayWiseViewHolder(@NonNull View itemView) {
            super(itemView);
            movieName = itemView.findViewById(R.id.movieName);
            hallName = itemView.findViewById(R.id.hallName);
            totalSeats = itemView.findViewById(R.id.totalSeats);
            showStartTime = itemView.findViewById(R.id.showStartTime);
            showEndTime = itemView.findViewById(R.id.showEndTime);
        }
    }
}
