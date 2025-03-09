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

import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.List;

public class EmployeeSideHallSelectionActivity extends AppCompatActivity {

    private RecyclerView recyclerView;
    private HallAdapter hallAdapter;
    private List<Hall> hallList;
    private FirebaseFirestore db;
    private ProgressBar progressBar;
    private String theatreId;
    Movie movie;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_employee_side_hall_selection);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        // Get theatreId from intent
        theatreId = getIntent().getStringExtra("theatreId");
        if (theatreId == null || theatreId.isEmpty()) {
            Toast.makeText(this, "Invalid theatre ID", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        movie = (Movie) getIntent().getSerializableExtra("movie");
        recyclerView = findViewById(R.id.recyclerView);
        progressBar = findViewById(R.id.progressBar);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        hallList = new ArrayList<>();
        db = FirebaseFirestore.getInstance();

        fetchHalls();

    }


    private void fetchHalls() {
        progressBar.setVisibility(View.VISIBLE);

        db.collection("theatre").document(theatreId).collection("halls")
                .get()
                .addOnCompleteListener(task -> {


                    if (task.isSuccessful()) {
                        hallList.clear();
                        QuerySnapshot snapshot = task.getResult();
                        if (snapshot != null) {
                            for (DocumentSnapshot doc : snapshot) {
                                String hallId = doc.getId();
                                String theatreId = doc.getString("theatreId");
                                String hallName = doc.getString("hallName");
                                String screenType = doc.getString("screenType");
                                String soundSystem = doc.getString("soundSystem");
                                long totalSeats = doc.getLong("totalSeats");

                                hallList.add(new Hall(hallId, theatreId, hallName, totalSeats, screenType, soundSystem));
                            }
                        }
                        progressBar.setVisibility(View.GONE);

                        hallAdapter = new HallAdapter(hallList, EmployeeSideHallSelectionActivity.this, movie, "employee");
                        recyclerView.setAdapter(hallAdapter);

                    } else {
                        Log.e("Firestore", "Error fetching halls", task.getException());
                        Toast.makeText(EmployeeSideHallSelectionActivity.this, "Error loading halls", Toast.LENGTH_SHORT).show();
                    }
                });
    }


}