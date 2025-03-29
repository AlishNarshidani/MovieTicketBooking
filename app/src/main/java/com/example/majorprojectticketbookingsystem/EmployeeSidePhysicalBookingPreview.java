package com.example.majorprojectticketbookingsystem;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.firebase.Timestamp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class EmployeeSidePhysicalBookingPreview extends AppCompatActivity {

    String caller;
    private String totalPrice;
    private TextView txtMovieName, txtTheatreName, txtHallName, txtShowTime, txtTotalPrice, txtSelectedSeats, txtTheatreAddress;

    private ProgressBar progressBar;

    Movie movie;
    Theatre theatre;
    Hall hall;
    Show show;

    ShowDayWise showDayWise;

    FirebaseAuth auth;
    FirebaseFirestore db;

    Button btnConfirmBooking;

    ArrayList<String> selectedSeatIds;
    ArrayList<Integer> selectedSeatPrices;

    String showId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_employee_side_physical_booking_preview);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });


        // Initialize UI Components
        txtMovieName = findViewById(R.id.txtMovieName);
        txtTheatreName = findViewById(R.id.txtTheatreName);
        txtTheatreAddress = findViewById(R.id.txtTheatreAddress);
        txtHallName = findViewById(R.id.txtHallName);
        txtShowTime = findViewById(R.id.txtShowTime);
        txtTotalPrice = findViewById(R.id.txtTotalPrice);
        txtSelectedSeats = findViewById(R.id.txtSelectedSeats);
        btnConfirmBooking = findViewById(R.id.btnConfirmBooking);
        progressBar = findViewById(R.id.progressBar);

        db= FirebaseFirestore.getInstance();
        auth= FirebaseAuth.getInstance();

        caller = getIntent().getStringExtra("caller");
        movie = (Movie) getIntent().getSerializableExtra("movie");
        theatre = (Theatre) getIntent().getSerializableExtra("theatre");
        hall = (Hall) getIntent().getSerializableExtra("hall");

        if(caller.equalsIgnoreCase("EmployeeViewUpcomingBookingsDayWise")) {
            showDayWise = (ShowDayWise) getIntent().getSerializableExtra("showDayWise");
            showId = showDayWise.getShowId();
        } else if (caller.equalsIgnoreCase("EmployeeViewUpcomingBookingsMovieWise")) {
            show = (Show) getIntent().getSerializableExtra("show");
            showId = show.getShowId();
        }

        totalPrice = getIntent().getStringExtra("totalPrice");
        selectedSeatIds = getIntent().getStringArrayListExtra("selectedSeatIds");
        selectedSeatPrices = getIntent().getIntegerArrayListExtra("selectedSeatPrices");

        displayBookingDetails(selectedSeatIds, selectedSeatPrices);

        btnConfirmBooking.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(movie != null && theatre != null && (show != null || showDayWise != null) && hall != null)
                {
                    btnConfirmBooking.setEnabled(false);
                    progressBar.setVisibility(View.VISIBLE);
                    updateSeatBooking();
                }
            }
        });


    }



    private void displayBookingDetails(ArrayList<String> selectedSeatIds, ArrayList<Integer> selectedSeatPrices) {
        // Set movie, theatre, and hall names
        txtMovieName.setText(movie.getTitle());
        txtTheatreName.setText("Theatre: " + theatre.getName());
        txtTheatreAddress.setText("Address: " + theatre.getLocation());
        txtHallName.setText("Hall: " + hall.getHallName());

        if(caller.equalsIgnoreCase("EmployeeViewUpcomingBookingsDayWise")) {
            txtShowTime.setText("Show Time: " + showDayWise.getShowStartTime() + " - " + showDayWise.getShowEndTime());
        } else if (caller.equalsIgnoreCase("EmployeeViewUpcomingBookingsMovieWise")) {
            txtShowTime.setText("Show Time: " + show.getShowStartTime() + " - " + show.getShowEndTime());
        }

        // Set total price
        txtTotalPrice.setText("Total Price: ₹" + totalPrice);

        // Display selected seats with their prices
        StringBuilder seatInfo = new StringBuilder();
        for (int i = 0; i < selectedSeatIds.size(); i++) {
            seatInfo.append("Seat ").append(selectedSeatIds.get(i)).append(" - ₹").append(selectedSeatPrices.get(i)).append("\n");
        }

        txtSelectedSeats.setText(seatInfo.toString().trim());
    }


    private void updateSeatBooking() {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        DocumentReference showRef = db.collection("shows").document(showId);

        db.runTransaction(transaction -> {
            // Fetch current seat layout
            DocumentSnapshot showSnapshot = transaction.get(showRef);
            Map<String, Object> seatsLayout = (Map<String, Object>) showSnapshot.get("seatsLayout");

            if (seatsLayout == null) {
                throw new FirebaseFirestoreException("Seat layout not found!", FirebaseFirestoreException.Code.ABORTED);
            }

            // Check if any selected seat is already booked
            for (String singleSeatId : selectedSeatIds) {
                int seatId = Integer.parseInt(singleSeatId); // ✅ Get seat ID from button text

                boolean seatAlreadyBooked = false;
                String bookedRow = null;

                for (String rowKey : seatsLayout.keySet()) {
                    List<Map<String, Object>> rowSeats = (List<Map<String, Object>>) seatsLayout.get(rowKey);

                    for (Map<String, Object> seat : rowSeats) {
                        if (((Number) seat.get("id")).intValue() == seatId) {
                            if (Boolean.TRUE.equals(seat.get("isBooked"))) {
                                seatAlreadyBooked = true;
                                bookedRow = rowKey;
                                break;
                            }
                        }
                    }
                    if (seatAlreadyBooked) break;
                }

                // ❌ If any seat is already booked → Abort & Refund
                if (seatAlreadyBooked) {
                    throw new FirebaseFirestoreException("Seat " + seatId + " is already booked!", FirebaseFirestoreException.Code.ABORTED);
                }
            }

            // ✅ If all seats are available, update them
            for (String singleSeatId : selectedSeatIds) {
                int seatId = Integer.parseInt(singleSeatId);

                for (String rowKey : seatsLayout.keySet()) {
                    List<Map<String, Object>> rowSeats = (List<Map<String, Object>>) seatsLayout.get(rowKey);

                    for (Map<String, Object> seat : rowSeats) {
                        if (((Number) seat.get("id")).intValue() == seatId) {
                            seat.put("isBooked", true);
                            seat.put("userId", FirebaseAuth.getInstance().getCurrentUser().getUid()); // ✅ Assign user ID
                            seat.put("bookingType","Physical");
                            break;
                        }
                    }
                }
            }

            transaction.update(showRef, "lastUpdated", Timestamp.now()); // 🔥 This forces Firestore to detect conflicts
            // ✅ Commit updated seat layout
            transaction.update(showRef, "seatsLayout", seatsLayout);
            return null;
        }).addOnSuccessListener(aVoid -> {
            Log.d("Firestore", "Seats booked successfully!");
            showToastForBookingConfirmed();
        }).addOnFailureListener(e -> {
            Log.e("Firestore", "Booking failed", e);
            showToastForBookingFailure();
        });
    }



    public void showToastForBookingConfirmed()
    {
        progressBar.setVisibility(View.GONE);
        btnConfirmBooking.setEnabled(true);

        LayoutInflater inflater = getLayoutInflater();
        View layout = inflater.inflate(R.layout.custom_toast_booked, findViewById(R.id.custom_toast_container));

        TextView amountText = layout.findViewById(R.id.toast_amount);
        amountText.setText("₹" + totalPrice);

        TextView transaction_type = layout.findViewById(R.id.toast_text);
        transaction_type.setText("Tickets Booked");

        TextView subText = layout.findViewById(R.id.toast_subtext);
        subText.setText("You can Now Provide the Offline Ticket");

        Toast toast = new Toast(getApplicationContext());
        toast.setDuration(Toast.LENGTH_LONG);
        toast.setView(layout);
        toast.setGravity(Gravity.CENTER,0,0);
        toast.show();

        Intent intent;
        if (caller.equalsIgnoreCase("EmployeeViewUpcomingBookingsMovieWise")) {
            intent = new Intent(EmployeeSidePhysicalBookingPreview.this, EmployeeSideSeatsSelectionForViewBookings.class);
            intent.putExtra("show",show);
        } else if (caller.equalsIgnoreCase("EmployeeViewUpcomingBookingsDayWise")){
            intent = new Intent(EmployeeSidePhysicalBookingPreview.this, EmployeeSideSeatsSelectionForViewBookings.class);
            intent.putExtra("showDayWise",showDayWise);
        } else {
            intent = new Intent(EmployeeSidePhysicalBookingPreview.this, EmployeeDashboard.class);
        }

        intent.putExtra("caller",caller);
        intent.putExtra("theatre",theatre);
        intent.putExtra("hall", hall);
        intent.putExtra("movie", movie);

        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivity(intent);
        finish();
    }

    public void showToastForBookingFailure()
    {
        progressBar.setVisibility(View.GONE);
        btnConfirmBooking.setEnabled(true);

        LayoutInflater inflater = getLayoutInflater();
        View layout = inflater.inflate(R.layout.custom_toast_booked, findViewById(R.id.custom_toast_container));

        ImageView toastImage = layout.findViewById(R.id.toast_image);
        toastImage.setImageResource(R.drawable.baseline_star_rate_24);

        TextView amountText = layout.findViewById(R.id.toast_amount);
        amountText.setText("Booking Failed");

        TextView transaction_type = layout.findViewById(R.id.toast_text);
        transaction_type.setText("Selected Seats are Already Booked");

        TextView subText = layout.findViewById(R.id.toast_subtext);
        subText.setText("Try Selecting Other Seats");

        Toast toast = new Toast(getApplicationContext());
        toast.setDuration(Toast.LENGTH_LONG);
        toast.setView(layout);
        toast.setGravity(Gravity.CENTER,0,0);
        toast.show();


        Intent intent;
        if (caller.equalsIgnoreCase("EmployeeViewUpcomingBookingsMovieWise")) {
            intent = new Intent(EmployeeSidePhysicalBookingPreview.this, EmployeeSideSeatsSelectionForViewBookings.class);
            intent.putExtra("show",show);
        } else if (caller.equalsIgnoreCase("EmployeeViewUpcomingBookingsDayWise")){
            intent = new Intent(EmployeeSidePhysicalBookingPreview.this, EmployeeSideSeatsSelectionForViewBookings.class);
            intent.putExtra("showDayWise",showDayWise);
        } else {
            intent = new Intent(EmployeeSidePhysicalBookingPreview.this, EmployeeDashboard.class);
        }

        intent.putExtra("caller",caller);
        intent.putExtra("theatre",theatre);
        intent.putExtra("hall", hall);
        intent.putExtra("movie", movie);

        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivity(intent);
        finish();
    }




}