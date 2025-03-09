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

public class ShowAdapter extends RecyclerView.Adapter<ShowAdapter.ShowViewHolder>{
    private List<Show> showList;
    private Context context;
    private Movie movie;
    private Hall hall;
    private Theatre theatre;

    public ShowAdapter(List<Show> showList, Context context, Movie movie, Hall hall, Theatre theatre) {
        this.showList = showList;
        this.context = context;
        this.movie = movie;
        this.hall = hall;
        this.theatre = theatre;
    }

    @NonNull
    @Override
    public ShowAdapter.ShowViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_show, parent, false);
        return new ShowAdapter.ShowViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ShowAdapter.ShowViewHolder holder, int position) {
        Show show = showList.get(position);
        holder.showStartTime.setText("Show Start Time: " + show.getShowStartTime());
        holder.showEndTime.setText("Show End Time: " + show.getShowEndTime());

        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(context, CustomerSideSeatsSelectionActivity.class);
                intent.putExtra("theatre",theatre);
                intent.putExtra("show", show);
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

    public static class ShowViewHolder extends RecyclerView.ViewHolder {
        TextView showStartTime, showEndTime;

        public ShowViewHolder(@NonNull View itemView) {
            super(itemView);
            showStartTime = itemView.findViewById(R.id.showStartTime);
            showEndTime = itemView.findViewById(R.id.showEndTime);
        }
    }

}
