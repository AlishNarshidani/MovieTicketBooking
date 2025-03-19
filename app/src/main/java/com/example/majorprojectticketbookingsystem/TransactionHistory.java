package com.example.majorprojectticketbookingsystem;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageButton;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class TransactionHistory extends AppCompatActivity {

    FirebaseFirestore db;
    FirebaseAuth auth;
    private List<Transaction> transactionList;
    private RecyclerView recyclerView;
    private TransactionAdapter adapter;

    ImageButton backButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_transaction_history);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });


        db = FirebaseFirestore.getInstance();
        auth= FirebaseAuth.getInstance();
        transactionList = new ArrayList<>();
        recyclerView = findViewById(R.id.transactionHistoryRecyclerView);
        backButton = findViewById(R.id.backButton);

        adapter = new TransactionAdapter(transactionList);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(adapter);


        FirebaseUser user=auth.getCurrentUser();
        String userId=user.getUid();


        db.collection("transactions")
                .whereEqualTo("userId",userId)
                .orderBy("transactionDate", Query.Direction.DESCENDING)
                .get()
                .addOnCompleteListener(task -> {
                    if(task.isSuccessful()) {

                        for(QueryDocumentSnapshot document : task.getResult()) {

                            String transactionIdOrBookingId = document.getId();
                            String transactionType = document.getString("transactionType");
                            Date transactionDate = document.getTimestamp("transactionDate").toDate();
                            double amount = document.getDouble("amount");
                            String transactionStatusOrMethod = "";

                            if(transactionType.equalsIgnoreCase("Deposit"))
                            {
                                transactionStatusOrMethod = document.getString("transactionStatus");
                            } else if (transactionType.equalsIgnoreCase("Booking")) {

                                transactionStatusOrMethod = document.getString("transactionMethod");
                                transactionIdOrBookingId = document.getString("bookingId");
                            }

                            Log.d("transaction"," transactionIdOrBookingId: "+transactionIdOrBookingId+" transactionType: "+transactionType+" transactionDate: "+transactionDate+" transactionStatusOrMethod: "+transactionStatusOrMethod+" amount: "+amount);

                            transactionList.add(new Transaction(transactionType,amount,transactionDate,transactionStatusOrMethod,transactionIdOrBookingId));
                        }
                        adapter.notifyDataSetChanged();
                    }
                })
                .addOnFailureListener(e -> {
                    Log.d("error", "in Transaction history.java: ");
                });


        backButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });


    }


    @Override
    public void onBackPressed() {
        super.onBackPressed();
    }
}