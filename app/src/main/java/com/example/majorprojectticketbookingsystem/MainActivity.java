    package com.example.majorprojectticketbookingsystem;

import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Paint;
import android.os.Bundle;
import android.text.InputType;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;

    public class MainActivity extends AppCompatActivity {

    EditText email,pass;
    Button login;
    TextView forgot,signup;
    FirebaseAuth auth;
    LinearLayout loadingIndicator;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        email=findViewById(R.id.editTextText);
        pass=findViewById(R.id.editTextText2);
        login=findViewById(R.id.button);
        forgot=findViewById(R.id.textView);
        signup=findViewById(R.id.textView2);
        loadingIndicator = findViewById(R.id.loadingIndicator);
        auth=FirebaseAuth.getInstance();

        SharedPreferences sharedPreferences = getSharedPreferences("MyAppPrefs", MODE_PRIVATE);
        boolean isLoggedIn = sharedPreferences.getBoolean("isLoggedIn", false);

        if(isLoggedIn)
        {

            FirebaseUser user = auth.getCurrentUser();
            if (user != null) {
                loadingIndicator.setVisibility(View.VISIBLE);
                updateUserRoleAndTheatreId(user.getUid()); // Call the function before redirecting

                SharedPreferences sharedPreferencesFCM = getSharedPreferences("MyAppPrefs", MODE_PRIVATE);
                String token = sharedPreferencesFCM.getString("fcmToken", null);
                if (token != null) {
                    // Save token to Firestore
                    saveTokenToFirestore(user.getUid(), token);
                }

            }
//            if(sharedPreferences.getString("userRole", "").equalsIgnoreCase("employee")) {
//                Intent i = new Intent(getApplicationContext(), EmployeeDashboard.class);
//                startActivity(i);
//                finish();
//            } else {
//                Intent i = new Intent(getApplicationContext(), CustomerDashboard.class);
//                startActivity(i);
//                finish();
//            }
        }
        else {
            pass.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
            signup.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    signup.setPaintFlags(signup.getPaintFlags() | Paint.UNDERLINE_TEXT_FLAG);

                    Intent i = new Intent(MainActivity.this, SignUp.class);
                    startActivity(i);
                }
            });

            forgot.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    forgot.setPaintFlags(forgot.getPaintFlags() | Paint.UNDERLINE_TEXT_FLAG);

                    Intent i = new Intent(MainActivity.this, ForgotPassword.class);
                    startActivity(i);
                }
            });

            login.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    String em = email.getText().toString().trim();
                    String password = pass.getText().toString().trim();

                    if (em.isEmpty() || password.isEmpty()) {
                        Toast.makeText(MainActivity.this, "Empty field", Toast.LENGTH_SHORT).show();
                    }

                    else {
                        auth.signInWithEmailAndPassword(em, password).addOnCompleteListener(MainActivity.this, task -> {
                            if (task.isSuccessful()) {
                                FirebaseUser user = auth.getCurrentUser();

                                if(user != null) {
                                    String userId = user.getUid();
                                    Log.d("userId", userId);
                                    FirebaseFirestore db = FirebaseFirestore.getInstance();

                                    SharedPreferences sharedPreferencesFCM = getSharedPreferences("MyAppPrefs", MODE_PRIVATE);
                                    String token = sharedPreferencesFCM.getString("fcmToken", null);
                                    if (token != null) {
                                        // Save token to Firestore
                                        saveTokenToFirestore(user.getUid(), token);
                                    }

                                    db.collection("users").document(userId).get().addOnSuccessListener(documentSnapshot -> {

                                        if (documentSnapshot.exists()) {
                                            String userRole = documentSnapshot.getString("userRole");
                                            Log.d("userRole", userRole);

                                            SharedPreferences sharedPreferences = getSharedPreferences("MyAppPrefs", MODE_PRIVATE);
                                            SharedPreferences.Editor editor = sharedPreferences.edit();
                                            editor.putBoolean("isLoggedIn", true);
                                            editor.putString("email", email.getText().toString());
                                            editor.putString("userRole", userRole);
                                            Log.d("email", email.getText().toString());

                                            Toast.makeText(MainActivity.this, "Login Successful!", Toast.LENGTH_SHORT).show();

                                            Intent i;
                                            if ("customer".equalsIgnoreCase(userRole)) {
                                                i = new Intent(getApplicationContext(), CustomerDashboard.class);
                                            } else if ("employee".equalsIgnoreCase(userRole)) {
                                                String theatreId = documentSnapshot.getString("theatreId");
                                                editor.putString("theatreId", theatreId);
                                                Log.d("theatreId", theatreId);
                                                i = new Intent(getApplicationContext(), EmployeeDashboard.class);
                                            } else {
                                                i = new Intent(getApplicationContext(), CustomerDashboard.class); // Fallback page
                                            }

                                            editor.apply();
                                            startActivity(i);
                                            finish();
                                        } else {
                                            Toast.makeText(MainActivity.this, "User data not found!", Toast.LENGTH_SHORT).show();
                                        }
                                    }).addOnFailureListener(e -> {
                                        Toast.makeText(MainActivity.this, "Failed to fetch user data!", Toast.LENGTH_SHORT).show();
                                        Log.e("Firebase", "Error fetching role", e);
                                    });
                                }

                            } else {
                                Toast.makeText(MainActivity.this, "Login Failed!", Toast.LENGTH_SHORT).show();
                            }
                        });
                    }
                }
            });
        }
    }

        private void saveTokenToFirestore(String uid, String token) {
            FirebaseFirestore.getInstance()
                    .collection("users")
                    .document(uid)
                    .update("fcmToken", token)
                    .addOnSuccessListener(unused -> Log.d("FCM", "Token saved"))
                    .addOnFailureListener(e -> Log.e("FCM", "Failed to save token", e));
        }


        private void updateUserRoleAndTheatreId(String userId)
        {
            FirebaseFirestore db = FirebaseFirestore.getInstance();

            db.collection("users").document(userId).get().addOnSuccessListener(documentSnapshot -> {
                if (documentSnapshot.exists()) {
                    String userRole = documentSnapshot.getString("userRole");
                    String theatreId = documentSnapshot.getString("theatreId");
                    Long discountPercLong = documentSnapshot.getLong("discountPerc");
                    int discountPerc = discountPercLong != null ? discountPercLong.intValue() : 0;

                    // Update Shared Preferences
                    SharedPreferences sharedPreferences = getSharedPreferences("MyAppPrefs", MODE_PRIVATE);
                    SharedPreferences.Editor editor = sharedPreferences.edit();
                    editor.putString("userRole", userRole);
                    editor.putString("theatreId", theatreId);
                    editor.putBoolean("isLoggedIn", true);
                    editor.putInt("discountPerc", discountPerc);
                    editor.apply();

                    Log.d("Updated Prefs", "Role: " + userRole + ", TheatreId: " + theatreId);

                    // Redirect to respective dashboard
                    Intent i;
                    if ("employee".equalsIgnoreCase(userRole)) {
                        i = new Intent(getApplicationContext(), EmployeeDashboard.class);
                    } else {
                        i = new Intent(getApplicationContext(), CustomerDashboard.class);
                    }

                    startActivity(i);
                    finish();
                } else {
                    Toast.makeText(MainActivity.this, "User data not found!", Toast.LENGTH_SHORT).show();
                }
            }).addOnFailureListener(e -> {
                Toast.makeText(MainActivity.this, "Failed to fetch user data!", Toast.LENGTH_SHORT).show();
                Log.e("Firebase", "Error fetching user data", e);
            });
        }


}