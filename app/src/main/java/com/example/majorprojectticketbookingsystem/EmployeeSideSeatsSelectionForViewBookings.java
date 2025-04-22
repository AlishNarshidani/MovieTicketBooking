package com.example.majorprojectticketbookingsystem;

import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.firebase.Timestamp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class EmployeeSideSeatsSelectionForViewBookings extends AppCompatActivity {

    private LinearLayout seatsContainer;
    private TextView totalPriceText;
    private Button btnPhysicalBook, btnViewBookingDetails, btnCancelShow;
    private FirebaseFirestore db;
    private String showId;  // Replace with actual Show ID
    private String theatreId;
    private String movieId;
    private String hallId;
    String showStartTime;

    private HashMap<Button, Integer> selectedSeats;
    private int totalPrice = 0;

    Movie movie;
    Theatre theatre;
    Hall hall;
    Show show;
    ShowDayWise showDayWise;

    String caller;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_employee_side_seats_selection_for_view_bookings);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        selectedSeats = new HashMap<>();
        seatsContainer = findViewById(R.id.seatsContainer);
        totalPriceText = findViewById(R.id.totalPriceText);
        btnPhysicalBook = findViewById(R.id.btnPhysicalBook);
        btnViewBookingDetails = findViewById(R.id.btnViewBookingDetails);
        btnCancelShow = findViewById(R.id.cancelShow);

        db = FirebaseFirestore.getInstance();

        caller = getIntent().getStringExtra("caller");
        movie = (Movie) getIntent().getSerializableExtra("movie");
        theatre = (Theatre) getIntent().getSerializableExtra("theatre");
        hall = (Hall) getIntent().getSerializableExtra("hall");

        if(caller.equalsIgnoreCase("EmployeeViewUpcomingBookingsDayWise") || caller.equalsIgnoreCase("EmployeeViewPastBookingsDayWise")) {
            showDayWise = (ShowDayWise) getIntent().getSerializableExtra("showDayWise");
            showId = showDayWise.getShowId();
        } else if (caller.equalsIgnoreCase("EmployeeViewUpcomingBookingsMovieWise") || caller.equalsIgnoreCase("EmployeeViewPastBookingsMovieWise")) {
            show = (Show) getIntent().getSerializableExtra("show");
            showId = show.getShowId();
        }



        theatreId = theatre.getId();
        movieId = movie.getId();
        hallId = hall.getHallId();

        fetchSeatsFromFirestore();



        btnPhysicalBook.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                // Convert HashMap<Button, Integer> to a List of seat IDs
                ArrayList<String> selectedSeatIds = new ArrayList<>();
                ArrayList<Integer> selectedSeatPrices = new ArrayList<>();

                for (Map.Entry<Button, Integer> entry : selectedSeats.entrySet()) {
                    Button seatButton = entry.getKey();
                    String seatId = seatButton.getText().toString(); // ✅ Seat ID stored as button text
                    int seatPrice = entry.getValue(); // ✅ Seat Price stored as Integer value

                    selectedSeatIds.add(seatId);
                    selectedSeatPrices.add(seatPrice);
                }

                Intent intent = new Intent(EmployeeSideSeatsSelectionForViewBookings.this, EmployeeSidePhysicalBookingPreview.class);
                intent.putExtra("movie",movie);
                intent.putExtra("hall",hall);
                intent.putExtra("theatre",theatre);

                if(caller.equalsIgnoreCase("EmployeeViewUpcomingBookingsDayWise")) {
                    intent.putExtra("showDayWise",showDayWise);
                } else if (caller.equalsIgnoreCase("EmployeeViewUpcomingBookingsMovieWise")) {
                    intent.putExtra("show",show);
                }

                intent.putExtra("caller",caller);
                intent.putExtra("totalPrice",String.valueOf(totalPrice));
                intent.putStringArrayListExtra("selectedSeatIds", selectedSeatIds);
                intent.putIntegerArrayListExtra("selectedSeatPrices", selectedSeatPrices);
                startActivity(intent);
            }
        });

        btnViewBookingDetails.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(EmployeeSideSeatsSelectionForViewBookings.this, EmployeeSideViewBookedSeatsDetails.class);
                intent.putExtra("showId",showId);
                startActivity(intent);
            }
        });

        if(caller.equalsIgnoreCase("EmployeeViewUpcomingBookingsDayWise") || caller.equalsIgnoreCase("EmployeeViewPastBookingsDayWise")) {
            showStartTime = showDayWise.getShowStartTime();
        } else if (caller.equalsIgnoreCase("EmployeeViewUpcomingBookingsMovieWise") || caller.equalsIgnoreCase("EmployeeViewPastBookingsMovieWise")) {
            showStartTime = show.getShowStartTime();
        }

        if(isCancelable(convertToTimestamp(showStartTime))) {
            btnCancelShow.setVisibility(View.VISIBLE);
        }

        btnCancelShow.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AlertDialog.Builder alert = new AlertDialog.Builder(EmployeeSideSeatsSelectionForViewBookings.this)
                        .setTitle("Cancel Show ?")
                        .setIcon(R.drawable.baseline_theater_comedy_24)
                        .setMessage("Are You Sure You Want To Cancel This Show ?")
                        .setPositiveButton("YES", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {

                                btnCancelShow.setEnabled(false);
                                refundAllBookedUsers(showId);
                            }
                        })
                        .setNegativeButton("NO", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                dialog.dismiss();
                            }
                        });

                alert.setCancelable(false);
                alert.show();
            }
        });


    }


    private void deleteShowFromFirestore(String showId) {
        FirebaseFirestore db = FirebaseFirestore.getInstance();

        db.collection("shows").document(showId)
                .delete()
                .addOnSuccessListener(aVoid -> {
                    Log.d("TAG", "Show deleted successfully");
                    Toast.makeText(EmployeeSideSeatsSelectionForViewBookings.this, "Show deleted successfully", Toast.LENGTH_SHORT).show();
                    cancelBookingsByShowId(showId);
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(EmployeeSideSeatsSelectionForViewBookings.this, "Failed to delete show: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    Log.e("Firestore", "Error deleting show", e);
                });
    }



    private void cancelBookingsByShowId(String showId) {
        FirebaseFirestore db = FirebaseFirestore.getInstance();

        db.collection("bookings")
                .whereEqualTo("showId", showId)
                .get()
                .addOnSuccessListener(querySnapshot -> {
                    for (QueryDocumentSnapshot document : querySnapshot) {
                        String bookingId = document.getId();
                        db.collection("bookings").document(bookingId)
                                .update("bookingStatus", "Show Cancelled")
                                .addOnSuccessListener(aVoid ->
                                        Log.d("BookingCancel", "Booking " + bookingId + " marked as Show Cancelled")
                                )
                                .addOnFailureListener(e ->
                                        Log.e("BookingCancel", "Failed to update booking " + bookingId, e)
                                );
                    }

                    if (querySnapshot.isEmpty()) {
                        Log.d("BookingCancel", "No bookings found for showId: " + showId);
                    }
                })
                .addOnFailureListener(e ->
                        Log.e("BookingCancel", "Error fetching bookings for showId: " + showId, e)
                );

        finish();
    }



    private void refundAllBookedUsers(String showId) {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        DocumentReference showRef = db.collection("shows").document(showId);

        showRef.get().addOnSuccessListener(documentSnapshot -> {
            if (documentSnapshot.exists()) {
                Map<String, Object> seatsLayout = (Map<String, Object>) documentSnapshot.get("seatsLayout");
                if (seatsLayout == null) return;

                Map<String, Double> refundMap = new HashMap<>();

                for (String rowKey : seatsLayout.keySet()) {
                    List<Map<String, Object>> seats = (List<Map<String, Object>>) seatsLayout.get(rowKey);

                    for (Map<String, Object> seat : seats) {
                        boolean isBooked = Boolean.TRUE.equals(seat.get("isBooked"));
                        String userId = (String) seat.get("userId");

                        if (isBooked && userId != null) {
                            double price = ((Number) seat.getOrDefault("price", 0)).doubleValue();
                            refundMap.put(userId, refundMap.getOrDefault(userId, 0.0) + price);
                        }
                    }
                }

                for (Map.Entry<String, Double> entry : refundMap.entrySet()) {
                    String userId = entry.getKey();
                    double amountToRefund = entry.getValue();

                    DocumentReference userRef = db.collection("users").document(userId);

                    userRef.get().addOnSuccessListener(userSnap -> {
                        if (userSnap.exists()) {
                            double currentBalance = userSnap.getDouble("depositedMoney") != null ?
                                    userSnap.getDouble("depositedMoney") : 0.0;

                            double updatedBalance = currentBalance + amountToRefund;

                            userRef.update("depositedMoney", updatedBalance)
                                    .addOnSuccessListener(unused -> {
                                        Log.d("Refund", "Refunded ₹" + amountToRefund + " to user " + userId);
                                        addTransactionForRefundOfCancelledShow(userId, amountToRefund);
                                    })
                                    .addOnFailureListener(e -> Log.e("Refund", "Failed refund for user: " + userId, e));
                        }
                    });
                }

                deleteShowFromFirestore(showId);

            }
        }).addOnFailureListener(e -> Log.e("Firestore", "Failed to fetch show for refund", e));
    }



    public void addTransactionForRefundOfCancelledShow(String userId, double refundAmountdouble)
    {
        Long amountVal_long = Long.valueOf((long) refundAmountdouble);

        // Save data in transactions collection
        Map<String, Object> transactionData = new HashMap<>();
        transactionData.put("userId", userId);
        transactionData.put("transactionType", "Show Cancel Refund");
        transactionData.put("amount", amountVal_long);
        transactionData.put("bookingId",showId);
        transactionData.put("transactionMethod", "Wallet");
        transactionData.put("transactionDate", new com.google.firebase.Timestamp(new java.util.Date()));


        db.collection("transactions")
                .add(transactionData)
                .addOnSuccessListener(documentReference -> {
                    String transactionId = documentReference.getId();
                    Log.d("transactionId", "transactionId: "+transactionId);
                })
                .addOnFailureListener(e -> {
                    Log.d("transaction", "failed");
                });
    }



    private void fetchSeatsFromFirestore() {
        db.collection("shows").document(showId)
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        Map<String, Object> seatsLayout = (Map<String, Object>) documentSnapshot.get("seatsLayout");

                        if (seatsLayout != null) {
                            displaySeats(seatsLayout);
                        } else {
                            Toast.makeText(this, "No seat layout found", Toast.LENGTH_SHORT).show();
                        }
                    }
                })
                .addOnFailureListener(e -> Log.e("Firestore", "Error fetching seats", e));
    }


    private void displaySeats(Map<String, Object> seatsLayout) {
        List<String> sortedKeys = new ArrayList<>(seatsLayout.keySet());

        // Custom Comparator to handle decimal-like row names (row1, row1.5, row2)
        Collections.sort(sortedKeys, (key1, key2) -> {
            double num1 = Double.parseDouble(key1.replace("row", "").replace("_", "."));
            double num2 = Double.parseDouble(key2.replace("row", "").replace("_", "."));
            return Double.compare(num1, num2);
        });
        Log.d("sortedKeys", "displaySeats: " + sortedKeys);

        for (String rowKey : sortedKeys) {
            LinearLayout rowLayout = new LinearLayout(this);
            rowLayout.setLayoutParams(new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT));
            rowLayout.setOrientation(LinearLayout.HORIZONTAL);

            List<Map<String, Object>> seats = (List<Map<String, Object>>) seatsLayout.get(rowKey);

            for (Map<String, Object> seat : seats) {
                int seatId = ((Number) seat.get("id")).intValue(); // Ensures Integer
                double price = ((Number) seat.getOrDefault("price",100)).doubleValue(); // Ensures Double
                boolean isBooked = Boolean.TRUE.equals(seat.getOrDefault("isBooked", false)); // Ensures Boolean

                if (seatId == 0) {
                    // ✅ Create an empty space (walking path)
                    View emptySpace = new View(this);
                    LinearLayout.LayoutParams spaceParams = new LinearLayout.LayoutParams(100, 100); // ✅ Different width
                    spaceParams.setMargins(5, 5, 5, 5);
                    emptySpace.setLayoutParams(spaceParams);
                    emptySpace.setBackgroundColor(Color.TRANSPARENT); // ✅ Transparent space
                    rowLayout.addView(emptySpace);
                } else {
                    Button seatButton = new Button(this);
                    seatButton.setText(String.valueOf(seatId));
                    seatButton.setTextSize(12);
                    seatButton.setPadding(8, 8, 8, 8);
                    seatButton.setTag(price);  // Store price in tag for easy access

                    // Set layout params with margins for spacing
                    LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(100, 100);
                    params.setMargins(5, 5, 5, 5); // Adds spacing
                    seatButton.setLayoutParams(params); // Fixed button size

                    // Create rounded background with border
                    GradientDrawable drawable = new GradientDrawable();
                    drawable.setCornerRadius(20); // Rounded corners

                    // Set color based on availability
                    if (isBooked) {
                        drawable.setColor(Color.RED);
                        //seatButton.setBackgroundColor(Color.RED);
                        seatButton.setEnabled(false);
                    } else {
                        drawable.setColor(Color.LTGRAY);
                        //seatButton.setBackgroundColor(Color.LTGRAY);
                    }

                    seatButton.setBackground(drawable);

                    // Handle seat selection
                    seatButton.setOnClickListener(view -> toggleSeatSelection(seatButton));

                    rowLayout.addView(seatButton);
                }
            }

            seatsContainer.addView(rowLayout);
        }
    }


    private void toggleSeatSelection(Button seatButton) {
        GradientDrawable drawable = new GradientDrawable();
        drawable.setCornerRadius(20);

        if (selectedSeats.containsKey(seatButton)) {
            // Deselect seat
            totalPrice -= selectedSeats.get(seatButton);
            selectedSeats.remove(seatButton);
            drawable.setColor(Color.LTGRAY);
            //seatButton.setBackgroundColor(Color.LTGRAY);
        } else {
            // Select seat
            int seatPrice = ((Double) seatButton.getTag()).intValue();
            totalPrice += seatPrice;
            selectedSeats.put(seatButton, seatPrice);
            drawable.setColor(Color.GREEN);
            //seatButton.setBackgroundColor(Color.GREEN);
        }

        seatButton.setBackground(drawable);

        // Update total price display
        totalPriceText.setText("Total: ₹" + totalPrice);

        // Show confirm button only if seats are selected
        if(caller.equalsIgnoreCase("EmployeeViewUpcomingBookingsMovieWise") || caller.equalsIgnoreCase("EmployeeViewUpcomingBookingsDayWise")) {
            btnPhysicalBook.setVisibility(selectedSeats.isEmpty() ? View.GONE : View.VISIBLE);
        }
    }


    private boolean isCancelable(Timestamp showStartTime) {
        long currentTime = System.currentTimeMillis();
        long showTime = showStartTime.toDate().getTime();

        // Calculate the difference in milliseconds (24 hours = 24 * 60 * 60 * 1000 ms)
        long timeDifference = showTime - currentTime;
        return timeDifference > 24 * 60 * 60 * 1000;
    }

    private Timestamp convertToTimestamp(String dateTimeStr) {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault());
        try {
            Date date = sdf.parse(dateTimeStr); // Convert string to Date
            return new Timestamp(date); // Convert Date to Firestore Timestamp
        } catch (ParseException e) {
            e.printStackTrace();
            return null; // Handle error properly
        }
    }


}