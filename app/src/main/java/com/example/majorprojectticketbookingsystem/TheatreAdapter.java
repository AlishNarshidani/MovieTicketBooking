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

public class TheatreAdapter extends RecyclerView.Adapter<TheatreAdapter.ThatreViewHolder> {

    private List<Theatre> theatreList;
    private Context context;
    private Movie movie;

    public TheatreAdapter(List<Theatre> theatreList, Context context, Movie movie) {
        this.theatreList = theatreList;
        this.context = context;
        this.movie = movie;
    }

    @NonNull
    @Override
    public TheatreAdapter.ThatreViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_theatre, parent, false);
        return new TheatreAdapter.ThatreViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull TheatreAdapter.ThatreViewHolder holder, int position) {
        Theatre theatre = theatreList.get(position);
        holder.theatreName.setText(theatre.getName());
        holder.theatreLocation.setText("Location: "+theatre.getLocation());

        holder.itemView.setOnClickListener(v -> {
            Intent intent = new Intent(context, CustomerSideChooseHallActivity.class);
            intent.putExtra("theatre", theatre);
            intent.putExtra("movie", movie);
            context.startActivity(intent);
        });
    }

    @Override
    public int getItemCount() {
        return theatreList.size();
    }

    public static class ThatreViewHolder extends RecyclerView.ViewHolder {
        TextView theatreName, theatreLocation;

        public ThatreViewHolder(@NonNull View itemView) {
            super(itemView);
            theatreName = itemView.findViewById(R.id.theatreName);
            theatreLocation = itemView.findViewById(R.id.theatreLocation);
        }
    }
}
