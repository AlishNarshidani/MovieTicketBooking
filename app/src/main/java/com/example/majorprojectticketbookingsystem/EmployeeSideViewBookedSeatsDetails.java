package com.example.majorprojectticketbookingsystem;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

public class EmployeeSideViewBookedSeatsDetails extends AppCompatActivity {

    private ProgressBar progressBar;
    private RecyclerView recyclerView;

    String showId;

    TextView noBookedSeatsTextView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_employee_side_view_booked_seats_details);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        noBookedSeatsTextView = findViewById(R.id.noBookedSeatsTextView);
        progressBar = findViewById(R.id.progressBar);
        recyclerView = findViewById(R.id.recyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        showId = getIntent().getStringExtra("showId");

        if(showId != null && !showId.isEmpty())
        {
            fetchBookedSeatsDetails(showId);
        } else {
            Toast.makeText(this, "Show ID not found", Toast.LENGTH_SHORT).show();
            finish();
        }


    }


    private void fetchBookedSeatsDetails(String showId) {
        FirebaseFirestore db = FirebaseFirestore.getInstance();

        db.collection("shows").document(showId)
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        List<BookedSeat> bookedSeatsList = new ArrayList<>();

                        Map<String, Object> seatsLayout = (Map<String, Object>) documentSnapshot.get("seatsLayout");

                        if(seatsLayout != null) {

                            List<String> sortedKeys = new ArrayList<>(seatsLayout.keySet());

                            // Custom Comparator to handle decimal-like row names (row1, row1.5, row2)
                            Collections.sort(sortedKeys, (key1, key2) -> {
                                double num1 = Double.parseDouble(key1.replace("row", "").replace("_", "."));
                                double num2 = Double.parseDouble(key2.replace("row", "").replace("_", "."));
                                return Double.compare(num1, num2);
                            });
                            Log.d("sortedKeys", "displaySeats: " + sortedKeys);


                            for (String rowKey : sortedKeys) {
                                List<Map<String, Object>> seats = (List<Map<String, Object>>) seatsLayout.get(rowKey);

                                for (Map<String, Object> seat : seats) {
                                    int seatId = ((Number) seat.get("id")).intValue(); // Ensures Integer
                                    double price = ((Number) seat.getOrDefault("price",100)).doubleValue(); // Ensures Double
                                    boolean isBooked = Boolean.TRUE.equals(seat.getOrDefault("isBooked", false)); // Ensures Boolean

                                    if(isBooked) {
                                        String userId = (String) seat.get("userId");
                                        String bookingType = (String) seat.get("bookingType");

                                        // ✅ Create a BookedSeat object
                                        BookedSeat bookedSeat = new BookedSeat(String.valueOf(seatId), userId, String.valueOf(price), bookingType);
                                        bookedSeatsList.add(bookedSeat);
                                    }
                                }
                            }

                            // ✅ Display seats in RecyclerView
                            displayBookedSeats(bookedSeatsList);
                        }

                    }
                })
                .addOnFailureListener(e -> Log.e("Firestore", "Error fetching booked seats", e));
    }




    private void displayBookedSeats(List<BookedSeat> bookedSeatsList) {
        if (bookedSeatsList.isEmpty()) {
            noBookedSeatsTextView.setVisibility(View.VISIBLE);
            return;
        }
        progressBar.setVisibility(View.GONE);
        BookedSeatsAdapter adapter = new BookedSeatsAdapter(EmployeeSideViewBookedSeatsDetails.this, bookedSeatsList);
        recyclerView.setAdapter(adapter);
    }




}