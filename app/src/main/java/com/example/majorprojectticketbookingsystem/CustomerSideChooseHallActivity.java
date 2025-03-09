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
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class CustomerSideChooseHallActivity extends AppCompatActivity {

    Movie movie;
    Theatre theatre;

    private RecyclerView recyclerView;
    private HallAdapter hallAdapter;
    private List<Hall> hallList;
    private FirebaseFirestore db;
    private String theatreId, movieId;
    private ProgressBar progressBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_customer_side_choose_hall);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        movie = (Movie) getIntent().getSerializableExtra("movie");
        theatre = (Theatre) getIntent().getSerializableExtra("theatre");
        hallList = new ArrayList<>();

        recyclerView = findViewById(R.id.recyclerView);
        progressBar = findViewById(R.id.progressBar);


        db = FirebaseFirestore.getInstance();


        if(theatre != null && movie != null)
        {
            theatreId = theatre.getId();
            movieId = movie.getId();

            recyclerView.setLayoutManager(new LinearLayoutManager(this));
            hallAdapter = new HallAdapter(hallList, CustomerSideChooseHallActivity.this, movie, "customer" , theatre);
            recyclerView.setAdapter(hallAdapter);

            fetchHallsForMovieInTheatre(theatreId, movieId);


        }


    }


    private void fetchHallsForMovieInTheatre(String theatreId, String movieId) {
        progressBar.setVisibility(View.VISIBLE);

        db.collection("shows")
                .whereEqualTo("theatreId", theatreId)
                .whereEqualTo("movieId", movieId)
                .whereGreaterThan("showStartTime", Timestamp.now())  // ✅ Fetch only upcoming shows
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    Set<String> uniqueHallIds = new HashSet<>();

                    // Extract unique hall IDs
                    for (QueryDocumentSnapshot document : queryDocumentSnapshots) {
                        uniqueHallIds.add(document.getString("hall_id"));
                    }

                    if (!uniqueHallIds.isEmpty()) {
                        fetchHallDetails(uniqueHallIds);
                    } else {
                        Toast.makeText(this, "No available halls for this movie!", Toast.LENGTH_SHORT).show();
                    }
                })
                .addOnFailureListener(e -> Log.e("Firestore", "Error fetching shows", e));
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