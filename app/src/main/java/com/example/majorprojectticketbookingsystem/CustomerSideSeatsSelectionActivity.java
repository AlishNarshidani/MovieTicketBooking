package com.example.majorprojectticketbookingsystem;

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
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class CustomerSideSeatsSelectionActivity extends AppCompatActivity {

    private LinearLayout seatsContainer;
    private TextView totalPriceText;
    private Button btnConfirmSelection;
    private FirebaseFirestore db;
    private String showId;  // Replace with actual Show ID
    private String theatreId;
    private String movieId;
    private String hallId;

    private HashMap<Button, Integer> selectedSeats;
    private int totalPrice = 0;

    Movie movie;
    Theatre theatre;
    Hall hall;
    Show show;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_customer_side_seats_selection);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        selectedSeats = new HashMap<>();
        seatsContainer = findViewById(R.id.seatsContainer);
        totalPriceText = findViewById(R.id.totalPriceText);
        btnConfirmSelection = findViewById(R.id.btnConfirmSelection);

        db = FirebaseFirestore.getInstance();

        movie = (Movie) getIntent().getSerializableExtra("movie");
        theatre = (Theatre) getIntent().getSerializableExtra("theatre");
        hall = (Hall) getIntent().getSerializableExtra("hall");
        show = (Show) getIntent().getSerializableExtra("show");

        if(show != null) {
            showId = show.getShowId();
            theatreId = theatre.getId();
            movieId = movie.getId();
            hallId = hall.getHallId();

            fetchSeatsFromFirestore();
        }

        btnConfirmSelection.setOnClickListener(new View.OnClickListener() {
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

                Intent intent = new Intent(CustomerSideSeatsSelectionActivity.this, CustomerSideBookingPreview.class);
                intent.putExtra("movie",movie);
                intent.putExtra("hall",hall);
                intent.putExtra("theatre",theatre);
                intent.putExtra("show",show);
                intent.putExtra("totalPrice",String.valueOf(totalPrice));
                intent.putStringArrayListExtra("selectedSeatIds", selectedSeatIds);
                intent.putIntegerArrayListExtra("selectedSeatPrices", selectedSeatPrices);
                startActivity(intent);
            }
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
        btnConfirmSelection.setVisibility(selectedSeats.isEmpty() ? View.GONE : View.VISIBLE);
    }


}