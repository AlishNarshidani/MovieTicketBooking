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
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class CustomerSideViewBookings extends AppCompatActivity {

    private RecyclerView recyclerView;
    private BookingAdapter adapter;
    private List<Booking> bookingList;
    private FirebaseFirestore db;
    private ProgressBar progressBar;
    private FirebaseAuth auth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_customer_side_view_bookings);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        String caller = getIntent().getStringExtra("caller");

        recyclerView = findViewById(R.id.recyclerView);
        progressBar = findViewById(R.id.progressBar);
        db = FirebaseFirestore.getInstance();
        auth = FirebaseAuth.getInstance();
        bookingList = new ArrayList<>();
        
        if(caller.equalsIgnoreCase("upcoming"))
        {
            fetchUpcomingBookings();

        } else if (caller.equalsIgnoreCase("past")) {
            fetchPastBookings();
        }


    }


    private void fetchUpcomingBookings() {
        progressBar.setVisibility(View.VISIBLE);
        String userId = auth.getCurrentUser().getUid();
        Timestamp currentTimestamp = Timestamp.now();

        db.collection("bookings")
                .whereEqualTo("userId", userId)
                .whereGreaterThan("showEndTime", currentTimestamp)
                .orderBy("showStartTime", Query.Direction.ASCENDING)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    bookingList.clear();
                    for (QueryDocumentSnapshot document : queryDocumentSnapshots) {
                        String bookingId = document.getId();
                        String bookingStatus = document.getString("bookingStatus");
                        String movieName = document.getString("movieName");
                        String theatreName = document.getString("theatreName");
                        String theatreLocation = document.getString("theatreLocation");
                        String hallName = document.getString("hallName");
                        Timestamp showStartTime = document.getTimestamp("showStartTime");
                        Timestamp showEndTime = document.getTimestamp("showEndTime");
                        List<String> seatList = (List<String>) document.get("selectedSeats");
                        // ✅ Convert List<String> to Comma-Separated String
                        String selectedSeats = (seatList != null) ? String.join(", ", seatList) : "N/A";
                        String totalPrice = document.getString("totalPrice");

                        String formattedStartTime = formatTimestamp(showStartTime);
                        String formattedEndTime = formatTimestamp(showEndTime);

                        Booking booking = new Booking(bookingId, bookingStatus, movieName, theatreName, theatreLocation, hallName, formattedStartTime, formattedEndTime, selectedSeats, totalPrice);
                        bookingList.add(booking);
                    }

                    if (bookingList.isEmpty()) {
                        Toast.makeText(this, "No Upcoming bookings!", Toast.LENGTH_SHORT).show();
                    } else {
                        updateBookingsUI(bookingList);
                    }
                })
                .addOnFailureListener(e -> {
                    Log.e("Firestore", "Error fetching bookings", e);
                    progressBar.setVisibility(View.GONE);
                });
    }



    private void fetchPastBookings() {
        progressBar.setVisibility(View.VISIBLE);
        String userId = auth.getCurrentUser().getUid();
        Timestamp currentTimestamp = Timestamp.now();

        db.collection("bookings")
                .whereEqualTo("userId", userId)
                .whereLessThan("showEndTime", currentTimestamp) // ✅ Fetch past bookings
                .orderBy("showStartTime", Query.Direction.ASCENDING)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    bookingList.clear();
                    for (QueryDocumentSnapshot document : queryDocumentSnapshots) {
                        String bookingId = document.getId();
                        String bookingStatus = document.getString("bookingStatus");
                        String movieName = document.getString("movieName");
                        String theatreName = document.getString("theatreName");
                        String theatreLocation = document.getString("theatreLocation");
                        String hallName = document.getString("hallName");
                        Timestamp showStartTime = document.getTimestamp("showStartTime");
                        Timestamp showEndTime = document.getTimestamp("showEndTime");
                        List<String> seatList = (List<String>) document.get("selectedSeats");
                        // ✅ Convert List<String> to Comma-Separated String
                        String selectedSeats = (seatList != null) ? String.join(", ", seatList) : "N/A";
                        String totalPrice = document.getString("totalPrice");

                        String formattedStartTime = formatTimestamp(showStartTime);
                        String formattedEndTime = formatTimestamp(showEndTime);

                        Booking booking = new Booking(bookingId, bookingStatus, movieName, theatreName, theatreLocation, hallName, formattedStartTime, formattedEndTime, selectedSeats, totalPrice);
                        bookingList.add(booking);
                    }


                    if (bookingList.isEmpty()) {
                        Toast.makeText(this, "No past bookings!", Toast.LENGTH_SHORT).show();
                    } else {
                        updateBookingsUI(bookingList);
                    }
                })
                .addOnFailureListener(e -> {
                    Log.e("Firestore", "Error fetching past bookings", e);
                    progressBar.setVisibility(View.GONE);
                });
    }


    private void updateBookingsUI(List<Booking> bookingList) {
        progressBar.setVisibility(View.GONE);

        adapter = new BookingAdapter(bookingList, CustomerSideViewBookings.this);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(adapter);
    }


    private String formatTimestamp(Timestamp timestamp) {
        if (timestamp == null) return "N/A"; // ✅ Handle null case

        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault());
        return sdf.format(timestamp.toDate()); // ✅ Convert Firestore Timestamp to formatted String
    }


}