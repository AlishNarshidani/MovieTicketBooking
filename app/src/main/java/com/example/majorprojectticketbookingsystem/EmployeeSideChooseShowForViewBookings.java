package com.example.majorprojectticketbookingsystem;

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

public class EmployeeSideChooseShowForViewBookings extends AppCompatActivity {

    private RecyclerView recyclerView;
    private ProgressBar progressBar;
    private ShowAdapter showAdapter;
    private List<Show> showList;

    Movie movie;
    Theatre theatre;
    Hall hall;

    String theatreId, movieId, hallId;

    String caller;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_employee_side_choose_show_for_view_bookings);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        caller = getIntent().getStringExtra("caller");

        showList = new ArrayList<>();

        recyclerView = findViewById(R.id.recyclerView);
        progressBar = findViewById(R.id.progressBar);


        movie = (Movie) getIntent().getSerializableExtra("movie");
        theatre = (Theatre) getIntent().getSerializableExtra("theatre");
        hall = (Hall) getIntent().getSerializableExtra("hall");

        if(movie != null && hall != null)
        {
            movieId = movie.getId();
            hallId = hall.getHallId();
            theatreId = theatre.getId();

            
            if(caller.equalsIgnoreCase("EmployeeViewUpcomingBookingsMovieWise")) {
                fetchUpcomingShowsFromFirestore();
                
            } else if (caller.equalsIgnoreCase("EmployeeViewPastBookingsMovieWise")) {
                fetchPastShowsFromFirestore();
            }


        }

    }


    private void fetchUpcomingShowsFromFirestore() {
        progressBar.setVisibility(View.VISIBLE);

        FirebaseFirestore db = FirebaseFirestore.getInstance();
        Timestamp currentTimestamp = Timestamp.now();

        db.collection("shows")
                .whereEqualTo("hall_id", hallId)  // ✅ Filter by specific hall
                .whereEqualTo("movieId", movieId) // ✅ Filter by specific movie
                .whereGreaterThan("showEndTime", currentTimestamp) // ✅ Only upcoming shows
                .orderBy("showStartTime", Query.Direction.ASCENDING) // ✅ Sort by start time
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    showList.clear();

                    for (QueryDocumentSnapshot document : queryDocumentSnapshots) {
                        String showId = document.getString("showId");
                        String hall_id = document.getString("hall_id");
                        String theatreId = document.getString("theatreId");
                        String movieId = document.getString("movieId");
                        String movieName = document.getString("movieName");
                        Timestamp showStartTime = document.getTimestamp("showStartTime");
                        Timestamp showEndTime = document.getTimestamp("showEndTime");

                        String formattedStartTime = formatTimestamp(showStartTime);
                        String formattedEndTime = formatTimestamp(showEndTime);

                        Show show = new Show(showId, hall_id, theatreId, movieId, movieName, formattedStartTime, formattedEndTime);

                        showList.add(show);
                    }

                    // ✅ If no upcoming shows
                    if (showList.isEmpty()) {
                        Toast.makeText(this, "No upcoming shows!", Toast.LENGTH_SHORT).show();
                    } else {
                        // ✅ Update RecyclerView with schedule
                        updateScheduleUI(showList);
                    }

                })
                .addOnFailureListener(e -> {
                    Log.e("Firestore", "Error fetching shows", e);
                    Toast.makeText(this, "Error loading schedule!", Toast.LENGTH_SHORT).show();
                });
    }


    private void fetchPastShowsFromFirestore() {
        progressBar.setVisibility(View.VISIBLE);

        FirebaseFirestore db = FirebaseFirestore.getInstance();
        Timestamp currentTimestamp = Timestamp.now();

        db.collection("shows")
                .whereEqualTo("hall_id", hallId)  // ✅ Filter by specific hall
                .whereEqualTo("movieId", movieId) // ✅ Filter by specific movie
                .whereLessThan("showEndTime", currentTimestamp) // ✅ Only upcoming shows
                .orderBy("showStartTime", Query.Direction.ASCENDING) // ✅ Sort by start time
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    showList.clear();

                    for (QueryDocumentSnapshot document : queryDocumentSnapshots) {
                        String showId = document.getString("showId");
                        String hall_id = document.getString("hall_id");
                        String theatreId = document.getString("theatreId");
                        String movieId = document.getString("movieId");
                        String movieName = document.getString("movieName");
                        Timestamp showStartTime = document.getTimestamp("showStartTime");
                        Timestamp showEndTime = document.getTimestamp("showEndTime");

                        String formattedStartTime = formatTimestamp(showStartTime);
                        String formattedEndTime = formatTimestamp(showEndTime);

                        Show show = new Show(showId, hall_id, theatreId, movieId, movieName, formattedStartTime, formattedEndTime);

                        showList.add(show);
                    }

                    // ✅ If no upcoming shows
                    if (showList.isEmpty()) {
                        Toast.makeText(this, "No upcoming shows!", Toast.LENGTH_SHORT).show();
                    } else {
                        // ✅ Update RecyclerView with schedule
                        updateScheduleUI(showList);
                    }

                })
                .addOnFailureListener(e -> {
                    Log.e("Firestore", "Error fetching shows", e);
                    Toast.makeText(this, "Error loading schedule!", Toast.LENGTH_SHORT).show();
                });
    }


    private void updateScheduleUI(List<Show> showList) {
        progressBar.setVisibility(View.GONE);

        showAdapter = new ShowAdapter(showList, EmployeeSideChooseShowForViewBookings.this, movie, hall, theatre, caller);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(showAdapter);
    }


    private String formatTimestamp(Timestamp timestamp) {
        if (timestamp == null) return "N/A"; // ✅ Handle null case

        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault());
        return sdf.format(timestamp.toDate()); // ✅ Convert Firestore Timestamp to formatted String
    }


}