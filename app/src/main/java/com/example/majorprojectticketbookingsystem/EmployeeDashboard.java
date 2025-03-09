package com.example.majorprojectticketbookingsystem;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

public class EmployeeDashboard extends AppCompatActivity {

    ImageView profileIcon;
    CardView addMovieToTheatreCard, addFoodAvailable, viewAllBookingsCard;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_employee_dashboard);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        profileIcon = findViewById(R.id.profileIcon);
        addMovieToTheatreCard = findViewById(R.id.addMovieToTheatreCard);
        addFoodAvailable = findViewById(R.id.addFoodAvailable);
        viewAllBookingsCard = findViewById(R.id.viewAllBookingsCard);


        profileIcon.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(EmployeeDashboard.this, EmployeeProfilePage.class);
                startActivity(intent);
            }
        });


        addMovieToTheatreCard.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(EmployeeDashboard.this, EmployeeSideMovieListPage.class);
                startActivity(intent);
            }
        });
    }


}