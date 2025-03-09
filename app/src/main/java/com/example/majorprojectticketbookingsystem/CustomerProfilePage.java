package com.example.majorprojectticketbookingsystem;

import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

public class CustomerProfilePage extends AppCompatActivity {

    LinearLayout logoutLayout;
    private TextView displayName, displayEmail, displayMobile, displayUserRole;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_customer_profile_page);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });


        logoutLayout = findViewById(R.id.logoutLayout);
        displayName = findViewById(R.id.displayName);
        displayEmail = findViewById(R.id.displayEmail);
        displayMobile = findViewById(R.id.displayMobile);
        displayUserRole = findViewById(R.id.displayUserRole);


        FirebaseAuth auth = FirebaseAuth.getInstance();
        FirebaseUser user = auth.getCurrentUser();

        if (user != null) {
            String userId = user.getUid();
            fetchUserData(userId);
        } else {
            Log.e("ProfiePage", "No user is logged in");
        }


        logoutLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {


                AlertDialog.Builder alert = new AlertDialog.Builder(CustomerProfilePage.this)
                        .setTitle("EXIT ?")
                        .setIcon(R.drawable.baseline_person_24)
                        .setMessage("Are you Sure you want to log out ?")
                        .setPositiveButton("YES", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                SharedPreferences sharedPreferences = getSharedPreferences("MyAppPrefs", MODE_PRIVATE);

                                SharedPreferences.Editor editor = sharedPreferences.edit();
                                editor.remove("isLoggedIn");
                                editor.remove("email");
                                editor.remove("userRole");
                                editor.apply();

                                Intent intent = new Intent(CustomerProfilePage.this, MainActivity.class);
                                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                                startActivity(intent);
                                finish();
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


    public void logout(View view)
    {
        AlertDialog.Builder alert = new AlertDialog.Builder(CustomerProfilePage.this)
                .setTitle("EXIT ?")
                .setIcon(R.drawable.baseline_person_24)
                .setMessage("Are you Sure you want to log out ?")
                .setPositiveButton("YES", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        SharedPreferences sharedPreferences = getSharedPreferences("MyAppPrefs", MODE_PRIVATE);

                        SharedPreferences.Editor editor = sharedPreferences.edit();
                        editor.remove("isLoggedIn");
                        editor.remove("email");
                        editor.remove("userRole");
                        editor.apply();

                        Intent intent = new Intent(CustomerProfilePage.this, MainActivity.class);
                        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                        startActivity(intent);
                        finish();
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


    private void fetchUserData(String userId)
    {
        FirebaseFirestore db = FirebaseFirestore.getInstance();

        db.collection("users").document(userId).get()
                .addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                        if (task.isSuccessful()) {
                            DocumentSnapshot document = task.getResult();
                            if (document.exists()) {
                                String userName = document.getString("name");
                                String userEmail = document.getString("email");
                                String mno = document.getString("mno");
                                String userRole = document.getString("userRole");

                                // Display user details
                                displayName.setText(userName);
                                displayEmail.setText(userEmail);
                                displayMobile.setText(mno);
                                displayUserRole.setText(userRole);

                            } else {
                                Log.d("ProfiePage", "No such document");
                            }
                        }
                    }
                });


    }


}