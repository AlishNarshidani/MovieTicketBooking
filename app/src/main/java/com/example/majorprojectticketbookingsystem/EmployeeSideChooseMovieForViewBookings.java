package com.example.majorprojectticketbookingsystem;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatButton;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.Timestamp;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class EmployeeSideChooseMovieForViewBookings extends AppCompatActivity {

    FirebaseFirestore db;
    Set<String> uniqueMovieIds;
    List<Movie> movieList;

    private RecyclerView recyclerView;
    private MovieAdapter movieAdapter;

    AppCompatButton searchButton;
    EditText search_edit_text;
    TextView no_results_text;
    private ProgressBar progressBar;

    List<Movie> filteredMovieList;

    String caller;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_employee_side_choose_movie_for_view_bookings);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        caller = getIntent().getStringExtra("caller");
        Log.d("final debug", "onCreate: "+caller);

        searchButton = findViewById(R.id.search_button);
        search_edit_text = findViewById(R.id.search_edit_text);
        no_results_text = findViewById(R.id.no_results_text);
        progressBar = findViewById(R.id.progressBar);

        recyclerView = findViewById(R.id.recyclerView);
        recyclerView.setLayoutManager(new GridLayoutManager(this, 2)); // 2 columns per row

        db = FirebaseFirestore.getInstance();
        uniqueMovieIds = new HashSet<>();
        movieList = new ArrayList<>();
        filteredMovieList = new ArrayList<>();

        SharedPreferences sharedPreferences = getSharedPreferences("MyAppPrefs", MODE_PRIVATE);
        String theatreId = sharedPreferences.getString("theatreId", "");

        if (!theatreId.isEmpty()) {
            if (caller.equalsIgnoreCase("upcoming")) {
                fetchUpcomingShowsAtMyTheatre(theatreId);
            } else if (caller.equalsIgnoreCase("past")){
                fetchPastShowsAtMyTheatre(theatreId);
            }
        }

        searchButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                String query = search_edit_text.getText().toString().trim();

                if(!query.isEmpty()) {
                    getListOfSearchedMovies();
                } else {
                    search_edit_text.setError("Please enter a movie name");

                    if (!theatreId.isEmpty()) {

                        if (caller.equalsIgnoreCase("upcoming")) {
                            fetchUpcomingShowsAtMyTheatre(theatreId);
                        } else if (caller.equalsIgnoreCase("past")){
                            fetchPastShowsAtMyTheatre(theatreId);
                        }
                    }
                }
            }
        });

    }



    public void getListOfSearchedMovies() {
        String searchCriteria = search_edit_text.getText().toString().trim();

        // Check if the input is empty
        if (searchCriteria.isEmpty()) {
            search_edit_text.setError("Please enter a movie name");
            return; // Stop execution
        }

        // Check minimum character length (optional)
        if (searchCriteria.length() < 3) {
            search_edit_text.setError("Enter at least 3 characters");
            return;
        }

        filterMovies(searchCriteria);


    }


    private void fetchUpcomingShowsAtMyTheatre(String theatreId) {
        caller = "EmployeeViewUpcomingBookingsMovieWise";

        FirebaseFirestore db = FirebaseFirestore.getInstance();
        Timestamp currentTimestamp = Timestamp.now();

        db.collection("shows")
                .whereEqualTo("theatreId", theatreId)  // ✅ Filter by theatre
                .whereGreaterThan("showEndTime", currentTimestamp)  // ✅ Only fetch upcoming shows
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    if(!queryDocumentSnapshots.isEmpty()) {
                        uniqueMovieIds.clear();
                        movieList.clear();
                        Set<String> uniqueMovieIds = new HashSet<>();

                        // ✅ Collect unique movie IDs
                        for (QueryDocumentSnapshot document : queryDocumentSnapshots) {
                            String movieId = document.getString("movieId");
                            if (movieId != null) {
                                uniqueMovieIds.add(movieId);
                            }
                        }

                        if (!uniqueMovieIds.isEmpty()) {
                            fetchMovieDetails(uniqueMovieIds);  // ✅ Fetch movie details
                        } else {
                            Toast.makeText(this, "No upcoming movies found for this theatre.", Toast.LENGTH_SHORT).show();
                        }
                    } else {
                        Log.d("Movies", "No upcoming shows available.");
                    }
                })
                .addOnFailureListener(e -> {
                    Log.e("Firestore", "Error fetching shows", e);
                    Toast.makeText(this, "Failed to fetch shows. Please try again.", Toast.LENGTH_SHORT).show();
                });
    }



    private void fetchPastShowsAtMyTheatre(String theatreId) {
        caller = "EmployeeViewPastBookingsMovieWise";

        FirebaseFirestore db = FirebaseFirestore.getInstance();
        Timestamp currentTimestamp = Timestamp.now();

        db.collection("shows")
                .whereEqualTo("theatreId", theatreId)  // ✅ Filter by theatre
                .whereLessThan("showStartTime", currentTimestamp)  // ✅ Only fetch past shows
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    if(!queryDocumentSnapshots.isEmpty()) {
                        uniqueMovieIds.clear();
                        movieList.clear();
                        Set<String> uniqueMovieIds = new HashSet<>();

                        // ✅ Collect unique movie IDs
                        for (QueryDocumentSnapshot document : queryDocumentSnapshots) {
                            String movieId = document.getString("movieId");
                            if (movieId != null) {
                                uniqueMovieIds.add(movieId);
                            }
                        }

                        if (!uniqueMovieIds.isEmpty()) {
                            fetchMovieDetails(uniqueMovieIds);  // ✅ Fetch movie details
                        } else {
                            Toast.makeText(this, "No past movies found for this theatre.", Toast.LENGTH_SHORT).show();
                        }
                    } else {
                        Log.d("Movies", "No past shows available.");
                    }
                })
                .addOnFailureListener(e -> {
                    Log.e("Firestore", "Error fetching shows", e);
                    Toast.makeText(this, "Failed to fetch shows. Please try again.", Toast.LENGTH_SHORT).show();
                });
    }




    private void fetchMovieDetails(Set<String> movieIds) {
        for (String movieId : movieIds) {
            Log.d("TAG", "fetchMovieDetails: "+movieId);
            db.collection("movies")
                    .document(movieId)
                    .get()
                    .addOnSuccessListener(movieSnapshot -> {
                        if (movieSnapshot.exists()) {
                            // ✅ Extract movie data
                            String id = movieSnapshot.getString("id");
                            String title = movieSnapshot.getString("title");
                            String originalTitle = movieSnapshot.getString("original_title");
                            String releaseDate = movieSnapshot.getString("release_date");
                            String overview = movieSnapshot.getString("overview");
                            double popularity = movieSnapshot.getDouble("popularity");
                            String originalLanguage = movieSnapshot.getString("original_language");
                            double voteAverage = movieSnapshot.getDouble("vote_average");
                            int voteCount = movieSnapshot.getLong("vote_count").intValue();
                            boolean adult = Boolean.TRUE.equals(movieSnapshot.getBoolean("adult"));
                            boolean video = Boolean.TRUE.equals(movieSnapshot.getBoolean("video"));
                            String posterPath = movieSnapshot.getString("poster_path");
                            String backdropPath = movieSnapshot.getString("backdrop_path");
                            String genreNames = movieSnapshot.getString("genre_names");

                            // ✅ Create Movie object
                            Movie movie = new Movie(id, title, originalTitle, releaseDate, overview, popularity, originalLanguage,
                                    voteAverage, voteCount, adult, video, posterPath, backdropPath, genreNames);

                            movieList.add(movie); // ✅ Add movie to list

                            if (movieList.size() == movieIds.size()) {
                                updateMovieUI(movieList); // ✅ Update UI once all movies are fetched
                            }

                        } else {
                            Log.d("Movies", "Movie not found: " + movieId);
                        }
                    })
                    .addOnFailureListener(e -> Log.e("Firestore", "Error fetching movie: " + movieId, e));
        }
    }


    // ✅ Function to update RecyclerView
    private void updateMovieUI(List<Movie> movies) {
        progressBar.setVisibility(View.GONE);
        // ✅ Update RecyclerView or any UI component
        movieAdapter = new MovieAdapter(EmployeeSideChooseMovieForViewBookings.this, movies, caller);
        recyclerView.setAdapter(movieAdapter);
    }


    private void filterMovies(String query) {
        filteredMovieList.clear(); // ✅ Clear previous filtered results

        if (!query.isEmpty()) {
            for (Movie movie : movieList) {
                if (movie.getTitle().toLowerCase().contains(query.toLowerCase())) {
                    filteredMovieList.add(movie); // ✅ Add matching movie
                }
            }
        }

        updateMovieUI(filteredMovieList); // ✅ Update RecyclerView
    }


}