package com.example.majorprojectticketbookingsystem;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.Timestamp;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class EmployeeSideChooseShowForViewBookingsDayWise extends AppCompatActivity {

    private RecyclerView recyclerView;
    private ProgressBar progressBar;
    private ShowDayWiseAdapter showDayWiseAdapter;

    Theatre theatre;

    String theatreId;

    String caller;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_employee_side_choose_show_for_view_bookings_day_wise);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        SharedPreferences sharedPreferences = getSharedPreferences("MyAppPrefs", MODE_PRIVATE);
        theatreId = sharedPreferences.getString("theatreId", "");

        caller = getIntent().getStringExtra("caller");
        Log.d("check flag", "onCreate: "+caller);

        recyclerView = findViewById(R.id.recyclerView);
        progressBar = findViewById(R.id.progressBar);

        if(!theatreId.isEmpty()) {
            createTheatreObject(theatreId);
        }

    }


    private void createTheatreObject(String theatreId) {

        FirebaseFirestore db = FirebaseFirestore.getInstance();
        db.collection("theatre")
                .document(theatreId)
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        String theatreName = documentSnapshot.getString("theatreName");
                        String location = documentSnapshot.getString("location");
                        String totalHalls = documentSnapshot.getString("totalHalls");

                        theatre = new Theatre(theatreId, theatreName, location, totalHalls);

                        if(caller.equalsIgnoreCase("upcoming") && !theatreId.isEmpty()) {
                            fetchUpcomingShowsAtTheatre(theatreId);

                        } else if (caller.equalsIgnoreCase("past") && !theatreId.isEmpty()) {
                            fetchPastShowsAtTheatre(theatreId);
                        }

                    }
                })
                .addOnFailureListener(e -> Log.e("Firestore", "Error fetching theatre details", e));
    }


    private void fetchUpcomingShowsAtTheatre(String theatreId) {
        caller = "EmployeeViewUpcomingBookingsDayWise";

        FirebaseFirestore db = FirebaseFirestore.getInstance();
        Timestamp currentTimestamp = Timestamp.now();

        db.collection("shows")
                .whereEqualTo("theatreId", theatreId)  // ✅ Filter by theatre
                .whereGreaterThan("showEndTime", currentTimestamp) // ✅ Only upcoming shows
                .orderBy("showStartTime", Query.Direction.ASCENDING) // ✅ Sort by showStartTime
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    List<ShowDayWise> showList = new ArrayList<>();

                    for (QueryDocumentSnapshot document : queryDocumentSnapshots) {
                        String showId = document.getString("showId");
                        String hall_id = document.getString("hall_id");
                        String movieId = document.getString("movieId");
                        String movieName = document.getString("movieName");
                        Timestamp showStartTime = document.getTimestamp("showStartTime");
                        Timestamp showEndTime = document.getTimestamp("showEndTime");

                        String formattedStartTime = formatTimestamp(showStartTime);
                        String formattedEndTime = formatTimestamp(showEndTime);

                        // ✅ Fetch Hall and Movie details in parallel
                        fetchHallAndMovieDetails(hall_id, movieId, (hall, movie) -> {
                            // ✅ Create a Show object with Hall and Movie as parameters
                            ShowDayWise showDayWise = new ShowDayWise(showId, hall_id, theatreId, movieId, movieName, formattedStartTime, formattedEndTime, hall, movie, theatre);
                            showList.add(showDayWise);

                            // ✅ Update RecyclerView with schedule
                            updateScheduleUI(showList);
                        });

                    }
                })
                .addOnFailureListener(e -> {
                    Log.e("Firestore", "Error fetching upcoming shows", e);
                });
    }



    private void fetchPastShowsAtTheatre(String theatreId) {
        caller = "EmployeeViewPastBookingsDayWise";

        FirebaseFirestore db = FirebaseFirestore.getInstance();
        Timestamp currentTimestamp = Timestamp.now();

        db.collection("shows")
                .whereEqualTo("theatreId", theatreId)  // ✅ Filter by theatre
                .whereLessThan("showEndTime", currentTimestamp) // ✅ Only upcoming shows
                .orderBy("showStartTime", Query.Direction.ASCENDING) // ✅ Sort by showStartTime
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    List<ShowDayWise> showList = new ArrayList<>();

                    for (QueryDocumentSnapshot document : queryDocumentSnapshots) {
                        String showId = document.getString("showId");
                        String hall_id = document.getString("hall_id");
                        String movieId = document.getString("movieId");
                        String movieName = document.getString("movieName");
                        Timestamp showStartTime = document.getTimestamp("showStartTime");
                        Timestamp showEndTime = document.getTimestamp("showEndTime");

                        String formattedStartTime = formatTimestamp(showStartTime);
                        String formattedEndTime = formatTimestamp(showEndTime);

                        // ✅ Fetch Hall and Movie details in parallel
                        fetchHallAndMovieDetails(hall_id, movieId, (hall, movie) -> {
                            // ✅ Create a Show object with Hall and Movie as parameters
                            ShowDayWise showDayWise = new ShowDayWise(showId, hall_id, theatreId, movieId, movieName, formattedStartTime, formattedEndTime, hall, movie, theatre);
                            showList.add(showDayWise);

                            // ✅ Update RecyclerView with schedule
                            updateScheduleUI(showList);
                        });

                    }
                })
                .addOnFailureListener(e -> {
                    Log.e("Firestore", "Error fetching upcoming shows", e);
                });
    }




    private void fetchHallAndMovieDetails(String hallId, String movieId, OnHallMovieFetchedListener callback) {
        FirebaseFirestore db = FirebaseFirestore.getInstance();

        // ✅ Fetch Hall details
        db.collection("theatre").document(theatreId)
                .collection("halls").document(hallId)
                .get()
                .addOnSuccessListener(hallSnapshot -> {
                    if (hallSnapshot.exists()) {
                        String hallName = hallSnapshot.getString("hallName");
                        String screenType = hallSnapshot.getString("screenType");
                        String soundSystem = hallSnapshot.getString("soundSystem");
                        long totalSeats = hallSnapshot.getLong("totalSeats");

                        Hall hall = new Hall(hallId, theatreId, hallName, totalSeats, screenType, soundSystem);

                        // ✅ Fetch Movie details after Hall is fetched
                        db.collection("movies").document(movieId)
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

                                        // ✅ Pass both Hall and Movie objects to callback
                                        callback.onFetched(hall, movie);
                                    }
                                })
                                .addOnFailureListener(e -> Log.e("Firestore", "Error fetching movie details", e));
                    }
                })
                .addOnFailureListener(e -> Log.e("Firestore", "Error fetching hall details", e));
    }



    private void updateScheduleUI(List<ShowDayWise> showList) {
        progressBar.setVisibility(View.GONE);

        showDayWiseAdapter = new ShowDayWiseAdapter(showList, EmployeeSideChooseShowForViewBookingsDayWise.this, caller);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(showDayWiseAdapter);
    }



    private String formatTimestamp(Timestamp timestamp) {
        if (timestamp == null) return "N/A"; // ✅ Handle null case

        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault());
        return sdf.format(timestamp.toDate()); // ✅ Convert Firestore Timestamp to formatted String
    }


    interface OnHallMovieFetchedListener {
        void onFetched(Hall hall, Movie movie);
    }

}