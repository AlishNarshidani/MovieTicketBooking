package com.example.majorprojectticketbookingsystem;

import android.app.Activity;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
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
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.firebase.Timestamp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.SetOptions;
import com.google.firebase.firestore.Source;
import com.razorpay.Checkout;
import com.razorpay.PaymentResultListener;

import org.json.JSONObject;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class CustomerSideBookingPreview extends AppCompatActivity implements PaymentResultListener {

    private String totalPrice;
    private TextView txtMovieName, txtTheatreName, txtHallName, txtShowTime, txtTotalPrice, txtSelectedSeats, txtTheatreAddress, txtDiscountPerc, txtPayablePrice;

    Movie movie;
    Theatre theatre;
    Hall hall;
    Show show;

    String bookingId;

    private ProgressBar progressBar;

    FirebaseAuth auth;
    FirebaseFirestore db;

    Long amountVal_long;

    Button btnPayDirect, btnPayWithWallet;

    ArrayList<String> selectedSeatIds;
    ArrayList<Integer> selectedSeatPrices;

    double depositedMoney;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_customer_side_booking_preview);
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
        txtDiscountPerc = findViewById(R.id.txtDiscountPerc);
        txtPayablePrice = findViewById(R.id.txtPayablePrice);
        btnPayDirect = findViewById(R.id.btnPayDirect);
        btnPayWithWallet = findViewById(R.id.btnPayWithWallet);
        progressBar = findViewById(R.id.progressBar);

        db=FirebaseFirestore.getInstance();
        auth= FirebaseAuth.getInstance();

        movie = (Movie) getIntent().getSerializableExtra("movie");
        theatre = (Theatre) getIntent().getSerializableExtra("theatre");
        hall = (Hall) getIntent().getSerializableExtra("hall");
        show = (Show) getIntent().getSerializableExtra("show");
        totalPrice = getIntent().getStringExtra("totalPrice");
        selectedSeatIds = getIntent().getStringArrayListExtra("selectedSeatIds");
        selectedSeatPrices = getIntent().getIntegerArrayListExtra("selectedSeatPrices");

        displayBookingDetails(selectedSeatIds, selectedSeatPrices);


        btnPayDirect.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(movie != null && theatre != null && show != null && hall != null)
                {
                    btnPayDirect.setEnabled(false);
                    progressBar.setVisibility(View.VISIBLE);
                    updateSeatBooking("direct");
                }
            }
        });

        btnPayWithWallet.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(movie != null && theatre != null && show != null && hall != null)
                {
                    AlertDialog.Builder alert = new AlertDialog.Builder(CustomerSideBookingPreview.this)
                            .setTitle("Pay Through Wallet ?")
                            .setIcon(R.drawable.baseline_account_balance_wallet_24)
                            .setMessage("Are you Sure you want to pay through wallet ?")
                            .setPositiveButton("YES", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {

                                    btnPayDirect.setEnabled(false);
                                    progressBar.setVisibility(View.VISIBLE);
                                    checkWalletBalance();

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
            }
        });
    }


    private void checkWalletBalance() {
        String userId = FirebaseAuth.getInstance().getCurrentUser().getUid();
        FirebaseFirestore db = FirebaseFirestore.getInstance();

        db.collection("users").document(userId)
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        depositedMoney = documentSnapshot.getDouble("depositedMoney");

                        if (depositedMoney >= Double.parseDouble(totalPrice)) {
                            // ✅ Proceed with booking
                            updateSeatBooking("wallet");
                        } else {
                            // ❌ Insufficient balance
                            Toast.makeText(this, "Insufficient wallet balance!", Toast.LENGTH_SHORT).show();
                            btnPayDirect.setEnabled(true);
                            progressBar.setVisibility(View.GONE);
                        }
                    } else {
                        Toast.makeText(this, "User data not found!", Toast.LENGTH_SHORT).show();
                        finish();
                    }
                })
                .addOnFailureListener(e -> {
                    Log.e("Firestore", "Error fetching wallet balance", e);
                    Toast.makeText(this, "Failed to fetch wallet balance!", Toast.LENGTH_SHORT).show();
                    finish();
                });
    }


    private void displayBookingDetails(ArrayList<String> selectedSeatIds, ArrayList<Integer> selectedSeatPrices) {
        // Set movie, theatre, and hall names
        txtMovieName.setText(movie.getTitle());
        txtTheatreName.setText("Theatre: " + theatre.getName());
        txtTheatreAddress.setText("Address: " + theatre.getLocation());
        txtHallName.setText("Hall: " + hall.getHallName());

        txtShowTime.setText("Show Time: " + show.getShowStartTime() + " - " + show.getShowEndTime());

        // Set total price
        txtTotalPrice.setText("Total Price: ₹" + totalPrice);

        // Display selected seats with their prices
        StringBuilder seatInfo = new StringBuilder();
        for (int i = 0; i < selectedSeatIds.size(); i++) {
            seatInfo.append("Seat ").append(selectedSeatIds.get(i)).append(" - ₹").append(selectedSeatPrices.get(i)).append("\n");
        }

        txtSelectedSeats.setText(seatInfo.toString().trim());

        SharedPreferences sharedPreferences = getSharedPreferences("MyAppPrefs", MODE_PRIVATE);
        int discountPerc = sharedPreferences.getInt("discountPerc", 0);

        if(discountPerc != 0) {
            txtDiscountPerc.setVisibility(View.VISIBLE);
            txtPayablePrice.setVisibility(View.VISIBLE);
            txtDiscountPerc.setText("Discount: " + discountPerc + "%");
            int discountedPrice = (int) (Double.parseDouble(totalPrice) - (Double.parseDouble(totalPrice) * discountPerc / 100));
            txtPayablePrice.setText("To Pay: ₹" + discountedPrice);
            totalPrice = String.valueOf(discountedPrice);
        }
    }


    public void PaymentNow(String val)
    {
        final Activity activity = this;

        Checkout checkout = new Checkout();
        checkout.setKeyID("rzp_test_lxZ3tAXnEknadJ");
        checkout.setImage(R.mipmap.ic_launcher);

        double finalAmount = Double.parseDouble(val)*100;

        try {
            JSONObject options = new JSONObject();

            options.put("name", "Movie Mania");
            options.put("description", "Reference No. #123456");
            options.put("image", "http://example.com/image/rzp.jpg");
            //options.put("order_id", "order_DBJOWzybf0sJbb");//from response of step 3.
            options.put("theme.color", "#3399cc");
            options.put("currency", "INR");
            options.put("amount", finalAmount+"");//pass amount in currency subunits
            options.put("prefill.email", "alish.narshidani@example.com");
            options.put("prefill.contact","9988776655");
//            JSONObject retryObj = new JSONObject();
//            retryObj.put("enabled", true);
//            retryObj.put("max_count", 4);
//            options.put("retry", retryObj);

            checkout.open(activity, options);

        } catch(Exception e) {
            Log.e("payError", "Error in starting Razorpay Checkout", e);
        }

    }


    private void updateSeatBooking(String caller) {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        DocumentReference showRef = db.collection("shows").document(show.getShowId());

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
                            seat.put("bookingType","Online");
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
            if(caller.equals("direct"))
            {
                PaymentNow(totalPrice);
            } else if(caller.equals("wallet")) {
                deductWalletBalance(depositedMoney);
            }
        }).addOnFailureListener(e -> {
            Log.e("Firestore", "Booking failed", e);
            showToastForBookingFailure();
        });
    }


    private void deductWalletBalance(double currentBalance) {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        String userId = FirebaseAuth.getInstance().getCurrentUser().getUid();

        db.collection("users").document(userId)
                .update("depositedMoney", currentBalance - Double.parseDouble(totalPrice))
                .addOnSuccessListener(aVoid -> {
                    Log.d("Firestore", "Wallet balance updated!");
                    addBookingDetailsToFirestore("wallet"); // ✅ Proceed with booking
                })
                .addOnFailureListener(e -> {
                    Log.e("Firestore", "Error updating wallet balance", e);
                    Toast.makeText(this, "Payment failed!", Toast.LENGTH_SHORT).show();
                    unBookSeats();
                });
    }



    private void initiateRefund() {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        FirebaseAuth auth = FirebaseAuth.getInstance();
        String userId = auth.getCurrentUser().getUid();

        DocumentReference userRef = db.collection("users").document(userId);

        db.runTransaction(transaction -> {
            DocumentSnapshot userSnapshot = transaction.get(userRef);

            if (!userSnapshot.exists()) {
                throw new FirebaseFirestoreException("User not found!", FirebaseFirestoreException.Code.ABORTED);
            }

            // ✅ Get current deposited money, default to 0 if not present
            double currentBalance = userSnapshot.contains("depositedMoney") ? userSnapshot.getDouble("depositedMoney") : 0.0;

            // ✅ Convert totalPrice to double and add to balance
            double refundAmount = Double.parseDouble(totalPrice);
            double newBalance = currentBalance + refundAmount;

            // ✅ Update the field
            transaction.update(userRef, "depositedMoney", newBalance);
            return null;
        }).addOnSuccessListener(aVoid -> {
            Log.d("Firestore", "Refund processed successfully!");
            addTransactionForRefundOfFailedBooking();
        }).addOnFailureListener(e -> {
            initiateRefund();
            Log.e("Firestore", "Refund failed", e);
            Toast.makeText(this, "Error processing refund: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        });
    }



    public void addTransactionForDirectBooking()
    {
        FirebaseUser user=auth.getCurrentUser();
        String userId=user.getUid();

        amountVal_long = Long.parseLong(totalPrice);

        // Save data in transactions collection
        Map<String, Object> transactionData = new HashMap<>();
        transactionData.put("userId", userId);
        transactionData.put("transactionType", "Booking");
        transactionData.put("amount", amountVal_long);
        transactionData.put("transactionMethod", "Direct");
        transactionData.put("bookingId",bookingId);
        transactionData.put("transactionDate", new com.google.firebase.Timestamp(new java.util.Date()));


        db.collection("transactions")
                .add(transactionData)
                .addOnSuccessListener(documentReference -> {
                    String transactionId = documentReference.getId();
                    showToastForBookingConfirmed();
                    Log.d("transactionId", "transactionId: "+transactionId);
                })
                .addOnFailureListener(e -> {
                    Log.d("transaction", "failed");
                    unBookSeats();
                    initiateRefund();
                });
    }


    public void addTransactionForWalletBooking()
    {
        FirebaseUser user=auth.getCurrentUser();
        String userId=user.getUid();

        amountVal_long = Long.parseLong(totalPrice);

        // Save data in transactions collection
        Map<String, Object> transactionData = new HashMap<>();
        transactionData.put("userId", userId);
        transactionData.put("transactionType", "Booking");
        transactionData.put("amount", amountVal_long);
        transactionData.put("transactionMethod", "Wallet");
        transactionData.put("bookingId",bookingId);
        transactionData.put("transactionDate", new com.google.firebase.Timestamp(new java.util.Date()));


        db.collection("transactions")
                .add(transactionData)
                .addOnSuccessListener(documentReference -> {
                    String transactionId = documentReference.getId();
                    showToastForBookingConfirmed();
                    Log.d("transactionId", "transactionId: "+transactionId);
                })
                .addOnFailureListener(e -> {
                    Log.d("transaction", "failed");
                    unBookSeats();
                    initiateRefund();
                });
    }


    private void addBookingDetailsToFirestore(String caller)
    {
        bookingId = db.collection("bookings").document().getId(); // ✅ Generate unique booking ID

        // ✅ Prepare booking details
        Map<String, Object> bookingDetails = new HashMap<>();
        bookingDetails.put("bookingId", bookingId);
        bookingDetails.put("userId", FirebaseAuth.getInstance().getCurrentUser().getUid());

        // ✅ Show details
        bookingDetails.put("showId", show.getShowId());
        bookingDetails.put("showStartTime", convertToTimestamp(show.getShowStartTime()));
        bookingDetails.put("showEndTime", convertToTimestamp(show.getShowEndTime()));

        // ✅ Movie details
        bookingDetails.put("movieId", movie.getId());
        bookingDetails.put("movieName", movie.getTitle());

        // ✅ Theatre details
        bookingDetails.put("theatreId", theatre.getId());
        bookingDetails.put("theatreName", theatre.getName());
        bookingDetails.put("theatreLocation", theatre.getLocation());

        bookingDetails.put("bookingStatus","Confirmed");
        bookingDetails.put("bookingType","Online");
        bookingDetails.put("totalBookedSeats", selectedSeatIds.size());
        bookingDetails.put("admittedPersons", 0);

        // ✅ Hall details
        bookingDetails.put("hall_id", hall.getHallId());
        bookingDetails.put("hallName", hall.getHallName());

        // ✅ Seat details
        bookingDetails.put("selectedSeats", selectedSeatIds);

        // ✅ Pricing
        bookingDetails.put("totalPrice", totalPrice);

        // ✅ Timestamp for booking time
        bookingDetails.put("bookingTimestamp", Timestamp.now());

        // ✅ Store in Firestore
        db.collection("bookings").document(bookingId)
                .set(bookingDetails)
                .addOnSuccessListener(aVoid -> {
                    Log.d("Firestore", "Booking added successfully!");
                    if(caller.equals("direct"))
                    {
                        addTransactionForDirectBooking();
                    } else if (caller.equals("wallet")){
                        addTransactionForWalletBooking();
                    }
                })
                .addOnFailureListener(e -> {
                    Log.e("Firestore", "Error adding booking", e);
                    Toast.makeText(this, "Booking failed!", Toast.LENGTH_SHORT).show();
                    unBookSeats();
                    initiateRefund();
                });
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


    public void addTransactionForRefundOfFailedBooking()
    {
        FirebaseUser user=auth.getCurrentUser();
        String userId=user.getUid();

        amountVal_long = Long.parseLong(totalPrice);

        // Save data in transactions collection
        Map<String, Object> transactionData = new HashMap<>();
        transactionData.put("userId", userId);
        transactionData.put("transactionType", "Refund");
        transactionData.put("amount", amountVal_long);
        transactionData.put("bookingId",bookingId);
        transactionData.put("transactionMethod", "Failed Booking");
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

        showToastForRefund();
    }


    public void showToastForBookingConfirmed()
    {
        resetDiscount();

        String showStartTimeString = show.getShowStartTime();
        Timestamp timestamp = convertToTimestamp(showStartTimeString);

        if (timestamp != null) {
            long showStartTimeMillis = timestamp.toDate().getTime(); // Convert to millis

            // Schedule notification 1 hour before
            scheduleNotification(CustomerSideBookingPreview.this, showStartTimeMillis);
        }

        progressBar.setVisibility(View.GONE);
        btnPayDirect.setEnabled(true);

        LayoutInflater inflater = getLayoutInflater();
        View layout = inflater.inflate(R.layout.custom_toast_booked, findViewById(R.id.custom_toast_container));

        TextView amountText = layout.findViewById(R.id.toast_amount);
        amountText.setText("₹" + totalPrice);

        TextView transaction_type = layout.findViewById(R.id.toast_text);
        transaction_type.setText("Tickets Booked");

        TextView subText = layout.findViewById(R.id.toast_subtext);
        subText.setText("You can View the Ticket in your Booking History");

        Toast toast = new Toast(getApplicationContext());
        toast.setDuration(Toast.LENGTH_LONG);
        toast.setView(layout);
        toast.setGravity(Gravity.CENTER,0,0);
        toast.show();

        Intent intent = new Intent(CustomerSideBookingPreview.this, CustomerDashboard.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }


    public void scheduleNotification(Context context, long showStartTimeInMillis) {
        // 1 hour before the show
        long triggerTime = showStartTimeInMillis - (60 * 60 * 1000);

        SimpleDateFormat sdf = new SimpleDateFormat("dd MMM yyyy, hh:mm a", Locale.getDefault());
        String formattedTriggerTime = sdf.format(new Date(triggerTime));
        Log.d("NotificationDebug", "Notification scheduled for: " + formattedTriggerTime);

        // Create an Intent for the BroadcastReceiver
        Intent intent = new Intent(context, NotificationReceiver.class);
        intent.putExtra("showTime", showStartTimeInMillis);
        intent.putExtra("message","you have booking of movie " + movie.getTitle() + " at theatre " + theatre.getName() + " on " + show.getShowStartTime());

        PendingIntent pendingIntent = PendingIntent.getBroadcast(
                context, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
        );

        // Set up the AlarmManager
        AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        if (alarmManager != null) {
            alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, triggerTime, pendingIntent);
        }
    }


    private void resetDiscount() {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        FirebaseUser user=auth.getCurrentUser();
        String userId=user.getUid();
        SharedPreferences sharedPreferences = getSharedPreferences("MyAppPrefs", MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();

        // Update Firestore
        db.collection("users").document(userId)
                .update("discountPerc", 0)
                .addOnSuccessListener(aVoid -> {
                    // Update SharedPreferences
                    editor.putInt("discountPerc", 0);
                    editor.apply();
                    Log.d("Firestore", "Discount reset successfully!");
                })
                .addOnFailureListener(e -> Log.e("Firestore", "Error resetting discount", e));
    }


    public void showToastForBookingFailure()
    {
        progressBar.setVisibility(View.GONE);
        btnPayDirect.setEnabled(true);

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

        Intent intent = new Intent(CustomerSideBookingPreview.this, CustomerSideSeatsSelectionActivity.class);
        intent.putExtra("movie",movie);
        intent.putExtra("theatre",theatre);
        intent.putExtra("hall",hall);
        intent.putExtra("show",show);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivity(intent);
        finish();
    }

    public void showToastForRefund()
    {
        progressBar.setVisibility(View.GONE);
        btnPayDirect.setEnabled(true);

        LayoutInflater inflater = getLayoutInflater();
        View layout = inflater.inflate(R.layout.custom_toast_booked, findViewById(R.id.custom_toast_container));

        ImageView toastImage = layout.findViewById(R.id.toast_image);
        toastImage.setImageResource(R.drawable.baseline_star_rate_24);

        TextView amountText = layout.findViewById(R.id.toast_amount);
        amountText.setText("Booking Failed");

        TextView transaction_type = layout.findViewById(R.id.toast_text);
        transaction_type.setText("We Faced Some Issue to Book Your Seats");

        TextView subText = layout.findViewById(R.id.toast_subtext);
        subText.setText("The Refund of Deducted Money Has Been Initiated");

        Toast toast = new Toast(getApplicationContext());
        toast.setDuration(Toast.LENGTH_LONG);
        toast.setView(layout);
        toast.setGravity(Gravity.CENTER,0,0);
        toast.show();

        Intent intent = new Intent(CustomerSideBookingPreview.this, CustomerSideSeatsSelectionActivity.class);
        intent.putExtra("movie",movie);
        intent.putExtra("theatre",theatre);
        intent.putExtra("hall",hall);
        intent.putExtra("show",show);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivity(intent);
        finish();
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
    }

    @Override
    public void onPaymentSuccess(String s) {

        addBookingDetailsToFirestore("direct");



//        // Reference to the document in the 'users' collection
//        DocumentReference userDocRef = db.collection("users").document(userId);
//
//        // Fetch the document of current user
//        userDocRef.get()
//                .addOnCompleteListener(task -> {
//                    if(task.isSuccessful()) {
//                        DocumentSnapshot document = task.getResult();
//
//                        if(document!=null && document.exists()) {
//                            // The document exists, get all fields as a Map
//                            Map<String, Object> fetchUserData = document.getData();
//
//                            if (fetchUserData != null) {
//                                // Iterate through the fields and log or use the data
//                                for (Map.Entry<String, Object> entry : fetchUserData.entrySet()) {
//                                    String key = entry.getKey();
//                                    Object value = entry.getValue();
//
//                                    // Log the key and value
//                                    Log.d("FirestoreData", key + ": " + value.toString());
//                                }
//
//                                // Example: Get specific fields (if needed)
//                                fetchedDepositMoney = document.getLong("depositedMoney");
//                                fetchedWithdrawableMoney = document.getLong("withdrawable money");
//                                fetchedBonusMoney = document.getLong("bonus money");
//
//
//
//
//                                amountVal_long = Long.parseLong(amountVal);
//                                Long new_deposit_balance = fetchedDepositMoney + amountVal_long;
//
//                                //update wallet data when money deposited
//                                Map<String,Object> userData = new HashMap<>();
//                                userData.put("deposit money",new_deposit_balance);
//                                Log.d("deposit", "money deposited, new deposit balance: "+new_deposit_balance);
//
//                                db.collection("users").document(userId).set(userData, SetOptions.merge())
//                                        .addOnSuccessListener(aVoid -> {
//                                            //Toast.makeText(PaymentAmount.this,"Successfully updated wallet !",Toast.LENGTH_SHORT).show();
//                                        })
//                                        .addOnFailureListener(e ->Toast.makeText(PaymentAmount.this,"Error Saving User Data!",Toast.LENGTH_SHORT).show());
//
//                                Log.d("Firestore Wallet Data", fetchedDepositMoney + ", " + fetchedWithdrawableMoney + ", " + fetchedBonusMoney);
//
//
//
//                                // Save data in transactions collection
//                                Map<String, Object> transactionData = new HashMap<>();
//                                transactionData.put("userId", userId);
//                                transactionData.put("transactionType", "Deposit");
//                                transactionData.put("amount", amountVal_long);
//                                transactionData.put("transactionStatus", "Completed");
//                                transactionData.put("transactionDate", new com.google.firebase.Timestamp(new java.util.Date()));
//
//
//                                db.collection("transactions")
//                                        .add(transactionData)
//                                        .addOnSuccessListener(documentReference -> {
//                                            String transactionId = documentReference.getId();
//                                            Log.d("transactionId", "transactionId: "+transactionId);
//                                        })
//                                        .addOnFailureListener(e -> {
//                                            Log.d("transaction", "failed");
//                                        });
//
//                            }
//                        } else {
//                            Toast.makeText(PaymentAmount.this, "No such document!", Toast.LENGTH_SHORT).show();
//                        }
//                    } else {
//                        Toast.makeText(PaymentAmount.this, "Error fetching data!", Toast.LENGTH_SHORT).show();
//                    }
//                });


    }

    @Override
    public void onPaymentError(int i, String s) {
        Toast.makeText(getApplicationContext(), "Payment Failed!", Toast.LENGTH_SHORT).show();
        unBookSeats();
//        onBackPressed();
    }

    public void unBookSeats()
    {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        DocumentReference showRef = db.collection("shows").document(show.getShowId());

        db.runTransaction(transaction -> {
            // Fetch current seat layout
            DocumentSnapshot showSnapshot = transaction.get(showRef);
            Map<String, Object> seatsLayout = (Map<String, Object>) showSnapshot.get("seatsLayout");

            if (seatsLayout == null) {
                throw new FirebaseFirestoreException("Seat layout not found!", FirebaseFirestoreException.Code.ABORTED);
            }



            // ✅ Iterate through selected seats and unbook them
            for (String singleSeatId : selectedSeatIds) {
                int seatId = Integer.parseInt(singleSeatId); // Extract seat ID

                for (String rowKey : seatsLayout.keySet()) {
                    List<Map<String, Object>> seats = (List<Map<String, Object>>) seatsLayout.get(rowKey);

                    for (Map<String, Object> seat : seats) {
                        int currentSeatId = ((Number) seat.get("id")).intValue();

                        // ✅ If this is the selected seat, mark it as unbooked
                        if (currentSeatId == seatId) {
                            seat.put("isBooked", false);
                            seat.remove("userId"); // Remove user association
                            seat.remove("bookingType");
                            break;
                        }
                    }
                }
            }

            // ✅ Commit updated seat layout
            transaction.update(showRef, "seatsLayout", seatsLayout);
            return null;
        }).addOnSuccessListener(aVoid -> {
            Log.d("Firestore", "Seats successfully unbooked after payment failure!");
        }).addOnFailureListener(e -> {
            Log.e("Firestore", "Error unbooking seats", e);
        });

        progressBar.setVisibility(View.GONE);
        btnPayDirect.setEnabled(true);
    }
}