package com.example.majorprojectticketbookingsystem;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
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

public class EmployeeSideViewHallSchedule extends AppCompatActivity {

    RecyclerView recyclerView;
    Movie movie;
    Hall hall;

    private HallScheduleAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_employee_side_view_hall_schedule);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        recyclerView = findViewById(R.id.hallScheduleRecyclerView);

        Intent intent = getIntent();
        if (intent != null) {
            movie = (Movie) intent.getSerializableExtra("movie");
            hall = (Hall) intent.getSerializableExtra("hall");

            fetchUpcomingShows(hall.getHallId());
        }



    }

    private void fetchUpcomingShows(String hallId) {
        FirebaseFirestore db = FirebaseFirestore.getInstance();

        // ✅ Get current timestamp
        Timestamp currentTimestamp = Timestamp.now();

        // ✅ Query upcoming shows
        db.collection("shows")
                .whereEqualTo("hall_id", hallId)  // ✅ Filter by selected hall
                .whereGreaterThan("showEndTime", currentTimestamp)  // ✅ Only upcoming shows
                .orderBy("showStartTime", Query.Direction.ASCENDING) // ✅ Sort by start time
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    List<Show> showList = new ArrayList<>();

                    for (QueryDocumentSnapshot document : queryDocumentSnapshots) {

                        // ✅ Extract data manually
                        String showId = document.getString("showId");
                        String hall_id = document.getString("hall_id");
                        String theatreId = document.getString("theatreId");
                        String movieId = document.getString("movieId");
                        String movieName = document.getString("movieName");
                        Timestamp showStartTime = document.getTimestamp("showStartTime");
                        Timestamp showEndTime = document.getTimestamp("showEndTime");

                        String formattedStartTime = formatTimestamp(showStartTime);
                        String formattedEndTime = formatTimestamp(showEndTime);

                        Log.d("movie", "fetchUpcomingShows: " + movieName);

                        // ✅ Add to show list
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
        adapter = new HallScheduleAdapter(showList);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(adapter);
    }

    private String formatTimestamp(Timestamp timestamp) {
        if (timestamp == null) return "N/A"; // ✅ Handle null case

        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault());
        return sdf.format(timestamp.toDate()); // ✅ Convert Firestore Timestamp to formatted String
    }



}