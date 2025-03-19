package com.example.majorprojectticketbookingsystem;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatButton;
import androidx.cardview.widget.CardView;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.Map;

public class CustomerSideWalletActivity extends AppCompatActivity {

    AppCompatButton addcash;
    TextView depositedMoney, discountPerc, profile_name;

    CardView transactionHistoryCardView, discountCardView;

    FirebaseAuth auth;
    FirebaseFirestore db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_customer_side_wallet);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        addcash = findViewById(R.id.add_cash_button);
        depositedMoney = findViewById(R.id.depositedMoney);
        discountPerc = findViewById(R.id.discountPerc);
        profile_name = findViewById(R.id.profile_name);
        transactionHistoryCardView = findViewById(R.id.transactionHistory);
        discountCardView = findViewById(R.id.discountCardView);
        auth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        getUserBalance();

        addcash.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(CustomerSideWalletActivity.this, PaymentAmount.class);
                startActivity(intent);
            }
        });

        transactionHistoryCardView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(CustomerSideWalletActivity.this, TransactionHistory.class);
                startActivity(i);
            }
        });

    }

    @Override
    protected void onResume() {
        super.onResume();

        getUserBalance();
    }

    private void getUserBalance()
    {
        FirebaseUser user=auth.getCurrentUser();
        String userId=user.getUid();

        // Reference to the document in the 'users' collection
        DocumentReference userDocRef = db.collection("users").document(userId);

        // Fetch the document of current user
        userDocRef.get()
                .addOnCompleteListener(task -> {
                    if(task.isSuccessful()) {
                        DocumentSnapshot document = task.getResult();

                        if(document!=null && document.exists()) {
                            // The document exists, get all fields as a Map
                            Map<String, Object> fetchUserData = document.getData();

                            if (fetchUserData != null) {
                                // Iterate through the fields and log or use the data
                                for (Map.Entry<String, Object> entry : fetchUserData.entrySet()) {
                                    String key = entry.getKey();
                                    Object value = entry.getValue();

                                    // Log the key and value
                                    Log.d("FirestoreData", key + ": " + value.toString());
                                }

                                // Example: Get specific fields (if needed)
                                Long fetchedDepositedMoney = document.getLong("depositedMoney");
                                Long fetchedDiscountPerc = document.getLong("discountPerc");
                                String userName = document.getString("name");

                                if(fetchedDepositedMoney!=null)
                                {
                                    depositedMoney.setText("₹" + fetchedDepositedMoney.toString());
                                }
                                else
                                {
                                    depositedMoney.setText("₹0");
                                }

                                if(fetchedDiscountPerc!=null)
                                {
                                    discountPerc.setVisibility(View.VISIBLE);
                                    discountPerc.setText(fetchedDiscountPerc.toString() + "%");
                                }

                                profile_name.setText(userName);

                            }
                        } else {
                            Log.d("error", "No such document!");
                            //Toast.makeText(getActivity(), "No such document!", Toast.LENGTH_SHORT).show();
                        }
                    } else {
                        Log.d("error", "Error fetching data!");
                        //Toast.makeText(getActivity(), "Error fetching data!", Toast.LENGTH_SHORT).show();
                    }
                });
    }

}