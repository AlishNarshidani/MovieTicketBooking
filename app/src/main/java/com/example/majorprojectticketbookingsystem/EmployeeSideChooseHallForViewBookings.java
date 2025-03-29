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
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class EmployeeSideChooseHallForViewBookings extends AppCompatActivity {

    Movie movie;
    Theatre theatre;
    String theatreId,movieId;
    FirebaseFirestore db;

    private RecyclerView recyclerView;
    private HallAdapter hallAdapter;
    private List<Hall> hallList;
    private ProgressBar progressBar;

    String caller;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_employee_side_choose_hall_for_view_bookings);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        db = FirebaseFirestore.getInstance();
        hallList = new ArrayList<>();

        caller = getIntent().getStringExtra("caller");
        movie = (Movie) getIntent().getSerializableExtra("movie");

        movieId = movie.getId();
        SharedPreferences sharedPreferences = getSharedPreferences("MyAppPrefs", MODE_PRIVATE);
        theatreId = sharedPreferences.getString("theatreId", "");

        recyclerView = findViewById(R.id.recyclerView);
        progressBar = findViewById(R.id.progressBar);

        if (!theatreId.isEmpty() && movieId != null && !movieId.isEmpty()) {

            createTheatreObject(theatreId);

        }
    }

    private void createTheatreObject(String theatreId) {
        db.collection("theatre")
                .document(theatreId)
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        String theatreName = documentSnapshot.getString("theatreName");
                        String location = documentSnapshot.getString("location");
                        String totalHalls = documentSnapshot.getString("totalHalls");

                        theatre = new Theatre(theatreId, theatreName, location, totalHalls);

                        recyclerView.setLayoutManager(new LinearLayoutManager(this));
                        hallAdapter = new HallAdapter(hallList, EmployeeSideChooseHallForViewBookings.this, movie, caller , theatre);
                        recyclerView.setAdapter(hallAdapter);

                        if(caller.equalsIgnoreCase("EmployeeViewUpcomingBookingsMovieWise")) {
                            fetchHallsRunningThisMovieInUpcoming(theatreId,movieId);

                        } else if (caller.equalsIgnoreCase("EmployeeViewPastBookingsMovieWise")) {

                            fetchHallsRunningThisMovieInPast(theatreId,movieId);
                        }
                    }
                })
                .addOnFailureListener(e -> Log.e("Firestore", "Error fetching theatre details", e));
    }

    private void fetchHallsRunningThisMovieInUpcoming(String theatreId, String movieId)
    {
        progressBar.setVisibility(View.VISIBLE);
        Timestamp currentTimestamp = Timestamp.now();

        db.collection("shows")
                .whereEqualTo("theatreId", theatreId)  // ✅ Filter by theatre
                .whereEqualTo("movieId", movieId)      // ✅ Filter by movie
                .whereGreaterThan("showEndTime", currentTimestamp) // ✅ Only fetch upcoming shows
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    Set<String> uniqueHallIds = new HashSet<>();

                    // ✅ Collect unique hall IDs
                    for (QueryDocumentSnapshot document : queryDocumentSnapshots) {
                        String hallId = document.getString("hall_id");
                        if (hallId != null) {
                            uniqueHallIds.add(hallId);
                        }
                    }

                    if (!uniqueHallIds.isEmpty()) {
                        fetchHallDetails(uniqueHallIds);  // ✅ Fetch hall details
                    } else {
                        Toast.makeText(this, "No upcoming shows found for this movie in your theatre.", Toast.LENGTH_SHORT).show();
                    }
                })
                .addOnFailureListener(e -> {
                    Log.e("Firestore", "Error fetching shows", e);
                    Toast.makeText(this, "Failed to fetch shows. Please try again.", Toast.LENGTH_SHORT).show();
                });
    }



    private void fetchHallsRunningThisMovieInPast(String theatreId, String movieId)
    {
        progressBar.setVisibility(View.VISIBLE);
        Timestamp currentTimestamp = Timestamp.now();

        db.collection("shows")
                .whereEqualTo("theatreId", theatreId)  // ✅ Filter by theatre
                .whereEqualTo("movieId", movieId)      // ✅ Filter by movie
                .whereLessThan("showEndTime", currentTimestamp) // ✅ Only fetch past shows
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    Set<String> uniqueHallIds = new HashSet<>();

                    // ✅ Collect unique hall IDs
                    for (QueryDocumentSnapshot document : queryDocumentSnapshots) {
                        String hallId = document.getString("hall_id");
                        if (hallId != null) {
                            uniqueHallIds.add(hallId);
                        }
                    }

                    if (!uniqueHallIds.isEmpty()) {
                        fetchHallDetails(uniqueHallIds);  // ✅ Fetch hall details
                    } else {
                        Toast.makeText(this, "No past shows found for this movie in your theatre.", Toast.LENGTH_SHORT).show();
                    }
                })
                .addOnFailureListener(e -> {
                    Log.e("Firestore", "Error fetching shows", e);
                    Toast.makeText(this, "Failed to fetch shows. Please try again.", Toast.LENGTH_SHORT).show();
                });
    }



    private void fetchHallDetails(Set<String> hallIds) {
        hallList.clear();
        progressBar.setVisibility(View.GONE);

        for (String hallId : hallIds) {
            db.collection("theatre").document(theatreId)
                    .collection("halls").document(hallId)
                    .get()
                    .addOnSuccessListener(documentSnapshot -> {
                        if (documentSnapshot.exists()) {
                            String hallName = documentSnapshot.getString("hallName");
                            String screenType = documentSnapshot.getString("screenType");
                            String soundSystem = documentSnapshot.getString("soundSystem");
                            long totalSeats = documentSnapshot.getLong("totalSeats");

                            Hall hall = new Hall(hallId, theatreId, hallName, totalSeats, screenType, soundSystem);
                            hallList.add(hall);
                            hallAdapter.notifyDataSetChanged();
                        }
                    })
                    .addOnFailureListener(e -> Log.e("Firestore", "Error fetching hall details", e));
        }
    }

}