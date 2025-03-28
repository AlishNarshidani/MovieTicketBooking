package com.example.majorprojectticketbookingsystem;

import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.google.android.material.card.MaterialCardView;

import java.util.List;

public class MovieAdapter extends RecyclerView.Adapter<MovieAdapter.MovieViewHolder> {

    private Context context;
    private List<Movie> movieList;
    private String caller;

    public MovieAdapter(Context context, List<Movie> movieList, String caller) {
        this.context = context;
        this.movieList = movieList;
        this.caller = caller;
    }

    @NonNull
    @Override
    public MovieAdapter.MovieViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_movie, parent, false);
        return new MovieViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull MovieAdapter.MovieViewHolder holder, int position) {
        Movie movie = movieList.get(position);
        holder.title.setText(movie.getTitle());
        holder.releaseDate.setText(movie.getReleaseDate());
        holder.movieGenre.setText(movie.getGenreNames());
        holder.movieRating.setText(String.valueOf(movie.getVoteAverage()));

        Glide.with(context)
                .load(movie.getPosterPath())
                .error(R.drawable.cinemalogo)
                .into(holder.poster);


        holder.materialCardView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                Intent intent;

                if ("customer".equalsIgnoreCase(caller)) {
                    intent = new Intent(context, CustomerSideMovieDetailsActivity.class);
                } else if ("employee".equalsIgnoreCase(caller)) {
                    intent = new Intent(context, EmployeeSideMovieDetailsActivity.class);
                } else if ("EmployeeViewUpcomingBookingsMovieWise".equalsIgnoreCase(caller) || "EmployeeViewPastBookingsMovieWise".equalsIgnoreCase(caller)){
                    intent = new Intent(context, EmployeeSideChooseHallForViewBookings.class); // Fallback page\
                    intent.putExtra("caller", caller);
                } else {
                    intent = new Intent(context, CustomerSideMovieDetailsActivity.class); // Fallback page
                }

                intent.putExtra("movie", movie);
                context.startActivity(intent);
            }
        });
    }

    @Override
    public int getItemCount() {
        return movieList.size();
    }


    static class MovieViewHolder extends RecyclerView.ViewHolder
    {
        TextView title, releaseDate, movieGenre, movieRating;
        ImageView poster;
        MaterialCardView materialCardView;

        public MovieViewHolder(@NonNull View itemView) {
            super(itemView);
            title = itemView.findViewById(R.id.movieTitle);
            releaseDate = itemView.findViewById(R.id.movieReleaseDate);
            poster = itemView.findViewById(R.id.moviePoster);
            movieGenre = itemView.findViewById(R.id.movieGenre);
            movieRating = itemView.findViewById(R.id.movieRating);
            materialCardView = itemView.findViewById(R.id.movieCard);

        }
    }
}
