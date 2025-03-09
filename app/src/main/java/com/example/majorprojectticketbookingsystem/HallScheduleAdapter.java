package com.example.majorprojectticketbookingsystem;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class HallScheduleAdapter extends RecyclerView.Adapter<HallScheduleAdapter.HallScheduleViewHolder> {


    private List<Show> viewHallScheduleList;


    public HallScheduleAdapter(List<Show> viewHallScheduleList) {
        this.viewHallScheduleList = viewHallScheduleList;
    }

    @NonNull
    @Override
    public HallScheduleAdapter.HallScheduleViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_hall_schedule,parent,false);

        return new HallScheduleViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull HallScheduleAdapter.HallScheduleViewHolder holder, int position) {
        Show show = viewHallScheduleList.get(position);
        holder.showId.setText("Show ID: "+show.getShowId());
        holder.movieName.setText("Movie Name: "+show.getMovieName());
        holder.startDateTime.setText("From: "+show.getShowStartTime());
        holder.endDateTime.setText("To: "+show.getShowEndTime());
    }

    @Override
    public int getItemCount() {
        return viewHallScheduleList.size();
    }

    public static class HallScheduleViewHolder extends RecyclerView.ViewHolder
    {
        CardView cardView;
        TextView showId;
        TextView movieName;
        TextView startDateTime;
        TextView endDateTime;

        public HallScheduleViewHolder(@NonNull View itemView) {
            super(itemView);
            cardView = itemView.findViewById(R.id.cardViewTransaction);
            showId = itemView.findViewById(R.id.showId);
            movieName = itemView.findViewById(R.id.movieName);
            startDateTime = itemView.findViewById(R.id.startDateTime);
            endDateTime = itemView.findViewById(R.id.endDateTime);
        }
    }
}
