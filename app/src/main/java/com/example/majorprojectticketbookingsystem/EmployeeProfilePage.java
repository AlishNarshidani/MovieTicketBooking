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

public class EmployeeProfilePage extends AppCompatActivity {

    LinearLayout logoutLayout;
    private TextView displayName, displayEmail, displayMobile, displayUserRole, displayTheatreName, displayTheatreAddress;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_employee_profile_page);
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
        displayTheatreName = findViewById(R.id.displayTheatreName);
        displayTheatreAddress = findViewById(R.id.displayTheatreAddress);


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


                AlertDialog.Builder alert = new AlertDialog.Builder(EmployeeProfilePage.this)
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

                                Intent intent = new Intent(EmployeeProfilePage.this, MainActivity.class);
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
        AlertDialog.Builder alert = new AlertDialog.Builder(EmployeeProfilePage.this)
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

                        Intent intent = new Intent(EmployeeProfilePage.this, MainActivity.class);
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
                                String theatreId = document.getString("theatreId");

                                // Display user details
                                displayName.setText(userName);
                                displayEmail.setText(userEmail);
                                displayMobile.setText(mno);
                                displayUserRole.setText(userRole);

                                // Now fetch theatre details using the theatreId
                                if (theatreId != null && !theatreId.isEmpty()) {
                                    fetchTheatreDetails(theatreId);
                                }


                            } else {
                                Log.d("ProfiePage", "No such document");
                            }
                        }
                    }
                });


    }


    // Method to fetch theatre details using theatreId
    private void fetchTheatreDetails(String theatreId) {
        FirebaseFirestore db = FirebaseFirestore.getInstance();

        db.collection("theatre").document(theatreId).get()
                .addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                        if (task.isSuccessful()) {
                            DocumentSnapshot document = task.getResult();
                            if (document.exists()) {
                                String theatreName = document.getString("theatreName");
                                String location = document.getString("location");

                                // Display theatre details (Make sure these TextViews exist)
                                displayTheatreName.setText(theatreName);
                                displayTheatreAddress.setText(location);

                                Log.d("TheatreData", "Theatre Name: " + theatreName + ", Location: " + location);
                            } else {
                                Log.d("TheatreData", "No such theatre document");
                            }
                        } else {
                            Log.e("TheatreData", "Error fetching theatre data", task.getException());
                        }
                    }
                });
    }



}