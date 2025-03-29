package com.example.majorprojectticketbookingsystem;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

public class EmployeeSideChooseUpcomingOrPastBookings extends AppCompatActivity {

    CardView viewUpcomingBookings, viewPastBookings;

    String caller;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_employee_side_choose_upcoming_or_past_booking_movie_wise);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        caller = getIntent().getStringExtra("caller");

        viewUpcomingBookings = findViewById(R.id.viewUpcomingBookings);
        viewPastBookings = findViewById(R.id.viewPastBookings);

        viewUpcomingBookings.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent;

                if(caller.equalsIgnoreCase("movieWise")) {
                    intent = new Intent(EmployeeSideChooseUpcomingOrPastBookings.this, EmployeeSideChooseMovieForViewBookings.class);
                } else if (caller.equalsIgnoreCase("dayWise")) {
                    intent = new Intent(EmployeeSideChooseUpcomingOrPastBookings.this, EmployeeSideChooseShowForViewBookingsDayWise.class);
                } else {
                    intent = new Intent(EmployeeSideChooseUpcomingOrPastBookings.this, EmployeeSideChooseMovieForViewBookings.class);
                }
                intent.putExtra("caller", "upcoming");
                startActivity(intent);
            }
        });

        viewPastBookings.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent;

                if(caller.equalsIgnoreCase("movieWise")) {
                    intent = new Intent(EmployeeSideChooseUpcomingOrPastBookings.this, EmployeeSideChooseMovieForViewBookings.class);
                } else if (caller.equalsIgnoreCase("dayWise")) {
                    intent = new Intent(EmployeeSideChooseUpcomingOrPastBookings.this, EmployeeSideChooseShowForViewBookingsDayWise.class);
                } else {
                    intent = new Intent(EmployeeSideChooseUpcomingOrPastBookings.this, EmployeeSideChooseMovieForViewBookings.class);
                }
                intent.putExtra("caller", "past");
                startActivity(intent);
            }
        });


    }
}