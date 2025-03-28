package com.example.majorprojectticketbookingsystem;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.cardview.widget.CardView;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.Timestamp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class BookingAdapter extends RecyclerView.Adapter<BookingAdapter.BookingViewHolder>{

    private List<Booking> bookingList;
    private Context context;

    public BookingAdapter(List<Booking> bookingList, Context context) {
        this.bookingList = bookingList;
        this.context = context;
    }

    @NonNull
    @Override
    public BookingAdapter.BookingViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_booking,parent,false);

        return new BookingAdapter.BookingViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull BookingAdapter.BookingViewHolder holder, int position) {
        Booking booking = bookingList.get(position);
        holder.txtMovieName.setText("Movie: "+booking.getMovieName());
        holder.txtTheatreName.setText("Theatre: "+booking.getTheatreName());
        holder.txtTheatreLocation.setText("Address: "+booking.getTheatreLocation());
        holder.txtHallName.setText("Hall: "+booking.getHallName());
        holder.txtShowStartTime.setText("Start: "+booking.getShowStartTime());
        holder.txtShowEndTime.setText("End: "+booking.getShowEndTime());
        holder.txtSelectedSeats.setText("Seat No: "+booking.getSelectedSeats());
        holder.txtTotalPrice.setText("Total Amount: ₹"+booking.getTotalPrice());
        String bookingStatus = booking.getBookingStatus();



        if(bookingStatus != null && !bookingStatus.isEmpty()) {
            holder.txtBookingStatus.setText("Booking: "+bookingStatus);
            if (bookingStatus.equalsIgnoreCase("confirmed")) {
                holder.txtBookingStatus.setTextColor(ContextCompat.getColor(context, R.color.mid_green));


                if(isCancelable(convertToTimestamp(booking.getShowStartTime()))) {
                    holder.btnCancelBooking.setVisibility(View.VISIBLE);
                }

            } else if (bookingStatus.equalsIgnoreCase("cancelled")) {
                holder.txtBookingStatus.setTextColor(Color.RED);
                holder.btnGenerateQR.setVisibility(View.GONE);
            }

        } else {
            holder.txtBookingStatus.setText("Booking Pending");
        }


        holder.btnCancelBooking.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showCancelConfirmationDialog(booking.getBookingId(),holder, booking.getSeatList(), booking.getShowId(), booking); // Pass the booking ID
            }
        });

        holder.btnGenerateQR.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(context, QRGenerationActivity.class);
                intent.putExtra("booking", booking);
                context.startActivity(intent);
            }
        });


    }

    @Override
    public int getItemCount() {
        return bookingList.size();
    }


    public static class BookingViewHolder extends RecyclerView.ViewHolder
    {
        TextView txtMovieName, txtTheatreName, txtTheatreLocation, txtHallName, txtShowStartTime, txtShowEndTime, txtSelectedSeats, txtTotalPrice, txtBookingStatus;
        LinearLayout buttonsLayout;
        Button btnGenerateQR, btnCancelBooking;

        public BookingViewHolder(@NonNull View itemView) {
            super(itemView);
            txtMovieName = itemView.findViewById(R.id.txtMovieName);
            txtTheatreName = itemView.findViewById(R.id.txtTheatreName);
            txtTheatreLocation = itemView.findViewById(R.id.txtTheatreLocation);
            txtHallName = itemView.findViewById(R.id.txtHallName);
            txtShowStartTime = itemView.findViewById(R.id.txtShowStartTime);
            txtShowEndTime = itemView.findViewById(R.id.txtShowEndTime);
            txtSelectedSeats = itemView.findViewById(R.id.txtSelectedSeats);
            txtTotalPrice = itemView.findViewById(R.id.txtTotalPrice);
            txtBookingStatus = itemView.findViewById(R.id.txtBookingStatus);
            buttonsLayout = itemView.findViewById(R.id.buttonsLayout);
            btnGenerateQR = itemView.findViewById(R.id.btnGenerateQR);
            btnCancelBooking = itemView.findViewById(R.id.btnCancelBooking);
        }
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


    private boolean isCancelable(Timestamp showStartTime) {
        long currentTime = System.currentTimeMillis();
        long showTime = showStartTime.toDate().getTime();

        // Calculate the difference in milliseconds (24 hours = 24 * 60 * 60 * 1000 ms)
        long timeDifference = showTime - currentTime;
        return timeDifference > 24 * 60 * 60 * 1000;
    }


    private void cancelBooking(String bookingId, BookingAdapter.BookingViewHolder holder) {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        DocumentReference bookingRef = db.collection("bookings").document(bookingId);

        bookingRef.update("bookingStatus", "Cancelled")
                .addOnSuccessListener(aVoid -> {
                    //Toast.makeText(context, "Booking Canceled Successfully!", Toast.LENGTH_SHORT).show();
                })
                .addOnFailureListener(e -> {
                    Log.e("Firestore", "Error canceling booking", e);
                    //Toast.makeText(context, "Failed to cancel booking. Please try again.", Toast.LENGTH_SHORT).show();
                });
    }



    private void showCancelConfirmationDialog(String bookingId, BookingAdapter.BookingViewHolder holder, List<String> seatList, String showId, Booking booking)
    {
        AlertDialog.Builder alert = new AlertDialog.Builder(context)
                .setTitle("Cancel Booking ?")
                .setIcon(R.drawable.baseline_theater_comedy_24)
                .setMessage("Are you sure you want to cancel this booking?")
                .setPositiveButton("YES", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {

                        unbookSeats(bookingId, holder, seatList, showId, booking);

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


    private void unbookSeats(String bookingId, BookingAdapter.BookingViewHolder holder, List<String> seatList, String showId, Booking booking) {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        DocumentReference showRef = db.collection("shows").document(showId);

        db.runTransaction(transaction -> {
                    DocumentSnapshot showSnapshot = transaction.get(showRef);

                    // ✅ Check if the show document exists
                    if (!showSnapshot.exists()) {
                        throw new FirebaseFirestoreException("Show not found!", FirebaseFirestoreException.Code.NOT_FOUND);
                    }

                    Map<String, Object> seatsLayout = (Map<String, Object>) showSnapshot.get("seatsLayout");

                    if (seatsLayout != null) {
                        for (String seatId : seatList) {
                            for (String rowKey : seatsLayout.keySet()) {
                                List<Map<String, Object>> rowSeats = (List<Map<String, Object>>) seatsLayout.get(rowKey);
                                for (Map<String, Object> seat : rowSeats) {
                                    if (seat.get("id").toString().equals(seatId)) {
                                        seat.put("isBooked", false);
                                        seat.remove("userId");
                                    }
                                }
                            }
                        }
                        transaction.update(showRef, "seatsLayout", seatsLayout);
                    }
                    return null;
                }).addOnSuccessListener(aVoid -> {
                    Log.d("Firestore", "Seats unbooked successfully");
                    holder.btnCancelBooking.setVisibility(View.GONE);
                    holder.btnGenerateQR.setVisibility(View.GONE);
                    holder.txtBookingStatus.setText("Booking Cancelled");
                    holder.txtBookingStatus.setTextColor(Color.RED);
                    initiateRefund(booking);
                    cancelBooking(bookingId, holder);
                })
                .addOnFailureListener(e -> Log.e("Firestore", "Error unbooking seats", e));
    }



    private void initiateRefund(Booking booking) {
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
            double refundAmount = Double.parseDouble(booking.getTotalPrice());
            double newBalance = currentBalance + refundAmount;

            // ✅ Update the field
            transaction.update(userRef, "depositedMoney", newBalance);
            return null;
        }).addOnSuccessListener(aVoid -> {
            Log.d("Firestore", "Refund processed successfully!");
            addTransactionForRefundOfFailedBooking(booking);
        }).addOnFailureListener(e -> {
            initiateRefund(booking);
            Log.e("Firestore", "Refund failed", e);
            Toast.makeText(context, "Error processing refund: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        });
    }



    public void addTransactionForRefundOfFailedBooking(Booking booking)
    {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        FirebaseAuth auth = FirebaseAuth.getInstance();
        FirebaseUser user=auth.getCurrentUser();
        String userId=user.getUid();

        Long amountVal_long = Long.parseLong(booking.getTotalPrice());

        // Save data in transactions collection
        Map<String, Object> transactionData = new HashMap<>();
        transactionData.put("userId", userId);
        transactionData.put("transactionType", "Refund");
        transactionData.put("amount", amountVal_long);
        transactionData.put("bookingId",booking.getBookingId());
        transactionData.put("transactionMethod", "Cancelled Booking");
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

        showToastForRefund(booking);
    }


    public void showToastForRefund(Booking booking)
    {
        LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View layout = inflater.inflate(R.layout.custom_toast_booked, null);

        ImageView toastImage = layout.findViewById(R.id.toast_image);
        toastImage.setImageResource(R.drawable.baseline_account_balance_wallet_24);

        TextView amountText = layout.findViewById(R.id.toast_amount);
        amountText.setText(booking.getTotalPrice());

        TextView transaction_type = layout.findViewById(R.id.toast_text);
        transaction_type.setText("Booking Cancelled");

        TextView subText = layout.findViewById(R.id.toast_subtext);
        subText.setText("The Refund of Deducted Money Has Been Initiated");

        Toast toast = new Toast(context);
        toast.setDuration(Toast.LENGTH_LONG);
        toast.setView(layout);
        toast.setGravity(Gravity.CENTER,0,0);
        toast.show();
    }


}
