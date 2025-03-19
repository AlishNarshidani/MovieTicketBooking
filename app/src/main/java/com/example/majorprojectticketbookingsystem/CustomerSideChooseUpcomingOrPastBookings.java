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

public class CustomerSideChooseUpcomingOrPastBookings extends AppCompatActivity {

    CardView upcomingBookingsCard, pastBookingsCard;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_customer_side_choose_upcoming_or_past_bookings);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        upcomingBookingsCard = findViewById(R.id.upcomingBookingsCard);
        pastBookingsCard = findViewById(R.id.pastBookingsCard);

        upcomingBookingsCard.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(CustomerSideChooseUpcomingOrPastBookings.this, CustomerSideViewBookings.class);
                intent.putExtra("caller", "upcoming");
                startActivity(intent);
            }
        });

        pastBookingsCard.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(CustomerSideChooseUpcomingOrPastBookings.this, CustomerSideViewBookings.class);
                intent.putExtra("caller", "past");
                startActivity(intent);
            }
        });
    }
}