package com.example.majorprojectticketbookingsystem;

import android.content.DialogInterface;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.activity.result.ActivityResultLauncher;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatButton;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.firebase.Timestamp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.journeyapps.barcodescanner.ScanContract;
import com.journeyapps.barcodescanner.ScanOptions;

import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Locale;

public class EmployeeSideQRCodeScanActivity extends AppCompatActivity {

    String bookingId;
    private FirebaseFirestore db;
    private FirebaseAuth auth;
    private ProgressBar progressBar;
    Button btnAdmitPerson;
    int maxRemainingAllowedPersons;

    int enteringPersons;

    TextView txtMovieName, txtTheatreName, txtTheatreLocation, txtHallName, txtShowStartTime, txtShowEndTime, txtSelectedSeats, txtTotalPrice, txtBookingStatus, txtTotalSeats, txtEnteredPerson;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_employee_side_qrcode_scan);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        progressBar = findViewById(R.id.progressBar);
        txtMovieName = findViewById(R.id.txtMovieName);
        txtTheatreName = findViewById(R.id.txtTheatreName);
        txtTheatreLocation = findViewById(R.id.txtTheatreLocation);
        txtHallName = findViewById(R.id.txtHallName);
        txtShowStartTime = findViewById(R.id.txtShowStartTime);
        txtShowEndTime = findViewById(R.id.txtShowEndTime);
        txtSelectedSeats = findViewById(R.id.txtSelectedSeats);
        txtTotalPrice = findViewById(R.id.txtTotalPrice);
        txtBookingStatus = findViewById(R.id.txtBookingStatus);
        txtTotalSeats = findViewById(R.id.txtTotalSeats);
        txtEnteredPerson = findViewById(R.id.txtEnteredPerson);
        btnAdmitPerson = findViewById(R.id.btnAdmitPerson);

        db = FirebaseFirestore.getInstance();
        auth = FirebaseAuth.getInstance();

        scanCode();

        btnAdmitPerson.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showAdmitPersonDialog();
            }
        });
    }



    private void showAdmitPersonDialog() {
        LayoutInflater inflater = getLayoutInflater();
        View dialogView = inflater.inflate(R.layout.dialog_admit_customer, null);

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setView(dialogView);
        builder.setCancelable(false);

        AlertDialog dialog = builder.create();

        EditText admitPersonEditText = dialogView.findViewById(R.id.admitPersonEditText);
        AppCompatButton incrementButton = dialogView.findViewById(R.id.incrementButton);
        AppCompatButton decrementButton = dialogView.findViewById(R.id.decrementButton);
        AppCompatButton confirmButton = dialogView.findViewById(R.id.confirmButton);
        AppCompatButton cancelButton = dialogView.findViewById(R.id.cancelButton);

        // Set the bet amount based on user's input and interaction
        incrementButton.setOnClickListener(v -> {
            String admitPersonStr = admitPersonEditText.getText().toString();
            enteringPersons = !admitPersonStr.isEmpty() ? Integer.parseInt(admitPersonStr) : 0;
            if(enteringPersons < maxRemainingAllowedPersons) {
                enteringPersons += 1;
                admitPersonEditText.setText(String.valueOf(enteringPersons));
            }
        });

        decrementButton.setOnClickListener(v -> {
            String admitPersonStr = admitPersonEditText.getText().toString();
            enteringPersons = !admitPersonStr.isEmpty() ? Integer.parseInt(admitPersonStr) : 0;
            if (enteringPersons > 0) {
                enteringPersons -= 1;
                admitPersonEditText.setText(String.valueOf(enteringPersons));
            }
        });

        confirmButton.setOnClickListener(v -> {
            String admitPersonStr = admitPersonEditText.getText().toString();

            if (!admitPersonStr.isEmpty() && Integer.parseInt(admitPersonStr) > 0 && Integer.parseInt(admitPersonStr) <= maxRemainingAllowedPersons) {
                enteringPersons = Integer.parseInt(admitPersonStr);
                dialog.dismiss();
                makeEntryInFirestore();
            } else {
                Toast.makeText(this, "Enter Valid Number", Toast.LENGTH_SHORT).show();
            }
        });

        cancelButton.setOnClickListener(v -> {
            dialog.dismiss();
        });

        dialog.show();
    }



    private void makeEntryInFirestore()
    {
        DocumentReference bookingRef = db.collection("bookings").document(bookingId);

        bookingRef.get().addOnSuccessListener(documentSnapshot -> {
            if (documentSnapshot.exists()) {
                Long currentAdmittedPersons = documentSnapshot.getLong("admittedPersons");
                Long totalBookedSeats = documentSnapshot.getLong("totalBookedSeats");

                if (currentAdmittedPersons == null) {
                    currentAdmittedPersons = 0L;
                }

                int updatedAdmittedPersons = currentAdmittedPersons.intValue() + enteringPersons;

                // ✅ Ensure it doesn't exceed totalBookedSeats
                if (totalBookedSeats != null && updatedAdmittedPersons > totalBookedSeats) {
                    Toast.makeText(this, "Cannot admit more than booked seats!", Toast.LENGTH_SHORT).show();
                } else {
                    bookingRef.update("admittedPersons", updatedAdmittedPersons)
                            .addOnSuccessListener(aVoid -> Toast.makeText(this, "Made Entry successfully!", Toast.LENGTH_SHORT).show())
                            .addOnFailureListener(e -> {
                                Log.e("Firestore", "Error updating admitted persons", e);
                                Toast.makeText(this, "Failed to update admitted persons.", Toast.LENGTH_SHORT).show();
                            });
                    fetchBookingById(bookingId);
                }
            } else {
                Toast.makeText(this, "Booking not found!", Toast.LENGTH_SHORT).show();
            }
        }).addOnFailureListener(e -> {
            Log.e("Firestore", "Error fetching booking details", e);
            Toast.makeText(this, "Error fetching booking details.", Toast.LENGTH_SHORT).show();
        });
    }


    private void scanCode()
    {
        ScanOptions options = new ScanOptions();
        options.setPrompt("volume up to flash on");
        options.setBeepEnabled(true);
        options.setOrientationLocked(true);
        options.setCaptureActivity(CaptureAct.class);
        qrLauncher.launch(options);
    }

    ActivityResultLauncher<ScanOptions> qrLauncher = registerForActivityResult(new ScanContract(), result -> {

        if(result.getContents() != null)
        {
            bookingId = result.getContents();
            fetchBookingById(bookingId);
        }

    });



    private void fetchBookingById(String bookingId) {
        progressBar.setVisibility(View.VISIBLE);

        db.collection("bookings")
                .document(bookingId)
                .get()
                .addOnSuccessListener(document -> {
                    if (document.exists()) {
                        String bookingStatus = document.getString("bookingStatus");
                        String movieName = document.getString("movieName");
                        String theatreName = document.getString("theatreName");
                        String theatreLocation = document.getString("theatreLocation");
                        String hallName = document.getString("hallName");
                        String showId = document.getString("showId");
                        Timestamp showStartTime = document.getTimestamp("showStartTime");
                        Timestamp showEndTime = document.getTimestamp("showEndTime");
                        List<String> seatList = (List<String>) document.get("selectedSeats");
                        Long totalBookedSeats = document.getLong("totalBookedSeats");
                        Long admittedPersons = document.getLong("admittedPersons");

                        maxRemainingAllowedPersons = (totalBookedSeats != null ? totalBookedSeats.intValue() : 0) -
                                (admittedPersons != null ? admittedPersons.intValue() : 0);



                        // ✅ Convert List<String> to Comma-Separated String
                        String selectedSeats = (seatList != null) ? String.join(", ", seatList) : "N/A";
                        String totalPrice = document.getString("totalPrice");

                        String formattedStartTime = formatTimestamp(showStartTime);
                        String formattedEndTime = formatTimestamp(showEndTime);

                        Booking booking = new Booking(bookingId, bookingStatus, movieName, theatreName, theatreLocation,
                                hallName, showId, formattedStartTime, formattedEndTime, selectedSeats, totalPrice, seatList);

                        if(bookingStatus != null && !bookingStatus.isEmpty()) {
                            txtBookingStatus.setText("Booking: "+bookingStatus);
                            if (bookingStatus.equalsIgnoreCase("confirmed")) {
                                txtBookingStatus.setTextColor(ContextCompat.getColor(EmployeeSideQRCodeScanActivity.this, R.color.mid_green));

                            } else if (bookingStatus.equalsIgnoreCase("cancelled")) {
                                txtBookingStatus.setTextColor(Color.RED);
                            }

                        } else {
                            txtBookingStatus.setText("Booking Pending");
                        }

                        txtMovieName.setText("Movie: "+movieName);
                        txtTheatreName.setText("Theatre: "+theatreName);
                        txtTheatreLocation.setText("Address: "+theatreLocation);
                        txtHallName.setText("Hall: "+hallName);
                        txtShowStartTime.setText("Start: "+formattedStartTime);
                        txtShowEndTime.setText("End: "+formattedEndTime);
                        txtSelectedSeats.setText("Seat No: "+selectedSeats);
                        txtTotalPrice.setText("Total Amount: ₹"+totalPrice);
                        txtTotalSeats.setText("Total Seats: "+totalBookedSeats);
                        txtEnteredPerson.setText("Entered Persons: "+admittedPersons);

                        if(maxRemainingAllowedPersons > 0)
                        {
                            btnAdmitPerson.setVisibility(View.VISIBLE);
                        } else {
                            btnAdmitPerson.setVisibility(View.GONE);
                        }

                    } else {
                        Toast.makeText(this, "Booking not found!", Toast.LENGTH_SHORT).show();
                        finish();
                    }
                    progressBar.setVisibility(View.GONE);
                })
                .addOnFailureListener(e -> {
                    Log.e("Firestore", "Error fetching booking", e);
                    progressBar.setVisibility(View.GONE);
                });
    }


    private String formatTimestamp(Timestamp timestamp) {
        if (timestamp == null) return "N/A"; // ✅ Handle null case

        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault());
        return sdf.format(timestamp.toDate()); // ✅ Convert Firestore Timestamp to formatted String
    }



}