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

public class CustomerSideChooseTheatreActivity extends AppCompatActivity {

    Movie movie;
    private RecyclerView recyclerView;
    private TheatreAdapter adapter;
    private List<Theatre> theatreList;
    private FirebaseFirestore db;
    private ProgressBar progressBar;
    private String movieId;
    private String userCity;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_customer_side_choose_theatre);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        SharedPreferences sharedPreferences = getSharedPreferences("MyAppPrefs", MODE_PRIVATE);
        userCity = sharedPreferences.getString("userCity", "NA");

        movie = (Movie) getIntent().getSerializableExtra("movie");
        recyclerView = findViewById(R.id.recyclerView);
        progressBar = findViewById(R.id.progressBar);
        db = FirebaseFirestore.getInstance();

        theatreList = new ArrayList<>();

        if(movie != null)
        {
            movieId = movie.getId();
            adapter = new TheatreAdapter(theatreList, CustomerSideChooseTheatreActivity.this, movie);
            recyclerView.setLayoutManager(new LinearLayoutManager(this));
            recyclerView.setAdapter(adapter);

            fetchTheatresForMovie(movie.getId());
        }


    }


    private void fetchTheatresForMovie(String movieId) {
        progressBar.setVisibility(View.VISIBLE);

        db.collection("shows")
                .whereEqualTo("movieId", movieId)  // ✅ Filter by movieId
                .whereGreaterThan("showStartTime", Timestamp.now()) // ✅ Only fetch upcoming shows
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    Set<String> uniqueTheatreIds = new HashSet<>();

                    // Extract theatre IDs
                    for (QueryDocumentSnapshot document : queryDocumentSnapshots) {
                        uniqueTheatreIds.add(document.getString("theatreId"));
                    }

                    if (!uniqueTheatreIds.isEmpty()) {
                        fetchTheatreDetails(uniqueTheatreIds);
                    } else {
                        Toast.makeText(this, "No upcoming shows in Any Theatre for this movie!", Toast.LENGTH_SHORT).show();
                    }
                })
                .addOnFailureListener(e -> Log.e("Firestore", "Error fetching shows", e));
    }

    private void fetchTheatreDetails(Set<String> theatreIds) {
        theatreList.clear();
        progressBar.setVisibility(View.GONE);

        for (String theatreId : theatreIds) {
            db.collection("theatre")
                    .document(theatreId)
                    .get()
                    .addOnSuccessListener(documentSnapshot -> {
                        if (documentSnapshot.exists()) {
                            String theatreName = documentSnapshot.getString("theatreName");
                            String location = documentSnapshot.getString("location");
                            String totalHalls = documentSnapshot.getString("totalHalls");

                            if(!userCity.equals("NA") && location!=null && location.toLowerCase().contains(userCity.toLowerCase())) {
                                Theatre theatre = new Theatre(theatreId, theatreName, location, totalHalls);
                                theatreList.add(theatre);
                                adapter.notifyDataSetChanged();
                            }
                        }
                    })
                    .addOnFailureListener(e -> Log.e("Firestore", "Error fetching theatre details", e));
        }
    }



}